# scripts/06_update_element.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando el último elemento disponible..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

$AllElements = @()
foreach ($m in $Tree.modules) { foreach ($u in $m.units) { $AllElements += $u.elements } }

if ($AllElements.Count -eq 0) { Write-Host "No hay elementos para actualizar." -ForegroundColor Red; exit 1 }
$LastEl = $AllElements | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Elemento detectado: $($LastEl.title) (ID: $($LastEl.id))" -ForegroundColor Gray

Write-Host "`nActualizando Elemento (Operación Quirúrgica)..." -ForegroundColor Cyan
$Payload = @{
    resourceType = "TEXT" # Cambiamos de VIDEO a TEXT para probar
    title = "Elemento RENOMBRADO INDIVIDUALMENTE"
    summary = "Este cambio solo afecta a este recurso."
    orderIndex = $LastEl.orderIndex
} | ConvertTo-Json

try {
    $TargetUrl = "$BaseUrl/content/api/v1/elements/$($LastEl.id)"
    Write-Host "Enviando PUT a $TargetUrl..." -ForegroundColor Gray
    $UpdatedEl = Invoke-RestMethod -Uri $TargetUrl -Method Put -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Elemento actualizado." -ForegroundColor Green
    Show-Json $UpdatedEl
} catch {
    Write-Host "FAILED PUT: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
