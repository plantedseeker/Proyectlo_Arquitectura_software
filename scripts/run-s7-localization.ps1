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

Write-Host '1/6 Verificando Docker...'
Invoke-Checked -Executable $docker -Arguments @('version')

Write-Host '2/6 Levantando PostgreSQL y API para asegurar datos demo...'
if ($SkipBuild) {
    Invoke-Checked -Executable $compose -Arguments @('up', '-d')
}
else {
    Invoke-Checked -Executable $compose -Arguments @('up', '--build', '-d')
}

$healthy = $false
foreach ($attempt in 1..60) {
    try {
        $health = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 3
        if ($health.status -eq 'UP') { $healthy = $true; break }
    }
    catch { Start-Sleep -Seconds 5 }
}
if (-not $healthy) {
    Invoke-Checked -Executable $compose -Arguments @('logs', '--no-color', '--tail', '150', 'api', 'db')
    throw 'La API no alcanzo estado saludable.'
}

Write-Host '3/6 Verificando semilla extrema...'
if (-not $SkipSeed) {
    $seed = Get-Content -LiteralPath 'experimentos\medicion-escenario-01\seed-mensajeria.sql' -Raw
    $seed | & $compose exec -T db psql -v ON_ERROR_STOP=1 -U utrabajo -d utrabajo
    if ($LASTEXITCODE -ne 0) { throw 'La carga de la semilla fallo.' }
}

Write-Host '4/6 Capturando condiciones del equipo...'
$computer = Get-CimInstance Win32_ComputerSystem -ErrorAction SilentlyContinue
$battery = Get-CimInstance Win32_Battery -ErrorAction SilentlyContinue | Select-Object -First 1
$env:S7_MACHINE_MANUFACTURER = if ($computer) { [string] $computer.Manufacturer } else { 'not_available' }
$env:S7_MACHINE_MODEL = if ($computer) { [string] $computer.Model } else { 'not_available' }
$env:S7_MEMORY_GIB = if ($computer) { [string] ([math]::Round($computer.TotalPhysicalMemory / 1GB, 1)) } else { 'not_available' }
$env:S7_BATTERY_PERCENT = if ($battery) { [string] $battery.EstimatedChargeRemaining } else { 'not_available' }
$env:S7_ENERGY_CONDITION = if (-not $battery) {
    'desktop_or_not_available'
}
elseif ($battery.BatteryStatus -in @(2, 6, 7, 8, 9, 11)) {
    'plugged_in'
}
else {
    'on_battery_or_unknown'
}
$env:S7_POWER_SCHEME = ((& powercfg /getactivescheme 2>$null) -join ' ').Trim()
if (-not $env:S7_POWER_SCHEME) { $env:S7_POWER_SCHEME = 'not_available' }

Write-Host '5/6 Ejecutando EXPLAIN ANALYZE para tres alternativas...'
Invoke-Checked -Executable $python -Arguments @(
    'experimentos/localizacion-s7/ejecutar_explain.py',
    '--compose', $compose,
    '--output', 'experimentos/localizacion-s7/resultados/localizacion.json'
)

Write-Host '6/6 Resultado creado.' -ForegroundColor Green
Write-Host (Join-Path $projectRoot 'experimentos\localizacion-s7\resultados\localizacion.json')
