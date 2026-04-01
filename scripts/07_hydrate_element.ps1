# scripts/07_hydrate_element.ps1
. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders

Write-Host "Buscando el último elemento para hidratar..." -ForegroundColor Cyan
$Courses = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses" -Method Get -Headers $Headers
$LastCourse = $Courses | Sort-Object createdAt -Descending | Select-Object -First 1
$Tree = Invoke-RestMethod -Uri "$BaseUrl/content/api/v1/courses/$($LastCourse.id)/tree" -Method Get -Headers $Headers

$AllElements = @()
foreach ($m in $Tree.modules) { foreach ($u in $m.units) { $AllElements += $u.elements } }

if ($AllElements.Count -eq 0) { Write-Host "No hay elementos para hidratar." -ForegroundColor Red; exit 1 }
$LastEl = $AllElements | Sort-Object createdAt -Descending | Select-Object -First 1

Write-Host "Elemento a hidratar: $($LastEl.title) (ID: $($LastEl.id))" -ForegroundColor Gray

Write-Host "`nInyectando contenido profundo (Hydration)..." -ForegroundColor Cyan
$Payload = @{
    body = "### Contenido Hidratado Atómicamente`nEste es el resultado final que los alumnos verán. Incluye **Markdown** y una estructura rica."
} | ConvertTo-Json

try {
    # El prefijo /content es vital para el Gateway
    $TargetUrl = "$BaseUrl/content/api/v1/elements/$($LastEl.id)/body"
    Write-Host "Enviando PATCH a $TargetUrl..." -ForegroundColor Gray
    $HydratedEl = Invoke-RestMethod -Uri $TargetUrl -Method Patch -Body $Payload -Headers $Headers -ContentType "application/json; charset=utf-8"
    Write-Host "SUCCESS: Elemento hidratado." -ForegroundColor Green
    Show-Json $HydratedEl
} catch {
    Write-Host "FAILED PATCH: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "ERROR DETAIL: $($Reader.ReadToEnd())" -ForegroundColor Yellow
    }
}
