@echo off
REM Script para compilar el juego Snake

echo Compilando juego...

REM Creando directorio bin si no existe
if not exist bin mkdir bin

REM Compilando las clases
javac -d bin src/common/*.java src/server/*.java src/client/*.java
if %errorlevel% equ 0 (
    echo Compilacion exitosa
    echo.
    echo Para ejecutar:
    echo   Servidor: .\run-server.bat [puerto]
    echo   Cliente:  .\run-client.bat
) else (
    echo Error en la compilacion
    pause
    exit /b 1
)