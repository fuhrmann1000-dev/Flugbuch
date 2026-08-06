$nodeDir = Get-ChildItem "$env:LOCALAPPDATA\NodePortable" | Where-Object { $_.PSIsContainer } | Select-Object -First 1
$nodePath = $nodeDir.FullName
$env:Path = "$nodePath;$env:Path"

Set-Location "C:\Users\reise\IdeaProjects\Flugbuch\frontend"
$logPath = "C:\Users\reise\IdeaProjects\Flugbuch\frontend\build-output.log"

Write-Host "=== Running ng build (development) to type-check templates too ==="
& "$nodePath\npx.cmd" ng build --configuration development *>&1 | Tee-Object -FilePath $logPath

Write-Host ""
Write-Host "=== DONE. Full output also saved to $logPath ==="
