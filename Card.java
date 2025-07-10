import javax.swing.*;
import java.io.File;

public class Card {
    // Update enum names to match the rules image
    public enum Suit { STICKS, CUPS, SWORDS, GOLD }

    private Suit suit;
    private int value;

    public Card(Suit suit, int value) {
        this.suit = suit;
        this.value = value;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    /**
     * Determines if this card can be played on the top card
     * Based on the rules;
     * - Same suit
     * - Same number
     * - Card 7 can be played on any card (wild)
     * - If there's a forced suit, must match that suit
     */

    public boolean canBePlayedOn(Card topCard, Card.Suit forcedSuit) {
        if (forcedSuit != null) {
            System.out.println("Forced Suit: " + forcedSuit);
        }

        // Same number can always be played (highest priority rule)
        if (this.value == topCard.value) {
            System.out.println("Same value check passed");
            return true;
        }

        // If there's a forced suit from a 7, must match that suit
        if (forcedSuit != null) {
            boolean result = this.suit == forcedSuit;
            System.out.println("Forced suit check result: " + result);
            return result;
        }

        // Same suit as the top card can be played if there's no forced suit
        if (this.suit == topCard.suit) {
            System.out.println("Same suit check passed");
            return true;
        }

        // Wild card (7) can be played on anything
        if (this.value == 7) {
            System.out.println("Wild card check passed");
            return true;
        }

        System.out.println("No matching rule, card cannot be played");
        return false;

    }

    /**
     * Get the image path for this card with fallback options
     * @return Path to the card image
     */

    public String getImagePath() {
        String suitName = suit.name().toLowerCase();
        String primaryPath = "Hez/" + suitName + "/" + suitName + value + ".png";
        
        // Check if PNG Exists
        File pngFile = new File(primaryPath);
        if (pngFile.exists()) {
            return primaryPath;
        }

        // try SVG as fallback
        String svgPath = "Hez/" + suitName + "/" + suitName + value + ".svg";
        File svgFile = new File(svgPath);
        if(svgFile.exists()) {
            return svgPath;
        }

        // Rertunr the primary path anyway, the loadImage method will handle missing files
        return primaryPath;
    }

    @Override
    public String toString() {
        return suit + " " + value;
    }
}
