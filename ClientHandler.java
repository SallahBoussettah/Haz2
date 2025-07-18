import java.io.*;
import java.net.*;

/**
 * Handles communication with a single client connected to the game server
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private GameServer gameServer;
    private String playerName;
    private boolean isRunning = true;
    
    public ClientHandler(Socket clientSocket, GameServer gameServer) {
        this.clientSocket = clientSocket;
        this.gameServer = gameServer;
        
        try {
            // Create output stream first to avoid deadlock
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            
        } catch (IOException e) {
            System.err.println("Error creating client handler streams: " + e.getMessage());
            close();
        }
    }
    
    @Override
    public void run() {
        try {
            // Wait for initial join message
            NetworkMessage joinMessage = (NetworkMessage) inputStream.readObject();
            
            if (joinMessage.getType() == NetworkMessage.MessageType.JOIN_GAME) {
                playerName = joinMessage.getPlayerName();
                String gameKey = (String) joinMessage.getData();
                
                // Verify game key and add player
                if (gameKey.equals(gameServer.getGameKey())) {
                    boolean joined = gameServer.addClient(playerName, this);
                    
                    if (joined) {
                        // Send confirmation
                        sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_JOINED, "Welcome to the game!"));
                        
                        // Listen for messages from this client
                        while (isRunning) {
                            try {
                                NetworkMessage message = (NetworkMessage) inputStream.readObject();
                                
                                if (message != null) {
                                    System.out.println("Received from " + playerName + ": " + message);
                                    gameServer.handlePlayerMove(playerName, message);
                                }
                                
                            } catch (EOFException e) {
                                // Client disconnected
                                break;
                            } catch (SocketException e) {
                                // Socket closed
                                break;
                            }
                        }
                    } else {
                        // Game full or name taken
                        sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, "Cannot join game"));
                    }
                } else {
                    // Invalid game key
                    sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, "Invalid game key"));
                }
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    /**
     * Send a message to this client
     */
    public synchronized void sendMessage(NetworkMessage message) {
        if (outputStream != null && isRunning) {
            try {
                outputStream.writeObject(message);
                outputStream.flush();
                
            } catch (IOException e) {
                System.err.println("Error sending message to " + playerName + ": " + e.getMessage());
                close();
            }
        }
    }
    
    /**
     * Close the client connection
     */
    public void close() {
        isRunning = false;
        
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client handler: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup when client disconnects
     */
    private void cleanup() {
        if (playerName != null) {
            gameServer.removeClient(playerName);
        }
        close();
    }
    
    public String getPlayerName() {
        return playerName;
    }
}