package com.adnanpsd.rednet.listener;

import com.adnanpsd.rednet.RedNet;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockDestroyListener implements Listener {

    private final RedNet plugin;

    public BlockDestroyListener(RedNet plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        // Eğer bu bloğa bağlı devreler varsa ve başarıyla silindiyse oyuncuya mesaj gönder
        boolean removed = plugin.getCircuitManager().removeCircuitsAssociatedWithBlock(block.getLocation());
        
        if (removed) {
            event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("circuit.broken"));
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            plugin.getCircuitManager().removeCircuitsAssociatedWithBlock(block.getLocation());
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            plugin.getCircuitManager().removeCircuitsAssociatedWithBlock(block.getLocation());
        }
    }
}