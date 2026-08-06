$javaHome = "C:\Users\reise\AppData\Local\JdkPortable\jdk-21.0.12+8"
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Set-Location "C:\Users\reise\IdeaProjects\Flugbuch\backend"
Write-Host "=== Starting the Spring Boot backend (mvnw spring-boot:run) ==="
Write-Host "Wait for a line saying 'Started FlugbuchApplication' - that means it's ready."
& .\mvnw.cmd spring-boot:run
