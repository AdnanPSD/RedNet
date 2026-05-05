package com.adnanpsd.rednet.listener;

import com.adnanpsd.rednet.RedNet;
import com.adnanpsd.rednet.model.Circuit;
import com.adnanpsd.rednet.model.SetupSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class BlockClickListener implements Listener {

    private final RedNet plugin;

    public BlockClickListener(RedNet plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Eğer oyuncu bir devre kurma aşamasında değilse, işlemi atla
        if (!plugin.getSessionManager().hasSession(uuid)) {
            return;
        }

        // Sadece sol veya sağ tık ile blok etkileşimlerini kabul et
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        // Oyuncu kurulum aşamasında olduğu için bloğu kırmasını veya menüsünü açmasını engelliyoruz
        event.setCancelled(true); 

        SetupSession session = plugin.getSessionManager().getSession(uuid);
        Location clickedLocation = event.getClickedBlock().getLocation();

        if (session.getSenderLocation() == null) {
            // İLK TIKLAMA: Verici (Sender) bloğu ayarla
            session.setSenderLocation(clickedLocation);
            player.sendMessage(plugin.getMessageManager().getMessage("setup.sender-set"));
        } else {
            // İKİNCİ TIKLAMA: Alıcı (Receiver) bloğu ayarla ve devreyi oluştur
            Circuit circuit = new Circuit(
                    UUID.randomUUID(),
                    uuid,
                    session.getType(),
                    session.getDelay(),
                    session.getSenderLocation(),
                    clickedLocation
            );

            plugin.getCircuitManager().addCircuit(circuit);
            plugin.getSessionManager().removeSession(uuid);

            String successMsg = plugin.getMessageManager().getMessage("circuit.created")
                    .replace("%type%", session.getType().name());
            player.sendMessage(successMsg);
        }
    }
}