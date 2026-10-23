package com.discordchatboxbroadcaster.config;

public enum XpMilestoneInterval {
    ONE_MILLION(1_000_000, "1M"),
    FIVE_MILLION(5_000_000, "5M"),
    TEN_MILLION(10_000_000, "10M"),
    TWENTY_FIVE_MILLION(25_000_000, "25M"),
    FIFTY_MILLION(50_000_000, "50M");

    private final int xp;
    private final String name;

    XpMilestoneInterval(int xp, String name) {
        this.xp = xp;
        this.name = name;
    }

    public int getXp() {
        return xp;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
