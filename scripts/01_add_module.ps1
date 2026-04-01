# scripts/01_add_module.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando el último curso disponible en $BaseUrl/content/api/v1/courses..." -ForegroundColor Cyan
try {
    $Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
} catch {
    Write-Host "FAILED GET COURSES: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Response Body: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
    exit 1
}

if ($Courses.Count -eq 0) { Write-Host "No hay cursos. Crea uno con test_bulk_native.ps1 primero." -ForegroundColor Red; exit 1 }

$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
Write-Host "CURSO SELECCIONADO:" -ForegroundColor Gray
$LastCourse | Select-Object id, title, createdAt | Format-Table | Out-String | Write-Host -ForegroundColor Gray

$TargetUrl = "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/modules"
Write-Host "`nAñadiendo Módulo Granular a: $TargetUrl" -ForegroundColor Cyan
$NextOrder = $Courses.Count + 100
$Payload = @{
    title = "Módulo Añadido Individualmente"
    summary = "Este módulo se ha creado mediante POST /courses/{id}/modules"
    orderIndex = $NextOrder
} | ConvertTo-Json

try {
    Write-Host "Enviando POST a $TargetUrl..." -ForegroundColor Gray
    $NewMod = Invoke-RestMethod -Uri $TargetUrl -Method Post -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Módulo creado con ID: $($NewMod.id)" -ForegroundColor Green
    Show-Json $NewMod
} catch {
    Write-Host "FAILED POST: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
