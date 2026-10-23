[CmdletBinding()]
param(
    [switch] $GrafanaCloud,
    [ValidatePattern('^\d+$')]
    [string] $GrafanaCloudStackId = '1889948',
    [ValidatePattern('^\d*$')]
    [string] $GrafanaCloudProjectId = ''
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

Remove-Item Env:DOCKER_CONTEXT -ErrorAction SilentlyContinue
$env:DOCKER_HOST = 'npipe:////./pipe/docker_engine'
$env:COMPOSE_BAKE = 'false'

$levels = @(10, 50, 100, 150, 200)
$runs = 4
$iterationsPerVu = 4
$defaultK6Image = 'grafana/k6:0.54.0'
$cloudK6Image = 'grafana/k6:2.2.0'
$k6Image = if ($GrafanaCloud) { $cloudK6Image } else { $defaultK6Image }
$env:K6_IMAGE = $k6Image

if ($GrafanaCloud) {
    if (-not $env:K6_CLOUD_TOKEN) {
        $secureToken = Read-Host 'Pegue el Personal API token de Grafana Cloud (no se mostrara)' -AsSecureString
        $tokenPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureToken)
        try {
            $env:K6_CLOUD_TOKEN = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($tokenPointer)
        }
        finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($tokenPointer)
        }
    }
    if ([string]::IsNullOrWhiteSpace($env:K6_CLOUD_TOKEN)) {
        throw 'Grafana Cloud requiere un Personal API token no vacio.'
    }
    $env:K6_CLOUD_TOKEN = $env:K6_CLOUD_TOKEN.Trim()
    if ($env:K6_CLOUD_TOKEN -notmatch '^[\x21-\x7E]+$') {
        throw 'El valor pegado no es un token valido. No copie los puntos del campo oculto: regenere el Personal API token y copie el valor revelado una sola vez.'
    }
    $env:K6_CLOUD_STACK_ID = $GrafanaCloudStackId
    Write-Host "Grafana Cloud habilitado para el Stack ID $GrafanaCloudStackId." -ForegroundColor Cyan
    Write-Warning 'Esta reejecucion transmite metricas y consume la cuota de Grafana Cloud. No reemplaza los resultados locales ya aprobados.'
}

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

if ($GrafanaCloud -and -not $GrafanaCloudProjectId) {
    Write-Host 'Resolviendo el proyecto predeterminado de Grafana Cloud...'
    $projectStdout = Join-Path ([System.IO.Path]::GetTempPath()) "utrabajo-k6-projects-$PID.stdout.json"
    $projectStderr = Join-Path ([System.IO.Path]::GetTempPath()) "utrabajo-k6-projects-$PID.stderr.log"
    try {
        $projectArguments = @(
            '--profile', 'load', 'run', '--rm', '--no-deps',
            '-e', 'K6_CLOUD_TOKEN',
            '-e', "K6_CLOUD_STACK_ID=$GrafanaCloudStackId",
            'k6', 'cloud', 'project', 'list', '--json'
        )
        $projectProcess = Start-Process -FilePath $compose -ArgumentList $projectArguments `
            -NoNewWindow -PassThru -Wait -RedirectStandardOutput $projectStdout `
            -RedirectStandardError $projectStderr
        if ($projectProcess.ExitCode -ne 0) {
            Get-Content -LiteralPath $projectStderr -ErrorAction SilentlyContinue
            throw "No fue posible listar los proyectos de Grafana Cloud (codigo $($projectProcess.ExitCode))."
        }
        $projectJson = Get-Content -LiteralPath $projectStdout -Raw
        try {
            $projects = @($projectJson | ConvertFrom-Json)
        }
        catch {
            throw "Grafana Cloud no devolvio una lista JSON valida de proyectos: $projectJson"
        }
        if ($projects.Count -eq 0) {
            throw 'El stack no contiene proyectos disponibles para el token indicado.'
        }
        $selectedProject = $projects | Where-Object {
            $_.default -eq $true -or $_.is_default -eq $true -or
            $_.isDefault -eq $true -or $_.is_default_project -eq $true
        } | Select-Object -First 1
        if (-not $selectedProject) {
            $selectedProject = $projects | Where-Object {
                $_.name -match '^default( project)?$'
            } | Select-Object -First 1
        }
        if (-not $selectedProject -and $projects.Count -eq 1) {
            $selectedProject = $projects[0]
        }
        if (-not $selectedProject) {
            $availableProjects = $projects | ForEach-Object { "$($_.id): $($_.name)" }
            throw "No fue posible determinar el proyecto predeterminado. Disponibles: $($availableProjects -join ', ')"
        }
        $resolvedProjectId = if ($selectedProject.id) {
            $selectedProject.id
        }
        elseif ($selectedProject.project_id) {
            $selectedProject.project_id
        }
        else {
            $selectedProject.projectID
        }
        if ("$resolvedProjectId" -notmatch '^\d+$') {
            throw 'El proyecto predeterminado no contiene un ID numerico valido.'
        }
        $GrafanaCloudProjectId = "$resolvedProjectId"
        Write-Host "Proyecto Grafana Cloud resuelto: $GrafanaCloudProjectId ($($selectedProject.name))." -ForegroundColor Cyan
    }
    finally {
        Remove-Item -LiteralPath $projectStdout, $projectStderr -Force -ErrorAction SilentlyContinue
    }
}
if ($GrafanaCloud) {
    $env:K6_CLOUD_PROJECT_ID = $GrafanaCloudProjectId
}

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
$contextArguments = @(
    'experimentos/crecimiento-mensajeria/resumir_resultados.py',
    '--output', $outputDir,
    '--capture-context',
    '--git-revision', $revision,
    '--branch', $branch,
    '--docker-version', $dockerVersion,
    '--compose-version', $composeVersion,
    '--postgresql-version', $postgresqlVersion,
    '--k6-image', $k6Image
)
if ($GrafanaCloud) {
    $contextArguments += @(
        '--grafana-cloud-stack-id', $GrafanaCloudStackId,
        '--grafana-cloud-project-id', $GrafanaCloudProjectId
    )
}
Invoke-Checked -Executable $python -Arguments $contextArguments

Write-Host '7/9 Ejecutando cinco escalones; primera corrida de cada uno es calentamiento...'
$cloudRuns = @()
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
                '-e', "SUMMARY_PATH=$summaryPath"
            )
            if ($GrafanaCloud) {
                $cloudName = "utrabajo-s11-growth-vu${level}-run${run}-${timestamp}"
                $arguments += @(
                    '-e', 'K6_CLOUD_TOKEN',
                    '-e', "K6_CLOUD_STACK_ID=$GrafanaCloudStackId",
                    '-e', "K6_CLOUD_PROJECT_ID=$GrafanaCloudProjectId",
                    '-e', "K6_CLOUD_NAME=$cloudName",
                    'k6', 'cloud', 'run', '--local-execution', '--no-archive-upload',
                    '/growth/carga-crecimiento.js'
                )
            }
            else {
                $arguments += @('k6', 'run', '/growth/carga-crecimiento.js')
            }
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
        if ($GrafanaCloud) {
            $combinedOutput = (Get-Content -LiteralPath $stdoutPath -Raw) + "`n" + `
                (Get-Content -LiteralPath $stderrPath -Raw -ErrorAction SilentlyContinue)
            $urlMatch = [regex]::Match(
                $combinedOutput,
                'https://[^\s\)]+/a/k6-app/runs/[^\s\)]+'
            )
            $cloudRuns += [ordered]@{
                load_level_vus = $level
                run = $run
                discarded = $run -eq 1
                test_name = $cloudName
                dashboard_url = if ($urlMatch.Success) { $urlMatch.Value } else { $null }
            }
            if (-not $urlMatch.Success) {
                Write-Warning "No se encontro el enlace del panel cloud en la salida de VU $level, corrida $run."
            }
        }
    }
}

if ($GrafanaCloud) {
    $cloudIndex = [ordered]@{
        schema_version = 1
        captured_at_utc = [DateTime]::UtcNow.ToString('o')
        stack_id = $GrafanaCloudStackId
        project_id = $GrafanaCloudProjectId
        k6_image = $k6Image
        execution = 'local con transmision de metricas a Grafana Cloud'
        credential_persisted = $false
        result_directory = $relativeOutput
        runs = $cloudRuns
    }
    Write-JsonFile -Value $cloudIndex -Path (Join-Path $outputDir 'grafana-cloud-runs.json')

    $cloudDirectory = Join-Path $projectRoot 'experimentos\crecimiento-mensajeria\grafana-cloud'
    New-Item -ItemType Directory -Path $cloudDirectory -Force | Out-Null
    $markdown = @(
        '# Paneles Grafana Cloud - ultima reejecucion S11',
        '',
        "- Stack ID: ``$GrafanaCloudStackId``",
        "- Project ID: ``$GrafanaCloudProjectId``",
        "- Imagen de ejecucion: ``$k6Image``",
        "- Resultado local: [``$relativeOutput``](../resultados/$timestamp/resultado.json)",
        "- Indice estructurado: [``grafana-cloud-runs.json``](../resultados/$timestamp/grafana-cloud-runs.json)",
        '',
        '> Esta es una reejecucion complementaria para visualizacion. Los resultados primarios aprobados siguen siendo los JSON locales de 20261016T224331Z.',
        '',
        '## Ejecuciones'
    )
    foreach ($cloudRun in $cloudRuns) {
        $label = "VU $($cloudRun.load_level_vus), corrida $($cloudRun.run)"
        if ($cloudRun.discarded) { $label += ' (calentamiento)' }
        if ($cloudRun.dashboard_url) {
            $markdown += "- [$label]($($cloudRun.dashboard_url))"
        }
        else {
            $markdown += "- $label - enlace no detectado; revisar el log local."
        }
    }
    [System.IO.File]::WriteAllLines(
        (Join-Path $cloudDirectory 'ultima-ejecucion.md'),
        $markdown,
        [System.Text.UTF8Encoding]::new($false)
    )
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
