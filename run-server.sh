#!/bin/bash

# Script para el servidor
PORT=${1:-12345}

echo "Iniciando servidor Snake en puerto $PORT..."
echo "Presiona Ctrl+C para detener"
java -cp bin server.GameServer $PORT