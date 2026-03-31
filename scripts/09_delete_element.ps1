# scripts/09_delete_element.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando el último elemento disponible para borrar..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

$TargetElement = $null
$ParentUnitId = $null

foreach ($m in $Tree.modules) {
    foreach ($u in $m.units) {
        if ($u.elements.Count -gt 0) {
            $TargetElement = $u.elements | Sort-Object createdAt -Descending | Select-Object -First 1
            $ParentUnitId = $u.id
        }
    }
}

if (-not $TargetElement) { Write-Host "No hay elementos para borrar." -ForegroundColor Red; exit 1 }

Write-Host "Borrando Elemento: $($TargetElement.title) (ID: $($TargetElement.id))" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/elements/$($TargetElement.id)" -Method Delete -Headers $Headers
    Write-Host "SUCCESS: Elemento eliminado físicamente." -ForegroundColor Green

    Write-Host "`nVerificando estado de la Unidad Padre (ID: $ParentUnitId)..." -ForegroundColor Cyan
    # Buscamos la unidad en el nuevo árbol para ver que ya no tiene ese elemento
    $NewTree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers
    foreach ($m in $NewTree.modules) {
        foreach ($u in $m.units) {
            if ($u.id -eq $ParentUnitId) { Show-Json $u }
        }
    }
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
