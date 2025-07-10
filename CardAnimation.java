import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CardAnimation {

    /**
     * Animates a card moving from one position to another
     * 
     * @param card The card label to animate
     * @param startX Starting x position
     * @param startY Starting y position
     * @param endX Ending X position
     * @param endY Ending Y position
     * @param duration Duration of animation in milliseconds
     * @param container The container to add the animated card to
     * @param onComplete Runnable t o execute when animation completes
     */
    public static void animateCard(JLabel card, int startX, int startY, int endX, int endY,
                                    int duration, JLayeredPane container, Runnable onComplete) {
        

        // Add card to the container at the top layer
        container.add(card, JLayeredPane.DRAG_LAYER);
        card.setBounds(startX, startY, card.getPreferredSize().width, card.getPreferredSize().height);

        // Calculate the movement per step
        final int steps = 30; // 30 frames of animation
        final int delay = duration / steps;
        final float dx = (endX - startX) / (float)steps;
        final float dy = (endY - startY) / (float)steps;
        
        javax.swing.Timer timer = new javax.swing.Timer(delay, null);
        final int[] currentStep = {0};

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentStep[0]++;

                if (currentStep[0] <= steps) {
                    int newX = startX + (int)(dx * currentStep[0]);
                    int newY = startY + (int)(dy * currentStep[0]);
                    card.setBounds(newX, newY, card.getPreferredSize().width, card.getPreferredSize().height);
                } else {
                    timer.stop();
                    // Ensure card is properly removed and container is refreshed
                    container.remove(card);
                    container.revalidate();
                    container.repaint();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });

        timer.start();
    } 

    /**
     * Creates a hover effect for cards
     * 
     * @param card The card label to apply hover effect to
     */
    public static void applyHoverEffect(JLabel card) {
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Lift card up when hovered
                card.setLocation(card.getX(), card.getY() - 20);
                card.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 0), 2));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Return to original position
                card.setLocation(card.getX(), card.getY() + 20);
                card.setBorder(null);
            }
        });
    }

    /**
     * Create a "dealing" animation for a card
     * 
     * @param card The card to animate
     * @param deckX Deck X position
     * @param deckY Deck Y position
     * @param targetX Target X position
     * @param targetY Target Y position
     * @param duration Duration of animation in milliseconds
     * @param container The container to add the animated card to
     * @param onComplete Runnable to execute when animation completes
     */
    public static void dealCardAnimation(JLabel card, int deckX, int deckY, int targetX, int targetY,
                                        int duration, JLayeredPane container, Runnable onComplete) {
    
        // Add card to the container at the top layer
        container.add(card, JLayeredPane.DRAG_LAYER);
        card.setBounds(deckX, deckY, card.getPreferredSize().width, card.getPreferredSize().height);

        // Create arc animation
        final int steps = 30;
        final int delay = duration / steps;
        final float dx = (targetX - deckX) / (float)steps;
        final float dy = (targetY - deckY) / (float)steps;

        // Add arc effect
        final float arcHeight = 100; // Maximum height of the arc
        
        javax.swing.Timer timer = new javax.swing.Timer(delay, null);
        final int[] currentStep = {0};

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentStep[0]++;

                if (currentStep[0] <= steps) {
                    float progress = currentStep[0] / (float)steps;
                    int newX = deckX + (int)(dx * currentStep[0]);

                    // Arc calculation: sin curve for y movement
                    float arcOffset = (float)(Math.sin(Math.PI * progress) * arcHeight);
                    int newY = deckY + (int)(dy *currentStep[0]) - (int)arcOffset;

                    card.setBounds(newX, newY, card.getPreferredSize().width, card.getPreferredSize().height);
                } else {
                    timer.stop();
                    // Ensure card is removed from the container
                    container.remove(card);
                    container.revalidate();
                    container.repaint();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });

        timer.start();
    }
}