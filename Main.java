import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import java.awt.image.BufferedImage;

public class Main {
    private static Game game;
    private static JLayeredPane gamePane;
    private static JLabel topCardLabel;
    private static JLabel suitTypeLabel;
    private static JLabel deckLabel;
    private static JFrame frame;
    private static JLabel statusLabel;
    private static ArrayList<String> playerNames = new ArrayList<>();
    private static JPanel playerPanel;
    private static JPanel opponentPanel;
    private static boolean isAnimating = false;
    private static javax.swing.Timer aiTimer;
    private static boolean hasShownCard2Rule = false;
    
    // Constants for card dimensions and positions
    private static final int CARD_WIDTH = 100;
    private static final int CARD_HEIGHT = 150;
    private static final int CENTER_X = 450;
    private static final int CENTER_Y = 300;
    private static final Color TABLE_COLOR = new Color(0, 100, 0); // Dark green

    // Image cache to improve performance
    private static Map<String, ImageIcon> imageCache = new HashMap<>();

    public static void main(String[] args) {
        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            showStartMenu();
        });
    }
    
    /**
     * Load an image with error handling and caching
     * @param path Path to the image
     * @param width Target width (or -1 for original size)
     * @param height Target height (or -1 for original size)
     * @return ImageIcon of the loaded and possibly scaled image
     */
    private static ImageIcon loadImage(String path, int width, int height) {
        // Check cache first
        String cacheKey = path + "_" + width + "_" + height;
        if (imageCache.containsKey(cacheKey)) {
            return imageCache.get(cacheKey);
        }
        
        // Load the image
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            System.err.println("Image file not found: " + path);
            // Return a colored rectangle as placeholder
            return createPlaceholderImage(width, height);
        }
        
        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            System.err.println("Failed to load image: " + path);
            return createPlaceholderImage(width, height);
        }
        
        // Scale if needed
        if (width > 0 && height > 0) {
            Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage);
        }
        
        // Cache the result
        imageCache.put(cacheKey, icon);
        return icon;
    }
    
    /**
     * Creates a placeholder image when actual image can't be loaded
     */
    private static ImageIcon createPlaceholderImage(int width, int height) {
        int w = width > 0 ? width : 100;
        int h = height > 0 ? height : 150;
        
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        // Fill with gray background
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, w, h);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, w-1, h-1);
        
        // Draw X
        g2d.drawLine(0, 0, w-1, h-1);
        g2d.drawLine(0, h-1, w-1, 0);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    private static void showStartMenu() {
        JFrame menuFrame = new JFrame("Hez - Moroccan Card Game");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(600, 500);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setMinimumSize(new Dimension(500, 400));
        menuFrame.setResizable(true);
        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Create a gradient background
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 80, 0), 
                                                         getWidth(), getHeight(), new Color(0, 40, 0));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Title
        JLabel titleLabel = new JLabel("Hez - Moroccan Card Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        // Game mode selection
        JPanel gameModePanel = new JPanel();
        gameModePanel.setOpaque(false);
        gameModePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel modeLabel = new JLabel("Game Mode:");
        modeLabel.setForeground(Color.WHITE);
        gameModePanel.add(modeLabel);
        
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton aiButton = new JRadioButton("Play vs AI");
        aiButton.setSelected(true);
        aiButton.setForeground(Color.WHITE);
        aiButton.setOpaque(false);
        
        JRadioButton onlineButton = new JRadioButton("Online Multiplayer");
        onlineButton.setForeground(Color.WHITE);
        onlineButton.setOpaque(false);
        onlineButton.setEnabled(false); // Disabled for now
        
        modeGroup.add(aiButton);
        modeGroup.add(onlineButton);
        gameModePanel.add(aiButton);
        gameModePanel.add(onlineButton);
        
        mainPanel.add(gameModePanel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Player name panel
        JPanel playerNamePanel = new JPanel();
        playerNamePanel.setOpaque(false);
        playerNamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel("Your Name: ");
        nameLabel.setForeground(Color.WHITE);
        playerNamePanel.add(nameLabel);
        
        JTextField playerNameField = new JTextField("Player 1", 15);
        playerNamePanel.add(playerNameField);
        
        mainPanel.add(playerNamePanel);
        
        // Online multiplayer status (coming soon)
        JLabel onlineStatusLabel = new JLabel("Online Multiplayer: Under Development");
        onlineStatusLabel.setForeground(new Color(255, 215, 0)); // Gold color
        onlineStatusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        onlineStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(onlineStatusLabel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Start button
        JButton startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setBackground(new Color(255, 215, 0));
        startButton.setForeground(new Color(139, 69, 19));
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setFocusPainted(false);
        
        startButton.addActionListener(e -> {
            playerNames.clear();
            
            if (onlineButton.isSelected()) {
                JOptionPane.showMessageDialog(menuFrame, 
                    "Online multiplayer is currently under development.\nPlease check back later!", 
                    "Coming Soon", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // AI game mode - just one human player
            String playerName = playerNameField.getText().trim();
            if (playerName.isEmpty()) {
                playerName = "Player 1";
            }
            playerNames.add(playerName);
            playerNames.add("AI"); // Placeholder, will be replaced with AIPlayer
            
            menuFrame.dispose();
            startGame(true);
        });
        
        mainPanel.add(startButton);
        
        menuFrame.add(mainPanel);
        menuFrame.setVisible(true);
    }
    
    private static void startGame(boolean withAI) {
        // Always create a game with AI
        game = new Game(playerNames, true);
        createAndShowGUI();
        
        // Start AI turn if AI goes first (shouldn't happen with current setup)
        if (game.isCurrentPlayerAI()) {
            performAITurn();
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Hez - Moroccan Card Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(800, 600)); // Set minimum size
        frame.setResizable(true); // Make window resizable

        // Create a layered pane for the game area
        gamePane = new JLayeredPane() {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, width, height);
                // Reposition elements when window is resized
                repositionUIElements(width, height);
            }
        };
        gamePane.setPreferredSize(new Dimension(1000, 700));
        
        // Create a background panel with green color
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Create a gradient background
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, TABLE_COLOR, 
                                                         getWidth(), getHeight(), new Color(0, 50, 0));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw some decorative elements
                g2d.setColor(new Color(0, 80, 0));
                
                // Calculate center based on current size
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                g2d.drawRoundRect(centerX - 150, centerY - 200, 300, 400, 20, 20);
            }
        };
        
        // Fill the entire layered pane with the background
        backgroundPanel.setBounds(0, 0, 1000, 700);
        gamePane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        // Top card display in the center
        ImageIcon topCardIcon = loadImage(game.getTopCard().getImagePath(), CARD_WIDTH, CARD_HEIGHT);
        topCardLabel = new JLabel(topCardIcon);
        topCardLabel.setBounds(CENTER_X - CARD_WIDTH/2, CENTER_Y - CARD_HEIGHT/2, CARD_WIDTH, CARD_HEIGHT);
        gamePane.add(topCardLabel, JLayeredPane.PALETTE_LAYER);
        
        // Suit type display on the left
        String suitPath = "Hez/type/" + game.getTopCard().getSuit().name().toLowerCase() + ".png";
        ImageIcon suitIcon = loadImage(suitPath, 60, 60);
        suitTypeLabel = new JLabel(suitIcon);
        suitTypeLabel.setBounds(CENTER_X - 200, CENTER_Y - 30, 60, 60);
        gamePane.add(suitTypeLabel, JLayeredPane.PALETTE_LAYER);

        // Deck (empty.png) on the right
        ImageIcon deckIcon = loadImage("Hez/empty.png", CARD_WIDTH, CARD_HEIGHT);
        deckLabel = new JLabel(deckIcon);
        deckLabel.setBounds(CENTER_X + 100, CENTER_Y - CARD_HEIGHT/2, CARD_WIDTH, CARD_HEIGHT);
        deckLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover effect to deck
        deckLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                deckLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                deckLabel.setBorder(null);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isAnimating) return;
                
                Player current = game.getCurrentPlayer();
                boolean isAI = current instanceof AIPlayer;
                
                // Don't allow clicking the deck if it's AI's turn
                if (isAI) {
                    return;
                }
                
                // Create animation for drawing a card
                isAnimating = true;
                
                // Create a temporary card for animation
                JLabel tempCard = new JLabel(loadImage("Hez/empty.png", CARD_WIDTH, CARD_HEIGHT));
                tempCard.setSize(CARD_WIDTH, CARD_HEIGHT);
                
                // Always animate to the human player's hand at the bottom
                int targetX = gamePane.getWidth() / 2; // Center of the screen horizontally
                int targetY = gamePane.getHeight() - 150; // Near the bottom of the screen
                
                CardAnimation.dealCardAnimation(
                    tempCard,
                    deckLabel.getX(), deckLabel.getY(),
                    targetX, targetY,
                    300, // Duration in milliseconds
                    gamePane,
                    () -> {
                        // Remove the temporary card from the container
                        gamePane.remove(tempCard);
                        gamePane.repaint();
                        
                        // Store current player index before drawing cards
                        int previousPlayerIndex = game.getCurrentPlayerIndex();
                        
                        // If there are accumulated cards to draw, use drawCardFromDeck method
                        if (game.mustDrawCards() && game.getAccumulatedDrawCards() > 0) {
                            game.drawCardFromDeck();
                            // Note: drawCardFromDeck will now return turn to the previous player
                        } else {
                            // Regular draw - just one card
                            current.drawCard(game.getDeck());
                            game.advanceTurn();
                        }
                        
                        updateUI();
                        checkGameOver();
                        isAnimating = false;
                        
                        // If next player is AI, perform AI turn
                        if (!game.isGameOver() && game.isCurrentPlayerAI()) {
                            performAITurn();
                        }
                    }
                );
            }
        });
        gamePane.add(deckLabel, JLayeredPane.PALETTE_LAYER);
        
        // Status label at the top
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBounds(10, 10, 500, 30);
        gamePane.add(statusLabel, JLayeredPane.DRAG_LAYER);
        
        // Create a scrollable player panel at the bottom
        JPanel playerCardsPanel = new JPanel();
        playerCardsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        playerCardsPanel.setOpaque(false);
        
        JScrollPane playerScrollPane = new JScrollPane(playerCardsPanel);
        playerScrollPane.setOpaque(false);
        playerScrollPane.getViewport().setOpaque(false);
        playerScrollPane.setBorder(null);
        playerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        playerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        playerScrollPane.setBounds(50, 500, 900, 180);
        playerScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        playerPanel = playerCardsPanel;
        gamePane.add(playerScrollPane, JLayeredPane.PALETTE_LAYER);
        
        // Create a scrollable opponent panel at the top
        JPanel opponentCardsPanel = new JPanel();
        opponentCardsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        opponentCardsPanel.setOpaque(false);
        
        JScrollPane opponentScrollPane = new JScrollPane(opponentCardsPanel);
        opponentScrollPane.setOpaque(false);
        opponentScrollPane.getViewport().setOpaque(false);
        opponentScrollPane.setBorder(null);
        opponentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        opponentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        opponentScrollPane.setBounds(50, 20, 900, 180);
        opponentScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        opponentPanel = opponentCardsPanel;
        gamePane.add(opponentScrollPane, JLayeredPane.PALETTE_LAYER);
        
        // Menu button at the bottom
        JButton menuButton = new JButton("Back to Menu");
        menuButton.setBounds(10, 650, 120, 30);
        menuButton.addActionListener(e -> {
            if (aiTimer != null) {
                aiTimer.stop();
            }
            frame.dispose();
            showStartMenu();
        });
        gamePane.add(menuButton, JLayeredPane.DRAG_LAYER);

        // Help button to show rules
        JButton helpButton = new JButton("Game Rules");
        helpButton.setBounds(10, 610, 120, 30);
        helpButton.addActionListener(e -> {
            showGameRules();
        });
        gamePane.add(helpButton, JLayeredPane.DRAG_LAYER);

        frame.add(gamePane);
        
        // Add a resize listener to update components
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = frame.getContentPane().getSize();
                gamePane.setBounds(0, 0, size.width, size.height);
                backgroundPanel.setBounds(0, 0, size.width, size.height);
                repositionUIElements(size.width, size.height);
            }
        });
        
        // Make sure the layered pane fills the entire frame
        gamePane.setBounds(0, 0, frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
        
        updateUI();
        frame.setVisible(true);
    }
    
    /**
     * Gets the center position of the topCard for animations
     * @return Point containing the center coordinates
     */
    private static Point getTopCardPosition() {
        return new Point(
            gamePane.getWidth() / 2 - CARD_WIDTH/2,
            gamePane.getHeight() / 2 - CARD_HEIGHT/2
        );
    }
    
    /**
     * Repositions UI elements when the window is resized
     */
    private static void repositionUIElements(int width, int height) {
        if (topCardLabel == null) return; // Not initialized yet
        
        // Calculate new center positions
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Reposition center elements
        Point topCardPos = getTopCardPosition();
        topCardLabel.setBounds(topCardPos.x, topCardPos.y, CARD_WIDTH, CARD_HEIGHT);
        suitTypeLabel.setBounds(centerX - 200, centerY - 30, 60, 60);
        
        // Reposition deck label
        if (deckLabel != null) {
            deckLabel.setBounds(centerX + 100, centerY - CARD_HEIGHT/2, CARD_WIDTH, CARD_HEIGHT);
        }
        
        // Adjust player panels
        if (playerPanel != null && opponentPanel != null) {
            // Get the scroll panes
            Component[] components = gamePane.getComponents();
            for (Component comp : components) {
                if (comp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) comp;
                    if (scrollPane.getViewport().getView() == playerPanel) {
                        // Player panel at bottom
                        scrollPane.setBounds(50, height - 200, width - 100, 180);
                    } else if (scrollPane.getViewport().getView() == opponentPanel) {
                        // Opponent panel at top
                        scrollPane.setBounds(50, 20, width - 100, 180);
                    }
                }
            }
        }
        
        // Find and reposition buttons by searching for them by name
        for (Component comp : gamePane.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if ("Back to Menu".equals(button.getText())) {
                    button.setBounds(10, height - 50, 120, 30);
                } else if ("Game Rules".equals(button.getText())) {
                    button.setBounds(10, height - 90, 120, 30);
                }
            }
        }
        
        // Update the gamePane
        gamePane.revalidate();
        gamePane.repaint();
    }

    private static void updateUI() {
        playerPanel.removeAll();
        opponentPanel.removeAll();
        
        Player currentPlayer = game.getCurrentPlayer();

        // Update status label
        String statusText = "Turn: " + currentPlayer.getName();
        if (game.mustDrawCards() && game.getAccumulatedDrawCards() > 0) {
            statusText += " (Must draw " + game.getAccumulatedDrawCards() + " cards or play a 2)";
        }
        
        // Show who played the last card 2 and keeps the turn
        if (game.getForcedSuit() == null && game.getTopCard().getValue() == 2) {
            int previousPlayerIndex = (game.getCurrentPlayerIndex() - 1 + game.getPlayers().size()) % game.getPlayers().size();
            String previousPlayerName = game.getPlayers().get(previousPlayerIndex).getName();
            statusText += " - " + previousPlayerName + " played a card 2 and keeps turn after drawing";
        }
        
        // Show info when a card 1 has been played
        if (game.lastCardWasOne() && game.getTopCard().getValue() == 1) {
            statusText += " (Can counter with a card 1)";
            
            // Also show who played the card 1
            int previousPlayerIndex;
            if (game.getCurrentPlayerIndex() == 0) {
                // If the current player is the first one, the previous player is the last one
                previousPlayerIndex = game.getPlayers().size() - 1;
            } else {
                previousPlayerIndex = game.getCurrentPlayerIndex() - 1;
            }
            String previousPlayerName = game.getPlayers().get(previousPlayerIndex).getName();
            statusText += " - " + previousPlayerName + " played a card 1";
        }
        
        statusLabel.setText(statusText);
        
        // Update suit type display
        String suitPath = "Hez/type/";
        if (game.getForcedSuit() != null) {
            // If there's a forced suit from a wild card (7), show that suit icon
            suitPath += game.getForcedSuit().name().toLowerCase() + ".png";
        } else {
            // Otherwise show the suit of the top card
            suitPath += game.getTopCard().getSuit().name().toLowerCase() + ".png";
        }
        ImageIcon suitIcon = loadImage(suitPath, 60, 60);
        suitTypeLabel.setIcon(suitIcon);
        
        // Update top card display
        topCardLabel.setIcon(loadImage(game.getTopCard().getImagePath(), CARD_WIDTH, CARD_HEIGHT));
        
        // Display all players' cards
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            boolean isCurrentPlayer = (i == game.getCurrentPlayerIndex());
            boolean isHumanPlayer = !(player instanceof AIPlayer);
            
            if (isHumanPlayer) {
                // Human player's cards at the bottom
                for (Card card : player.getHand()) {
                    ImageIcon cardIcon = loadImage(card.getImagePath(), CARD_WIDTH, CARD_HEIGHT);
                    JLabel cardLabel = new JLabel(cardIcon);
                    cardLabel.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                    
                    if (isCurrentPlayer) {
                        // Special case: If last card was 1 and this is a card 1, it's always playable
                        boolean isCard1CounterPlayable = game.lastCardWasOne() && 
                                                        card.getValue() == 1 && 
                                                        card.canBePlayedOn(game.getTopCard(), game.getForcedSuit());
                        
                        // If player must draw cards and this isn't a card 2, disable it
                        // Unless it's a card 1 counter to a previous card 1
                        boolean canPlayThisCard = true;
                        if (game.mustDrawCards() && card.getValue() != 2 && !isCard1CounterPlayable) {
                            canPlayThisCard = false;
                        }
                        
                        if (canPlayThisCard) {
                            cardLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            
                            // Apply hover effect
                            cardLabel.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseEntered(MouseEvent e) {
                                    cardLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                                    cardLabel.setLocation(cardLabel.getX(), cardLabel.getY() - 20);
                                }
                                
                                @Override
                                public void mouseExited(MouseEvent e) {
                                    cardLabel.setBorder(null);
                                    cardLabel.setLocation(cardLabel.getX(), cardLabel.getY() + 20);
                                }
                                
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    if (isAnimating) return;
                                    if (card.canBePlayedOn(game.getTopCard(), game.getForcedSuit())) {
                                        // Create animation for playing a card
                                        isAnimating = true;
                                        
                                        // Create a temporary card for animation
                                        JLabel tempCard = new JLabel(loadImage(card.getImagePath(), CARD_WIDTH, CARD_HEIGHT));
                                        tempCard.setSize(CARD_WIDTH, CARD_HEIGHT);
                                        
                                        Point cardLocation = cardLabel.getLocationOnScreen();
                                        Point panelLocation = gamePane.getLocationOnScreen();
                                        int startX = cardLocation.x - panelLocation.x;
                                        int startY = cardLocation.y - panelLocation.y;
                                        
                                        Point targetPosition = getTopCardPosition();
                                        CardAnimation.animateCard(
                                            tempCard,
                                            startX, startY,
                                            targetPosition.x, targetPosition.y,
                                            300, // Duration in milliseconds
                                            gamePane,
                                            () -> {
                                                // Play the card after animation
                                                game.playCard(card);
                                                topCardLabel.setIcon(loadImage(game.getTopCard().getImagePath(), CARD_WIDTH, CARD_HEIGHT));
                                                
                                                // Handle wild card (7)
                                                if (card.getValue() == 7 && !player.getHand().isEmpty()) {
                                                    Card.Suit selectedSuit = promptSuitChoice();
                                                    if (selectedSuit != null) {
                                                        game.setForcedSuit(selectedSuit);
                                                        
                                                        // Update suit type display
                                                        String newSuitPath = "Hez/type/" + selectedSuit.name().toLowerCase() + ".png";
                                                        ImageIcon newSuitIcon = loadImage(newSuitPath, 60, 60);
                                                        suitTypeLabel.setIcon(newSuitIcon);
                                                    }
                                                }
                                                
                                                updateUI();
                                                checkGameOver();
                                                isAnimating = false;
                                                
                                                // If next player is AI, perform AI turn
                                                if (!game.isGameOver() && game.isCurrentPlayerAI()) {
                                                    performAITurn();
                                                }
                                            }
                                        );
                                    } else {
                                        JOptionPane.showMessageDialog(frame, "You can't play that card!", "Invalid Move", JOptionPane.WARNING_MESSAGE);
                                    }
                                }
                            });
                        } else {
                            // Card is disabled due to must draw cards rule
                            cardLabel.setEnabled(false);
                        }
                    }
                    playerPanel.add(cardLabel);
                }
            } else {
                // AI player's cards at the top (face down)
                for (int j = 0; j < player.getHand().size(); j++) {
                    JLabel cardBackLabel = new JLabel(loadImage("Hez/empty.png", CARD_WIDTH, CARD_HEIGHT));
                    cardBackLabel.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                    opponentPanel.add(cardBackLabel);
                }
                
                // Add card count label
                JLabel countLabel = new JLabel(player.getName() + ": " + player.getHand().size() + " cards");
                countLabel.setForeground(Color.WHITE);
                countLabel.setFont(new Font("Arial", Font.BOLD, 14));
                opponentPanel.add(countLabel);
            }
        }

        // Check if current player must draw cards and has no card 2 to play
        if (!isAnimating && !game.isCurrentPlayerAI() && game.mustDrawCards() && !game.hasCardTwo()) {
            // Show dialog and force drawing cards
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, 
                    "You must draw " + game.getAccumulatedDrawCards() + " cards!", 
                    "Draw Cards", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Create animation for drawing cards
                isAnimating = true;
                
                // Create a temporary card for animation
                JLabel tempCard = new JLabel(loadImage("Hez/empty.png", CARD_WIDTH, CARD_HEIGHT));
                tempCard.setSize(CARD_WIDTH, CARD_HEIGHT);
                
                // Always animate to the human player's hand at the bottom
                int targetX = gamePane.getWidth() / 2; // Center of the screen horizontally
                int targetY = gamePane.getHeight() - 150; // Near the bottom of the screen
                
                // Show only one animation but draw all accumulated cards
                CardAnimation.dealCardAnimation(
                    tempCard,
                    deckLabel.getX(), deckLabel.getY(),
                    targetX, targetY,
                    300, // Duration in milliseconds
                    gamePane,
                    () -> {
                        // Remove the temporary card from the container
                        gamePane.remove(tempCard);
                        gamePane.repaint();
                        
                        // Store current player index before drawing cards
                        int previousPlayerIndex = game.getCurrentPlayerIndex();
                        
                        // Draw all accumulated cards
                        game.drawCardFromDeck();
                        // Note: drawCardFromDeck will now return turn to the previous player (AI)

                        // Show message to explain the rule if it's the first time
                        if (!hasShownCard2Rule) {
                            hasShownCard2Rule = true;
                            JOptionPane.showMessageDialog(frame, 
                                "After drawing cards from a card 2, you lose your turn and " +
                                "the player who played the card 2 gets to play again.",
                                "Card 2 Rule", 
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                        
                        updateUI();
                        checkGameOver();
                        isAnimating = false;
                        
                        // Since turn returned to previous player (AI), perform its turn
                        if (!game.isGameOver() && game.isCurrentPlayerAI()) {
                            performAITurn();
                        }
                    }
                );
            });
        }

        playerPanel.revalidate();
        playerPanel.repaint();
        opponentPanel.revalidate();
        opponentPanel.repaint();
    }
    
    private static void performAITurn() {
        // Add a delay before AI makes its move
        aiTimer = new javax.swing.Timer(1000, e -> {
            if (game.isCurrentPlayerAI()) {
                AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
                
                // Check if AI must draw cards and has no card 2 to play
                if (game.mustDrawCards() && !game.hasCardTwo()) {
                    // AI must draw cards
                    isAnimating = true;
                    
                    // Create a temporary card for animation
                    JLabel tempCard = new JLabel(loadImage("Hez/empty.png", CARD_WIDTH, CARD_HEIGHT));
                    tempCard.setSize(CARD_WIDTH, CARD_HEIGHT);
                    
                    // Show only one animation but draw all accumulated cards
                    CardAnimation.dealCardAnimation(
                        tempCard,
                        deckLabel.getX(), deckLabel.getY(),
                        opponentPanel.getX() + opponentPanel.getWidth()/2, opponentPanel.getY() + 50,
                        300, // Duration in milliseconds
                        gamePane,
                        () -> {
                            // Remove the temporary card from the container
                            gamePane.remove(tempCard);
                            gamePane.repaint();
                            
                            // Draw all accumulated cards instead of just one
                            game.drawCardFromDeck();
                            
                            updateUI();
                            checkGameOver();
                            isAnimating = false;
                            
                            // If next player is still AI, perform another AI turn
                            if (!game.isGameOver() && game.isCurrentPlayerAI()) {
                                performAITurn();
                            }
                        }
                    );
                    
                    ((javax.swing.Timer)e.getSource()).stop();
                    return;
                }
                
                Card cardToPlay = game.getAIMove();
                
                if (cardToPlay != null) {
                    // AI has a card to play
                    isAnimating = true;
                    
                    // Create a temporary card for animation
                    JLabel tempCard = new JLabel(loadImage(cardToPlay.getImagePath(), CARD_WIDTH, CARD_HEIGHT));
                    tempCard.setSize(CARD_WIDTH, CARD_HEIGHT);
                    
                    Point targetPosition = getTopCardPosition();
                    CardAnimation.animateCard(
                        tempCard,
                        opponentPanel.getX() + opponentPanel.getWidth()/2, opponentPanel.getY() + 50,
                        targetPosition.x, targetPosition.y,
                        300, // Duration in milliseconds
                        gamePane,
                        () -> {
                            // Play the card after animation
                            game.playCard(cardToPlay);
                            topCardLabel.setIcon(loadImage(game.getTopCard().getImagePath(), CARD_WIDTH, CARD_HEIGHT));
                            
                            // Handle wild card (7)
                            if (cardToPlay.getValue() == 7 && !ai.getHand().isEmpty()) {
                                Card.Suit selectedSuit = game.getAISuitChoice();
                                if (selectedSuit != null) {
                                    game.setForcedSuit(selectedSuit);
                                    
                                    // Update suit type display
                                    String newSuitPath = "Hez/type/" + selectedSuit.name().toLowerCase() + ".png";
                                    ImageIcon newSuitIcon = loadImage(newSuitPath, 60, 60);
                                    suitTypeLabel.setIcon(newSuitIcon);
                                    
                                    // Show message about AI's choice with explanation
                                    JOptionPane.showMessageDialog(frame, 
                                        ai.getName() + " played a wild card (7) and changed the suit to " + selectedSuit.name() + 
                                        "\n(AI chooses based on the most cards of that suit in its hand)", 
                                        "Suit Changed", 
                                        JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                            
                            updateUI();
                            checkGameOver();
                            isAnimating = false;
                            
                            // If next player is still AI, perform another AI turn
                            if (!game.isGameOver() && game.isCurrentPlayerAI()) {
                                performAITurn();
                            }
                        }
                    );
                } else {
                    // AI needs to draw a card
                    isAnimating = true;
                    
                    // Create a temporary card for animation
                    JLabel tempCard = new JLabel(loadImage("Hez/empty.png", CARD_WIDTH, CARD_HEIGHT));
                    tempCard.setSize(CARD_WIDTH, CARD_HEIGHT);
                    
                    CardAnimation.dealCardAnimation(
                        tempCard,
                        deckLabel.getX(), deckLabel.getY(),
                        opponentPanel.getX() + opponentPanel.getWidth()/2, opponentPanel.getY() + 50,
                        300, // Duration in milliseconds
                        gamePane,
                        () -> {
                            // Remove the temporary card from the container
                            gamePane.remove(tempCard);
                            gamePane.repaint();
                            
                            // Draw a single card (not accumulated)
                            ai.drawCard(game.getDeck());
                            game.advanceTurn();
                            updateUI();
                            checkGameOver();
                            isAnimating = false;
                            
                            // If next player is still AI, perform another AI turn
                            if (!game.isGameOver() && game.isCurrentPlayerAI()) {
                                performAITurn();
                            }
                        }
                    );
                }
            }
            
            ((javax.swing.Timer)e.getSource()).stop();
        });
        
        aiTimer.setRepeats(false);
        aiTimer.start();
    }
    
    private static void checkGameOver() {
        if (game.isGameOver()) {
            Player winner = game.getWinner();
            JOptionPane.showMessageDialog(frame, 
                winner.getName() + " wins the game!", 
                "Game Over", 
                JOptionPane.INFORMATION_MESSAGE);
            
            int option = JOptionPane.showConfirmDialog(frame, 
                "Do you want to play again?", 
                "Play Again", 
                JOptionPane.YES_NO_OPTION);
                
            if (option == JOptionPane.YES_OPTION) {
                frame.dispose();
                startGame(true); // Always start with AI mode
            } else {
                frame.dispose();
                showStartMenu();
            }
        }
    }

    private static Card.Suit promptSuitChoice() {
        JDialog dialog = new JDialog(frame, "Choose a suit", true);
        dialog.setLayout(new FlowLayout());
    
        Card.Suit[] suits = Card.Suit.values();
        final Card.Suit[] selectedSuit = {null};
    
        for (Card.Suit suit : suits) {
            String path = "Hez/type/" + suit.name().toLowerCase() + ".png";
            ImageIcon icon = loadImage(path, 80, 120);
            JButton button = new JButton(icon);
            button.addActionListener(e -> {
                selectedSuit[0] = suit;
                dialog.dispose();
            });
            dialog.add(button);
        }
    
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    
        return selectedSuit[0];
    }

    private static void showGameRules() {
        JDialog rulesDialog = new JDialog(frame, "How to play Hez", true);
        rulesDialog.setSize(700, 520);
        rulesDialog.setLocationRelativeTo(frame);
        rulesDialog.setResizable(true);
        rulesDialog.setMinimumSize(new Dimension(500, 400));
        
        // Add a brown header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(139, 69, 19)); // Brown color
        headerPanel.setPreferredSize(new Dimension(700, 60));
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel titleLabel = new JLabel("How to play Hez");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Main content panel
        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
        rulesPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        rulesPanel.setBackground(Color.WHITE);
        
        String[] rules = {
            "• There are four types of cards: Sticks (Sticks), Cups (Cups), Swords (Swords), Gold (Gold).",
            "• Each type consists of 10 cards numbered from 1 to 7 and from 10 to 12.",
            "• You play against an AI opponent.",
            "• The game starts with four cards for each player and one card in the middle.",
            "• The game consists of playing a card that is similar to the one in the middle (same type or same number).",
            "• If you do not have any card that meets these criteria, you must take an additional card.",
            "• Special cards:",
            "  - If you play a card with number 1, the next player's turn is skipped. However, if they have a card 1,",
            "    they can play it to counter yours, and your turn will be skipped instead.",
            "  - If you play a card with number 2, the next player will not play and will be required to take 2 more cards.",
            "    If they also have a 2, they can play it and the next player takes 4 cards (2 + 2).",
            "  - If you play a card whose number is 7, you have the right to change the type of card as you wish.",
            "• The winner of the game is the one who no longer has a card in their possession."
        };
        
        for (String rule : rules) {
            JLabel ruleLabel = new JLabel("<html><body width='600'>" + rule + "</body></html>");
            ruleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            ruleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            ruleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            rulesPanel.add(ruleLabel);
        }
        
        // Add close button at the bottom with a green background
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(120, 35));
        closeButton.setBackground(new Color(0, 100, 0)); // Dark green
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> rulesDialog.dispose());
        buttonPanel.add(closeButton);
        
        rulesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rulesPanel.add(buttonPanel);
        
        // Main container panel with BorderLayout
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(rulesPanel, BorderLayout.CENTER);
        
        rulesDialog.add(containerPanel);
        rulesDialog.setVisible(true);
    }
}
