import java.util.*;

public class AIPlayer extends Player {
    public AIPlayer(String name) {
        super(name); // method to add later
    }

    /**
     * AI Logic to select the best card to play
     * @param topCard the current top card
     * @param forcedSuit The currently forced suit (if any)
     * @param The card to play, or null if no playable card
     */
    public Card selectCardToPlay(Card topCard, Card.Suit forcedSuit) {
        List<Card> playableCards = new ArrayList<>();
        
        // Find all playable cards
        for (Card card : getHand()) {
            if (card.canBePlayedOn(topCard, forcedSuit)) {
                playableCards.add(card);
            }
        }

        if(playableCards.isEmpty()) {
            return null;
        }

        // Check if we need to counter a card 2 first
        Game gameInstance = Game.getInstance(); // Method for later as well
        if (gameInstance != null && gameInstance.mustDrawCards()) {
            // Look for a card 2 to counter
            for (Card card : playableCards) {
                if (card.getValue() == 2) {
                    return card;
                }
            }
            // If no card 2 found, we'ill have to draw cards
            return null;   
        }
        
        // Check if we need to counter a card 1
        if (gameInstance != null && gameInstance.lastCardWasOne() && topCard.getValue() == 1) {
            // Look for a card 1 to counter
            for (Card card : playableCards) {
                if (card.getValue() == 1) {
                    return card; // Prioritize playing a card 1 to counter
                }
            }
        }
        
        // Prioritize cards in this order:
        // 1. Special cards (1, 2, 7) if available
        // 2. Cards matching the top card's suit
        // 3. Cards matching the top card's number
        // 4. Any playable card

        // Check for special cards first (In order of priority)
        for (int specialValue : new int[]{1, 2, 7}) {
            for (Card card : playableCards) {
                if (card.getValue() == specialValue) {
                    return card;
                }
            }
        }

        // Check for cards matching the top card's suit
        for (Card card : playableCards) {
            if (card.getSuit() == topCard.getSuit()) {
                return card;
            }
        }

        // Check for cards matching the top card's value
        for (Card card : playableCards) {
            if (card.getValue() == topCard.getValue()) {
                return card;
            }
        }

        // If we get here, just plaay the first playable card
        return playableCards.get(0);
    }

    /**
     * AI Logic to select a suit when playing a wild card (7)
     * @return The suit to force
     */
    public Card.Suit selectForcedSuit() {
        // Count cards of each suit in hand
        Map<Card.Suit, Integer> suitCounts = new HashMap<>();
        for (Card.Suit suit : Card.Suit.values()) {
            suitCounts.put(suit, 0);
        }

        // Count the suits in hand
        for (Card card : getHand()) {
            Card.Suit suit = card.getSuit();
            suitCounts.put(suit, suitCounts.get(suit) + 1);
        }
        
        // Find the most common suit in hand
        Card.Suit mostCommonSuit = Card.Suit.STICKS; // Default
        int maxCount = -1;

        for (Map.Entry<Card.Suit, Integer> entry : suitCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonSuit = entry.getKey();
            }
        }

        // Never select a suit that isn't in the hand at all (unless hand is empty)
        if (maxCount == 0) {
            // If no cards in hand, return any suit
            return Card.Suit.values()[new Random().nextInt(Card.Suit.values().length)];
        }

        // Add weight to suits with special cards (1, 2)
        Map<Card.Suit, Integer> suitWeights = new HashMap<>();
        
        // Initialize with base counts
        for (Card.Suit suit : Card.Suit.values()) {
            // Start with the count of cards of this suit
            suitWeights.put(suit, suitCounts.get(suit) * 2); // Base weight is double the count
        }
        
        // Add extra weight for special cards
        for (Card card : getHand()) {
            if (card.getValue() == 1 || card.getValue() == 2) {
                // Add extra weight for special card
                Card.Suit suit = card.getSuit();
                suitWeights.put(suit, suitWeights.get(suit) + 3); // More weight for special cards
            }
        }

        // Find the suit with the highest weight
        Card.Suit bestSuit = mostCommonSuit;
        int bestWeight = -1;

        for (Map.Entry<Card.Suit, Integer> entry : suitWeights.entrySet()) {
            if (entry.getValue() > bestWeight) {
                bestWeight = entry.getValue();
                bestSuit = entry.getKey();
            }
        }

        // Always select a suit that we actually have cards for
        if (suitCounts.get(bestSuit) == 0) {
            // Fallback to the most common suit if the "best" suit has no cards
            return mostCommonSuit;
        }

        return bestSuit;
    }        
}