[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

Remove-Item Env:DOCKER_CONTEXT -ErrorAction SilentlyContinue
$env:DOCKER_HOST = 'npipe:////./pipe/docker_engine'
$env:COMPOSE_BAKE = 'false'
$env:MESSAGE_CACHE_ENABLED = 'true'
$env:MESSAGE_CACHE_TTL_MS = '1000'
$env:MESSAGE_CACHE_MAX_ENTRIES = '1000'
$integrationToken = 'spike-local-token'
$env:INTEGRATION_TOKEN = $integrationToken
$runs = 4
$loadLevel = 200
$iterationsPerVu = 4

function Resolve-Executable {
    param(
        [Parameter(Mandatory)] [string] $Command,
        [Parameter(Mandatory)] [string[]] $Candidates
    )
    $resolved = Get-Command $Command -ErrorAction SilentlyContinue
    if ($resolved) { return $resolved.Source }
    foreach ($candidate in $Candidates) {
        if (Test-Path -LiteralPath $candidate) { return $candidate }
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

function Write-JsonFile {
    param(
        [Parameter(Mandatory)] $Value,
        [Parameter(Mandatory)] [string] $Path
    )
    [System.IO.File]::WriteAllText(
        $Path,
        (($Value | ConvertTo-Json -Depth 10) + "`n"),
        [System.Text.UTF8Encoding]::new($false)
    )
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
$git = Resolve-Executable -Command 'git.exe' -Candidates @('C:\Program Files\Git\cmd\git.exe')

Write-Host '1/9 Verificando protocolo confirmado y arbol limpio...'
$status = @(& $git -c "safe.directory=$projectRoot" -C $projectRoot status --porcelain)
if ($LASTEXITCODE -ne 0) { throw 'No fue posible consultar el estado de Git.' }
if ($status.Count -gt 0) {
    throw "El arbol Git debe estar limpio antes de medir.`n$($status -join "`n")"
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

Write-Host '3/9 Construyendo la API con la tactica habilitada...'
Invoke-Checked -Executable $compose -Arguments @('up', '--build', '--force-recreate', '-d')

Write-Host '4/9 Esperando salud de API y base...'
$healthy = $false
foreach ($attempt in 1..60) {
    try {
        $health = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 3
        if ($health.status -eq 'UP') { $healthy = $true; break }
    }
    catch { Start-Sleep -Seconds 5 }
}
if (-not $healthy) {
    & $compose logs --no-color --tail 150 api db
    throw 'La API no alcanzo el estado saludable.'
}
Invoke-Checked -Executable $compose -Arguments @(
    'exec', '-T', 'db', 'pg_isready', '-U', 'utrabajo', '-d', 'utrabajo'
)
$postgresqlVersion = (& $compose exec -T db psql -U utrabajo -d utrabajo -tA -c 'SHOW server_version;').Trim()
if (-not $dockerVersion -or -not $composeVersion -or -not $postgresqlVersion) {
    throw 'No fue posible capturar las versiones del entorno.'
}

Write-Host '5/9 Restaurando y verificando la misma semilla...'
$seed = Get-Content -LiteralPath 'experimentos\medicion-escenario-01\seed-mensajeria.sql' -Raw
$seed | & $compose exec -T db psql -v ON_ERROR_STOP=1 -U utrabajo -d utrabajo
if ($LASTEXITCODE -ne 0) { throw 'La carga de la semilla de mensajeria fallo.' }

$timestamp = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ')
$relativeOutput = "experimentos/crecimiento-mensajeria/comparacion-cache/$timestamp"
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
if ($LASTEXITCODE -ne 0 -or -not $seedJson) { throw 'No fue posible verificar la semilla.' }
$seedObject = $seedJson | ConvertFrom-Json
if (-not $seedObject.matches_preregistered_seed) {
    throw 'La semilla no coincide con la distribucion prerregistrada.'
}
Write-JsonFile -Value $seedObject -Path (Join-Path $outputDir 'verificacion-semilla.json')

Write-Host '6/9 Capturando condiciones y reiniciando contadores de cache...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/crecimiento-mensajeria/comparar_tactica.py',
    '--output', $outputDir,
    '--capture-context',
    '--git-revision', $revision,
    '--branch', $branch,
    '--docker-version', $dockerVersion,
    '--compose-version', $composeVersion,
    '--postgresql-version', $postgresqlVersion
)
$headers = @{ 'X-Integration-Token' = $integrationToken }
$cacheBefore = Invoke-RestMethod -Method Post `
    -Uri 'http://localhost:8080/internal/integration/v1/message-cache/reset' `
    -Headers $headers -TimeoutSec 10
Write-JsonFile -Value $cacheBefore -Path (Join-Path $outputDir 'cache-stats-before.json')
if (-not $cacheBefore.enabled -or $cacheBefore.hits -ne 0) {
    throw 'La cache no quedo habilitada y reiniciada.'
}

Write-Host '7/9 Ejecutando cuatro corridas a 200 VU; la primera es calentamiento...'
foreach ($run in 1..$runs) {
    $expected = $loadLevel * $iterationsPerVu
    Write-Host "Corrida $run/$runs - $loadLevel VU - $expected solicitudes"
    $summaryPath = "/growth/comparacion-cache/$timestamp/run-vu$loadLevel-$run.json"
    $stdoutPath = Join-Path $outputDir "run-vu$loadLevel-$run.stdout.log"
    $stderrPath = Join-Path $outputDir "run-vu$loadLevel-$run.stderr.log"
    $containerName = "utrabajo-s11-cache-vu-$loadLevel-run-$run"
    $arguments = @(
        '--profile', 'load', 'run', '--rm', '--name', $containerName,
        '-e', "LOAD_LEVEL=$loadLevel",
        '-e', "ITERATIONS_PER_VU=$iterationsPerVu",
        '-e', "RUN_NUMBER=$run",
        '-e', "SUMMARY_PATH=$summaryPath",
        'k6', 'run', '/growth/carga-crecimiento.js'
    )
    $process = Start-Process -FilePath $compose -ArgumentList $arguments -NoNewWindow `
        -PassThru -Wait -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath
    $exitCode = $process.ExitCode
    Get-Content -LiteralPath $stdoutPath
    if (Test-Path -LiteralPath $stderrPath) { Get-Content -LiteralPath $stderrPath }
    if ($exitCode -ne 0 -and $exitCode -ne 99) {
        throw "k6 termino con codigo inesperado $exitCode en la corrida $run."
    }
    if (-not (Test-Path -LiteralPath (Join-Path $outputDir "run-vu$loadLevel-$run.json"))) {
        throw "k6 no creo el resumen esperado para la corrida $run."
    }
}
$cacheAfter = Invoke-RestMethod -Method Get `
    -Uri 'http://localhost:8080/internal/integration/v1/message-cache/stats' `
    -Headers $headers -TimeoutSec 10
Write-JsonFile -Value $cacheAfter -Path (Join-Path $outputDir 'cache-stats-after.json')

Write-Host '8/9 Comparando el resultado contra el escalon base de 200 VU...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/crecimiento-mensajeria/comparar_tactica.py',
    '--output', $outputDir,
    '--baseline', 'experimentos/crecimiento-mensajeria/resultados/20261016T224331Z/resultado.json'
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
Write-Host 'Comparacion antes/despues registrada:' -ForegroundColor Green
Write-Host (Join-Path $outputDir 'resultado-comparacion.json')
