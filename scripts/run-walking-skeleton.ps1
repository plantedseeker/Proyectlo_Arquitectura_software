[CmdletBinding()]
param(
    [switch] $SkipBuild,
    [switch] $StopAfter,
    [ValidateRange(30, 600)]
    [int] $TimeoutSeconds = 180,
    [string] $OutputPath = 'experimentos\walking-skeleton\resultados\ultima-ejecucion.json'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $projectRoot 'docker-compose.yml'
$baseUrl = 'http://localhost:8080'
$checks = [System.Collections.Generic.List[object]]::new()
$composeWasRequested = $false

Set-Location -LiteralPath $projectRoot

# Docker Desktop puede bloquear brevemente el archivo de contexto en Windows.
# El pipe evita depender de ese archivo y apunta al motor local de Linux.
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

    throw "No se encontro $Command. Instala la herramienta y abre una PowerShell nueva."
}

function Add-Check {
    param(
        [Parameter(Mandatory)] [string] $Name,
        [Parameter(Mandatory)] [string] $Evidence
    )

    $checks.Add([ordered]@{
        name = $Name
        status = 'PASS'
        evidence = $Evidence
    })
    Write-Host "  PASS - $Name" -ForegroundColor Green
}

function Invoke-Compose {
    param([Parameter(ValueFromRemainingArguments)] [string[]] $Arguments)

    $allArguments = @('compose', '-f', $composeFile) + $Arguments
    & $script:docker @allArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose termino con codigo ${LASTEXITCODE}: docker $($allArguments -join ' ')"
    }
}

function Get-ComposeOutput {
    param([Parameter(ValueFromRemainingArguments)] [string[]] $Arguments)

    $allArguments = @('compose', '-f', $composeFile) + $Arguments
    $output = & $script:docker @allArguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose termino con codigo ${LASTEXITCODE}: docker $($allArguments -join ' ')"
    }
    return @($output)
}

function Invoke-Api {
    param(
        [Parameter(Mandatory)] [ValidateSet('GET', 'POST')] [string] $Method,
        [Parameter(Mandatory)] [string] $Path,
        [string] $Token,
        [object] $Body
    )

    $request = @{
        Uri = "$baseUrl$Path"
        Method = $Method
        TimeoutSec = 15
    }
    if ($Token) {
        $request.Headers = @{ Authorization = "Bearer $Token" }
    }
    if ($null -ne $Body) {
        $request.ContentType = 'application/json'
        $request.Body = $Body | ConvertTo-Json -Compress
    }
    return Invoke-RestMethod @request
}

function Wait-ApiHealthy {
    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    $lastProblem = 'sin respuesta'
    while ([DateTime]::UtcNow -lt $deadline) {
        try {
            $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -TimeoutSec 3
            if ($health.status -eq 'UP') {
                return $health
            }
            $lastProblem = "estado $($health.status)"
        }
        catch {
            $lastProblem = $_.Exception.Message
        }
        Start-Sleep -Seconds 3
    }
    throw "La API no alcanzo salud UP en $TimeoutSeconds segundos. Ultimo problema: $lastProblem"
}

function Get-HttpStatusWithoutToken {
    param([Parameter(Mandatory)] [string] $Path)

    try {
        $response = Invoke-WebRequest -Uri "$baseUrl$Path" -Method GET -TimeoutSec 10 -UseBasicParsing
        return [int] $response.StatusCode
    }
    catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            return [int] $_.Exception.Response.StatusCode
        }
        if ($_.Exception.Message -match '(?<status>401|403)') {
            return [int] $Matches.status
        }
        throw
    }
}

$dockerRoot = Join-Path $env:LOCALAPPDATA 'Programs\DockerDesktop\resources\bin'
$script:docker = Resolve-Executable -Command 'docker.exe' -Candidates @(
    (Join-Path $dockerRoot 'docker.exe'),
    'C:\Program Files\Docker\Docker\resources\bin\docker.exe'
)
$git = Resolve-Executable -Command 'git.exe' -Candidates @('C:\Program Files\Git\cmd\git.exe')

$failure = $null
$dockerVersion = $null
$composeVersion = $null
$postgresVersion = $null
$gitRevision = $null
$chatId = $null
$messageId = $null
$marker = $null

try {
    Write-Host '1/9 Preflight de Docker y Compose...'
    $dockerVersion = (& $docker version --format '{{.Server.Version}}' 2>&1 | Select-Object -Last 1).ToString().Trim()
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($dockerVersion)) {
        throw 'Docker Desktop no responde. Abre Docker Desktop y espera a que muestre Engine running.'
    }
    $composeVersion = (& $docker compose version --short 2>&1 | Select-Object -Last 1).ToString().Trim()
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($composeVersion)) {
        throw 'El complemento Docker Compose no esta disponible.'
    }
    Add-Check -Name 'Motor local disponible' -Evidence "Docker $dockerVersion; Compose $composeVersion"

    $managedIds = @(Get-ComposeOutput -Arguments @('ps', '--all', '-q') |
        ForEach-Object { $_.ToString().Trim() } |
        Where-Object { $_ })
    $foreignPortOwners = [System.Collections.Generic.List[string]]::new()
    foreach ($port in @(5432, 8080)) {
        $owners = & $docker ps --filter "publish=$port" --format '{{.ID}}|{{.Names}}|{{.Ports}}' 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "No fue posible inspeccionar el puerto $port con Docker."
        }
        foreach ($owner in @($owners)) {
            $parts = $owner.ToString().Split('|', 3)
            if ($parts.Count -eq 3 -and $parts[0] -notin $managedIds) {
                $foreignPortOwners.Add("puerto $port -> $($parts[1]) [$($parts[0])] ($($parts[2]))")
            }
        }
    }
    if ($foreignPortOwners.Count -gt 0) {
        $detail = $foreignPortOwners -join '; '
        throw "Hay otro contenedor usando un puerto requerido: $detail. No se detuvo ni borro nada. Identifica su proyecto con 'docker inspect' y detenlo de forma controlada antes de repetir."
    }
    Add-Check -Name 'Puertos del despliegue disponibles' -Evidence 'No hay contenedores ajenos publicando 5432 o 8080'

    Write-Host '2/9 Levantando PostgreSQL 16 y la API...'
    $composeWasRequested = $true
    if ($SkipBuild) {
        Invoke-Compose -Arguments @('up', '-d')
    }
    else {
        Invoke-Compose -Arguments @('up', '--build', '-d')
    }
    Add-Check -Name 'Contenedores solicitados' -Evidence $(if ($SkipBuild) { 'docker compose up -d' } else { 'docker compose up --build -d' })

    Write-Host '3/9 Esperando salud de la API y PostgreSQL...'
    $health = Wait-ApiHealthy
    Add-Check -Name 'API saludable' -Evidence "$baseUrl/actuator/health = $($health.status)"

    $readyOutput = Get-ComposeOutput -Arguments @(
        'exec', '-T', 'db', 'pg_isready', '-U', 'utrabajo', '-d', 'utrabajo'
    )
    $readyText = ($readyOutput -join ' ').Trim()
    if ($readyText -notmatch 'accepting connections') {
        throw "PostgreSQL no reporto conexiones disponibles: $readyText"
    }
    $postgresOutput = Get-ComposeOutput -Arguments @(
        'exec', '-T', 'db', 'psql', '-X', '-q', '-tA', '-v', 'ON_ERROR_STOP=1',
        '-U', 'utrabajo', '-d', 'utrabajo', '-c', 'SHOW server_version;'
    )
    $postgresVersion = ($postgresOutput | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ } | Select-Object -Last 1)
    Add-Check -Name 'PostgreSQL disponible' -Evidence "PostgreSQL $postgresVersion; $readyText"

    Write-Host '4/9 Verificando migraciones Flyway...'
    $migrationOutput = Get-ComposeOutput -Arguments @(
        'exec', '-T', 'db', 'psql', '-X', '-q', '-tA', '-v', 'ON_ERROR_STOP=1',
        '-U', 'utrabajo', '-d', 'utrabajo', '-c',
        'SELECT count(*) FROM flyway_schema_history WHERE success IS NOT TRUE;'
    )
    $failedMigrations = ($migrationOutput | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ -match '^\d+$' } | Select-Object -Last 1)
    if ($failedMigrations -ne '0') {
        throw "Flyway reporta migraciones fallidas: $failedMigrations"
    }
    Add-Check -Name 'Esquema migrado' -Evidence 'flyway_schema_history no contiene migraciones fallidas'

    Write-Host '5/9 Autenticando al estudiante sintetico...'
    $login = Invoke-Api -Method POST -Path '/api/auth/login' -Body @{
        email = 'estudiante@utrabajo.local'
        password = 'UTrabajo1!'
    }
    if ([string]::IsNullOrWhiteSpace($login.token)) {
        throw 'El login no devolvio un token.'
    }
    $me = Invoke-Api -Method GET -Path '/api/auth/me' -Token $login.token
    if ($me.email -ne 'estudiante@utrabajo.local' -or $me.role -ne 'student') {
        throw "La sesion no corresponde al estudiante demo: $($me.email), $($me.role)"
    }
    Add-Check -Name 'Autenticacion y sesion' -Evidence "$($me.email), rol $($me.role)"

    Write-Host '6/9 Recorriendo oferta -> chat -> mensaje...'
    $jobs = @(Invoke-Api -Method GET -Path '/api/jobs?limit=1&offset=0' -Token $login.token)
    if ($jobs.Count -ne 1 -or [string]::IsNullOrWhiteSpace($jobs[0].id)) {
        throw 'No existe una oferta sintetica activa. Revisa SEED_DEMO=true y los logs de la API.'
    }
    $chat = Invoke-Api -Method POST -Path '/api/chats' -Token $login.token -Body @{ jobId = $jobs[0].id }
    $chatId = $chat.id
    if ($chatId -notmatch '^[0-9a-fA-F-]{36}$') {
        throw "La API no devolvio un chat valido: $chatId"
    }
    $marker = "walking-skeleton-$([DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ'))"
    $sent = Invoke-Api -Method POST -Path "/api/chats/$chatId/messages" -Token $login.token -Body @{ message = $marker }
    $messageId = $sent.id
    $messages = @(Invoke-Api -Method GET -Path "/api/chats/$chatId/messages?limit=100&offset=0" -Token $login.token)
    $observed = $messages | Where-Object { $_.id -eq $messageId -and $_.message -eq $marker } | Select-Object -First 1
    if (-not $observed) {
        throw "El mensaje $messageId no regreso por la API."
    }
    Add-Check -Name 'Flujo vertical de mensajeria' -Evidence "oferta $($jobs[0].id); chat $chatId; mensaje $messageId"

    Write-Host '7/9 Confirmando persistencia directa en PostgreSQL...'
    $countSql = "SELECT count(*) FROM message WHERE id = '$messageId'::uuid AND body = '$marker';"
    $countOutput = Get-ComposeOutput -Arguments @(
        'exec', '-T', 'db', 'psql', '-X', '-q', '-tA', '-v', 'ON_ERROR_STOP=1',
        '-U', 'utrabajo', '-d', 'utrabajo', '-c', $countSql
    )
    $persistedCount = ($countOutput | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ -match '^\d+$' } | Select-Object -Last 1)
    if ($persistedCount -ne '1') {
        throw "PostgreSQL no contiene exactamente una fila para el mensaje $messageId."
    }
    Add-Check -Name 'Persistencia comprobada' -Evidence "message.id=$messageId existe una vez en PostgreSQL"

    Write-Host '8/9 Verificando proteccion del limite de confianza...'
    $unauthorizedStatus = Get-HttpStatusWithoutToken -Path '/api/chats'
    if ($unauthorizedStatus -notin @(401, 403)) {
        throw "GET /api/chats sin token devolvio HTTP $unauthorizedStatus; se esperaba rechazo 401/403."
    }
    Add-Check -Name 'Ruta protegida sin token' -Evidence "GET /api/chats devolvio HTTP $unauthorizedStatus"

    Write-Host '9/9 Guardando evidencia reproducible...'
    $gitRevision = (& $git -c "safe.directory=$projectRoot" -C $projectRoot rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $gitRevision -notmatch '^[0-9a-f]{40}$') {
        throw 'No fue posible identificar la revision Git de la ejecucion.'
    }

    $result = [ordered]@{
        schema_version = 1
        captured_at_utc = [DateTime]::UtcNow.ToString('o')
        decision = 'supported'
        walking_skeleton = 'PostgreSQL -> Flyway/seed -> API -> auth -> job -> chat -> message -> API/DB verification'
        conditions = [ordered]@{
            base_url = $baseUrl
            deployment_topology = 'Docker Desktop local; API y PostgreSQL comparten equipo'
            docker_version = $dockerVersion
            compose_version = $composeVersion
            postgres_version = $postgresVersion
            powershell_version = $PSVersionTable.PSVersion.ToString()
            os = [System.Environment]::OSVersion.VersionString
            logical_cpus = [System.Environment]::ProcessorCount
            git_revision = $gitRevision
        }
        artifacts = [ordered]@{
            chat_id = $chatId
            message_id = $messageId
            message_marker = $marker
        }
        checks = $checks
        manual_android_checkpoint = [ordered]@{
            status = 'PENDING'
            expected_message_marker = $marker
            evidence = $null
        }
    }

    $absoluteOutput = if ([System.IO.Path]::IsPathRooted($OutputPath)) {
        $OutputPath
    }
    else {
        Join-Path $projectRoot $OutputPath
    }
    $outputDirectory = Split-Path -Parent $absoluteOutput
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
    $result | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $absoluteOutput -Encoding UTF8
    Add-Check -Name 'Evidencia escrita' -Evidence $absoluteOutput

    Write-Host ''
    Write-Host 'WALKING SKELETON: SOPORTADO' -ForegroundColor Green
    Write-Host "Evidencia: $absoluteOutput"
    Write-Host "Checkpoint Android: inicia como estudiante y busca el mensaje $marker en el chat $chatId."
}
catch {
    $failure = $_
    Write-Host ''
    Write-Host "WALKING SKELETON: FALLO - $($_.Exception.Message)" -ForegroundColor Red
    if ($composeWasRequested) {
        Write-Host 'Estado de contenedores:' -ForegroundColor Yellow
        & $docker compose -f $composeFile ps 2>&1
        Write-Host 'Ultimos logs de API y base:' -ForegroundColor Yellow
        & $docker compose -f $composeFile logs --no-color --tail 80 api db 2>&1
    }
}
finally {
    if ($StopAfter -and $composeWasRequested) {
        Write-Host 'Deteniendo contenedores sin eliminar volumenes...'
        & $docker compose -f $composeFile down
    }
}

if ($failure) {
    throw $failure
}
