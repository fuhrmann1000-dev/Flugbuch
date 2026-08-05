Write-Host "=== Installing portable Node.js (no admin rights needed) ==="
$versions = Invoke-RestMethod -Uri "https://nodejs.org/dist/index.json"
$lts = $versions | Where-Object { $_.lts -ne $false } | Select-Object -First 1
$nodeVersion = $lts.version
Write-Host "Latest LTS: $nodeVersion"

$zipUrl = "https://nodejs.org/dist/$nodeVersion/node-$nodeVersion-win-x64.zip"
$zipPath = "$env:TEMP\node.zip"
Write-Host "Downloading $zipUrl ..."
Invoke-WebRequest -Uri $zipUrl -OutFile $zipPath

$installDir = "$env:LOCALAPPDATA\NodePortable"
if (Test-Path $installDir) { Remove-Item $installDir -Recurse -Force }
Write-Host "Extracting to $installDir ..."
Expand-Archive -Path $zipPath -DestinationPath $installDir -Force

$nodeFolder = Get-ChildItem $installDir | Where-Object { $_.PSIsContainer } | Select-Object -First 1
$nodePath = $nodeFolder.FullName
Write-Host "Node installed at $nodePath"

$currentUserPath = [Environment]::GetEnvironmentVariable('Path', 'User')
if ($currentUserPath -notlike "*$nodePath*") {
    [Environment]::SetEnvironmentVariable('Path', "$currentUserPath;$nodePath", 'User')
    Write-Host "Added to your User PATH (new terminals will have node/npm available)."
}

$env:Path = "$nodePath;$env:Path"

Write-Host ""
Write-Host "Node version:"
& "$nodePath\node.exe" -v
Write-Host "npm version:"
& "$nodePath\npm.cmd" -v

Write-Host ""
Write-Host "=== Installing frontend dependencies (npm install) ==="
Set-Location "C:\Users\reise\IdeaProjects\Flugbuch\frontend"
& "$nodePath\npm.cmd" install

Write-Host ""
Write-Host "=== Starting Angular dev server (npm start / ng serve) ==="
Write-Host "Once it says 'Local: http://localhost:4200/', open that in your browser."
& "$nodePath\npm.cmd" start
