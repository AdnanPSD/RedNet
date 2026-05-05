package com.adnanpsd.rednet.model;

public enum CircuitType {
    QUANTUM,
    ON,
    OFF,
    TOGGLE,
    RANDOM,
    REVERSE,
    IMPULSE;

    public static CircuitType fromString(String name) {
        for (CircuitType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}