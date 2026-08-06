$ErrorActionPreference = "Continue"

Write-Host "=== Installing portable JDK 21 (Eclipse Temurin, ignoring any older Java already on this machine) ==="
$jdkDir = "$env:LOCALAPPDATA\JdkPortable"
$javaExe = $null

if (Test-Path $jdkDir) {
    $existing = Get-ChildItem $jdkDir -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
        Select-Object -First 1
    if ($existing) {
        $javaExe = Join-Path $existing.FullName "bin\java.exe"
        Write-Host "Portable JDK already installed at $javaExe"
    }
}

if (-not $javaExe) {
    New-Item -ItemType Directory -Force -Path $jdkDir | Out-Null
    $jdkUrl = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse"
    $zipPath = "$env:TEMP\jdk21.zip"
    Write-Host "Downloading $jdkUrl ..."
    Invoke-WebRequest -Uri $jdkUrl -OutFile $zipPath
    Write-Host "Extracting to $jdkDir ..."
    Expand-Archive -Path $zipPath -DestinationPath $jdkDir -Force
    $javaExe = (Get-ChildItem "$jdkDir\*\bin\java.exe")[0].FullName
    Write-Host "Installed portable JDK at $javaExe"
}

$javaHome = Split-Path (Split-Path $javaExe -Parent) -Parent
Write-Host "Using JAVA_HOME = $javaHome"
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Write-Host ""
Write-Host "Java version:"
& java -version

Write-Host ""
Write-Host "=== Running .\mvnw.cmd test (downloads Maven + all dependencies on first run, can take a while) ==="
Set-Location "C:\Users\reise\IdeaProjects\Flugbuch\backend"
$logPath = "C:\Users\reise\IdeaProjects\Flugbuch\backend\test-output.log"
& .\mvnw.cmd test *>&1 | Tee-Object -FilePath $logPath

Write-Host ""
Write-Host "=== DONE. Full output also saved to $logPath ==="
