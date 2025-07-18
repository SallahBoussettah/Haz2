import java.io.Serializable;

/**
 * Represents messages sent between client and server for multiplayer gameplay
 */
public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        // Connection messages
        JOIN_GAME,
        GAME_JOINED,
        PLAYER_CONNECTED,
        PLAYER_DISCONNECTED,
        
        // Game state messages
        GAME_START,
        GAME_STATE_UPDATE,
        
        // Player actions
        PLAY_CARD,
        DRAW_CARD,
        CHOOSE_SUIT,
        
        // Game events
        TURN_CHANGE,
        GAME_OVER,
        
        // Error messages
        ERROR,
        INVALID_MOVE
    }
    
    private MessageType type;
    private Object data;
    private String playerName;
    private long timestamp;
    
    public NetworkMessage(MessageType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    public NetworkMessage(MessageType type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public NetworkMessage(MessageType type, Object data, String playerName) {
        this.type = type;
        this.data = data;
        this.playerName = playerName;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "NetworkMessage{type=" + type + ", data=" + data + ", player=" + playerName + "}";
    }
}