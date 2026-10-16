[CmdletBinding()]
param(
    [switch] $SkipBuild,
    [string] $ResultsDir,
    [switch] $AllowDirty
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$spikeRoot = Join-Path $projectRoot 'experimentos\spike-01-integracion'
Set-Location -LiteralPath $projectRoot

Remove-Item Env:DOCKER_CONTEXT -ErrorAction SilentlyContinue
$env:DOCKER_HOST = 'npipe:////./pipe/docker_engine'
$env:COMPOSE_BAKE = 'false'

function Resolve-Executable {
    param(
        [Parameter(Mandatory)] [string] $Command,
        [Parameter(Mandatory)] [string[]] $Candidates
    )

    $resolved = Get-Command $Command -ErrorAction SilentlyContinue
    if ($resolved) {
        return $resolved.Source
    }

    foreach ($candidate in $Candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw "No se encontro $Command."
}

function Invoke-Checked {
    param(
        [Parameter(Mandatory)] [string] $Executable,
        [Parameter(ValueFromRemainingArguments)] [string[]] $Arguments
    )

    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "El comando termino con codigo ${LASTEXITCODE}: $Executable $($Arguments -join ' ')"
    }
}

function Invoke-Captured {
    param(
        [Parameter(Mandatory)] [string] $Executable,
        [Parameter(ValueFromRemainingArguments)] [string[]] $Arguments
    )

    $output = & $Executable @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "El comando termino con codigo ${LASTEXITCODE}: $Executable $($Arguments -join ' ')`n$($output -join "`n")"
    }
    return ($output -join "`n").Trim()
}

$dockerRoot = Join-Path $env:LOCALAPPDATA 'Programs\DockerDesktop\resources\bin'
$docker = Resolve-Executable -Command 'docker.exe' -Candidates @(
    (Join-Path $dockerRoot 'docker.exe'),
    'C:\Program Files\Docker\Docker\resources\bin\docker.exe'
)
$compose = Resolve-Executable -Command 'docker-compose.exe' -Candidates @(
    (Join-Path $dockerRoot 'docker-compose.exe'),
    'C:\Program Files\Docker\Docker\resources\bin\docker-compose.exe'
)
$python = Resolve-Executable -Command 'python.exe' -Candidates @(
    (Join-Path $env:USERPROFILE '.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe')
)
$git = Resolve-Executable -Command 'git.exe' -Candidates @(
    'C:\Program Files\Git\cmd\git.exe'
)

$integrationTokenSource = 'environment'
if ([string]::IsNullOrWhiteSpace($env:INTEGRATION_TOKEN)) {
    $env:INTEGRATION_TOKEN = 'spike-local-token'
    $integrationTokenSource = 'local-default'
}

$runId = (Get-Date).ToUniversalTime().ToString('yyyyMMddTHHmmssZ')
if ([string]::IsNullOrWhiteSpace($ResultsDir)) {
    $resultsPath = Join-Path $spikeRoot "resultados\$runId"
}
elseif ([System.IO.Path]::IsPathRooted($ResultsDir)) {
    $resultsPath = [System.IO.Path]::GetFullPath($ResultsDir)
}
else {
    $resultsPath = [System.IO.Path]::GetFullPath((Join-Path $projectRoot $ResultsDir))
}

$normalizedSpikeRoot = [System.IO.Path]::GetFullPath($spikeRoot).TrimEnd('\')
$normalizedResultsPath = [System.IO.Path]::GetFullPath($resultsPath)
if (-not $normalizedResultsPath.StartsWith(
        "$normalizedSpikeRoot\",
        [System.StringComparison]::OrdinalIgnoreCase
    )) {
    throw 'ResultsDir debe estar dentro de experimentos\spike-01-integracion para que k6 pueda escribir la evidencia.'
}
$resultsPath = $normalizedResultsPath
$relativeResults = $resultsPath.Substring($normalizedSpikeRoot.Length).TrimStart('\')
if (Test-Path -LiteralPath $resultsPath) {
    throw "La carpeta de resultados ya existe: $resultsPath"
}

Write-Host '1/9 Verificando Git, Docker, Compose y Python...'
$revision = Invoke-Captured -Executable $git -Arguments @(
    '-c', "safe.directory=$projectRoot", '-C', $projectRoot, 'rev-parse', 'HEAD'
)
$branch = Invoke-Captured -Executable $git -Arguments @(
    '-c', "safe.directory=$projectRoot", '-C', $projectRoot, 'branch', '--show-current'
)
$status = Invoke-Captured -Executable $git -Arguments @(
    '-c', "safe.directory=$projectRoot", '-C', $projectRoot, 'status', '--porcelain', '--untracked-files=all'
)
$workingTreeClean = [string]::IsNullOrWhiteSpace($status)
if (-not $workingTreeClean -and -not $AllowDirty) {
    throw 'El arbol de trabajo no esta limpio. Confirma la implementacion antes de medir o usa -AllowDirty solo para una corrida diagnostica invalida.'
}
if ($branch -ne 'spike/s10-integracion-mensajeria-ofertas') {
    throw "La medicion oficial debe ejecutarse en spike/s10-integracion-mensajeria-ofertas; rama actual: $branch"
}

$dockerVersion = Invoke-Captured -Executable $docker -Arguments @('version', '--format', '{{.Server.Version}}')
$composeVersion = Invoke-Captured -Executable $compose -Arguments @('version', '--short')
Invoke-Checked -Executable $compose -Arguments @('config', '--quiet')

Write-Host '2/9 Levantando PostgreSQL 16 y la API experimental...'
if ($SkipBuild) {
    Invoke-Checked -Executable $compose -Arguments @('up', '-d')
}
else {
    Invoke-Checked -Executable $compose -Arguments @('up', '--build', '-d')
}

Write-Host '3/9 Esperando salud de la API...'
$healthy = $false
foreach ($attempt in 1..60) {
    try {
        $health = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 3
        if ($health.status -eq 'UP') {
            $healthy = $true
            break
        }
    }
    catch {
        Start-Sleep -Seconds 5
    }
}
if (-not $healthy) {
    Invoke-Checked -Executable $compose -Arguments @('logs', '--no-color', '--tail', '150', 'api', 'db')
    throw 'La API no alcanzo el estado saludable.'
}

Write-Host '4/9 Seleccionando datos demo deterministas...'
$jobRow = Invoke-Captured -Executable $compose -Arguments @(
    'exec', '-T', 'db', 'psql', '-v', 'ON_ERROR_STOP=1', '-U', 'utrabajo', '-d', 'utrabajo',
    '-tA', '-c',
    "SELECT j.id || '|' || j.company_id FROM job_offer j JOIN app_user u ON u.id = j.company_id WHERE u.email = 'empresa@utrabajo.local' ORDER BY j.id LIMIT 1;"
)
$jobParts = $jobRow.Trim() -split '\|'
if ($jobParts.Count -ne 2 -or $jobParts[0] -notmatch '^[0-9a-f-]{36}$' -or $jobParts[1] -notmatch '^[0-9a-f-]{36}$') {
    throw "No fue posible obtener jobId y companyId demo: $jobRow"
}
$jobId = $jobParts[0]
$companyId = $jobParts[1]
$postgresVersion = Invoke-Captured -Executable $compose -Arguments @(
    'exec', '-T', 'db', 'psql', '-U', 'utrabajo', '-d', 'utrabajo', '-tA', '-c', 'SHOW server_version;'
)

New-Item -ItemType Directory -Path $resultsPath -Force | Out-Null
$relativeContainerPath = ($relativeResults -replace '\\', '/').Trim('/')

Write-Host '5/9 Capturando commit, maquina, energia y condiciones...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/spike-01-integracion/resumir_resultados.py',
    'capture-context',
    '--results-dir', $resultsPath,
    '--git-revision', $revision,
    '--git-branch', $branch,
    '--working-tree-clean', $workingTreeClean.ToString().ToLowerInvariant(),
    '--docker-version', $dockerVersion,
    '--compose-version', $composeVersion,
    '--postgres-version', $postgresVersion,
    '--job-id', $jobId,
    '--company-id', $companyId,
    '--integration-token-source', $integrationTokenSource
)

Write-Host '6/9 Ejecutando cuatro corridas; la primera es calentamiento...'
$thresholdFailures = @()
foreach ($run in 1..4) {
    Write-Host "Corrida $run de 4"
    $summaryContainer = "/spike/$relativeContainerPath/run-$run.json"
    $rawContainer = "/spike/$relativeContainerPath/run-$run.ndjson"
    $logPath = Join-Path $resultsPath "run-$run.log"

    $arguments = @(
        '--profile', 'load', 'run', '--rm',
        '-e', "RUN_NUMBER=$run",
        '-e', "JOB_ID=$jobId",
        '-e', "EXPECTED_COMPANY_ID=$companyId",
        '-e', "SUMMARY_PATH=$summaryContainer",
        'k6', 'run', '--out', "json=$rawContainer", '/spike/carga-integracion.js'
    )
    $output = & $compose @arguments 2>&1
    $runExitCode = $LASTEXITCODE
    $output | Set-Content -LiteralPath $logPath -Encoding utf8

    $summaryHost = Join-Path $resultsPath "run-$run.json"
    if (-not (Test-Path -LiteralPath $summaryHost)) {
        throw "La corrida $run no produjo su resumen. Revisa $logPath"
    }
    if ($runExitCode -ne 0) {
        $thresholdFailures += $run
        Write-Warning "La corrida $run termino con codigo $runExitCode. Se conserva como evidencia y el agregador decidira si refuta la hipotesis."
    }
}

Write-Host '7/9 Agregando las tres corridas validas sin ocultar fallos...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/spike-01-integracion/resumir_resultados.py',
    'aggregate',
    '--results-dir', $resultsPath
)

Write-Host '8/9 Calculando integridad SHA-256...'
$hashFiles = Get-ChildItem -LiteralPath $resultsPath -File |
    Where-Object { $_.Name -ne 'SHA256SUMS.txt' } |
    Sort-Object Name
$hashLines = foreach ($file in $hashFiles) {
    $hash = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash *$($file.Name)"
}
$hashLines | Set-Content -LiteralPath (Join-Path $resultsPath 'SHA256SUMS.txt') -Encoding ascii

Write-Host '9/9 Evidencia creada.'
Write-Host "Commit medido: $revision"
Write-Host "Rama: $branch"
Write-Host "Resultados: $resultsPath" -ForegroundColor Green
if ($thresholdFailures.Count -gt 0) {
    Write-Warning "k6 reporto thresholds no cumplidos en corridas: $($thresholdFailures -join ', '). El resultado sigue siendo evidencia valida y no fue descartado."
}
