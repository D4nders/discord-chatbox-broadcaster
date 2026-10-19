package com.discordchatboxbroadcaster.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageScale {
    X1(1, "1x"),
    X2(2, "2x"),
    X3(3, "3x");

    private final int factor;
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
