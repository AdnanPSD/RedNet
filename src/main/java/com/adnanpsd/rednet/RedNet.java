package com.adnanpsd.rednet;

import com.adnanpsd.rednet.command.RedNetCommand;
import com.adnanpsd.rednet.database.DatabaseManager;
import com.adnanpsd.rednet.listener.BlockClickListener;
import com.adnanpsd.rednet.listener.BlockDestroyListener;
import com.adnanpsd.rednet.listener.BlockPhysicsListener;
import com.adnanpsd.rednet.listener.RedstoneListener;
import com.adnanpsd.rednet.manager.CircuitManager;
import com.adnanpsd.rednet.manager.MessageManager;
import com.adnanpsd.rednet.manager.SessionManager;
import com.adnanpsd.rednet.manager.VisualizerManager;
import com.adnanpsd.rednet.menu.EditMenu;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class RedNet extends JavaPlugin {

    private static RedNet instance;
    private Logger log;

    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private CircuitManager circuitManager;
    private SessionManager sessionManager;
    private VisualizerManager visualizerManager;
    
    private RedstoneListener redstoneListener;
    private EditMenu editMenu;

    @Override
    public void onEnable() {
        instance = this;
        log = this.getLogger();

        saveDefaultConfig();

        log.info("=================================");
        log.info("RedNet v1.0.0 Aktif Ediliyor...");
        log.info("Yazar: AdnanPSD");
        log.info("Sürüm: 1.21.1");
        log.info("=================================");

        // Sistem Yöneticilerini Başlatma
        this.messageManager = new MessageManager(this);
        log.info("Dil sistemi yüklendi: " + getConfig().getString("language"));

        this.databaseManager = new DatabaseManager(this);
        this.circuitManager = new CircuitManager(this);
        this.sessionManager = new SessionManager();
        this.visualizerManager = new VisualizerManager(this);

        // YENİ: EditMenu'yü Başlatma
        this.editMenu = new EditMenu(this);

        // Event (Olay) Dinleyicilerini Kaydetme
        this.redstoneListener = new RedstoneListener(this);
        Bukkit.getPluginManager().registerEvents(new BlockClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.redstoneListener, this);
        Bukkit.getPluginManager().registerEvents(new BlockDestroyListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockPhysicsListener(this), this);
        
        // Eğer EditMenu bir Listener (tıklama olaylarını dinliyorsa) ise onu da güvenlice kaydet
        if (this.editMenu instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this.editMenu, this);
        }

        // Komutları Kaydetme
        RedNetCommand redNetCommand = new RedNetCommand(this);
        getCommand("rednet").setExecutor(redNetCommand);
        getCommand("rednet").setTabCompleter(redNetCommand);
    }

    @Override
    public void onDisable() {
        log.info("RedNet devre dışı bırakılıyor...");
        
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        
        log.info("RedNet başarıyla kapatıldı.");
    }

    public static RedNet getInstance() {
        return instance;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CircuitManager getCircuitManager() {
        return circuitManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public VisualizerManager getVisualizerManager() {
        return visualizerManager;
    }

    public RedstoneListener getRedstoneListener() {
        return redstoneListener;
    }

    // YENİ EKLENEN: Komut sınıfının ulaşabilmesi için getter
    public EditMenu getEditMenu() {
        return editMenu;
    }
}