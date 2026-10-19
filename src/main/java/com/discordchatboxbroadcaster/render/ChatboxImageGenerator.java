package com.discordchatboxbroadcaster.render;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.config.BackgroundStyle;
import com.discordchatboxbroadcaster.config.ImageScale;
import net.runelite.client.ui.FontManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ChatboxImageGenerator {

    private static final Color DEFAULT_HIGHLIGHT_COLOR = new Color(239, 16, 32);
    private static final Color DEFAULT_CLAN_NAME_COLOR = Color.BLUE;
    private static final Color CLASSIC_BACKGROUND_COLOR = new Color(201, 191, 169);
    private static final Color DARK_BACKGROUND_COLOR = new Color(0, 0, 0, 180);

    private final DiscordChatboxBroadcasterConfig config;

    public ChatboxImageGenerator(DiscordChatboxBroadcasterConfig config) {
        this.config = config;
    }

    public byte[] generateChatboxImage(List<ChatSegment> activeChatSegments) {
        try {
            boolean showClan = config == null || config.showClanName();
            boolean showIcon = config == null || config.showAccountIcon();

            BackgroundStyle backgroundStyle = (config != null && config.backgroundStyle() != null)
                    ? config.backgroundStyle()
                    : BackgroundStyle.CLASSIC;

            Color highlightColor = (config != null && config.highlightColor() != null)
                    ? config.highlightColor()
                    : DEFAULT_HIGHLIGHT_COLOR;

            Color clanNameColor = (config != null && config.clanNameColor() != null)
                    ? config.clanNameColor()
                    : DEFAULT_CLAN_NAME_COLOR;

            ImageScale imageScale = (config != null && config.imageScale() != null)
                    ? config.imageScale()
                    : ImageScale.X1;

            List<ChatSegment> visibleSegments = new ArrayList<>();
            if (activeChatSegments != null) {
                for (ChatSegment segment : activeChatSegments) {
                    if (segment == null) {
                        continue;
                    }
                    if (segment.isImage()) {
                        if (showIcon && segment.retrieveInlineImage() != null) {
                            visibleSegments.add(segment);
                        }
                    } else {
                        if (!showClan && segment.retrieveRole() == ChatSegment.Role.CLAN) {
                            continue;
                        }
                        visibleSegments.add(segment);
                    }
                }
            }

            BufferedImage metricsCanvas = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D metricsGraphics = metricsCanvas.createGraphics();
            metricsGraphics.setFont(FontManager.getRunescapeFont());

            int dynamicCanvasWidth = 10;
            for (ChatSegment currentSegment : visibleSegments) {
                if (currentSegment.isImage()) {
                    dynamicCanvasWidth += currentSegment.retrieveInlineImage().getWidth() + 2;
                } else {
                    String text = currentSegment.retrieveTextContent();
                    if (text != null) {
                        dynamicCanvasWidth += metricsGraphics.getFontMetrics().stringWidth(text);
                    }
                }
            }
            metricsGraphics.dispose();

            int imageCanvasHeight = 30;
            BufferedImage activeImageCanvas = new BufferedImage(dynamicCanvasWidth, imageCanvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D activeGraphicsContext = activeImageCanvas.createGraphics();

            activeGraphicsContext.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            if (backgroundStyle == BackgroundStyle.CLASSIC) {
                activeGraphicsContext.setColor(CLASSIC_BACKGROUND_COLOR);
                activeGraphicsContext.fillRect(0, 0, dynamicCanvasWidth, imageCanvasHeight);
            } else if (backgroundStyle == BackgroundStyle.DARK) {
                activeGraphicsContext.setColor(DARK_BACKGROUND_COLOR);
                activeGraphicsContext.fillRect(0, 0, dynamicCanvasWidth, imageCanvasHeight);
            }
            // BackgroundStyle.TRANSPARENT leaves the ARGB canvas fully transparent (no fill)

            activeGraphicsContext.setFont(FontManager.getRunescapeFont());
            int currentHorizontalPosition = 5;
            int currentVerticalPosition = 20;

            boolean drawShadow = (backgroundStyle == BackgroundStyle.TRANSPARENT || backgroundStyle == BackgroundStyle.DARK);

            for (ChatSegment currentSegment : visibleSegments) {
                currentHorizontalPosition = renderIndividualSegment(
                        activeGraphicsContext,
                        currentSegment,
                        currentHorizontalPosition,
                        currentVerticalPosition,
                        backgroundStyle,
                        drawShadow,
                        highlightColor,
                        clanNameColor
                );
            }

            activeGraphicsContext.dispose();

            BufferedImage outputImage = activeImageCanvas;
            int scaleFactor = imageScale.getFactor();
            if (scaleFactor > 1) {
                int scaledWidth = dynamicCanvasWidth * scaleFactor;
                int scaledHeight = imageCanvasHeight * scaleFactor;
                BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D scaledGraphics = scaledImage.createGraphics();
                scaledGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                scaledGraphics.drawImage(activeImageCanvas, 0, 0, scaledWidth, scaledHeight, null);
                scaledGraphics.dispose();
                outputImage = scaledImage;
            }

            return convertImageToByteArray(outputImage);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private int renderIndividualSegment(
            Graphics2D graphicsContext,
            ChatSegment targetSegment,
            int horizontalCoordinate,
            int verticalCoordinate,
            BackgroundStyle backgroundStyle,
            boolean drawShadow,
            Color highlightColor,
            Color clanNameColor
    ) {
        if (targetSegment.isImage()) {
            BufferedImage inlineImage = targetSegment.retrieveInlineImage();
            int verticalOffset = verticalCoordinate - inlineImage.getHeight() + 1;
            graphicsContext.drawImage(inlineImage, horizontalCoordinate, verticalOffset, null);
            return horizontalCoordinate + inlineImage.getWidth() + 2;
        } else {
            String text = targetSegment.retrieveTextContent();
            if (text == null || text.isEmpty()) {
                return horizontalCoordinate;
            }
            Color textColor = mapRoleToColor(targetSegment.retrieveRole(), backgroundStyle, highlightColor, clanNameColor);
            if (drawShadow) {
                graphicsContext.setColor(Color.BLACK);
                graphicsContext.drawString(text, horizontalCoordinate + 1, verticalCoordinate + 1);
            }
            graphicsContext.setColor(textColor);
            graphicsContext.drawString(text, horizontalCoordinate, verticalCoordinate);
            return horizontalCoordinate + graphicsContext.getFontMetrics().stringWidth(text);
        }
    }

    private Color mapRoleToColor(
            ChatSegment.Role role,
            BackgroundStyle backgroundStyle,
            Color highlightColor,
            Color clanNameColor
    ) {
        if (role == null) {
            return (backgroundStyle == BackgroundStyle.CLASSIC) ? Color.BLACK : Color.WHITE;
        }
        switch (role) {
            case HIGHLIGHT:
                return highlightColor;
            case CLAN:
                return clanNameColor;
            case TEXT:
            default:
                return (backgroundStyle == BackgroundStyle.CLASSIC) ? Color.BLACK : Color.WHITE;
        }
    }

    private byte[] convertImageToByteArray(BufferedImage targetImageCanvas) {
        try {
            ByteArrayOutputStream outputStreamBridge = new ByteArrayOutputStream();
            ImageIO.write(targetImageCanvas, "png", outputStreamBridge);
            return outputStreamBridge.toByteArray();
        } catch (Exception conversionException) {
            return new byte[0];
        }
    }
}