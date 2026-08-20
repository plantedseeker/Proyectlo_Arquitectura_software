[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

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

    throw "No se encontró $Command. Cierra y vuelve a abrir PowerShell después de instalarlo."
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

function Invoke-Checked {
    param(
        [Parameter(Mandatory)] [string] $Executable,
        [Parameter(ValueFromRemainingArguments)] [string[]] $Arguments
    )

    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "El comando terminó con código ${LASTEXITCODE}: $Executable $($Arguments -join ' ')"
    }
}

Write-Host '1/6 Verificando Docker...'
Invoke-Checked -Executable $docker -Arguments @('version')
Invoke-Checked -Executable $compose -Arguments @('version')

Write-Host '2/6 Construyendo y levantando PostgreSQL 16 y la API...'
Invoke-Checked -Executable $compose -Arguments @('up', '--build', '-d')
Invoke-Checked -Executable $compose -Arguments @('ps')

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
    throw 'La API no alcanzó el estado saludable.'
}

Write-Host '4/6 Verificando el instrumento...'
Invoke-Checked -Executable $python -Arguments @(
    '-m', 'unittest', 'discover', '-s', 'experiments/postgresql/tests', '-v'
)

Write-Host '5/6 Cargando 10.000 ofertas sintéticas...'
$seed = Get-Content -LiteralPath 'experiments\postgresql\seed.sql' -Raw
$seed | & $compose exec -T db psql -v ON_ERROR_STOP=1 -U utrabajo -d utrabajo
if ($LASTEXITCODE -ne 0) {
    throw 'La carga de la semilla falló.'
}

Write-Host '6/6 Ejecutando cuatro corridas de rendimiento...'
Invoke-Checked -Executable $python -Arguments @(
    'experiments/postgresql/run_experiment.py',
    '--runs', '4',
    '--requests', '40',
    '--concurrency', '10',
    '--output', 'docs/experiment/results/baseline.json'
)

Write-Host ''
Write-Host 'Línea base creada correctamente:' -ForegroundColor Green
Write-Host (Join-Path $projectRoot 'docs\experiment\results\baseline.json')
Write-Host 'Deja Docker Desktop abierto y avísame cuando veas este mensaje.'
