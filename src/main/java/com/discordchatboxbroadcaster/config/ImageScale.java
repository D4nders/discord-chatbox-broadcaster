package com.discordchatboxbroadcaster.config;

public enum ImageScale {
    X1(1, "1x"),
    X2(2, "2x"),
    X3(3, "3x");

    private final int factor;
    private final String name;

    ImageScale(int factor, String name) {
        this.factor = factor;
        this.name = name;
    }

    public int getFactor() {
        return factor;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
