import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * Client that connects to a multiplayer Hez game
 */
public class GameClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private boolean isConnected = false;
    private String playerName;
    private Consumer<NetworkMessage> messageHandler;
    private Thread listenerThread;
    
    /**
     * Connect to a game server
     */
    public boolean connect(String gameKey, String playerName, Consumer<NetworkMessage> messageHandler) {
        this.playerName = playerName;
        this.messageHandler = messageHandler;
        
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            
            // Create streams
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            
            // Send join request
            NetworkMessage joinMessage = new NetworkMessage(NetworkMessage.MessageType.JOIN_GAME, gameKey, playerName);
            sendMessage(joinMessage);
            
            // Wait for response
            NetworkMessage response = (NetworkMessage) inputStream.readObject();
            
            if (response.getType() == NetworkMessage.MessageType.GAME_JOINED) {
                isConnected = true;
                
                // Start listening for messages
                startMessageListener();
                
                System.out.println("Successfully connected to game!");
                return true;
                
            } else if (response.getType() == NetworkMessage.MessageType.ERROR) {
                System.err.println("Failed to join game: " + response.getData());
                disconnect();
                return false;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            disconnect();
            return false;
        }
        
        return false;
    }
    
    /**
     * Start listening for messages from the server
     */
    private void startMessageListener() {
        listenerThread = new Thread(() -> {
            try {
                while (isConnected) {
                    try {
                        NetworkMessage message = (NetworkMessage) inputStream.readObject();
                        
                        if (message != null && messageHandler != null) {
                            // Handle message on EDT for UI updates
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                messageHandler.accept(message);
                            });
                        }
                        
                    } catch (EOFException e) {
                        // Server disconnected
                        break;
                    } catch (SocketException e) {
                        // Socket closed
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isConnected) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
            } finally {
                isConnected = false;
            }
        });
        
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * Send a message to the server
     */
    public synchronized boolean sendMessage(NetworkMessage message) {
        if (!isConnected || outputStream == null) {
            return false;
        }
        
        try {
            message.setPlayerName(playerName);
            outputStream.writeObject(message);
            outputStream.flush();
            return true;
            
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            disconnect();
            return false;
        }
    }
    
    /**
     * Send a card play action
     */
    public boolean playCard(Card card) {
        NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.PLAY_CARD, card);
        return sendMessage(message);
    }
    
    /**
     * Send a draw card action
     */
    public boolean drawCard() {
        NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.DRAW_CARD);
        return sendMessage(message);
    }
    
    /**
     * Send a suit choice (for wild cards)
     */
    public boolean chooseSuit(Card.Suit suit) {
        NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.CHOOSE_SUIT, suit);
        return sendMessage(message);
    }
    
    /**
     * Disconnect from the server
     */
    public void disconnect() {
        isConnected = false;
        
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }
            
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
        
        System.out.println("Disconnected from server");
    }
    
    // Getters
    public boolean isConnected() { return isConnected; }
    public String getPlayerName() { return playerName; }
}