package common;

import java.io.Serializable;
import java.awt.Point;


// Clase que representa estado de juego
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Difficulty {
        EASY(1.0f),      // Solo bordes, velocidad normal
        MEDIUM(1.25f),   // Paredes y velocidad x1.25
        HARD(1.5f);      // Paredes y velocidad x1.5
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
    private java.util.List<Food> foods;
    private boolean gameRunning;
    private int boardWidth;
    private int boardHeight;
    private long lastUpdateTime;
    
    public GameState(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        this.board = new Point[height][width];
        this.foods = new java.util.ArrayList<>();
        this.gameRunning = false;
        this.difficulty = Difficulty.EASY;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public Difficulty getDifficulty(){
        return difficulty;
    }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }    
    public Point[][] getBoard() {
        return board;
    }
    public void setBoard(Point[][] board) {
        this.board = board;
    }    
    public Snake[] getSnakes() {
        return snakes;
    }
    public void setSnakes(Snake[] snakes) {
        this.snakes = snakes;
    }    
    public java.util.List<Food> getFoods(){
        return foods;
    }
    public void setFoods(java.util.List<Food> foods){
        this.foods = foods;
    }
    
    // Métodos de compatibilidad para la primera comida (si existe)
    public Food getFood(){
        return foods.isEmpty() ? null : foods.get(0);
    }
    public void setFood(Food food){
        if (foods.isEmpty()) {
            foods.add(food);
        } else {
            foods.set(0, food);
        }
    }    
    public boolean isGameRunning() {
        return gameRunning;
    }
    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }    
    public int getBoardWidth() {
        return boardWidth;
    }
    public int getBoardHeight(){
        return boardHeight;
    }    
    public long getLastUpdateTime(){
        return lastUpdateTime;
    }
    public void setLastUpdateTime(long lastUpdateTime){
        this.lastUpdateTime = lastUpdateTime;
        }
}