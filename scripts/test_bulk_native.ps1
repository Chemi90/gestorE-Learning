# scripts/test_bulk_native.ps1
$BaseUrl = "http://localhost:8080"
$OrgId = "550e8400-e29b-41d4-a716-446655440000"

# 1. Login usando la ruta exacta de Postman
$LoginBody = @{
    email = "admin@alpha.com"
    password = "password123"
    organizationId = $OrgId
} | ConvertTo-Json

Write-Host "Iniciando Login en $BaseUrl/auth/api/v1/auth/login..." -ForegroundColor Cyan
try {
    $AuthResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/api/v1/auth/login" -Method Post -Body $LoginBody -ContentType "application/json"
    $Token = $AuthResponse.token
    
    if (-not $Token) {
        # Probar con accessToken si token viene vacio
        $Token = $AuthResponse.accessToken
    }

    $Headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
    $Headers.Add("Authorization", "Bearer $Token")
    $Headers.Add("Content-Type", "application/json")
    
    Write-Host "Login Exitoso. Token obtenido." -ForegroundColor Green
} catch {
    Write-Host "ERROR EN LOGIN: $_" -ForegroundColor Red
    exit 1
}

# 2. Bulk Create (Usando el body que funciona desde el Front)
Write-Host "Enviando cuerpo EXACTO del frontend a $BaseUrl/content/api/v1/courses/bulk..." -ForegroundColor Cyan
$BulkPayload = @'
{
    "title": "qqq",
    "description": "qsqsqs",
    "level": "BEGINNER",
    "version": 1,
    "organizationId": "550e8400-e29b-41d4-a716-446655440000",
    "modules": [
        {
            "title": "qsqsqs",
            "summary": "qsqssq",
            "orderIndex": 0,
            "units": [
                {
                    "title": "Unit Alpha",
                    "orderIndex": 0,
                    "elements": [
                        {
                            "resourceType": "TEXT",
                            "title": "Element Alpha",
                            "summary": "Mandatory Summary Only",
                            "orderIndex": 0
                        }
                    ],
                    "objectives": []
                }
            ]
        }
    ]
}
'@

try {
    $TargetUrl = "$BaseUrl/content/api/v1/courses/bulk"
    # Asegurar ContentType explicito y Body codificado en UTF8 plano
    $CourseResp = Invoke-RestMethod -Uri $TargetUrl -Method Post -Body $BulkPayload -Headers $Headers -ContentType "application/json; charset=utf-8"
    $CourseId = $CourseResp.courseId
    Write-Host "SUCCESS: Curso Creado con ID: $CourseId" -ForegroundColor Green
} catch {
    Write-Host "FAILED BULK: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR BODY: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
    exit 1
}


# 3. Final Fetch and Save
Write-Host "Consultando estado final de la Base de Datos..." -ForegroundColor Cyan
try {
    $FinalTree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$CourseId/tree" -Method Get -Headers $Headers
    $FinalTree | ConvertTo-Json -Depth 10 | Out-File -FilePath "scripts/01_course_created_native.json" -Encoding utf8
    Write-Host "EVIDENCIA GUARDADA EN: scripts/01_course_created_native.json" -ForegroundColor Green
} catch {
    Write-Host "ERROR AL CONSULTAR EL ARBOL: $_" -ForegroundColor Red
}
