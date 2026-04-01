# scripts/03_add_unit.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando el último módulo disponible..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

if ($Tree.modules.Count -eq 0) { Write-Host "No hay módulos. Crea uno con 01_add_module.ps1 primero." -ForegroundColor Red; exit 1 }
$LastMod = $Tree.modules | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Módulo padre: $($LastMod.title) (ID: $($LastMod.id))" -ForegroundColor Gray

Write-Host "`nAñadiendo Unidad Granular..." -ForegroundColor Cyan
$NextOrder = $LastMod.units.Count + 1
# JSON estructurado EXACTAMENTE como piden los Records de Java
$Payload = @"
{
    "title": "Unit from Script",
    "orderIndex": $NextOrder,
    "elements": [
        {
            "resourceType": "TEXT",
            "title": "Initial Element",
            "summary": "Required Summary",
            "body": "Content here",
            "orderIndex": 0
        }
    ],
    "objectives": []
}
"@

try {
    # USANDO EL GATEWAY (8080) COMO CORRESPONDE A UNA ARQUITECTURA PROFESIONAL
    $TargetUrl = "$BaseUrl/content/api/v1/modules/$($LastMod.id)/units"
    Write-Host "Enviando POST a $TargetUrl..." -ForegroundColor Gray
    $NewUnit = Invoke-RestMethod -Uri $TargetUrl -Method Post -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Unidad creada con ID: $($NewUnit.id)" -ForegroundColor Green
    Show-Json $NewUnit
} catch {
    Write-Host "FAILED POST: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
