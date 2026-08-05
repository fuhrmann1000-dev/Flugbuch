$javaHome = "C:\Users\reise\AppData\Local\JdkPortable\jdk-21.0.12+8"
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Set-Location "C:\Users\reise\IdeaProjects\Flugbuch\backend"
$logPath = "C:\Users\reise\IdeaProjects\Flugbuch\backend\test-output.log"
& .\mvnw.cmd test *>&1 | Tee-Object -FilePath $logPath

Write-Host ""
Write-Host "=== DONE. Full output also saved to $logPath ==="
