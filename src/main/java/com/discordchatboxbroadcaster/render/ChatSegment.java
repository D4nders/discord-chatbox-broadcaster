package com.discordchatboxbroadcaster.render;

import java.awt.image.BufferedImage;

public class ChatSegment {

    public enum Role {
        TEXT,
        HIGHLIGHT,
        CLAN
    }

    private final String textContent;
    private final Role role;
    private final BufferedImage inlineImage;

    public ChatSegment(String textContent, Role role) {
        this.textContent = textContent;
        this.role = role;
        this.inlineImage = null;
    }

    public ChatSegment(BufferedImage inlineImage) {
        this.textContent = "";
        this.role = null;
        this.inlineImage = inlineImage;
    }

    public String retrieveTextContent() {
        return textContent;
    }

    public Role retrieveRole() {
        return role;
    }

    public BufferedImage retrieveInlineImage() {
        return inlineImage;
    }

    public boolean isImage() {
        return inlineImage != null;
    }
}