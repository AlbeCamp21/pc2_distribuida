# Snake Distribuido

Este proyecto implementa un juego Snake multijugador usando sockets en Java.

## Estructura del Proyecto

```
src/
├── common/           # Clases compartidas entre cliente y servidor
│   ├── Message.java  # Protocolo de comunicación
│   ├── GameState.java# Estado del juego
│   └── Snake.java    # Representación de una serpiente
├── server/           # Código del servidor
│   └── GameServer.java
└── client/           # Código del cliente
    └── SnakeClient.java
```

## Características

### Dificultades
- **Fácil**: Solo bordes como obstáculos, velocidad base
- **Medio**: Paredes adicionales + velocidad x1.25
- **Difícil**: Paredes adicionales + velocidad x1.5

### Funcionalidades
- Hasta 4 jugadores simultáneos
- Interfaz gráfica con campos para IP y puerto
- Controles con teclas de dirección (↑↓←→)
- Sistema de puntuación
- Sincronización en tiempo real

## Cómo ejecutar

### Compilar
```bash
# Desde el directorio raíz del proyecto
javac -d bin src/common/*.java src/server/*.java src/client/*.java
```

### Ejecutar Servidor
```bash
# Puerto por defecto (12345)
java -cp bin server.GameServer

# Puerto personalizado
java -cp bin server.GameServer 8080
```

### Ejecutar Cliente
```bash
java -cp bin client.SnakeClient
```

## Instrucciones de Juego

1. **Conectar**: Ingresar IP y puerto del servidor, hacer clic en "Conectar"
2. **Configurar**: Seleccionar dificultad (solo el primer jugador puede cambiarla)
3. **Iniciar**: Hacer clic en "Iniciar Juego"
4. **Controlar**: Usar las teclas de dirección para mover la serpiente
5. **Objetivo**: Comer la comida roja para crecer y obtener puntos
6. **Evitar**: Colisiones con bordes, paredes, otras serpientes y tu propio cuerpo

## Protocolo de Comunicación

### Mensajes Cliente → Servidor
- `CONNECT`: Solicitud de conexión
- `DISCONNECT`: Desconexión
- `MOVE_UP/DOWN/LEFT/RIGHT`: Movimientos
- `SET_DIFFICULTY`: Cambiar dificultad
- `START_GAME`: Iniciar juego

### Mensajes Servidor → Cliente
- `CONNECTION_ACCEPTED/REJECTED`: Respuesta de conexión
- `GAME_STATE`: Estado actual del juego
- `GAME_OVER`: Fin del juego
- `PLAYER_JOINED/LEFT`: Jugadores que se unen/salen
- `ERROR`: Mensajes de error

## Arquitectura

El proyecto usa un patrón cliente-servidor donde:
- El **servidor** mantiene el estado autoritativo del juego
- Los **clientes** envían comandos de movimiento
- El servidor procesa la lógica y envía actualizaciones a todos los clientes
- La comunicación se realiza mediante ObjectStreams sobre sockets TCP

## Requisitos

- Java 8 o superior
- Conexión de red (local o remota)