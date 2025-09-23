#!/bin/bash

# Script para compilar el proyecto Snake Distribuido

echo "Compilando proyecto Snake Distribuido..."

# Crear directorio bin si no existe
mkdir -p bin

# Compilar todas las clases
javac -d bin src/common/*.java src/server/*.java src/client/*.java

if [ $? -eq 0 ]; then
    echo "Compilación exitosa"
    echo ""
    echo "Para ejecutar:"
    echo "  Servidor: java -cp bin server.GameServer [puerto]"
    echo "  Cliente:  java -cp bin client.SnakeClient"
else
    echo "Error en la compilación"
    exit 1
fi