@echo off
REM Script para ejecutar el servidor Snake en Windows

set PORT=%1
if "%PORT%"=="" set PORT=12345

echo Iniciando servidor Snake en puerto %PORT%...
echo Presiona Ctrl+C para detener

java -cp bin server.GameServer %PORT%

pause