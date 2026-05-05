package com.adnanpsd.rednet.model;

import org.bukkit.Location;

import java.util.UUID;

public class Circuit {

    private final UUID id;
    private final UUID owner;
    private CircuitType type; // Artık final değil
    private double delay;     // Artık final değil
    private final Location sender;
    private final Location receiver;

    public Circuit(UUID id, UUID owner, CircuitType type, double delay, Location sender, Location receiver) {
        this.id = id;
        this.owner = owner;
        this.type = type;
        this.delay = delay;
        this.sender = sender.getBlock().getLocation();
        this.receiver = receiver.getBlock().getLocation();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public CircuitType getType() {
        return type;
    }

    public void setType(CircuitType type) {
        this.type = type;
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = Math.max(0.0, delay); // Gecikme eksiye düşemez
    }

    public Location getSender() {
        return sender;
    }

    public Location getReceiver() {
        return receiver;
    }
}