@echo off
setlocal

set GRADLE_HOME=D:\Downloads\gradle-8.14.5
set APP_HOME=%~dp0

cd /d "%APP_HOME%"
"%GRADLE_HOME%\bin\gradle.bat" %*

endlocal
