package com.discordchatboxbroadcaster.ui;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.config.ConfigShareCodeManager;
import com.discordchatboxbroadcaster.config.ImageScale;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatSegment.Role;
import com.discordchatboxbroadcaster.render.ChatboxImageGenerator;
import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

@Slf4j
public class DiscordChatboxBroadcasterPanel extends PluginPanel {

    public static final Color SUCCESS_COLOR = new Color(0, 200, 83);
    public static final Color ERROR_COLOR = new Color(255, 82, 82);

    private final DiscordChatboxBroadcasterConfig config;
    private final ConfigManager configManager;
    private final Gson gson;

    private final BufferedImage defaultIronmanHelm;
    private String currentClanName = "Clan";
    private String currentPlayerName = "Player";
    private BufferedImage currentAccountIcon;

    @Getter
    private final List<ChatSegment> sampleSegments = new ArrayList<>();

    @Getter
    private final JLabel previewLabel;

    @Getter
    private final JScrollPane previewScrollPane;

    @Getter
    private final JLabel previewDimensionsLabel;

    @Getter
    private final JButton exportButton;

    @Getter
    private final JCheckBox importEventsCheckbox;

    @Getter
    private final JTextField shareCodeInputField;

    @Getter
    private final JButton importButton;

    @Getter
    private final JLabel statusLabel;

    public DiscordChatboxBroadcasterPanel(
            DiscordChatboxBroadcasterConfig config,
            ConfigManager configManager,
            Gson gson
    ) {
        super(true);
        this.config = config;
        this.configManager = configManager;
        this.gson = gson;

        this.defaultIronmanHelm = getSampleIronmanHelm();
        this.currentAccountIcon = defaultIronmanHelm;
        rebuildSampleSegments();

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new DynamicGridLayout(0, 1, 0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // 1) Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JLabel titleLabel = new JLabel("Discord Broadcaster");
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel);

        // 2) Live Appearance Preview Card
        JPanel previewCard = createCard("Live Appearance Preview");

        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        previewLabel.setBorder(new EmptyBorder(4, 6, 4, 6));

        previewScrollPane = new JScrollPane(
                previewLabel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        previewScrollPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        previewScrollPane.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
        previewScrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1));
        previewScrollPane.setPreferredSize(new Dimension(0, 52));
        previewScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        previewScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        previewCard.add(previewScrollPane);
        previewCard.add(Box.createRigidArea(new Dimension(0, 6)));

        previewDimensionsLabel = new JLabel();
        previewDimensionsLabel.setFont(FontManager.getRunescapeSmallFont());
        previewDimensionsLabel.setForeground(Color.GRAY);
        previewDimensionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewCard.add(previewDimensionsLabel);

        add(previewCard);

        // 3) Export Card
        JPanel exportCard = createCard("Export Config");

        JLabel exportDesc = new JLabel("<html><body style='width: 145px;'>Copy your appearance &amp; filter settings as a share code. Webhook URL is never included.</body></html>");
        exportDesc.setFont(FontManager.getRunescapeSmallFont());
        exportDesc.setForeground(Color.LIGHT_GRAY);
        exportDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportCard.add(exportDesc);
        exportCard.add(Box.createRigidArea(new Dimension(0, 8)));

        exportButton = new JButton("Export to Clipboard");
        exportButton.setFont(FontManager.getRunescapeFont());
        exportButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        exportButton.addActionListener(e -> exportShareCode());
        exportCard.add(exportButton);
        add(exportCard);

        // 4) Import Card
        JPanel importCard = createCard("Import Config");

        importEventsCheckbox = new JCheckBox("Also import event toggles & filters", true);
        importEventsCheckbox.setFont(FontManager.getRunescapeSmallFont());
        importEventsCheckbox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        importEventsCheckbox.setForeground(Color.WHITE);
        importEventsCheckbox.setFocusPainted(false);
        importEventsCheckbox.setToolTipText("When unchecked, only Appearance settings are applied.");
        importEventsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        importCard.add(importEventsCheckbox);
        importCard.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel inputLabel = new JLabel("Share Code (DCB1:...):");
        inputLabel.setFont(FontManager.getRunescapeSmallFont());
        inputLabel.setForeground(Color.LIGHT_GRAY);
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        importCard.add(inputLabel);
        importCard.add(Box.createRigidArea(new Dimension(0, 4)));

        shareCodeInputField = new JTextField();
        shareCodeInputField.setFont(FontManager.getRunescapeFont());
        shareCodeInputField.setBackground(ColorScheme.DARK_GRAY_COLOR);
        shareCodeInputField.setForeground(Color.WHITE);
        shareCodeInputField.setCaretColor(Color.WHITE);
        shareCodeInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        shareCodeInputField.setAlignmentX(Component.LEFT_ALIGNMENT);
        shareCodeInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        importCard.add(shareCodeInputField);
        importCard.add(Box.createRigidArea(new Dimension(0, 8)));

        importButton = new JButton("Import Share Code");
        importButton.setFont(FontManager.getRunescapeFont());
        importButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
        importButton.setForeground(Color.WHITE);
        importButton.setFocusPainted(false);
        importButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        importButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        importButton.addActionListener(e -> importShareCode());
        importCard.add(importButton);

        statusLabel = new JLabel();
        statusLabel.setFont(FontManager.getRunescapeSmallFont());
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        importCard.add(Box.createRigidArea(new Dimension(0, 8)));
        importCard.add(statusLabel);

        add(importCard);

        updatePreview();
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1),
                new EmptyBorder(8, 8, 8, 8)
        ));
        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FontManager.getRunescapeBoldFont());
            titleLabel.setForeground(ColorScheme.BRAND_ORANGE);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(titleLabel);
            card.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        return card;
    }

    public void updatePreview() {
        ChatboxImageGenerator generator = new ChatboxImageGenerator(config);
        BufferedImage img = generator.generateChatboxBufferedImage(sampleSegments, false);
        if (img == null) {
            previewLabel.setIcon(null);
            previewLabel.setText("Preview unavailable");
            if (previewDimensionsLabel != null) {
                previewDimensionsLabel.setText("");
            }
            return;
        }

        previewLabel.setIcon(new ImageIcon(img));
        previewLabel.setText(null);

        ImageScale scale = (config != null && config.imageScale() != null)
                ? config.imageScale()
                : ImageScale.X1;
        int factor = scale.getFactor();
        int outputWidth = img.getWidth() * factor;
        int outputHeight = img.getHeight() * factor;

        String sizeCaption = "Output size: " + outputWidth + "x" + outputHeight + " px (" + scale + ")";
        previewLabel.setToolTipText(sizeCaption);
        if (previewDimensionsLabel != null) {
            previewDimensionsLabel.setText(sizeCaption);
        }
        previewLabel.revalidate();
        previewLabel.repaint();
    }

    public void updatePlayerContext(String clanName, String playerName, BufferedImage accountIcon) {
        this.currentClanName = (clanName != null && !clanName.trim().isEmpty()) ? clanName.trim() : "Clan";
        this.currentPlayerName = (playerName != null && !playerName.trim().isEmpty()) ? playerName.trim() : "Player";
        this.currentAccountIcon = (accountIcon != null) ? accountIcon : defaultIronmanHelm;
        rebuildSampleSegments();
        updatePreview();
    }

    private void rebuildSampleSegments() {
        sampleSegments.clear();
        String clan = (currentClanName != null && !currentClanName.trim().isEmpty()) ? currentClanName.trim() : "Clan";
        String player = (currentPlayerName != null && !currentPlayerName.trim().isEmpty()) ? currentPlayerName.trim() : "Player";
        BufferedImage icon = (currentAccountIcon != null) ? currentAccountIcon : defaultIronmanHelm;

        sampleSegments.add(new ChatSegment("[" + clan + "] ", Role.CLAN));
        if (icon != null) {
            sampleSegments.add(new ChatSegment(icon));
        }
        sampleSegments.add(new ChatSegment(player + " ", Role.TEXT));
        sampleSegments.add(new ChatSegment("received a drop: ", Role.TEXT));
        sampleSegments.add(new ChatSegment("Twisted bow", Role.HIGHLIGHT));
    }

    public void exportShareCode() {
        String code = ConfigShareCodeManager.generateShareCode(config, gson);
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
        } catch (HeadlessException | IllegalStateException e) {
            log.debug("Clipboard unavailable: {}", e.getMessage());
        }
        setStatusMessage("Copied share code to clipboard!", SUCCESS_COLOR);
    }

    public void importShareCode() {
        String rawCode = shareCodeInputField.getText();
        if (rawCode == null || rawCode.trim().isEmpty()) {
            setStatusMessage("Please paste a DCB1:... share code first.", ERROR_COLOR);
            return;
        }

        boolean importEvents = importEventsCheckbox.isSelected();
        boolean success = ConfigShareCodeManager.applyShareCode(rawCode, configManager, gson, importEvents);
        if (success) {
            shareCodeInputField.setText("");
            if (importEvents) {
                setStatusMessage("Share code imported successfully!", SUCCESS_COLOR);
            } else {
                setStatusMessage("Appearance settings imported!", SUCCESS_COLOR);
            }
            updatePreview();
        } else {
            shareCodeInputField.selectAll();
            setStatusMessage("Invalid share code. Expected a code starting with DCB1:.", ERROR_COLOR);
        }
    }

    public void setStatusMessage(String message, Color color) {
        statusLabel.setForeground(color);
        if (message == null || message.isEmpty()) {
            statusLabel.setText("");
        } else if (message.startsWith("<html>")) {
            statusLabel.setText(message);
        } else {
            statusLabel.setText("<html>" + message + "</html>");
        }
    }

    public String getStatusMessage() {
        String text = statusLabel.getText();
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "").trim();
    }

    public static BufferedImage getSampleIronmanHelm() {
        try {
            BufferedImage helm = ImageUtil.loadImageResource(
                    DiscordChatboxBroadcasterPanel.class,
                    "/net/runelite/client/plugins/hiscore/ironman.png"
            );
            if (helm != null) {
                return helm;
            }
        } catch (Exception e) {
            log.debug("Unable to load ironman icon resource: {}", e.getMessage());
        }
        return createFallbackIronmanHelm();
    }

    public static BufferedImage createSampleIronmanHelm() {
        return getSampleIronmanHelm();
    }

    private static BufferedImage createFallbackIronmanHelm() {
        BufferedImage helm = new BufferedImage(13, 13, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = helm.createGraphics();
        Color darkGrey = new Color(70, 70, 70);
        Color silver = new Color(180, 180, 180);
        Color lightSilver = new Color(220, 220, 220);
        Color visorBlack = new Color(20, 20, 20);

        g.setColor(darkGrey);
        g.drawRect(2, 2, 8, 8);
        g.setColor(silver);
        g.fillRect(3, 3, 7, 7);
        g.setColor(lightSilver);
        g.drawLine(3, 3, 9, 3);
        g.drawLine(3, 4, 3, 7);
        g.setColor(visorBlack);
        g.drawLine(4, 6, 8, 6);
        g.drawLine(6, 6, 6, 9);
        g.dispose();
        return helm;
    }
}
