#!/bin/bash

# Script para compilar

echo "Compilando proyecto Snake Distribuido..."

# Crear directorio bin si no existe
mkdir -p bin

# Compilando todas las clases
javac -d bin src/common/*.java src/server/*.java src/client/*.java

if [ $? -eq 0 ]; then
    echo "Compilación exitosa"
    echo ""
    echo "Para ejecutar:"
    echo "  Servidor: ./run-server.sh [puerto]"
    echo "  Cliente:  ./run-client.sh"
else
    echo "Error en la compilación"
    exit 1
fi