package com.discordchatboxbroadcaster;

public class SharedEventState {

    private int lastPetDropTick;
    private int lastCollectionLogTick;

    public SharedEventState() {
        this.lastPetDropTick = -1;
        this.lastCollectionLogTick = -1;
    }

    public void registerPetDrop(int currentTick) {
        this.lastPetDropTick = currentTick;
    }

    public boolean isWithinPetDropWindow(int currentTick) {
        return lastPetDropTick != -1 && (currentTick - lastPetDropTick) <= 2;
    }

    public void registerCollectionLogDrop(int currentTick) {
        this.lastCollectionLogTick = currentTick;
    }

    public boolean isWithinCollectionLogWindow(int currentTick) {
        return lastCollectionLogTick != -1 && (currentTick - lastCollectionLogTick) <= 2;
    }
}