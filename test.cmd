@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0test.ps1"
set "testExit=%ERRORLEVEL%"
pause
exit /b %testExit%
