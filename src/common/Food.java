package common;

import java.io.Serializable;
import java.awt.Point;

// Clase comida con diferentes puntajes
public class Food implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum FoodType {
        SMALL(10, 1),   // da 10 puntos y serpiente crece 1 cuadradito
        MEDIUM(20, 2),  // da 20 puntos y serpiente crece 2 cuadradito
        LARGE(30, 3);   // da 30 puntos y serpiente crece 3 cuadradito
        private final int points;
        private final int growth;        
        FoodType(int points, int growth) {
            this.points = points;
            this.growth = growth;
        }        
        public int getPoints() {
            return points;
        }        
        public int getGrowth() {
            return growth;
        }
    }
    
    private Point position;
    private FoodType type;
    
    public Food(Point position, FoodType type) {
        this.position = new Point(position.x, position.y);
        this.type = type;
    }    
    public Food(int x, int y, FoodType type) {
        this.position = new Point(x, y);
        this.type = type;
    }    
    public Point getPosition() {
        return position;
    }    
    public void setPosition(Point position) {
        this.position = new Point(position.x, position.y);
    }    
    public FoodType getType() {
        return type;
    }    
    public void setType(FoodType type) {
        this.type = type;
    }    
    public int getPoints() {
        return type.getPoints();
    }    
    public int getGrowth() {
        return type.getGrowth();
    }
    public int getX() {
        return position.x;
    }    
    public int getY() {
        return position.y;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;        
        if (obj instanceof Food) {
            Food other = (Food) obj;
            return position.equals(other.position);
        } else if (obj instanceof Point) {
            Point other = (Point) obj;
            return position.equals(other);
        }        
        return false;
    }
    
    @Override
    public int hashCode() {
        return position.hashCode();
    }
    
    @Override
    public String toString() {
        return "Food{" +
                "position=" + position +
                ", type=" + type +
                ", points=" + getPoints() +
                ", growth=" + getGrowth() +
                '}';
    }
}