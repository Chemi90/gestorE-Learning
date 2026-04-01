. "$PSScriptRoot/auth_helper.ps1"
$Headers = Get-AuthHeaders
$Token = $Headers["Authorization"]

$Payload = '{"title": "Modulo Test", "summary": "Resumen", "orderIndex": 0}'
$Uri = "http://localhost:8080/content/api/v1/courses/ffaff4df-ed35-492d-858e-8344e5857aa2/modules"

Write-Host "URL: $Uri"
Write-Host "Token: $Token"

curl.exe -v -X POST -H "Content-Type: application/json" -H "Authorization: $Token" -d $Payload $Uri
