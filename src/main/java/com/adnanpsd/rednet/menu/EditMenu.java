package com.adnanpsd.rednet.menu;

import com.adnanpsd.rednet.RedNet;
import com.adnanpsd.rednet.model.Circuit;
import com.adnanpsd.rednet.model.CircuitType;
import org.bukkit.Bukkit;
import org.bukkit.Location; // EKSİK OLAN IMPORT BURADA
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditMenu implements Listener {

    private final RedNet plugin;
    // Hangi oyuncunun hangi devreyi düzenlediğini RAM'de tutuyoruz
    private final Map<UUID, Circuit> openMenus;

    public EditMenu(RedNet plugin) {
        this.plugin = plugin;
        this.openMenus = new HashMap<>();
    }

    public void open(Player player, Circuit circuit) {
        String title = plugin.getMessageManager().getMessage("menu.edit-title");
        Inventory inv = Bukkit.createInventory(null, 36, title);

        // 1. Devre Türü Değiştirici (Slot 10)
        String typeName = plugin.getMessageManager().getMessage("menu.type-item").replace("%type%", circuit.getType().name());
        inv.setItem(10, createGuiItem(Material.COMPARATOR, typeName, "menu.type-lore"));

        // 2. Gecikme (Delay) Ayarı (Slot 12)
        String delayName = plugin.getMessageManager().getMessage("menu.delay-item").replace("%delay%", String.valueOf(circuit.getDelay()));
        inv.setItem(12, createGuiItem(Material.CLOCK, delayName, "menu.delay-lore"));

        // 3. Uzaktan Tetikleyici (Slot 14)
        String triggerName = plugin.getMessageManager().getMessage("menu.trigger-item");
        inv.setItem(14, createGuiItem(Material.LEVER, triggerName, "menu.trigger-lore"));

        // 4. Bağlantı Gösterici / Visualizer (Slot 16)
        String visName = plugin.getMessageManager().getMessage("menu.visualize-item");
        inv.setItem(16, createGuiItem(Material.ENDER_EYE, visName, "menu.visualize-lore"));

        // 5. Vericiye (Sender) Işınlan (Slot 21)
        String tpSenderName = plugin.getMessageManager().getMessage("menu.teleport-sender");
        inv.setItem(21, createGuiItem(Material.COMPASS, tpSenderName, null));

        // 6. Alıcıya (Receiver) Işınlan (Slot 23)
        String tpReceiverName = plugin.getMessageManager().getMessage("menu.teleport-receiver");
        inv.setItem(23, createGuiItem(Material.RECOVERY_COMPASS, tpReceiverName, null));

        // 7. Devreyi Sil (Slot 31)
        String deleteName = plugin.getMessageManager().getMessage("menu.delete-item");
        inv.setItem(31, createGuiItem(Material.BARRIER, deleteName, "menu.delete-lore"));

        openMenus.put(player.getUniqueId(), circuit);
        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String lorePath) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lorePath != null) {
                meta.setLore(plugin.getMessageManager().getMessageList(lorePath));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // Eğer oyuncunun açık bir menüsü yoksa umursama
        if (!openMenus.containsKey(player.getUniqueId())) return;
        
        String title = plugin.getMessageManager().getMessage("menu.edit-title");
        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true); // Eşyaları çalmasını engelle
        if (event.getCurrentItem() == null) return;

        Circuit circuit = openMenus.get(player.getUniqueId());
        int slot = event.getSlot();

        switch (slot) {
            case 10: // Tür Değiştirme
                CircuitType[] types = CircuitType.values();
                int nextIndex = (circuit.getType().ordinal() + 1) % types.length;
                circuit.setType(types[nextIndex]);
                plugin.getCircuitManager().updateCircuit(circuit);
                player.sendMessage(plugin.getMessageManager().getMessage("circuit.updated"));
                open(player, circuit); // Menüyü yenile
                break;

            case 12: // Gecikme Ayarı
                double currentDelay = circuit.getDelay();
                if (event.getClick() == ClickType.LEFT) {
                    circuit.setDelay(currentDelay + 0.5);
                } else if (event.getClick() == ClickType.RIGHT) {
                    circuit.setDelay(Math.max(0.0, currentDelay - 0.5));
                }
                plugin.getCircuitManager().updateCircuit(circuit);
                player.sendMessage(plugin.getMessageManager().getMessage("circuit.updated"));
                open(player, circuit); // Menüyü yenile
                break;

            case 14: // Uzaktan Tetikleyici (Sanal Şalter)
                player.closeInventory();
                // Devreyi manuel olarak AÇ
                plugin.getRedstoneListener().processCircuit(circuit, true, true);
                
                // Düğme gibi çalışması için 10 tick (0.5 saniye) sonra otomatik KAPAT
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getRedstoneListener().processCircuit(circuit, false, false);
                }, 10L);
                break;

            case 16: // Bağlantıyı Göster
                player.closeInventory();
                plugin.getVisualizerManager().showCircuit(circuit);
                break;

            case 21: // Vericiye Işınlan
                player.closeInventory();
                // Bloğun tam ortasına ve üstüne ışınla
                Location senderLoc = circuit.getSender().clone().add(0.5, 1.0, 0.5);
                player.teleport(senderLoc);
                break;

            case 23: // Alıcıya Işınlan
                player.closeInventory();
                Location receiverLoc = circuit.getReceiver().clone().add(0.5, 1.0, 0.5);
                player.teleport(receiverLoc);
                break;

            case 31: // Devreyi Sil
                plugin.getCircuitManager().removeCircuit(circuit);
                player.closeInventory();
                player.sendMessage(plugin.getMessageManager().getMessage("circuit.removed"));
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Oyuncu menüyü kapatırsa RAM'den sil (Bellek sızıntısını önler)
        openMenus.remove(event.getPlayer().getUniqueId());
    }
}