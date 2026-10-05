$ErrorActionPreference = 'Stop'
& "$PSScriptRoot/build.ps1"
New-Item -ItemType Directory -Force -Path "$PSScriptRoot/build/test-classes" | Out-Null
$sources = @(Get-ChildItem -LiteralPath "$PSScriptRoot/src/test/java" -Recurse -Filter *.java | ForEach-Object FullName)
& javac --release 17 -encoding UTF-8 -cp "$PSScriptRoot/build/classes" -d "$PSScriptRoot/build/test-classes" $sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation des tests impossible.' }
& java '-Djava.awt.headless=true' -cp "$PSScriptRoot/build/classes;$PSScriptRoot/build/test-classes" memecoinclash.SimulationTest
if ($LASTEXITCODE -ne 0) { throw 'Tests en echec.' }
