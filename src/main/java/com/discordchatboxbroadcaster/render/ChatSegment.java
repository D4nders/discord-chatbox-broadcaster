package com.discordchatboxbroadcaster.render;

import java.awt.Color;

public class ChatSegment {

    private final String textContent;
    private final Color textColor;

    public ChatSegment(String textContent, Color textColor) {
        this.textContent = textContent;
        this.textColor = textColor;
    }

    public String retrieveTextContent() {
        return textContent;
    }

    public Color retrieveTextColor() {
        return textColor;
    }
}