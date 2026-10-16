[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

Remove-Item Env:DOCKER_CONTEXT -ErrorAction SilentlyContinue
$env:DOCKER_HOST = 'npipe:////./pipe/docker_engine'
$env:COMPOSE_BAKE = 'false'

$levels = @(10, 50, 100, 150, 200)
$runs = 4
$iterationsPerVu = 4

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

function Start-ResourceSampler {
    param(
        [Parameter(Mandatory)] [string] $OutputPath,
        [Parameter(Mandatory)] [string] $MarkerPath,
        [Parameter(Mandatory)] [string] $LoadContainerName
    )

    New-Item -ItemType File -Path $MarkerPath -Force | Out-Null
    return Start-Job -ScriptBlock {
        param($DockerExecutable, $ComposeExecutable, $Root, $Output, $Marker, $K6Name)
        Set-Location -LiteralPath $Root
        while (Test-Path -LiteralPath $Marker) {
            $ids = @(& $ComposeExecutable ps -q 2>$null | Where-Object { $_ })
            $k6Id = (& $DockerExecutable ps -q --filter "name=^/$K6Name$" 2>$null).Trim()
            if ($k6Id -and $ids -notcontains $k6Id) {
                $ids += $k6Id
            }
            $containers = @()
            if ($ids.Count -gt 0) {
                $lines = @(& $DockerExecutable stats --no-stream --format '{{json .}}' @ids 2>$null)
                foreach ($line in $lines) {
                    if ($line) {
                        try { $containers += ($line | ConvertFrom-Json) } catch { }
                    }
                }
            }
            $record = [ordered]@{
                captured_at_utc = [DateTime]::UtcNow.ToString('o')
                containers = $containers
            }
            Add-Content -LiteralPath $Output -Value ($record | ConvertTo-Json -Compress -Depth 6) -Encoding UTF8
            Start-Sleep -Milliseconds 500
        }
    } -ArgumentList $docker, $compose, $projectRoot, $OutputPath, $MarkerPath, $LoadContainerName
}

function Stop-ResourceSampler {
    param(
        [Parameter(Mandatory)] $Job,
        [Parameter(Mandatory)] [string] $MarkerPath
    )

    Remove-Item -LiteralPath $MarkerPath -Force -ErrorAction SilentlyContinue
    Wait-Job -Job $Job -Timeout 15 | Out-Null
    Receive-Job -Job $Job -ErrorAction SilentlyContinue | Out-Null
    Remove-Job -Job $Job -Force -ErrorAction SilentlyContinue
}

Write-Host '1/9 Verificando que el protocolo este confirmado y el arbol limpio...'
$status = @(& $git -c "safe.directory=$projectRoot" -C $projectRoot status --porcelain)
if ($LASTEXITCODE -ne 0) {
    throw 'No fue posible consultar el estado de Git.'
}
if ($status.Count -gt 0) {
    throw "El arbol Git debe estar limpio antes de medir. Confirme primero protocolo e instrumento.`n$($status -join "`n")"
}
$revision = (& $git -c "safe.directory=$projectRoot" -C $projectRoot rev-parse HEAD).Trim()
$branch = (& $git -c "safe.directory=$projectRoot" -C $projectRoot branch --show-current).Trim()
if ($revision -notmatch '^[0-9a-f]{40}$' -or -not $branch) {
    throw 'No fue posible registrar revision y rama.'
}

Write-Host '2/9 Verificando Docker y Compose...'
Invoke-Checked -Executable $docker -Arguments @('version')
Invoke-Checked -Executable $compose -Arguments @('version')
$dockerVersion = (& $docker version --format '{{.Client.Version}} client / {{.Server.Version}} server').Trim()
$composeVersion = (& $compose version --short).Trim()

Write-Host '3/9 Construyendo y levantando PostgreSQL 16 y la API...'
Invoke-Checked -Executable $compose -Arguments @('up', '--build', '-d')

Write-Host '4/9 Esperando salud de API y base...'
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
    & $compose logs --no-color --tail 150 api db
    throw 'La API no alcanzo el estado saludable.'
}
Invoke-Checked -Executable $compose -Arguments @('exec', '-T', 'db', 'pg_isready', '-U', 'utrabajo', '-d', 'utrabajo')
$postgresqlVersion = (& $compose exec -T db psql -U utrabajo -d utrabajo -tA -c 'SHOW server_version;').Trim()
if (-not $dockerVersion -or -not $composeVersion -or -not $postgresqlVersion) {
    throw 'No fue posible capturar las versiones del entorno.'
}

Write-Host '5/9 Cargando y verificando la semilla determinista...'
$seed = Get-Content -LiteralPath 'experimentos\medicion-escenario-01\seed-mensajeria.sql' -Raw
$seed | & $compose exec -T db psql -v ON_ERROR_STOP=1 -U utrabajo -d utrabajo
if ($LASTEXITCODE -ne 0) {
    throw 'La carga de la semilla de mensajeria fallo.'
}

$timestamp = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ')
$relativeOutput = "experimentos/crecimiento-mensajeria/resultados/$timestamp"
$outputDir = Join-Path $projectRoot ($relativeOutput -replace '/', '\')
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

$seedQuery = @'
WITH sizes AS (
    SELECT c.id, count(m.id) AS message_count
    FROM chat c
    JOIN message m ON m.chat_id = c.id
    WHERE c.id IN (
        SELECT md5('mensajeria-chat-' || g)::uuid FROM generate_series(1, 1000) AS g
    )
    GROUP BY c.id
), facts AS (
    SELECT count(*) AS conversations,
           sum(message_count) AS messages,
           count(*) FILTER (WHERE message_count = 10) AS small_count,
           count(*) FILTER (WHERE message_count = 1000) AS medium_count,
           count(*) FILTER (WHERE message_count = 10000) AS large_count,
           count(*) FILTER (WHERE message_count = 100000) AS extreme_count,
           max(message_count) AS largest
    FROM sizes
)
SELECT json_build_object(
    'schema_version', 1,
    'captured_at_utc', now(),
    'verification_method', 'SQL de solo lectura posterior a la semilla',
    'seeded_conversations', conversations,
    'total_messages', messages,
    'distribution', json_build_object(
        'conversations_with_10_messages', small_count,
        'conversations_with_1000_messages', medium_count,
        'conversations_with_10000_messages', large_count,
        'conversations_with_100000_messages', extreme_count
    ),
    'maximum_messages_per_conversation', largest,
    'matches_preregistered_seed',
        conversations = 1000 AND messages = 289000 AND
        small_count = 900 AND medium_count = 90 AND large_count = 9 AND
        extreme_count = 1 AND largest = 100000
)::text FROM facts;
'@
$seedJson = ($seedQuery | & $compose exec -T db psql -v ON_ERROR_STOP=1 -U utrabajo -d utrabajo -tA).Trim()
if ($LASTEXITCODE -ne 0 -or -not $seedJson) {
    throw 'No fue posible verificar la semilla.'
}
$seedObject = $seedJson | ConvertFrom-Json
if (-not $seedObject.matches_preregistered_seed) {
    throw 'La semilla no coincide con la distribucion prerregistrada.'
}
[System.IO.File]::WriteAllText(
    (Join-Path $outputDir 'verificacion-semilla.json'),
    (($seedObject | ConvertTo-Json -Depth 8) + "`n"),
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host '6/9 Capturando maquina, energia, rama y commit...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/crecimiento-mensajeria/resumir_resultados.py',
    '--output', $outputDir,
    '--capture-context',
    '--git-revision', $revision,
    '--branch', $branch,
    '--docker-version', $dockerVersion,
    '--compose-version', $composeVersion,
    '--postgresql-version', $postgresqlVersion
)

Write-Host '7/9 Ejecutando cinco escalones; primera corrida de cada uno es calentamiento...'
foreach ($level in $levels) {
    foreach ($run in 1..$runs) {
        $expected = $level * $iterationsPerVu
        Write-Host "VU $level - corrida $run/$runs - $expected solicitudes"
        $summaryPath = "/growth/resultados/$timestamp/run-vu$level-$run.json"
        $stdoutPath = Join-Path $outputDir "run-vu$level-$run.stdout.log"
        $stderrPath = Join-Path $outputDir "run-vu$level-$run.stderr.log"
        $telemetryPath = Join-Path $outputDir "telemetry-vu$level-$run.jsonl"
        $markerPath = Join-Path $outputDir ".sampling-vu$level-$run"
        $containerName = "utrabajo-s11-k6-vu-$level-run-$run"
        $sampler = Start-ResourceSampler -OutputPath $telemetryPath -MarkerPath $markerPath `
            -LoadContainerName $containerName
        try {
            $arguments = @(
                '--profile', 'load', 'run', '--rm', '--name', $containerName,
                '-e', "LOAD_LEVEL=$level",
                '-e', "ITERATIONS_PER_VU=$iterationsPerVu",
                '-e', "RUN_NUMBER=$run",
                '-e', "SUMMARY_PATH=$summaryPath",
                'k6', 'run', '/growth/carga-crecimiento.js'
            )
            $process = Start-Process -FilePath $compose -ArgumentList $arguments -NoNewWindow -PassThru -Wait `
                -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath
            $exitCode = $process.ExitCode
        }
        finally {
            Stop-ResourceSampler -Job $sampler -MarkerPath $markerPath
        }
        Get-Content -LiteralPath $stdoutPath
        if (Test-Path -LiteralPath $stderrPath) {
            Get-Content -LiteralPath $stderrPath
        }
        if ($exitCode -ne 0 -and $exitCode -ne 99) {
            throw "k6 termino con codigo inesperado $exitCode en VU $level, corrida $run."
        }
        if (-not (Test-Path -LiteralPath (Join-Path $outputDir "run-vu$level-$run.json"))) {
            throw "k6 no creo el resumen esperado para VU $level, corrida $run."
        }
    }
}

Write-Host '8/9 Agregando escalones y detectando la primera degradacion sostenida...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/crecimiento-mensajeria/resumir_resultados.py',
    '--output', $outputDir
)

Write-Host '9/9 Registrando huellas SHA-256...'
$hashLines = Get-ChildItem -LiteralPath $outputDir -File |
    Where-Object { $_.Name -ne 'SHA256SUMS.txt' } |
    Sort-Object Name |
    ForEach-Object {
        $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        "$hash  $($_.Name)"
    }
[System.IO.File]::WriteAllLines(
    (Join-Path $outputDir 'SHA256SUMS.txt'),
    $hashLines,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host ''
Write-Host 'Experimento de crecimiento registrado:' -ForegroundColor Green
Write-Host (Join-Path $outputDir 'resultado.json')
