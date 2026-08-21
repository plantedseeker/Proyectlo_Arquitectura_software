[CmdletBinding()]
param(
    [switch] $SkipBuild,
    [switch] $SkipSeed
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
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

Write-Host '1/7 Verificando Docker y Compose...'
Invoke-Checked -Executable $docker -Arguments @('version')
Invoke-Checked -Executable $compose -Arguments @('version')

Write-Host '2/7 Levantando PostgreSQL 16 y la API paginada...'
if ($SkipBuild) {
    Invoke-Checked -Executable $compose -Arguments @('up', '-d')
}
else {
    Invoke-Checked -Executable $compose -Arguments @('up', '--build', '-d')
}

Write-Host '3/7 Esperando salud de la API...'
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

Write-Host '4/7 Cargando la distribucion extrema de mensajeria...'
if (-not $SkipSeed) {
    $seed = Get-Content -LiteralPath 'experimentos\medicion-escenario-01\seed-mensajeria.sql' -Raw
    $seed | & $compose exec -T db psql -v ON_ERROR_STOP=1 -U utrabajo -d utrabajo
    if ($LASTEXITCODE -ne 0) {
        throw 'La carga de la semilla de mensajeria fallo.'
    }
}
else {
    Write-Host 'Semilla conservada de una ejecucion anterior.'
}

$revision = (& $git -c "safe.directory=$projectRoot" -C $projectRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $revision -notmatch '^[0-9a-f]{40}$') {
    throw 'No fue posible registrar el commit de Git.'
}

Write-Host '5/7 Capturando maquina, energia y topologia...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/medicion-escenario-01/resumir_resultados.py',
    '--capture-context',
    '--git-revision', $revision
)

Write-Host '6/7 Ejecutando cuatro corridas con k6; la primera es calentamiento...'
foreach ($run in 1..4) {
    Write-Host "Corrida $run de 4"
    $summaryPath = "/experiment/resultados/run-$run.json"
    Invoke-Checked -Executable $compose -Arguments @(
        '--profile', 'load',
        'run', '--rm',
        '-e', "RUN_NUMBER=$run",
        '-e', "SUMMARY_PATH=$summaryPath",
        'k6', 'run', '/experiment/carga-mensajeria.js'
    )
}

Write-Host '7/7 Calculando mediana y decision...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/medicion-escenario-01/resumir_resultados.py'
)

Write-Host ''
Write-Host 'Linea base de Mensajeria y mesa de ayuda creada:' -ForegroundColor Green
Write-Host (Join-Path $projectRoot 'experimentos\medicion-escenario-01\resultados\resultado.json')
