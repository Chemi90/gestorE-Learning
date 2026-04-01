# scripts/test_04_delete.ps1
$BaseUrl = "http://localhost:8080"
$OrgId = "550e8400-e29b-41d4-a716-446655440000"

# 1. Login
$LoginBody = @{ email = "admin@alpha.com"; password = "password123"; organizationId = $OrgId } | ConvertTo-Json
$AuthResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/api/v1/auth/login" -Method Post -Body $LoginBody -ContentType "application/json"
$Token = $AuthResponse.token
$Headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$Headers.Add("Authorization", "Bearer $Token")

if (-not (Test-Path "last_course_id.tmp")) { exit 1 }
$CourseId = Get-Content "last_course_id.tmp"

# 2. Test Cascade Delete
Write-Host "Iniciando Borrado Físico en Cascada del curso $CourseId..." -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId" -Method Delete -Headers $Headers
    Write-Host "SUCCESS: Curso borrado físicamente." -ForegroundColor Green

    # 3. Verificación de inexistencia
    Write-Host "Verificando inexistencia del curso..." -ForegroundColor Cyan
    try {
        Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId" -Method Get -Headers $Headers
        Write-Host "FAILED: El curso aún existe en la base de datos." -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode -eq "NotFound") {
            Write-Host "VERIFIED: 404 Not Found. El curso y sus hijos han sido eliminados de la DB." -ForegroundColor Green
        } else {
            Write-Host "RESPUESTA INESPERADA: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "ERROR EN BORRADO: $_" -ForegroundColor Red
}

# Limpieza de archivos temporales
Remove-Item "last_course_id.tmp" -ErrorAction SilentlyContinue
Write-Host "`n--- PRUEBAS FINALIZADAS ---" -ForegroundColor Cyan
