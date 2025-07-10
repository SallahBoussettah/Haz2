import java.util.*;

public class Player {
    private String name;
    private List<Card> hand = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public void drawCard(Deck deck) {
        Card card = deck.draw();
        if (card != null) hand.add(card);
    }

    public List<Card> getHand() {
        return hand;
    }

    public String getName() {
        return name;
    }

    public void playCard(Card card) {
        hand.remove(card);
    }

    public boolean hasPlayableCard(Card topCard, Card.Suit forcedSuit) {
        for (Card card : hand) {
            if (card.canBePlayedOn(topCard, forcedSuit)) return true;
        }
        return false;
    }

}
