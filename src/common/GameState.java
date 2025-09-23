package common;

import java.io.Serializable;
import java.awt.Point;

/**
 * Clase que representa el estado del juego
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Difficulty {
        EASY(1.0f),      // Solo bordes, velocidad base
        MEDIUM(1.25f),   // Paredes + velocidad x1.25
        HARD(1.5f);      // Paredes + velocidad x1.5
        
        private final float speedMultiplier;
        
        Difficulty(float speedMultiplier) {
            this.speedMultiplier = speedMultiplier;
        }
        
        public float getSpeedMultiplier() {
            return speedMultiplier;
        }
    }
    
    private Difficulty difficulty;
    private Point[][] board;
    private Snake[] snakes;
    private Point food;
    private boolean gameRunning;
    private int boardWidth;
    private int boardHeight;
    private long lastUpdateTime;
    
    public GameState(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        this.board = new Point[height][width];
        this.gameRunning = false;
        this.difficulty = Difficulty.EASY;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    // Getters y setters
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    
    public Point[][] getBoard() { return board; }
    public void setBoard(Point[][] board) { this.board = board; }
    
    public Snake[] getSnakes() { return snakes; }
    public void setSnakes(Snake[] snakes) { this.snakes = snakes; }
    
    public Point getFood() { return food; }
    public void setFood(Point food) { this.food = food; }
    
    public boolean isGameRunning() { return gameRunning; }
    public void setGameRunning(boolean gameRunning) { this.gameRunning = gameRunning; }
    
    public int getBoardWidth() { return boardWidth; }
    public int getBoardHeight() { return boardHeight; }
    
    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
}