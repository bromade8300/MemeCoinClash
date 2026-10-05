param([switch]$Package)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
New-Item -ItemType Directory -Force -Path build/classes | Out-Null
$sources = @(Get-ChildItem -LiteralPath src/main/java -Recurse -Filter *.java | ForEach-Object FullName)
& javac --release 17 -encoding UTF-8 -d build/classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation Java impossible.' }
if ($Package) {
    $jarCommand = Get-Command jar -ErrorAction SilentlyContinue
    $jarPath = if ($jarCommand) { $jarCommand.Source } else { $null }
    if (-not $jarPath -and $env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME 'bin/jar.exe'
        if (Test-Path -LiteralPath $candidate) { $jarPath = $candidate }
    }
    if (-not $jarPath) {
        $installations = @(Get-ChildItem -LiteralPath "$env:ProgramFiles/Java" -Directory -Filter 'jdk*' -ErrorAction SilentlyContinue)
        foreach ($installation in $installations) {
            $candidate = Join-Path $installation.FullName 'bin/jar.exe'
            if (Test-Path -LiteralPath $candidate) { $jarPath = $candidate; break }
        }
    }
    if (-not $jarPath) { throw 'Outil jar introuvable : definir JAVA_HOME vers le JDK.' }
    New-Item -ItemType Directory -Force -Path output/app | Out-Null
    & $jarPath --create --file output/app/MemeCoinClash.jar --main-class memecoinclash.MemeCoinClash -C build/classes . -C . assets
    if ($LASTEXITCODE -ne 0) { throw 'Creation du JAR impossible.' }
}
