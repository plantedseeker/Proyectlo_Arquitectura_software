[CmdletBinding()]
param(
    [switch] $MeasureOnly
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

# Evita depender del archivo de contexto que Docker Desktop puede bloquear
# brevemente mientras actualiza su estado.
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

    throw "No se encontro $Command. Cierra y vuelve a abrir PowerShell despues de instalarlo."
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

Write-Host '1/6 Verificando Docker...'
Invoke-Checked -Executable $docker -Arguments @('version')
Invoke-Checked -Executable $compose -Arguments @('version')

if (-not $MeasureOnly) {
    Write-Host '2/6 Construyendo y levantando PostgreSQL 16 y la API...'
    $composeStarted = $false
    foreach ($attempt in 1..3) {
        & $compose @('up', '--build', '-d')
        if ($LASTEXITCODE -eq 0) {
            $composeStarted = $true
            break
        }

        if ($attempt -lt 3) {
            Write-Warning "Docker Compose no pudo iniciar (intento $attempt de 3). Reintentando..."
            Start-Sleep -Seconds 3
        }
    }
    if (-not $composeStarted) {
        throw 'Docker Compose no pudo construir y levantar los servicios despues de tres intentos.'
    }
    Invoke-Checked -Executable $compose -Arguments @('ps')
}
else {
    Write-Host '2/6 Omitiendo reconstruccion: modo de solo medicion.'
}

Write-Host '3/6 Esperando a que la API esté saludable...'
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

if (-not $MeasureOnly) {
    Write-Host '4/6 Verificando el instrumento...'
    Invoke-Checked -Executable $python -Arguments @(
        '-m', 'unittest', 'discover', '-s', 'experiments/postgresql/tests', '-v'
    )

    Write-Host '5/6 Cargando 10.000 ofertas sinteticas...'
    $seed = Get-Content -LiteralPath 'experiments\postgresql\seed.sql' -Raw
    $seed | & $compose exec -T db psql -v ON_ERROR_STOP=1 -U utrabajo -d utrabajo
    if ($LASTEXITCODE -ne 0) {
        throw 'La carga de la semilla fallo.'
    }
}
else {
    Write-Host '4-5/6 Conservando pruebas y semilla de la ejecucion anterior.'
}

Write-Host '6/6 Ejecutando cuatro corridas de rendimiento...'
$revision = (& $git -c "safe.directory=$projectRoot" -C $projectRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $revision -notmatch '^[0-9a-f]{40}$') {
    throw 'No fue posible registrar el commit de Git para la medicion.'
}
Invoke-Checked -Executable $python -Arguments @(
    'experiments/postgresql/run_experiment.py',
    '--runs', '4',
    '--requests', '40',
    '--concurrency', '10',
    '--deployment-topology', 'same-machine',
    '--git-revision', $revision,
    '--output', 'docs/experiment/results/baseline-s4-audit.json'
)

Write-Host ''
Write-Host 'Linea base auditable creada correctamente:' -ForegroundColor Green
Write-Host (Join-Path $projectRoot 'docs\experiment\results\baseline-s4-audit.json')
Write-Host 'Deja Docker Desktop abierto y avisame cuando veas este mensaje.'
