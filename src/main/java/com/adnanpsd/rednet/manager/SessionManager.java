package com.adnanpsd.rednet.manager;

import com.adnanpsd.rednet.model.SetupSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private final Map<UUID, SetupSession> activeSessions;

    public SessionManager() {
        this.activeSessions = new HashMap<>();
    }

    public void startSession(UUID uuid, SetupSession session) {
        activeSessions.put(uuid, session);
    }

    public SetupSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    public void removeSession(UUID uuid) {
        activeSessions.remove(uuid);
    }

    public boolean hasSession(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }
}   