# scripts/test_02_read.ps1
$BaseUrl = "http://localhost:8080"
$OrgId = "550e8400-e29b-41d4-a716-446655440000"

# 1. Login
$LoginBody = @{ email = "admin@alpha.com"; password = "password123"; organizationId = $OrgId } | ConvertTo-Json
$AuthResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/api/v1/auth/login" -Method Post -Body $LoginBody -ContentType "application/json"
$Token = $AuthResponse.token
$Headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$Headers.Add("Authorization", "Bearer $Token")

# Obtener ID del curso creado en el test anterior
if (-not (Test-Path "last_course_id.tmp")) {
    Write-Host "ERROR: No hay ID de curso. Ejecuta test_bulk_native.ps1 primero." -ForegroundColor Red
    exit 1
}
$CourseId = Get-Content "last_course_id.tmp"

# 2. Test Get Metadata
Write-Host "Consultando Metadatos del curso $CourseId..." -ForegroundColor Cyan
try {
    $Meta = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId" -Method Get -Headers $Headers
    Write-Host "SUCCESS: Curso: $($Meta.title) (v$($Meta.version))" -ForegroundColor Green
} catch {
    Write-Host "ERROR EN METADATOS: $_" -ForegroundColor Red
}

# 3. Test Full Tree (Muñeca Rusa)
Write-Host "Consultando Árbol Completo (Matryoshka)..." -ForegroundColor Cyan
try {
    $Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId/tree" -Method Get -Headers $Headers
    Write-Host "SUCCESS: Árbol cargado. Módulos encontrados: $($Tree.modules.Count)" -ForegroundColor Green
    foreach ($mod in $Tree.modules) {
        Write-Host "  - Módulo: $($mod.title) (Unidades: $($mod.units.Count))" -ForegroundColor Gray
    }
} catch {
    Write-Host "ERROR EN ARBOL: $_" -ForegroundColor Red
}
