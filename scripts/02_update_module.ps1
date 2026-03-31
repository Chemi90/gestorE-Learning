# scripts/02_update_module.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando el último módulo del último curso..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

if ($Tree.modules.Count -eq 0) { Write-Host "No hay módulos para actualizar." -ForegroundColor Red; exit 1 }
$LastMod = $Tree.modules | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Módulo detectado: $($LastMod.title) (ID: $($LastMod.id))" -ForegroundColor Gray

Write-Host "`nActualizando Módulo (Operación Quirúrgica)..." -ForegroundColor Cyan
$Payload = @{
    title = "Módulo RENOMBRADO INDIVIDUALMENTE"
    summary = "Este cambio solo ha afectado a este módulo. Los hijos siguen intactos."
    orderIndex = $LastMod.orderIndex
} | ConvertTo-Json

try {
    $TargetUrl = "$BaseUrl/content/api/v1/modules/$($LastMod.id)"
    Write-Host "Enviando PUT a $TargetUrl..." -ForegroundColor Gray
    $UpdatedMod = Invoke-RestMethod -Uri $TargetUrl -Method Put -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Módulo actualizado." -ForegroundColor Green
    Show-Json $UpdatedMod
} catch {
    Write-Host "FAILED PUT: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
