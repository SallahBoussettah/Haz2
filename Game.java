import java.util.*;

public class Game {
    private static Game instance; // Static reference to the current game in instance

    private Card.Suit forcedSuit = null;
    private Deck deck;
    private List<Player> players;
    private int currentPlayerIndex;
    private Card topCard;
    private boolean skipNext;
    private boolean isAIGame = false;
    private int accumulatedDrawCards = 0; // For  tracking accumulated cards from "2" cards
    private boolean mustDrawCards = false;
    private boolean lastCardWasOne = false; // Track if the last card played was a 1

    // Constructor for the regular game
    public Game(List<String> playerNames) {
        this(playerNames, false);
    }
    
        // Constructor with AI option // Multiplayer will be added laters
        public Game(List<String> playerNames, boolean withAI) {
            deck = new Deck();
            players = new ArrayList<>();
            isAIGame = withAI;

            // Create players
            for (int i = 0; i < playerNames.size(); i++) { // For more players later on
                String name = playerNames.get(i);
                if (withAI && i > 0) {
                    // Create AI Players for all except the first player
                    players.add(new AIPlayer("AI " + i));
                } else {
                    players.add(new Player(name));
                }
            }

            // Deal initial cards - 4 cards per player according to the rules of our game
            for (Player player : players) {
                for (int i = 0; i < 4; i++) {
                    player.drawCard(deck);
                }
            }

            topCard = deck.draw();
            currentPlayerIndex = 0;
            skipNext = false;
            accumulatedDrawCards = 0; 
            lastCardWasOne = false;

            // Set this as the current instance
            instance = this;
        }

        // Static method to get the current game instance
        public static Game getInstance() {
            return instance;
        }

        public Card getTopCard() {
            return topCard;
        }

        public void setTopCard(Card card) {
            this.topCard = card;
        }

        public List<Player> getPlayers() {
            return players;
        }

        public int getCurrentPlayerIndex() {
            return currentPlayerIndex;
        }

        public Player getCurrentPlayer() {
            return players.get(currentPlayerIndex);
        }

        public Deck getDeck() {
            return deck;
        }

        public boolean isCurrentPlayerAI() {
            return getCurrentPlayer() instanceof AIPlayer;
        }

        public boolean isAIGame() {
            return isAIGame;
        }

        public void playCard(Card card) {
            Player player = getCurrentPlayer();
            player.playCard(card);
            topCard = card;

            // Special card logic
            boolean shouldAdvanceTurn = true;

            switch (card.getValue()) {
                case 1:
                    // Card 1: Skip next player's turn, but they can play if they also have a card 1
                    Player nextPlayer = getNextPlayer();
                    
                    // Check if next player has a card 1 to play
                    if (!nextPlayerHasCardOne()) {
                        skipNext = true;
                    }
                    
                    // Track that we played a card 1
                    lastCardWasOne = true;
                    
                    // Reset forced suit when a non-7 card is played
                    forcedSuit = null;
                    break;
                case 2:
                    // Next player draws 2 cards (or more if they also play a 2)
                    accumulatedDrawCards += 2;
                    mustDrawCards = true;
                    // Reset forced suit when a non-7 card is played
                    forcedSuit = null;
                    // Reset lastCardWasOne flag
                    lastCardWasOne = false;
                    // Dont advance turn yet - next player needs to either play a 2 or draw cards
                    shouldAdvanceTurn = false;
                    advanceTurn(); // Move to next player who must respond to the card 2
                    return;
                case 7:
                    // Wild card - player can change suit
                    // The forceSuit will be set by the UI
                    // Do not reset forcedSuit here as it will be set by the UI
                    
                    // Reset lastCardWasOne flag
                    lastCardWasOne = false;
                    break;
                default:
                    // Regular cards
                    // Reset forced suit when a non-7 card is played
                    forcedSuit = null;
                    // Reset lastCardWasOne flag
                    lastCardWasOne = false;
                    break;
            }

            // Advance turn after playing any card (except card 2 which is handled above)
            if (shouldAdvanceTurn) {
                advanceTurn();
            }
        }

        /**
         * Handles the special effects of cards after they are played
         * This method is used when we need to apply card effects separately from playCard
         * @param card The card whose effects need to be applied
         */
        public void handleSpecialCardEffects(Card card) {
            // Special card logic
            boolean shouldAdvanceTurn = true;

            switch (card.getValue()) {
                case 1:
                    // Card 1: Skip next player's turn, but they can play if they also have a card 1
                    Player nextPlayer = getNextPlayer();
                    
                    // Check if next player has a card 1 to play
                    if (!nextPlayerHasCardOne()) {
                        skipNext = true;
                    }
                    
                    // Track that we played a card 1
                    lastCardWasOne = true;
                    
                    // Reset forced suit when a non-7 card is played
                    forcedSuit = null;
                    break;
                case 2:
                    // Next player draws 2 cards (or more if they also play a 2)
                    accumulatedDrawCards += 2;
                    mustDrawCards = true;
                    // Reset forced suit when a non-7 card is played
                    forcedSuit = null;
                    // Reset lastCardWasOne flag
                    lastCardWasOne = false;
                    // Dont advance turn yet - next player needs to either play a 2 or draw cards
                    shouldAdvanceTurn = false;
                    advanceTurn(); // Move to next player who must respond to the card 2
                    return;
                case 7:
                    // Wild card - player can change suit
                    // The forceSuit will be set by the UI
                    // Do not reset forcedSuit here as it will be set by the UI
                    
                    // Reset lastCardWasOne flag
                    lastCardWasOne = false;
                    break;
                default:
                    // Regular cards
                    // Reset forced suit when a non-7 card is played
                    forcedSuit = null;
                    // Reset lastCardWasOne flag
                    lastCardWasOne = false;
                    break;
            }

            // Advance turn after playing any card (except card 2 which is handled above)
            if (shouldAdvanceTurn) {
                advanceTurn();
            }
        }

        public Card getAIMove() {
            if (getCurrentPlayer() instanceof AIPlayer) {
                AIPlayer ai = (AIPlayer) getCurrentPlayer();
                
                // If last card was 1 and we have a card 1, prioritize playing it
                if (lastCardWasOne) {
                    for (Card card : ai.getHand()) {
                        if (card.getValue() == 1 && card.canBePlayedOn(topCard, forcedSuit)) {
                            return card;
                        }
                    }
                }
                
                return ai.selectCardToPlay(topCard, forcedSuit);
            }
            return null;
        }

        public Card.Suit getAISuitChoice() {
            if (getCurrentPlayer() instanceof AIPlayer) {
                AIPlayer ai = (AIPlayer) getCurrentPlayer();
                return ai.selectForcedSuit();
            }
            return null;
        }

        public void drawCardFromDeck() {
            Player current = getCurrentPlayer();

            // If there are accumulated draw cards from "2" cards
            if (accumulatedDrawCards > 0) {
                System.out.println("Player must draw " + accumulatedDrawCards + " cards");

                // Draw the accumulated number of cards
                for (int i = 0; i < accumulatedDrawCards; i++) {
                    Card drawCard = deck.draw();
                    if (drawCard != null) {
                        current.getHand().add(drawCard);
                        System.out.println("Drew card: " + drawCard);
                    }
                }

                // Reset the accumulated draw count and flag
                accumulatedDrawCards = 0;
                mustDrawCards = false;
                
                // Skip the current player's turn and return to the previous player
                // This is done by going back one player in the rotation
                currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
            } else {
                // Normal draw - Just one card
                current.drawCard(deck);
                
                // Reset forced suit when a player draws a card and ends their turn
                forcedSuit = null;
            }
        }

        public void advanceTurn() {
            if (skipNext) {
                currentPlayerIndex = (currentPlayerIndex + 2) % players.size();
                skipNext = false;
            } else {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }

        public Player getNextPlayer() {
            return players.get((currentPlayerIndex + 1) % players.size());
        }

        public Card.Suit getForcedSuit() {
            return forcedSuit;
        }

        public void setForcedSuit(Card.Suit suit) {
            this.forcedSuit = suit;
        }

        public int getAccumulatedDrawCards() {
            return accumulatedDrawCards;
        }

        public boolean mustDrawCards() {
            return mustDrawCards;
        }

        /**
         * Checks if the current player has a card 2 that can be played on the top card
         */
        public boolean hasCardTwo() {
            Player current = getCurrentPlayer();
            for (Card card : current.getHand()) {
                if (card.getValue() == 2 && card.canBePlayedOn(topCard, forcedSuit)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Checks if the next player has a card 1 that can be played on the top card
         */
        public boolean nextPlayerHasCardOne() {
            Player nextPlayer = getNextPlayer();
            for (Card card : nextPlayer.getHand()) {
                if (card.getValue() == 1 && card.canBePlayedOn(topCard, forcedSuit)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Checks if the current player has a card 1 that can be played on the top card
         */
        public boolean hasCardOne() {
            Player current = getCurrentPlayer();
            for (Card card : current.getHand()) {
                if (card.getValue() == 1 && card.canBePlayedOn(topCard, forcedSuit)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Checks if the last card played was a 1
         */
        public boolean lastCardWasOne() {
            return lastCardWasOne;
        }

        public boolean isGameOver() {
            for (Player player : players) {
                if (player.getHand().isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        public Player getWinner() {
            for (Player player : players) {
                if (player.getHand().isEmpty()) {
                    return player;
                }
            }
            return null;
        }
    
    // Additional methods for multiplayer support
    
    /**
     * Set the current player index (for network synchronization)
     */
    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }
    
    /**
     * Set whether cards must be drawn (for network synchronization)
     */
    public void setMustDrawCards(boolean mustDraw) {
        this.mustDrawCards = mustDraw;
    }
    
    /**
     * Set accumulated draw cards count (for network synchronization)
     */
    public void setAccumulatedDrawCards(int count) {
        this.accumulatedDrawCards = count;
    }
    
    /**
     * Set last card was one flag (for network synchronization)
     */
    public void setLastCardWasOne(boolean wasOne) {
        this.lastCardWasOne = wasOne;
    }
}
