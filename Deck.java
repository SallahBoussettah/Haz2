import java.util.*;

/**
 * Represents a deck of Spanish cards for the Moroccan Hez card game.
 * The deck includes only cards with values 1-7 and 10-12 for each suit.
 * Cards 8 and 9 are not used in the traditional game.
 */
public class Deck {
    private List<Card> cards = new ArrayList<>();

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            // add cards 1-7
            for (int i = 1; i <= 7; i++) {
                cards.add(new Card(suit, i));
            }

            // add cards 10-12
            for (int i = 10; i <= 12; i++) {
                cards.add(new Card(suit, i));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) return null;
        return cards.remove(0);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }
}