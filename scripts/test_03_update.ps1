# scripts/test_03_update.ps1
$BaseUrl = "http://localhost:8080"
$OrgId = "550e8400-e29b-41d4-a716-446655440000"

# 1. Login
$LoginBody = @{ email = "admin@alpha.com"; password = "password123"; organizationId = $OrgId } | ConvertTo-Json
$AuthResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/api/v1/auth/login" -Method Post -Body $LoginBody -ContentType "application/json"
$Token = $AuthResponse.token
$Headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$Headers.Add("Authorization", "Bearer $Token")
$Headers.Add("Content-Type", "application/json")

if (-not (Test-Path "last_course_id.tmp")) { exit 1 }
$CourseId = Get-Content "last_course_id.tmp"

# --- TEST A: SMART BULK MERGE (RECONCILIATION) ---
Write-Host "Iniciando Smart Bulk Merge (Validando persistencia de UUIDs)..." -ForegroundColor Cyan
$OldTree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId/tree" -Method Get -Headers $Headers
$OldModuleId = $OldTree.modules[0].id

$UpdatePayload = @{
    title = "Curso IA (RECONCILIADO)"
    description = "Título y descripción cambiados, pero el Módulo 1 debería mantener su ID"
    level = "ADVANCED"
    version = 2
    organizationId = $OrgId
    modules = @(
        @{
            title = "Modulo 1: Fundamentos (RENOMBRADO)"
            summary = "Bases actualizadas"
            orderIndex = 0
            units = @(
                @{
                    title = "Unidad 1.1: El Origen (RENOMBRADA)"
                    orderIndex = 0
                    elements = @(
                        @{ resourceType = "TEXT"; title = "Texto Base (ACTUALIZADO)"; summary = "Nuevos conceptos"; orderIndex = 0 }
                    )
                    objectives = @( @{ description = "Entender el origen"; orderIndex = 0 } )
                }
            )
        }
    )
} | ConvertTo-Json -Depth 10

try {
    Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId" -Method Put -Body $UpdatePayload -Headers $Headers
    $NewTree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId/tree" -Method Get -Headers $Headers
    $NewModuleId = $NewTree.modules[0].id

    Write-Host "Old Module ID: $OldModuleId" -ForegroundColor Gray
    Write-Host "New Module ID: $NewModuleId" -ForegroundColor Gray

    if ($OldModuleId -eq $NewModuleId) {
        Write-Host "SUCCESS: Smart Merge funcionó. El UUID se ha conservado." -ForegroundColor Green
    } else {
        Write-Host "FAILED: El UUID ha cambiado. El Smart Merge ha fallado (borró y recreó)." -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR EN SMART MERGE: $_" -ForegroundColor Red
}

# --- TEST B: ATOMIC HYDRATION (PATCH BODY) ---
Write-Host "`nIniciando Hidratación Atómica de Elemento..." -ForegroundColor Cyan
$ElementId = $NewTree.modules[0].units[0].elements[0].id
$BodyPayload = @{ body = "### Contenido Hidratado desde PowerShell`nEste es el texto rico generado por el LLM." } | ConvertTo-Json

try {
    $ElResp = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/elements/$ElementId/body" -Method Patch -Body $BodyPayload -Headers $Headers
    Write-Host "SUCCESS: Elemento hidratado. Estado: $($ElResp.status)" -ForegroundColor Green
    
    # Guardar estado final actualizado
    $FinalTree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId/tree" -Method Get -Headers $Headers
    $FinalTree | ConvertTo-Json -Depth 10 | Out-File -FilePath "scripts/03_course_updated_native.json" -Encoding utf8
    Write-Host "EVIDENCIA ACTUALIZADA EN: scripts/03_course_updated_native.json" -ForegroundColor Green
} catch {
    Write-Host "ERROR EN HIDRATACION: $_" -ForegroundColor Red
}
