# scripts/05_add_element.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando la última unidad disponible..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

$AllUnits = @()
foreach ($m in $Tree.modules) { $AllUnits += $m.units }

if ($AllUnits.Count -eq 0) { Write-Host "No hay unidades. Crea una con 03_add_unit.ps1 primero." -ForegroundColor Red; exit 1 }
$LastUnit = $AllUnits | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Unidad padre: $($LastUnit.title) (ID: $($LastUnit.id))" -ForegroundColor Gray

Write-Host "`nAñadiendo Elemento Granular (VIDEO)..." -ForegroundColor Cyan
$Payload = @{
    resourceType = "VIDEO"
    title = "Video Tutorial de Integración"
    summary = "Un video sobre cómo funcionan los scripts de PowerShell."
    orderIndex = ($LastUnit.elements.Count + 1)
} | ConvertTo-Json

try {
    $TargetUrl = "$BaseUrl/content/api/v1/units/$($LastUnit.id)/elements"
    Write-Host "Enviando POST a $TargetUrl..." -ForegroundColor Gray
    $NewEl = Invoke-RestMethod -Uri $TargetUrl -Method Post -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Elemento creado con ID: $($NewEl.id)" -ForegroundColor Green
    Show-Json $NewEl
} catch {
    Write-Host "FAILED POST: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
