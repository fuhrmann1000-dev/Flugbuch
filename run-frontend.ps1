$nodeDir = Get-ChildItem "$env:LOCALAPPDATA\NodePortable" | Where-Object { $_.PSIsContainer } | Select-Object -First 1
$nodePath = $nodeDir.FullName
$env:Path = "$nodePath;$env:Path"

Set-Location "C:\Users\reise\IdeaProjects\Flugbuch\frontend"
Write-Host "=== Starting the Angular dev server (npm start / ng serve) ==="
& "$nodePath\npm.cmd" start
