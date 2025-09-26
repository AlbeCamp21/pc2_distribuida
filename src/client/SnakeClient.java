package client;

import common.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


// Cliente del juego con interfaz gráfica
public class SnakeClient extends JFrame {
    private static final int CELL_SIZE = 15;
    private static final int DEFAULT_BOARD_WIDTH = 40;
    private static final int DEFAULT_BOARD_HEIGHT = 30;
    
    // Componentes para interfaz
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton startGameButton;
    private JComboBox<GameState.Difficulty> difficultyCombo;
    private GamePanel gamePanel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    
    // Conexión con sockets
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean connected = false;
    private int playerId = -1;
    
    // Estado del juego
    private GameState gameState;
    
    public SnakeClient() {
        initializeGUI();
        setupKeyListeners();
    }
    
    private void initializeGUI() {
        setTitle("Snake Distribuido - Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel de conexión
        JPanel connectionPanel = createConnectionPanel();
        add(connectionPanel, BorderLayout.NORTH);
        
        // Panel del juego
        gamePanel = new GamePanel();
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });
        add(gamePanel, BorderLayout.CENTER);        
        // Panel de estado
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Conexión"));        
        panel.add(new JLabel("IP:"));
        ipField = new JTextField("localhost", 10);
        panel.add(ipField);        
        panel.add(new JLabel("Puerto:"));
        portField = new JTextField("12345", 6);
        panel.add(portField);        
        connectButton = new JButton("Conectar");
        connectButton.addActionListener(this::connectToServer);
        panel.add(connectButton);        
        disconnectButton = new JButton("Desconectar");
        disconnectButton.addActionListener(this::disconnectFromServer);
        disconnectButton.setEnabled(false);
        panel.add(disconnectButton);        
        panel.add(new JLabel("Dificultad:"));
        difficultyCombo = new JComboBox<>(GameState.Difficulty.values());
        difficultyCombo.addActionListener(this::changeDifficulty);
        difficultyCombo.setEnabled(false);
        panel.add(difficultyCombo);        
        startGameButton = new JButton("Iniciar Juego");
        startGameButton.addActionListener(this::startGame);
        startGameButton.setEnabled(false);
        panel.add(startGameButton);        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));        
        statusLabel = new JLabel("Desconectado");
        panel.add(statusLabel);        
        panel.add(new JLabel(" | "));        
        scoreLabel = new JLabel("Puntuación: 0");
        panel.add(scoreLabel);        
        panel.add(new JLabel(" | Controles: ↑↓←→"));        
        return panel;
    }
    
    private void setupKeyListeners() {
        KeyAdapter keyHandler = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!connected) {
                    return;
                }                
                Message message = null;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        message = new Message(Message.Type.MOVE_UP);
                        break;
                    case KeyEvent.VK_DOWN:
                        message = new Message(Message.Type.MOVE_DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                        message = new Message(Message.Type.MOVE_LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                        message = new Message(Message.Type.MOVE_RIGHT);
                        break;
                }                
                if (message != null) {
                    sendMessage(message);
                }
            }
        };
        
        // Añadir el KeyListener al JFrame y al GamePanel
        addKeyListener(keyHandler);
        gamePanel.addKeyListener(keyHandler);        
        setFocusable(true);
        gamePanel.setFocusable(true);
        requestFocus();
    }
    
    private void connectToServer(ActionEvent e) {
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();
        
        if (ip.isEmpty() || portText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese IP y puerto", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            socket = new Socket(ip, port);            
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());            
            // Iniciar hilo para recibir mensajes
            new Thread(this::receiveMessages).start();            
            // Enviar mensaje de conexión
            sendMessage(new Message(Message.Type.CONNECT));            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Puerto inválido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error conectando al servidor: " + ex.getMessage(), "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void disconnectFromServer(ActionEvent e) {
        if (connected) {
            sendMessage(new Message(Message.Type.DISCONNECT));
        }
        closeConnection();
    }
    
    private void changeDifficulty(ActionEvent e) {
        if (connected) {
            GameState.Difficulty difficulty = (GameState.Difficulty) difficultyCombo.getSelectedItem();
            sendMessage(new Message(Message.Type.SET_DIFFICULTY, difficulty));
        }
    }
    
    private void startGame(ActionEvent e) {
        if (connected) {
            sendMessage(new Message(Message.Type.START_GAME));
        }
    }
    
    private void sendMessage(Message message) {
        if (output != null) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                System.err.println("Error enviando mensaje: " + e.getMessage());
                closeConnection();
            }
        }
    }
    
    private void receiveMessages() {
        try {
            Message message;
            while ((message = (Message) input.readObject()) != null) {
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Conexión perdida con el servidor","Error", JOptionPane.ERROR_MESSAGE);
                    closeConnection();
                });
            }
        }
    }
    
    private void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case CONNECTION_ACCEPTED:
                    connected = true;
                    playerId = (Integer) message.getData();
                    statusLabel.setText("Conectado - Jugador " + playerId);
                    updateButtonStates();
                    // Asegurar el foco para los controles
                    requestFocusInWindow();
                    gamePanel.requestFocusInWindow();
                    break;
                    
                case CONNECTION_REJECTED:
                    JOptionPane.showMessageDialog(this, "Conexión rechazada: " + message.getData(), "Conexión Rechazada", JOptionPane.WARNING_MESSAGE);
                    closeConnection();
                    break;
                    
                case GAME_STATE:
                    gameState = (GameState) message.getData();
                    updateScore();
                    gamePanel.repaint();
                    break;
                    
                case GAME_OVER:
                    gameState = (GameState) message.getData();
                    gamePanel.repaint();
                    JOptionPane.showMessageDialog(this, "Juego Terminado", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
                    break;
                    
                case PLAYER_JOINED:
                    statusLabel.setText(statusLabel.getText() + " | Jugador " + message.getData() + " se unió");
                    break;
                    
                case PLAYER_LEFT:
                    statusLabel.setText(statusLabel.getText() + " | Jugador " + message.getData() + " salió");
                    break;
                    
                case ERROR:
                    JOptionPane.showMessageDialog(this, "Error del servidor: " + message.getData(), 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        });
    }
    
    private void updateScore() {
        if (gameState != null && gameState.getSnakes() != null && 
            playerId > 0 && playerId <= gameState.getSnakes().length) {
            Snake playerSnake = gameState.getSnakes()[playerId - 1];
            if (playerSnake != null) {
                scoreLabel.setText("Puntuación: " + playerSnake.getScore());
            }
        }
    }
    
    private void updateButtonStates() {
        connectButton.setEnabled(!connected);
        disconnectButton.setEnabled(connected);
        difficultyCombo.setEnabled(connected);
        startGameButton.setEnabled(connected);
        ipField.setEnabled(!connected);
        portField.setEnabled(!connected);
    }
    
    private void closeConnection() {
        connected = false;
        playerId = -1;        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ignorar errores al cerrar
        }        
        socket = null;
        input = null;
        output = null;
        gameState = null;
        
        statusLabel.setText("Desconectado");
        updateButtonStates();
        gamePanel.repaint();
    }
    
    // Panel que muestra el juego
    private class GamePanel extends JPanel {
        public GamePanel() {
            setPreferredSize(new Dimension(
                DEFAULT_BOARD_WIDTH * CELL_SIZE + 1, 
                DEFAULT_BOARD_HEIGHT * CELL_SIZE + 1
            ));
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.WHITE));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);            
            if (gameState == null) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g.getFontMetrics();
                String text = "Conectate al servidor para jugar";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = getHeight() / 2;
                g.drawString(text, x, y);
                return;
            }
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);            
            // Dibujar paredes si la dificultad no es "facil"
            if (gameState.getDifficulty() != GameState.Difficulty.EASY) {
                drawWalls(g2d);
            }            
            // Dibujar serpientes
            Snake[] snakes = gameState.getSnakes();
            if (snakes != null) {
                for (int i = 0; i < snakes.length; i++) {
                    Snake snake = snakes[i];
                    if (snake != null) {
                        drawSnake(g2d, snake);
                    }
                }
            }            
            // Dibujar todas las frutas
            java.util.List<Food> foods = gameState.getFoods();
            if (foods != null) {
                for (Food food : foods) {
                    if (food != null) {
                        drawFood(g2d, food);
                    }
                }
            }            
            // Dibujar grid
            drawGrid(g2d);
        }
        
        private void drawWalls(Graphics2D g) {
            g.setColor(Color.GRAY);
            int boardWidth = DEFAULT_BOARD_WIDTH;
            int boardHeight = DEFAULT_BOARD_HEIGHT;
            // Dibujar cada celda verificando si es una pared
            for (int x = 0; x < boardWidth; x++) {
                for (int y = 0; y < boardHeight; y++) {
                    if (isWallAtPosition(x, y, boardWidth, boardHeight)) {
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
        
        private boolean isWallAtPosition(int x, int y, int boardWidth, int boardHeight) {            
            // Pared que sale del borde arriba (hacia abajo)
            boolean topWall = (x == boardWidth/2 && y >= 0 && y <= 8);            
            // Pared que sale del borde abajo (hacia arriba)  
            boolean bottomWall = (x == boardWidth/2 && y >= boardHeight-9 && y < boardHeight);            
            // Pared que sale del borde izquierda (hacia la derecha)
            boolean leftWall = (y == boardHeight/2 && x >= 0 && x <= 10);            
            // Pared que sale del borde derecha (hacia la izquierda)
            boolean rightWall = (y == boardHeight/2 && x >= boardWidth-11 && x < boardWidth);            
            // Bloque central
            boolean centerBlock = (x >= boardWidth/2-1 && x <= boardWidth/2+1 && 
                                  y >= boardHeight/2-1 && y <= boardHeight/2+1);            
            return topWall || bottomWall || leftWall || rightWall || centerBlock;
        }
        
        private void drawSnake(Graphics2D g, Snake snake) {
            // Configurar color basado en el estado de la serpiente
            if (!snake.isAlive()) {
                g.setColor(Color.DARK_GRAY);
            } else {
                g.setColor(snake.getColor());
            }            
            // Configurar fuente para las letras
            Font font = new Font("Monospaced", Font.BOLD, CELL_SIZE - 2);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();            
            // Obtener la letra del cuerpo basada en el ID del jugador (A=1, B=2, C=3, D=4)
            char bodyLetter = (char)('A' + snake.getPlayerId() - 1);            
            java.util.List<Point> body = snake.getBody();
            for (int i = 0; i < body.size(); i++) {
                Point segment = body.get(i);                
                // Poner fondo para la letra
                g.fillRect(segment.x * CELL_SIZE + 1, segment.y * CELL_SIZE + 1, 
                         CELL_SIZE - 2, CELL_SIZE - 2);                
                // Elegir la letra
                char letter = (i == 0) ? 'O' : bodyLetter; // Cabeza = "o" mayuscula, Cuerpo = letra del jugador                
                // Configurar color del texto
                g.setColor(Color.WHITE);                
                // Calcular posición centrada para la letra
                int textWidth = fm.stringWidth(String.valueOf(letter));
                int textHeight = fm.getAscent();
                int x = segment.x * CELL_SIZE + (CELL_SIZE-textWidth)/2;
                int y = segment.y * CELL_SIZE + (CELL_SIZE+textHeight)/2 - fm.getDescent();                
                // Mostrar letra
                g.drawString(String.valueOf(letter), x, y);                
                // Restaurar color
                if (!snake.isAlive()) {
                    g.setColor(Color.DARK_GRAY);
                } else {
                    g.setColor(snake.getColor());
                }
            }
        }
        
        // Mostrar comida con puntaje en el mapa
        private void drawFood(Graphics2D g, Food food) {
            Point position = food.getPosition();
            // Dibujar fondo de la comida (rojo)
            g.setColor(Color.RED);
            g.fillOval(position.x * CELL_SIZE + 2, position.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
            // Dibujar borde
            g.setColor(new Color(128, 0, 0));
            g.drawOval(position.x * CELL_SIZE + 2, position.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);            
            // fuente
            Font font = new Font("Monospaced", Font.BOLD, CELL_SIZE - 6);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            String text = "+" + food.getPoints();            
            // Ccolor de texto
            g.setColor(Color.WHITE);            
            // Centrar texto
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int x = position.x * CELL_SIZE + (CELL_SIZE-textWidth)/2;
            int y = position.y * CELL_SIZE + (CELL_SIZE+textHeight)/2 - fm.getDescent();
            // Dibujar el texto
            g.drawString(text, x, y);
        }
        
        private void drawGrid(Graphics2D g) {
            g.setColor(Color.DARK_GRAY);            
            // Líneas verticales del mapa
            for (int x = 0; x <= DEFAULT_BOARD_WIDTH; x++) {
                g.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, DEFAULT_BOARD_HEIGHT * CELL_SIZE);
            }            
            // Líneas horizontales del mapa
            for (int y = 0; y <= DEFAULT_BOARD_HEIGHT; y++) {
                g.drawLine(0, y * CELL_SIZE, DEFAULT_BOARD_WIDTH * CELL_SIZE, y * CELL_SIZE);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SnakeClient().setVisible(true);
        });
    }
}