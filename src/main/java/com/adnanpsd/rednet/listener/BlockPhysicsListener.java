package com.adnanpsd.rednet.listener;

import com.adnanpsd.rednet.RedNet;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockPhysicsListener implements Listener {

    private final RedNet plugin;

    public BlockPhysicsListener(RedNet plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        
        // Eğer güncellenen blok bizim eklentimizin "Alıcısı" (Receiver) ise...
        if (plugin.getCircuitManager().isReceiver(block.getLocation())) {
            
            // Lamba, kapı, şalter veya güç alabilen bir bloksa fizik motorunu İPTAL ET
            if (block.getBlockData() instanceof Lightable || 
                block.getBlockData() instanceof Openable || 
                block.getBlockData() instanceof Powerable || 
                block.getBlockData() instanceof Switch) {
                
                // Minecraft'a diyoruz ki: "Bu bloğun gücüyle oynama, ben ayarlıyorum!"
                event.setCancelled(true);
            }
        }
    }
    
    // Bazen redstone kabloları fizik yerine doğrudan redstone akımı (RedstoneEvent) ile blokları günceller.
    // Alıcı bloğa dışarıdan gelen vanilla redstone sinyalini de engelliyoruz ki çakışma olmasın.
    @EventHandler
    public void onRedstoneReceive(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (plugin.getCircuitManager().isReceiver(block.getLocation())) {
            // Eğer blok bizim alıcımızsa, vanilla sistemin onu etkilemesini durdurmak için 
            // eski durumu neyse onu yeni durum olarak sabitliyoruz.
            event.setNewCurrent(event.getOldCurrent());
        }
    }
}