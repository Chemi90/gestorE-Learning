# scripts/11_delete_module.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando el último módulo disponible para borrar..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

if ($Tree.modules.Count -eq 0) { Write-Host "No hay módulos para borrar." -ForegroundColor Red; exit 1 }
$TargetMod = $Tree.modules | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Borrando Módulo: $($TargetMod.title) (ID: $($TargetMod.id))" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/modules/$($TargetMod.id)" -Method Delete -Headers $Headers
    Write-Host "SUCCESS: Módulo eliminado físicamente (y sus hijos en cascada)." -ForegroundColor Green

    Write-Host "`nVerificando estado del Árbol del Curso (ID: $($LastCourse.id))..." -ForegroundColor Cyan
    $FinalTree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers
    Show-Json $FinalTree
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
