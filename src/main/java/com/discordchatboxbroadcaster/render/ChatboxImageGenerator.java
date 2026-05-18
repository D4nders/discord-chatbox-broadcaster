package com.discordchatboxbroadcaster.render;

import net.runelite.client.ui.FontManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;

public class ChatboxImageGenerator {

    public byte[] generateChatboxImage(List<ChatSegment> activeChatSegments) {
        BufferedImage metricsCanvas = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D metricsGraphics = metricsCanvas.createGraphics();
        metricsGraphics.setFont(FontManager.getRunescapeFont());

        int dynamicCanvasWidth = 10;
        for (ChatSegment currentSegment : activeChatSegments) {
            dynamicCanvasWidth += metricsGraphics.getFontMetrics().stringWidth(currentSegment.retrieveTextContent());
        }
        metricsGraphics.dispose();

        int imageCanvasHeight = 30;
        BufferedImage activeImageCanvas = new BufferedImage(dynamicCanvasWidth, imageCanvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D activeGraphicsContext = activeImageCanvas.createGraphics();

        activeGraphicsContext.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        activeGraphicsContext.setColor(new Color(201, 191, 169));
        activeGraphicsContext.fillRect(0, 0, dynamicCanvasWidth, imageCanvasHeight);

        activeGraphicsContext.setFont(FontManager.getRunescapeFont());
        int currentHorizontalPosition = 5;
        int currentVerticalPosition = 20;

        for (ChatSegment currentSegment : activeChatSegments) {
            currentHorizontalPosition = renderIndividualSegment(activeGraphicsContext, currentSegment, currentHorizontalPosition, currentVerticalPosition);
        }

        activeGraphicsContext.dispose();
        return convertImageToByteArray(activeImageCanvas);
    }

    private int renderIndividualSegment(Graphics2D graphicsContext, ChatSegment targetSegment, int horizontalCoordinate, int verticalCoordinate) {
        graphicsContext.setColor(targetSegment.retrieveTextColor());
        graphicsContext.drawString(targetSegment.retrieveTextContent(), horizontalCoordinate, verticalCoordinate);
        return horizontalCoordinate + graphicsContext.getFontMetrics().stringWidth(targetSegment.retrieveTextContent());
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