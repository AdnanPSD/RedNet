package com.adnanpsd.rednet.manager;

import com.adnanpsd.rednet.RedNet;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {

    private final RedNet plugin;
    private FileConfiguration langConfig;
    private String prefix;

    public MessageManager(RedNet plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        String lang = plugin.getConfig().getString("language", "tr_TR");
        File langFile = new File(plugin.getDataFolder() + File.separator + "lang", lang + ".yml");

        if (!langFile.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
        prefix = langConfig.getString("prefix", "&8[&4RedNet&8] &7");
    }

    public String getMessage(String path) {
        String message = langConfig.getString(path);
        if (message == null) {
            return "Mesaj bulunamadı: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    // YENİ EKLENEN: Menülerdeki çok satırlı açıklamalar (Lore) için liste çeken metod
    public List<String> getMessageList(String path) {
        List<String> list = langConfig.getStringList(path);
        List<String> coloredList = new ArrayList<>();
        for (String s : list) {
            coloredList.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return coloredList;
    }

    // MAVEN HATASINI ÇÖZEN METOD: EditMenu.java'nın config dosyasına doğrudan erişebilmesi için
    public FileConfiguration getConfig() {
        return langConfig;
    }
}