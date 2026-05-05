package com.adnanpsd.rednet.model;

import org.bukkit.Location;
import java.util.UUID;

public class SetupSession {

    private final UUID playerUuid;
    private final CircuitType type;
    private final double delay;
    private Location senderLocation;

    public SetupSession(UUID playerUuid, CircuitType type, double delay) {
        this.playerUuid = playerUuid;
        this.type = type;
        this.delay = delay;
        this.senderLocation = null;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public CircuitType getType() {
        return type;
    }

    public double getDelay() {
        return delay;
    }

    public Location getSenderLocation() {
        return senderLocation;
    }

    public void setSenderLocation(Location senderLocation) {
        this.senderLocation = senderLocation;
    }
}