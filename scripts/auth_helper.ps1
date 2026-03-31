# scripts/auth_helper.ps1
$BaseUrl = "http://localhost:8080"
$ContentDirectUrl = "http://localhost:8082" # Puerto directo del Content Service
$OrgId = "550e8400-e29b-41d4-a716-446655440000"

function Get-AuthHeaders {
    $LoginBody = @{
        email = "admin@alpha.com"
        password = "password123"
        organizationId = $OrgId
    } | ConvertTo-Json

    try {
        $AuthResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/api/v1/auth/login" -Method Post -Body $LoginBody -ContentType "application/json"
        $Token = $AuthResponse.token
        if (-not $Token) { $Token = $AuthResponse.accessToken }

        $Headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
        $Headers.Add("Authorization", "Bearer $Token")
        $Headers.Add("Content-Type", "application/json")
        return $Headers
    } catch {
        Write-Host "ERROR CRITICO EN AUTH: $_" -ForegroundColor Red
        exit 1
    }
}

function Show-Json($obj) {
    $obj | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Gray
}
