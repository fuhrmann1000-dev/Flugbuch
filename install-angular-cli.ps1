$nodeDir = Get-ChildItem "$env:LOCALAPPDATA\NodePortable" | Where-Object { $_.PSIsContainer } | Select-Object -First 1
$nodePath = $nodeDir.FullName
$env:Path = "$nodePath;$env:Path"

Write-Host "Using Node at $nodePath"
Write-Host "Node version:"
& "$nodePath\node.exe" -v

Write-Host ""
Write-Host "=== Installing @angular/cli@21 globally ==="
& "$nodePath\npm.cmd" install -g "@angular/cli@21"

Write-Host ""
Write-Host "=== Verifying installation ==="
& "$nodePath\npx.cmd" ng version
