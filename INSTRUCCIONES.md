# Instrucciones de Uso - Snake Distribuido

## Compilación (Terminal #1)

### Windows
```cmd
compile.bat
```

### Linux/Mac
```bash
./compile.sh
```

### Manual
```bash
javac -d bin src/common/*.java src/server/*.java src/client/*.java
```

## Ejecución

### 1. Ejecutar el Servidor (Terminal #1)

#### Windows
```cmd
run-server.bat [puerto]
```

#### Linux/Mac
```bash
./run-server.sh [puerto]
```

#### Manual
```bash
# Puerto por defecto (12345)
java -cp bin server.GameServer

# Puerto personalizado
java -cp bin server.GameServer 8080
```

### 2. Ejecutar Clientes (Terminal #2, etc)

#### Windows
```cmd
run-client.bat
```

#### Linux/Mac
```bash
./run-client.sh
```

#### Manual
```bash
java -cp bin client.SnakeClient
```

### 3. Ejecutar Múltiples Clientes de Prueba
```bash
java -cp bin test.TestClient
```

## Configuración del Juego

### Dificultades Disponibles

1. **EASY (Fácil)**
   - Solo bordes como obstáculos
   - Velocidad base (150ms entre actualizaciones)
   - Ideal para principiantes

2. **MEDIUM (Medio)**
   - Paredes adicionales en el tablero
   - Velocidad x1.25 (120ms entre actualizaciones)
   - Dificultad intermedia

3. **HARD (Difícil)**
   - Paredes adicionales en el tablero
   - Velocidad x1.5 (100ms entre actualizaciones)
   - Máxima dificultad

### Controles
- **↑** (Flecha Arriba): Mover hacia arriba
- **↓** (Flecha Abajo): Mover hacia abajo
- **←** (Flecha Izquierda): Mover hacia la izquierda
- **→** (Flecha Derecha): Mover hacia la derecha

## Flujo del Juego

1. **Conectar al Servidor**
   - Ingresar IP del servidor (localhost para pruebas locales)
   - Ingresar puerto (12345 por defecto)
   - Hacer clic en "Conectar"

2. **Configurar Partida**
   - El primer jugador puede seleccionar la dificultad
   - Esperar a que se conecten otros jugadores (opcional)
   - Hacer clic en "Iniciar Juego"

3. **Jugar**
   - Controlar la serpiente con las teclas de dirección
   - Comer la comida roja para crecer y obtener puntos
   - Evitar colisiones con:
     - Bordes del tablero
     - Paredes (en dificultad Media y Difícil)
     - Otras serpientes
     - Tu propio cuerpo

4. **Fin del Juego**
   - El juego termina cuando queda solo 1 jugador vivo
   - Se muestra el mensaje de "Juego Terminado"
   - Los jugadores pueden iniciar una nueva partida

## Características Técnicas

### Arquitectura
- **Cliente-Servidor** usando sockets TCP
- **Multithreading** para manejar múltiples clientes
- **Sincronización en tiempo real** del estado del juego
- **Serialización de objetos** para comunicación

### Capacidades
- Hasta **4 jugadores simultáneos**
- **Tablero de 40x30 celdas**
- **Tamaño de celda de 15 píxeles**
- **Frecuencia de actualización variable** según dificultad

### Colores de Jugadores
- Jugador 1: Rojo
- Jugador 2: Azul
- Jugador 3: Verde
- Jugador 4: Amarillo
