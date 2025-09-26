package common;

import java.io.Serializable;

// Clase representación de mensajes entre cliente y servidor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type {
        // Mensajes de cliente al servidor
        CONNECT,
        DISCONNECT,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        SET_DIFFICULTY,
        START_GAME,        
        // Mensajes de servidor al cliente
        CONNECTION_ACCEPTED,
        CONNECTION_REJECTED,
        GAME_STATE,
        GAME_OVER,
        PLAYER_JOINED,
        PLAYER_LEFT,
        ERROR
    }
    
    private Type type;
    private Object data;
    private int playerId;
    
    public Message(Type type) {
        this.type = type;
    }
    
    public Message(Type type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    public Message(Type type, Object data, int playerId) {
        this.type = type;
        this.data = data;
        this.playerId = playerId;
    }
    
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }    
    public Object getData() {
        return data;
    }
    public void setData(Object data){
        this.data = data;
    }    
    public int getPlayerId(){
        return playerId;
    }
    public void setPlayerId(int playerId){
        this.playerId = playerId;
    }
}