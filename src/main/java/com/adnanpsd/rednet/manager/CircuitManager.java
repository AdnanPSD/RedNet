package com.adnanpsd.rednet.manager;

import com.adnanpsd.rednet.RedNet;
import com.adnanpsd.rednet.model.Circuit;
import com.adnanpsd.rednet.model.CircuitType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class CircuitManager {

    private final RedNet plugin;
    private final Map<Location, List<Circuit>> circuits;

    public CircuitManager(RedNet plugin) {
        this.plugin = plugin;
        this.circuits = new HashMap<>();
        loadCircuitsFromDatabase();
    }

    private void loadCircuitsFromDatabase() {
        plugin.getLogger().info("Devreler veritabanından yükleniyor...");
        int count = 0;

        String query = "SELECT * FROM circuits;";
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID id = UUID.fromString(resultSet.getString("id"));
                UUID owner = UUID.fromString(resultSet.getString("owner_uuid"));
                CircuitType type = CircuitType.fromString(resultSet.getString("circuit_type"));
                double delay = resultSet.getDouble("delay");

                World senderWorld = Bukkit.getWorld(resultSet.getString("sender_world"));
                World receiverWorld = Bukkit.getWorld(resultSet.getString("receiver_world"));

                if (senderWorld == null || receiverWorld == null) continue;

                Location sender = new Location(senderWorld, resultSet.getInt("sender_x"), resultSet.getInt("sender_y"), resultSet.getInt("sender_z"));
                Location receiver = new Location(receiverWorld, resultSet.getInt("receiver_x"), resultSet.getInt("receiver_y"), resultSet.getInt("receiver_z"));

                Circuit circuit = new Circuit(id, owner, type, delay, sender, receiver);
                cacheCircuit(circuit);
                count++;
            }
            plugin.getLogger().info(count + " adet devre başarıyla yüklendi.");

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Devreler yüklenirken hata oluştu!", e);
        }
    }

    public void addCircuit(Circuit circuit) {
        cacheCircuit(circuit);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "INSERT INTO circuits (id, owner_uuid, circuit_type, delay, sender_world, sender_x, sender_y, sender_z, receiver_world, receiver_x, receiver_y, receiver_z) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (Connection connection = plugin.getDatabaseManager().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, circuit.getId().toString());
                statement.setString(2, circuit.getOwner().toString());
                statement.setString(3, circuit.getType().name());
                statement.setDouble(4, circuit.getDelay());
                statement.setString(5, circuit.getSender().getWorld().getName());
                statement.setInt(6, circuit.getSender().getBlockX());
                statement.setInt(7, circuit.getSender().getBlockY());
                statement.setInt(8, circuit.getSender().getBlockZ());
                statement.setString(9, circuit.getReceiver().getWorld().getName());
                statement.setInt(10, circuit.getReceiver().getBlockX());
                statement.setInt(11, circuit.getReceiver().getBlockY());
                statement.setInt(12, circuit.getReceiver().getBlockZ());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Devre veritabanına kaydedilemedi!", e);
            }
        });
    }

    // YENİ EKLENEN: Devrenin türünü veya gecikmesini veritabanında günceller
    public void updateCircuit(Circuit circuit) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "UPDATE circuits SET circuit_type = ?, delay = ? WHERE id = ?;";
            try (Connection connection = plugin.getDatabaseManager().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, circuit.getType().name());
                statement.setDouble(2, circuit.getDelay());
                statement.setString(3, circuit.getId().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Devre güncellenirken hata oluştu!", e);
            }
        });
    }

    // YENİ EKLENEN: Bir oyuncunun sahip olduğu tüm devreleri listeler (Ana menü için)
    public List<Circuit> getCircuitsByOwner(UUID ownerUuid) {
        List<Circuit> result = new ArrayList<>();
        for (List<Circuit> list : circuits.values()) {
            for (Circuit c : list) {
                if (c.getOwner().equals(ownerUuid)) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    private void cacheCircuit(Circuit circuit) {
        circuits.computeIfAbsent(circuit.getSender(), k -> new ArrayList<>()).add(circuit);
    }

    public List<Circuit> getCircuitsBySender(Location senderLocation) {
        return circuits.getOrDefault(senderLocation.getBlock().getLocation(), new ArrayList<>());
    }

    public List<Circuit> getCircuitsInvolvingBlock(Location blockLoc) {
        Location targetLoc = blockLoc.getBlock().getLocation();
        List<Circuit> found = new ArrayList<>();
        for (List<Circuit> circuitList : circuits.values()) {
            for (Circuit circuit : circuitList) {
                if (circuit.getSender().equals(targetLoc) || circuit.getReceiver().equals(targetLoc)) {
                    found.add(circuit);
                }
            }
        }
        return found;
    }

    public boolean isReceiver(Location blockLoc) {
        Location targetLoc = blockLoc.getBlock().getLocation();
        for (List<Circuit> circuitList : circuits.values()) {
            for (Circuit circuit : circuitList) {
                if (circuit.getReceiver().equals(targetLoc)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeCircuit(Circuit circuit) {
        List<Circuit> senderList = circuits.get(circuit.getSender());
        if (senderList != null) {
            senderList.remove(circuit);
            if (senderList.isEmpty()) {
                circuits.remove(circuit.getSender());
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "DELETE FROM circuits WHERE id = ?;";
            try (Connection connection = plugin.getDatabaseManager().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, circuit.getId().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Devre veritabanından silinirken hata oluştu!", e);
            }
        });
    }

    public boolean removeCircuitsAssociatedWithBlock(Location blockLoc) {
        Location targetLoc = blockLoc.getBlock().getLocation();
        List<Circuit> toRemove = new ArrayList<>();
        for (List<Circuit> circuitList : circuits.values()) {
            for (Circuit circuit : circuitList) {
                if (circuit.getSender().equals(targetLoc) || circuit.getReceiver().equals(targetLoc)) {
                    toRemove.add(circuit);
                }
            }
        }
        if (toRemove.isEmpty()) return false;
        
        for (Circuit circuit : toRemove) {
            List<Circuit> senderList = circuits.get(circuit.getSender());
            if (senderList != null) {
                senderList.remove(circuit);
                if (senderList.isEmpty()) circuits.remove(circuit.getSender());
            }
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "DELETE FROM circuits WHERE id = ?;";
            try (Connection connection = plugin.getDatabaseManager().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                for (Circuit circuit : toRemove) {
                    statement.setString(1, circuit.getId().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Devreler veritabanından silinirken hata oluştu!", e);
            }
        });
        return true;
    }
}