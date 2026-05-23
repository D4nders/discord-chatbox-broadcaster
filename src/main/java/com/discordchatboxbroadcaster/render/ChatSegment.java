package com.discordchatboxbroadcaster.render;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ChatSegment {

    private final String textContent;
    private final Color textColor;
    private final BufferedImage inlineImage;

    public ChatSegment(String textContent, Color textColor) {
        this.textContent = textContent;
        this.textColor = textColor;
        this.inlineImage = null;
    }

    public ChatSegment(BufferedImage inlineImage) {
        this.textContent = "";
        this.textColor = Color.WHITE;
        this.inlineImage = inlineImage;
    }

    public String retrieveTextContent() {
        return textContent;
    }

    public Color retrieveTextColor() {
        return textColor;
    }

    public BufferedImage retrieveInlineImage() {
        return inlineImage;
    }

    public boolean isImage() {
        return inlineImage != null;
    }
}