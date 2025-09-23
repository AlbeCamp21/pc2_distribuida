package common;

import java.io.Serializable;
import java.awt.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una serpiente en el juego
 */
public class Snake implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);
        
        private final int dx, dy;
        
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
        
        public int getDx() { return dx; }
        public int getDy() { return dy; }
    }
    
    private List<Point> body;
    private Direction direction;
    private Direction nextDirection;
    private Color color;
    private int playerId;
    private boolean alive;
    private int score;
    
    public Snake(int playerId, Point startPosition, Color color) {
        this.playerId = playerId;
        this.body = new ArrayList<>();
        
        // Inicializar serpiente con 3 segmentos en línea hacia la izquierda
        // Cabeza en startPosition, cuerpo extendiéndose hacia la izquierda
        this.body.add(new Point(startPosition.x, startPosition.y));     // Cabeza
        this.body.add(new Point(startPosition.x - 1, startPosition.y)); // Cuerpo 1
        this.body.add(new Point(startPosition.x - 2, startPosition.y)); // Cuerpo 2
        
        this.direction = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
        this.color = color;
        this.alive = true;
        this.score = 0;
    }
    
    /**
     * Mueve la serpiente en la dirección actual
     */
    public void move() {
        if (!alive) return;
        
        // Actualizar dirección si no es opuesta a la actual
        if (isValidDirectionChange(nextDirection)) {
            direction = nextDirection;
        }
        
        // Calcular nueva posición de la cabeza
        Point head = body.get(0);
        Point newHead = new Point(
            head.x + direction.getDx(),
            head.y + direction.getDy()
        );
        
        // Agregar nueva cabeza
        body.add(0, newHead);
    }
    
    /**
     * Hace crecer la serpiente (no quita la cola)
     */
    public void grow() {
        // La serpiente ya creció al mover, solo aumentamos el score
        score += 10;
    }
    
    /**
     * Quita la cola de la serpiente
     */
    public void removeTail() {
        if (body.size() > 1) {
            body.remove(body.size() - 1);
        }
    }
    
    /**
     * Verifica si el cambio de dirección es válido
     */
    private boolean isValidDirectionChange(Direction newDirection) {
        // No se puede ir en dirección opuesta
        switch (direction) {
            case UP: return newDirection != Direction.DOWN;
            case DOWN: return newDirection != Direction.UP;
            case LEFT: return newDirection != Direction.RIGHT;
            case RIGHT: return newDirection != Direction.LEFT;
            default: return true;
        }
    }
    
    /**
     * Verifica si la serpiente colisiona consigo misma
     */
    public boolean checkSelfCollision() {
        Point head = body.get(0);
        // Empezar desde el índice 1 para verificar colisión de cabeza con cuerpo
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica si la serpiente colisiona con otra serpiente
     */
    public boolean checkCollisionWith(Snake other) {
        Point head = body.get(0);
        for (Point segment : other.body) {
            if (head.equals(segment)) {
                return true;
            }
        }
        return false;
    }
    
    // Getters y setters
    public List<Point> getBody() { return body; }
    public Point getHead() { return body.isEmpty() ? null : body.get(0); }
    
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.nextDirection = direction; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public int getPlayerId() { return playerId; }
    
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}