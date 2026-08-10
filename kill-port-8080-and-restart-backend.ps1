$connections = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($connections) {
    $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($processId in $pids) {
        Write-Host "Stopping process $processId that's holding port 8080..."
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Seconds 2
} else {
    Write-Host "Nothing found listening on port 8080."
}

$javaHome = "C:\Users\reise\AppData\Local\JdkPortable\jdk-21.0.12+8"
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Set-Location "C:\Users\reise\IdeaProjects\Flugbuch\backend"
Write-Host "=== Starting the Spring Boot backend (mvnw spring-boot:run) ==="
Write-Host "Wait for a line saying 'Started FlugbuchApplication' - that means it's ready."
& .\mvnw.cmd spring-boot:run
