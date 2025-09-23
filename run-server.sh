#!/bin/bash

# Script para ejecutar el servidor Snake

PORT=${1:-12345}

echo "Iniciando servidor Snake en puerto $PORT..."
echo "Presiona Ctrl+C para detener"

java -cp bin server.GameServer $PORT