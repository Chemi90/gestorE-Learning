# scripts/08_add_objective.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando la última unidad para añadir objetivo..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

$AllUnits = @()
foreach ($m in $Tree.modules) { $AllUnits += $m.units }

if ($AllUnits.Count -eq 0) { Write-Host "No hay unidades para añadir objetivos." -ForegroundColor Red; exit 1 }
$LastUnit = $AllUnits | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Unidad padre: $($LastUnit.title) (ID: $($LastUnit.id))" -ForegroundColor Gray

Write-Host "`nAñadiendo Objetivo de Aprendizaje..." -ForegroundColor Cyan
$Payload = @{
    description = "Aprender a gestionar la granularidad del sistema."
    orderIndex = ($LastUnit.objectives.Count + 1)
} | ConvertTo-Json

try {
    $TargetUrl = "$BaseUrl/content/api/v1/units/$($LastUnit.id)/objectives"
    Write-Host "Enviando POST a $TargetUrl..." -ForegroundColor Gray
    $NewObj = Invoke-RestMethod -Uri $TargetUrl -Method Post -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Objetivo creado con ID: $($NewObj.id)" -ForegroundColor Green
    Show-Json $NewObj
} catch {
    Write-Host "FAILED POST: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
