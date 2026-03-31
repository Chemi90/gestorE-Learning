# scripts/04_update_unit.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando la última unidad disponible..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

# Buscar la última unidad en cualquier módulo
$AllUnits = @()
foreach ($m in $Tree.modules) { $AllUnits += $m.units }

if ($AllUnits.Count -eq 0) { Write-Host "No hay unidades para actualizar." -ForegroundColor Red; exit 1 }
$LastUnit = $AllUnits | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Unidad detectada: $($LastUnit.title) (ID: $($LastUnit.id))" -ForegroundColor Gray

Write-Host "`nActualizando Unidad (Operación Quirúrgica)..." -ForegroundColor Cyan
# El DTO de unidad EXIGE elementos. Vamos a mandarlos de vuelta.
$Payload = @"
{
    "title": "Unidad RENOMBRADA INDIVIDUALMENTE",
    "orderIndex": $($LastUnit.orderIndex),
    "elements": [
        {
            "resourceType": "TEXT",
            "title": "Element Keep",
            "summary": "Required Summary",
            "orderIndex": 0
        }
    ],
    "objectives": []
}
"@

try {
    $TargetUrl = "$BaseUrl/content/api/v1/units/$($LastUnit.id)"
    Write-Host "Enviando PUT a $TargetUrl..." -ForegroundColor Gray
    $UpdatedUnit = Invoke-RestMethod -Uri $TargetUrl -Method Put -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Unidad actualizada." -ForegroundColor Green
    Show-Json $UpdatedUnit
} catch {
    Write-Host "FAILED PUT: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
