$ErrorActionPreference = 'Stop'
& "$PSScriptRoot/build.ps1"
& java -cp "$PSScriptRoot/build/classes" memecoinclash.MemeCoinClash
if ($LASTEXITCODE -ne 0) { throw 'Execution Java impossible.' }
