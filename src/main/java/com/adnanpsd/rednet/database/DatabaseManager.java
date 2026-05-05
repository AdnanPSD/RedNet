package com.adnanpsd.rednet.database;

import com.adnanpsd.rednet.RedNet;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {

    private final RedNet plugin;
    private Connection connection;
    private final String type;

    public DatabaseManager(RedNet plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("storage.type", "SQLITE").toUpperCase();
        connect();
        setupTable();
    }

    private void connect() {
        try {
            if (type.equals("MYSQL")) {
                ConfigurationSection mysqlConfig = plugin.getConfig().getConfigurationSection("storage.mysql");
                if (mysqlConfig != null) {
                    String host = mysqlConfig.getString("host", "localhost");
                    int port = mysqlConfig.getInt("port", 3306);
                    String database = mysqlConfig.getString("database", "rednet");
                    String username = mysqlConfig.getString("username", "root");
                    String password = mysqlConfig.getString("password", "");

                    String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
                    connection = DriverManager.getConnection(url, username, password);
                    plugin.getLogger().info("MySQL veritabanına başarıyla bağlanıldı.");
                } else {
                    plugin.getLogger().warning("MySQL yapılandırması bulunamadı! SQLite'a geçiliyor.");
                    connectSQLite();
                }
            } else {
                connectSQLite();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Veritabanına bağlanılamadı!", e);
        }
    }

    private void connectSQLite() throws SQLException {
        File dataFolder = new File(plugin.getDataFolder(), "database.db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.getParentFile().mkdirs();
                dataFolder.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite veritabanı dosyası oluşturulamadı!", e);
            }
        }
        String url = "jdbc:sqlite:" + dataFolder;
        connection = DriverManager.getConnection(url);
        plugin.getLogger().info("SQLite veritabanına başarıyla bağlanıldı.");
    }

    private void setupTable() {
        if (connection == null) return;

        String query = "CREATE TABLE IF NOT EXISTS circuits (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "owner_uuid VARCHAR(36), " +
                "circuit_type VARCHAR(32), " +
                "delay DOUBLE, " +
                "sender_world VARCHAR(64), sender_x INT, sender_y INT, sender_z INT, " +
                "receiver_world VARCHAR(64), receiver_x INT, receiver_y INT, receiver_z INT" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Tablo oluşturulurken bir hata meydana geldi!", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Bağlantı durumu kontrol edilemedi!", e);
        }
        return connection;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Veritabanı bağlantısı kapatıldı.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Veritabanı bağlantısı kapatılırken hata oluştu!", e);
            }
        }
    }
}