import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server that hosts multiplayer Hez games
 */
public class GameServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private String gameKey;
    private Game game;
    private Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private List<String> playerNames = new ArrayList<>();
    private boolean gameStarted = false;
    
    public GameServer() {
        generateGameKey();
    }
    
    /**
     * Generate a random 6-digit game key
     */
    private void generateGameKey() {
        Random random = new Random();
        gameKey = String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * Start the server
     */
    public void start(String hostPlayerName) throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;
        
        // Add host as first player
        playerNames.add(hostPlayerName);
        
        System.out.println("Game server started on port " + PORT);
        System.out.println("Game Key: " + gameKey);
        
        // Accept client connections
        while (isRunning && playerNames.size() < 2) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Close all client connections
            for (ClientHandler client : clients.values()) {
                client.close();
            }
            clients.clear();
            
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Add a client to the game
     */
    public synchronized boolean addClient(String playerName, ClientHandler clientHandler) {
        if (gameStarted || playerNames.size() >= 2) {
            return false;
        }
        
        // Check if name is already taken
        if (playerNames.contains(playerName)) {
            return false;
        }
        
        playerNames.add(playerName);
        clients.put(playerName, clientHandler);
        
        System.out.println("Player joined: " + playerName);
        
        // Notify all clients about the new player
        broadcastMessage(new NetworkMessage(NetworkMessage.MessageType.PLAYER_CONNECTED, playerName));
        
        // Start game if we have 2 players
        if (playerNames.size() == 2) {
            startGame();
        }
        
        return true;
    }
    
    /**
     * Remove a client from the game
     */
    public synchronized void removeClient(String playerName) {
        clients.remove(playerName);
        playerNames.remove(playerName);
        
        System.out.println("Player disconnected: " + playerName);
        
        // Notify remaining clients
        broadcastMessage(new NetworkMessage(NetworkMessage.MessageType.PLAYER_DISCONNECTED, playerName));
        
        // End game if a player disconnects during gameplay
        if (gameStarted && game != null) {
            broadcastMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_OVER, "Player disconnected"));
        }
    }
    
    /**
     * Start the multiplayer game
     */
    private void startGame() {
        gameStarted = true;
        game = new Game(playerNames, false); // No AI for multiplayer
        
        System.out.println("Starting game with players: " + playerNames);
        
        // Send game start message to all clients
        NetworkMessage startMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_START, playerNames);
        broadcastMessage(startMessage);
        
        // Send initial game state
        sendGameStateUpdate();
    }
    
    /**
     * Handle a player's move
     */
    public synchronized void handlePlayerMove(String playerName, NetworkMessage message) {
        if (!gameStarted || game == null) {
            return;
        }
        
        // Verify it's the player's turn
        if (!game.getCurrentPlayer().getName().equals(playerName)) {
            // Send invalid move message
            ClientHandler client = clients.get(playerName);
            if (client != null) {
                client.sendMessage(new NetworkMessage(NetworkMessage.MessageType.INVALID_MOVE, "Not your turn"));
            }
            return;
        }
        
        try {
            switch (message.getType()) {
                case PLAY_CARD:
                    handlePlayCard(playerName, message);
                    break;
                case DRAW_CARD:
                    handleDrawCard(playerName);
                    break;
                case CHOOSE_SUIT:
                    handleChooseSuit(playerName, message);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling player move: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle playing a card
     */
    private void handlePlayCard(String playerName, NetworkMessage message) {
        Card cardToPlay = (Card) message.getData();
        Player currentPlayer = game.getCurrentPlayer();
        
        // Find the actual card in player's hand
        Card actualCard = null;
        for (Card card : currentPlayer.getHand()) {
            if (card.getSuit() == cardToPlay.getSuit() && card.getValue() == cardToPlay.getValue()) {
                actualCard = card;
                break;
            }
        }
        
        if (actualCard == null || !actualCard.canBePlayedOn(game.getTopCard(), game.getForcedSuit())) {
            // Invalid move
            ClientHandler client = clients.get(playerName);
            if (client != null) {
                client.sendMessage(new NetworkMessage(NetworkMessage.MessageType.INVALID_MOVE, "Invalid card"));
            }
            return;
        }
        
        // Play the card
        game.playCard(actualCard);
        
        // Check if game is over
        if (game.isGameOver()) {
            Player winner = game.getWinner();
            broadcastMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_OVER, winner.getName()));
        } else {
            // Send game state update
            sendGameStateUpdate();
        }
    }
    
    /**
     * Handle drawing a card
     */
    private void handleDrawCard(String playerName) {
        game.drawCardFromDeck();
        sendGameStateUpdate();
    }
    
    /**
     * Handle choosing a suit (for wild cards)
     */
    private void handleChooseSuit(String playerName, NetworkMessage message) {
        Card.Suit chosenSuit = (Card.Suit) message.getData();
        game.setForcedSuit(chosenSuit);
        sendGameStateUpdate();
    }
    
    /**
     * Send game state update to all clients
     */
    private void sendGameStateUpdate() {
        if (game == null) return;
        
        GameStateData gameState = new GameStateData(
            game.getTopCard(),
            game.getForcedSuit(),
            game.getCurrentPlayerIndex(),
            game.getPlayers(),
            game.mustDrawCards(),
            game.getAccumulatedDrawCards(),
            game.lastCardWasOne()
        );
        
        NetworkMessage stateMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_STATE_UPDATE, gameState);
        broadcastMessage(stateMessage);
    }
    
    /**
     * Broadcast a message to all connected clients
     */
    public void broadcastMessage(NetworkMessage message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }
    
    /**
     * Send a message to a specific client
     */
    public void sendMessageToClient(String playerName, NetworkMessage message) {
        ClientHandler client = clients.get(playerName);
        if (client != null) {
            client.sendMessage(message);
        }
    }
    
    // Getters
    public String getGameKey() { return gameKey; }
    public boolean isGameStarted() { return gameStarted; }
    public List<String> getPlayerNames() { return new ArrayList<>(playerNames); }
    public Game getGame() { return game; }
    
    /**
     * Data class to hold game state information
     */
    public static class GameStateData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public final Card topCard;
        public final Card.Suit forcedSuit;
        public final int currentPlayerIndex;
        public final List<Player> players;
        public final boolean mustDrawCards;
        public final int accumulatedDrawCards;
        public final boolean lastCardWasOne;
        
        public GameStateData(Card topCard, Card.Suit forcedSuit, int currentPlayerIndex, 
                           List<Player> players, boolean mustDrawCards, int accumulatedDrawCards, 
                           boolean lastCardWasOne) {
            this.topCard = topCard;
            this.forcedSuit = forcedSuit;
            this.currentPlayerIndex = currentPlayerIndex;
            this.players = new ArrayList<>(players);
            this.mustDrawCards = mustDrawCards;
            this.accumulatedDrawCards = accumulatedDrawCards;
            this.lastCardWasOne = lastCardWasOne;
        }
    }
}