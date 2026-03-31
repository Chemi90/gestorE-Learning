# scripts/10_delete_unit.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando la última unidad disponible para borrar..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

$TargetUnit = $null
$ParentModuleId = $null

foreach ($m in $Tree.modules) {
    if ($m.units.Count -gt 0) {
        $TargetUnit = $m.units | Sort-Object createdAt -Descending | Select-Object -First 1
        $ParentModuleId = $m.id
    }
}

if (-not $TargetUnit) { Write-Host "No hay unidades para borrar." -ForegroundColor Red; exit 1 }

Write-Host "Borrando Unidad: $($TargetUnit.title) (ID: $($TargetUnit.id))" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/units/$($TargetUnit.id)" -Method Delete -Headers $Headers
    Write-Host "SUCCESS: Unidad eliminada físicamente (y sus elementos en cascada)." -ForegroundColor Green

    Write-Host "`nVerificando estado del Módulo Padre (ID: $ParentModuleId)..." -ForegroundColor Cyan
    $NewTree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers
    foreach ($m in $NewTree.modules) {
        if ($m.id -eq $ParentModuleId) { Show-Json $m }
    }
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
