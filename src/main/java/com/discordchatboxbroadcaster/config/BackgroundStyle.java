package com.discordchatboxbroadcaster.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BackgroundStyle {
    CLASSIC("Classic"),
    TRANSPARENT("Transparent"),
    DARK("Dark");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
