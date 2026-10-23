package com.discordchatboxbroadcaster.notifier;

import com.discordchatboxbroadcaster.render.ChatSegment;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public final class PendingNotification {

    private final List<ChatSegment> segments;
    private final BufferedImage screenshot;

    public PendingNotification(List<ChatSegment> segments, BufferedImage screenshot) {
        this.segments = segments != null ? Collections.unmodifiableList(segments) : Collections.emptyList();
        this.screenshot = screenshot;
    }

    public List<ChatSegment> getSegments() {
        return segments;
    }

    public BufferedImage getScreenshot() {
        return screenshot;
    }
}
