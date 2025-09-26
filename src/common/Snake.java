package common;

import java.io.Serializable;
import java.awt.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// Clase representa una serpiente
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
        public int getDx(){
            return dx;
        }
        public int getDy(){
            return dy;
        }
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
        // Comienza serpiente con tamaño 3 hacia la izquierda
        // Cabeza en startPosition, cuerpo se exitende hacia la izquierda
        this.body.add(new Point(startPosition.x, startPosition.y));     // Cabeza
        this.body.add(new Point(startPosition.x - 1, startPosition.y)); // Cuerpo
        this.body.add(new Point(startPosition.x - 2, startPosition.y)); // Cuerpo        
        this.direction = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
        this.color = color;
        this.alive = true;
        this.score = 0;
    }
    
    // Movimiento hacia donde ve la serpiente
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
    
    // Crecimiento de la serpiente
    public void grow() {
        // aumentamos el score (valor por defecto)
        score += 10;
    }
    
    // Crecimiento de la serpiente con comida específica
    public void grow(Food food) {
        // Aumentamos el score según el tipo de comida
        score += food.getPoints();
        
        // Crecemos segmentos adicionales si es necesario
        for (int i = 1; i < food.getGrowth(); i++) {
            // Duplicamos el último segmento para hacer crecer la serpiente
            if (!body.isEmpty()) {
                Point lastSegment = body.get(body.size() - 1);
                body.add(new Point(lastSegment.x, lastSegment.y));
            }
        }
    }
    
    // Quita la cola de la serpiente
    public void removeTail() {
        if (body.size() > 1) {
            body.remove(body.size() - 1);
        }
    }
    
    // Verificación de movimiento
    private boolean isValidDirectionChange(Direction newDirection) {
        // No se puede ir en dirección contraria
        switch (direction) {
            case UP: return newDirection != Direction.DOWN;
            case DOWN: return newDirection != Direction.UP;
            case LEFT: return newDirection != Direction.RIGHT;
            case RIGHT: return newDirection != Direction.LEFT;
            default: return true;
        }
    }
    
    // Verificación si la serpiente choca consigo misma
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
    
    // Verificación si la serpiente chocha con otra
    public boolean checkCollisionWith(Snake other) {
        Point head = body.get(0);
        for (Point segment : other.body) {
            if (head.equals(segment)) {
                return true;
            }
        }
        return false;
    }
    
    public List<Point> getBody(){
        return body;
    }
    public Point getHead(){
        return body.isEmpty() ? null : body.get(0);
    }    
    public Direction getDirection(){
        return direction;
    }
    public void setDirection(Direction direction){
        this.nextDirection = direction;
    }
    public Color getColor(){
        return color;
    }
    public void setColor(Color color){
        this.color = color;
    }    
    public int getPlayerId(){
        return playerId;
    }    
    public boolean isAlive(){
        return alive;
    }
    public void setAlive(boolean alive){
        this.alive = alive;
    }    
    public int getScore(){
        return score;
    }
    public void setScore(int score){
        this.score = score;
    }
}