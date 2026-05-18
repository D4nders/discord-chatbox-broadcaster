package com.discordchatboxbroadcaster.notifier;

import com.discordchatboxbroadcaster.render.ChatSegment;
import java.util.List;

public interface Notifier {
    void initialize();
    void terminate();
    void dispatchNotification(List<ChatSegment> notificationSegments);
}