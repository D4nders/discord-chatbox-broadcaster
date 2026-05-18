package com.discordchatboxbroadcaster.notifier;

public interface Notifier {
    void dispatchNotification(byte[] generatedImageData);
}