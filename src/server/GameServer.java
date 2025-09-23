package server;

import common.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Servidor del juego Snake que maneja múltiples clientes
 */
public class GameServer {
    private static final int DEFAULT_PORT = 12345;
    private static final int MAX_PLAYERS = 4;
    private static final int BOARD_WIDTH = 40;
    private static final int BOARD_HEIGHT = 30;
    private static final int BASE_GAME_SPEED = 150; // ms entre actualizaciones
    
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private GameEngine gameEngine;
    private boolean running;
    private ExecutorService threadPool;
    
    public GameServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        threadPool = Executors.newCachedThreadPool();
        running = true;
        
        gameEngine = new GameEngine();
        
        System.out.println("Servidor Snake iniciado en puerto " + port);
        System.out.println("Esperando conexiones...");
    }
    
    public void start() {
        // Iniciar el motor del juego en un hilo separado
        threadPool.submit(gameEngine);
        
        // Aceptar conexiones de clientes
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                if (clients.size() < MAX_PLAYERS) {
                    ClientHandler client = new ClientHandler(clientSocket, clients.size() + 1);
                    clients.add(client);
                    threadPool.submit(client);
                    
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + 
                                     " (Jugador " + client.getPlayerId() + ")");
                    
                    // Notificar a todos los clientes sobre el nuevo jugador
                    broadcastMessage(new Message(Message.Type.PLAYER_JOINED, client.getPlayerId()));
                } else {
                    // Rechazar conexión si el servidor está lleno
                    try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                        out.writeObject(new Message(Message.Type.CONNECTION_REJECTED, "Servidor lleno"));
                    }
                    clientSocket.close();
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error aceptando conexión: " + e.getMessage());
                }
            }
        }
    }
    
    public void stop() {
        System.out.println("\nCerrando servidor...");
        running = false;
        
        try {
            // Desconectar todos los clientes
            for (ClientHandler client : new ArrayList<>(clients)) {
                client.cleanup();
            }
            clients.clear();
            
            // Cerrar el socket del servidor
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Puerto liberado correctamente.");
            }
            
            // Cerrar el pool de threads
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            
            System.out.println("Servidor cerrado exitosamente.");
            
        } catch (IOException e) {
            System.err.println("Error cerrando servidor: " + e.getMessage());
        }
    }
    
    private void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    private void removeClient(ClientHandler client) {
        clients.remove(client);
        gameEngine.removePlayer(client.getPlayerId());
        broadcastMessage(new Message(Message.Type.PLAYER_LEFT, client.getPlayerId()));
        System.out.println("Cliente desconectado: Jugador " + client.getPlayerId());
    }
    
    /**
     * Clase que maneja la comunicación con un cliente específico
     */
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private int playerId;
        
        public ClientHandler(Socket socket, int playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
            
            // Enviar confirmación de conexión
            sendMessage(new Message(Message.Type.CONNECTION_ACCEPTED, playerId));
        }
        
        @Override
        public void run() {
            try {
                Message message;
                while ((message = (Message) input.readObject()) != null) {
                    handleMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                // Cliente desconectado
            } finally {
                cleanup();
            }
        }
        
        private void handleMessage(Message message) {
            switch (message.getType()) {
                case MOVE_UP:
                    gameEngine.movePlayer(playerId, Snake.Direction.UP);
                    break;
                case MOVE_DOWN:
                    gameEngine.movePlayer(playerId, Snake.Direction.DOWN);
                    break;
                case MOVE_LEFT:
                    gameEngine.movePlayer(playerId, Snake.Direction.LEFT);
                    break;
                case MOVE_RIGHT:
                    gameEngine.movePlayer(playerId, Snake.Direction.RIGHT);
                    break;
                case SET_DIFFICULTY:
                    if (message.getData() instanceof GameState.Difficulty) {
                        gameEngine.setDifficulty((GameState.Difficulty) message.getData());
                    }
                    break;
                case START_GAME:
                    gameEngine.startGame();
                    break;
                case DISCONNECT:
                    cleanup();
                    break;
            }
        }
        
        public void sendMessage(Message message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                // Error enviando mensaje, cliente probablemente desconectado
                cleanup();
            }
        }
        
        private void cleanup() {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignorar errores al cerrar
            }
            removeClient(this);
        }
        
        public int getPlayerId() {
            return playerId;
        }
    }
    
    /**
     * Motor del juego que maneja la lógica y envía actualizaciones
     */
    private class GameEngine implements Runnable {
        private GameState gameState;
        private Random random;
        
        public GameEngine() {
            gameState = new GameState(BOARD_WIDTH, BOARD_HEIGHT);
            random = new Random();
            initializeBoard();
        }
        
        private void initializeBoard() {
            // Inicializar tablero vacío
            Point[][] board = gameState.getBoard();
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                for (int x = 0; x < BOARD_WIDTH; x++) {
                    board[y][x] = null;
                }
            }
        }
        
        @Override
        public void run() {
            while (running) {
                if (gameState.isGameRunning()) {
                    updateGame();
                    broadcastGameState();
                } else {
                    // Juego en pausa
                }
                
                try {
                    int sleepTime = (int)(BASE_GAME_SPEED / gameState.getDifficulty().getSpeedMultiplier());
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        
        private void updateGame() {
            Snake[] snakes = gameState.getSnakes();
            if (snakes == null) {
                return;
            }
            
            // Mover todas las serpientes
            for (Snake snake : snakes) {
                if (snake != null && snake.isAlive()) {
                    snake.move();
                    
                    // Verificar colisiones
                    if (checkCollisions(snake)) {
                        snake.setAlive(false);
                        continue;
                    }
                    
                    // Verificar si comió comida
                    if (snake.getHead().equals(gameState.getFood())) {
                        snake.grow();
                        generateFood();
                    } else {
                        snake.removeTail();
                    }
                }
            }
            
            // Verificar si el juego debe terminar
            int alivePlayers = 0;
            for (Snake snake : snakes) {
                if (snake != null && snake.isAlive()) {
                    alivePlayers++;
                }
            }
            
            // Solo terminar si NO hay jugadores vivos
            if (alivePlayers == 0) {
                gameState.setGameRunning(false);
                broadcastMessage(new Message(Message.Type.GAME_OVER, gameState));
            }
            // En modo multijugador, ganar cuando solo queda 1 vivo
            else if (alivePlayers == 1 && clients.size() > 1) {
                gameState.setGameRunning(false);
                broadcastMessage(new Message(Message.Type.GAME_OVER, gameState));
            }
        }
        
        private boolean checkCollisions(Snake snake) {
            Point head = snake.getHead();
            
            // Colisión con bordes
            if (head.x < 0 || head.x >= BOARD_WIDTH || 
                head.y < 0 || head.y >= BOARD_HEIGHT) {
                return true;
            }
            
            // Colisión con paredes (en dificultad media y difícil)
            if (gameState.getDifficulty() != GameState.Difficulty.EASY) {
                if (checkWallCollision(head)) {
                    return true;
                }
            }
            
            // Colisión consigo mismo
            if (snake.checkSelfCollision()) {
                return true;
            }
            
            // Colisión con otras serpientes
            Snake[] snakes = gameState.getSnakes();
            for (Snake other : snakes) {
                if (other != null && other != snake && other.isAlive()) {
                    if (snake.checkCollisionWith(other)) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        private boolean checkWallCollision(Point point) {
            // Paredes que salen SOLO de los bordes, sin formar cruces
            // Crear un laberinto simple con entradas y salidas
            
            // Pared que sale del borde SUPERIOR (hacia abajo)
            boolean topWall = (point.x == 15 && point.y >= 0 && point.y <= 8);
            
            // Pared que sale del borde INFERIOR (hacia arriba)  
            boolean bottomWall = (point.x == 25 && point.y >= BOARD_HEIGHT-9 && point.y < BOARD_HEIGHT);
            
            // Pared que sale del borde IZQUIERDO (hacia la derecha)
            boolean leftWall = (point.y == 12 && point.x >= 0 && point.x <= 10);
            
            // Pared que sale del borde DERECHO (hacia la izquierda)
            boolean rightWall = (point.y == 18 && point.x >= BOARD_WIDTH-11 && point.x < BOARD_WIDTH);
            
            // Pequeño obstáculo central aislado (sin tocar otras paredes)
            boolean centerBlock = (point.x >= BOARD_WIDTH/2-1 && point.x <= BOARD_WIDTH/2+1 && 
                                  point.y >= BOARD_HEIGHT/2-1 && point.y <= BOARD_HEIGHT/2+1);
            
            return topWall || bottomWall || leftWall || rightWall || centerBlock;
        }
        
        private void generateFood() {
            Point food;
            Snake[] snakes = gameState.getSnakes();
            
            do {
                food = new Point(random.nextInt(BOARD_WIDTH), random.nextInt(BOARD_HEIGHT));
            } while (isFoodOnSnake(food, snakes) || 
                    (gameState.getDifficulty() != GameState.Difficulty.EASY && checkWallCollision(food)));
            
            gameState.setFood(food);
        }
        
        private boolean isFoodOnSnake(Point food, Snake[] snakes) {
            if (snakes == null) return false;
            
            for (Snake snake : snakes) {
                if (snake != null) {
                    for (Point segment : snake.getBody()) {
                        if (segment.equals(food)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        private void broadcastGameState() {
            // CREAR UN NUEVO GAMESTATE PARA EVITAR PROBLEMAS DE REFERENCIA
            GameState freshGameState = new GameState(BOARD_WIDTH, BOARD_HEIGHT);
            freshGameState.setDifficulty(gameState.getDifficulty());
            freshGameState.setGameRunning(gameState.isGameRunning());
            freshGameState.setFood(new Point(gameState.getFood().x, gameState.getFood().y));
            freshGameState.setLastUpdateTime(System.currentTimeMillis());
            
            // CLONAR LAS SERPIENTES CON NUEVOS OBJETOS POINT
            Snake[] freshSnakes = new Snake[gameState.getSnakes().length];
            for (int i = 0; i < gameState.getSnakes().length; i++) {
                Snake originalSnake = gameState.getSnakes()[i];
                if (originalSnake != null) {
                    // Crear nueva serpiente con puntos clonados
                    Snake freshSnake = new Snake(originalSnake.getPlayerId(), 
                                               new Point(0, 0), // Posición temporal
                                               originalSnake.getColor());
                    
                    // Limpiar y agregar cada segmento como nuevo Point
                    freshSnake.getBody().clear();
                    for (Point segment : originalSnake.getBody()) {
                        freshSnake.getBody().add(new Point(segment.x, segment.y));
                    }
                    
                    freshSnake.setAlive(originalSnake.isAlive());
                    freshSnake.setScore(originalSnake.getScore());
                    freshSnakes[i] = freshSnake;
                }
            }
            freshGameState.setSnakes(freshSnakes);
            
            broadcastMessage(new Message(Message.Type.GAME_STATE, freshGameState));
        }
        
        public void movePlayer(int playerId, Snake.Direction direction) {
            Snake[] snakes = gameState.getSnakes();
            if (snakes != null && playerId > 0 && playerId <= snakes.length) {
                Snake snake = snakes[playerId - 1];
                if (snake != null && snake.isAlive()) {
                    snake.setDirection(direction);
                }
            }
        }
        
        public void removePlayer(int playerId) {
            Snake[] snakes = gameState.getSnakes();
            if (snakes != null && playerId > 0 && playerId <= snakes.length) {
                snakes[playerId - 1] = null;
            }
        }
        
        public void setDifficulty(GameState.Difficulty difficulty) {
            gameState.setDifficulty(difficulty);
        }
        
        public void startGame() {
            // Inicializar serpientes para los jugadores conectados
            Snake[] snakes = new Snake[MAX_PLAYERS];
            Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
            
            for (int i = 0; i < clients.size(); i++) {
                Point startPos = getStartPosition(i);
                snakes[i] = new Snake(i + 1, startPos, colors[i]);
            }
            
            gameState.setSnakes(snakes);
            generateFood();
            gameState.setGameRunning(true);
        }
        
        private Point getStartPosition(int playerIndex) {
            // Posiciones de inicio muy seguras, con espacio suficiente hacia la izquierda para 3 segmentos
            switch (playerIndex) {
                case 0: return new Point(10, 8);                          // Esquina superior izq (espacio suficiente)
                case 1: return new Point(BOARD_WIDTH - 8, 8);             // Esquina superior der 
                case 2: return new Point(10, BOARD_HEIGHT - 8);           // Esquina inferior izq
                case 3: return new Point(BOARD_WIDTH - 8, BOARD_HEIGHT - 8); // Esquina inferior der
                default: return new Point(BOARD_WIDTH/2, BOARD_HEIGHT/2);
            }
        }
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido, usando puerto por defecto: " + DEFAULT_PORT);
            }
        }
        
        GameServer server = null;
        try {
            server = new GameServer(port);
            
            // Agregar hook para shutdown graceful
            final GameServer finalServer = server;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                finalServer.stop();
            }));
            
            // Crear un hilo para escuchar comandos del usuario
            final GameServer serverRef = server;
            Thread commandThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                
                while (serverRef.running) {
                    try {
                        if (scanner.hasNextLine()) {
                            String command = scanner.nextLine().trim().toLowerCase();
                            if ("quit".equals(command) || "exit".equals(command)) {
                                serverRef.stop();
                                System.exit(0);
                                break;
                            }
                        }
                        Thread.sleep(100); // Pequeña pausa para no consumir CPU
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            commandThread.setDaemon(true);
            commandThread.start();
            
            server.start();
            
        } catch (IOException e) {
            System.err.println("Error iniciando servidor: " + e.getMessage());
            if (e.getMessage().contains("Address already in use")) {
                System.err.println("SOLUCION: El puerto " + port + " ya está en uso.");
                System.err.println("- Opcion 1: Usa otro puerto: java -cp bin server.GameServer " + (port + 1));
                System.err.println("- Opcion 2: Espera unos segundos y vuelve a intentar");
                System.err.println("- Opcion 3: Cierra otros programas que usen el puerto " + port);
            }
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }
}