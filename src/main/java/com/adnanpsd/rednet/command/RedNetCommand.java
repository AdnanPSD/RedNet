package com.adnanpsd.rednet.command;

import com.adnanpsd.rednet.RedNet;
import com.adnanpsd.rednet.model.Circuit;
import com.adnanpsd.rednet.model.CircuitType;
import com.adnanpsd.rednet.model.SetupSession;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RedNetCommand implements CommandExecutor, TabCompleter {

    private final RedNet plugin;

    public RedNetCommand(RedNet plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            sendHelpMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("help") || subCommand.equals("?")) {
            sendHelpMenu(player);
            return true;
        }

        if (subCommand.equals("cancel") || subCommand.equals("abort")) {
            if (plugin.getSessionManager().hasSession(uuid)) {
                plugin.getSessionManager().removeSession(uuid);
                player.sendMessage(plugin.getMessageManager().getMessage("setup.cancelled"));
            } else {
                player.sendMessage(plugin.getMessageManager().getMessage("setup.no-session"));
            }
            return true;
        }
        
        if (subCommand.equals("info")) {
            Block targetBlock = player.getTargetBlockExact(5);
            
            if (targetBlock == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("info.no-block"));
                return true;
            }

            List<Circuit> involving = plugin.getCircuitManager().getCircuitsInvolvingBlock(targetBlock.getLocation());
            if (involving.isEmpty()) {
                player.sendMessage(plugin.getMessageManager().getMessage("info.no-circuit"));
                return true;
            }

            String foundMsg = plugin.getMessageManager().getMessage("info.found").replace("%count%", String.valueOf(involving.size()));
            player.sendMessage(foundMsg);

            for (Circuit c : involving) {
                plugin.getVisualizerManager().showCircuit(c);
            }
            return true;
        }

        // HATAYI ÇÖZEN KISIM: Edit komutu düzeltildi
        if (subCommand.equals("edit")) {
            Block targetBlock = player.getTargetBlockExact(5);
            
            if (targetBlock == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("info.no-block"));
                return true;
            }

            List<Circuit> involving = plugin.getCircuitManager().getCircuitsInvolvingBlock(targetBlock.getLocation());
            if (involving.isEmpty()) {
                player.sendMessage(plugin.getMessageManager().getMessage("info.no-circuit"));
                return true;
            }

            // Şimdilik blokta bulunan ilk devreyi düzenlemek için menüye yolluyoruz
            plugin.getEditMenu().open(player, involving.get(0));
            return true;
        }

        CircuitType type = CircuitType.fromString(subCommand);
        if (type == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("setup.invalid-type"));
            return true;
        }

        if (!player.hasPermission("rednet.create." + type.name().toLowerCase()) && !player.hasPermission("rednet.create.*")) {
            player.sendMessage(plugin.getMessageManager().getMessage("commands.no-permission"));
            return true;
        }

        double delay = 0.0;
        if (args.length > 1) {
            try {
                delay = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getMessageManager().getMessage("commands.invalid-syntax"));
                return true;
            }
        }

        SetupSession session = new SetupSession(uuid, type, delay);
        plugin.getSessionManager().startSession(uuid, session);

        String startMsg = plugin.getMessageManager().getMessage("setup.started").replace("%type%", type.name());
        player.sendMessage(startMsg);

        return true;
    }

    private void sendHelpMenu(Player player) {
        player.sendMessage(plugin.getMessageManager().getMessage("commands.help-header"));
        player.sendMessage(plugin.getMessageManager().getMessage("commands.help-create"));
        player.sendMessage(plugin.getMessageManager().getMessage("commands.help-cancel"));
        player.sendMessage(plugin.getMessageManager().getMessage("commands.help-info"));
        player.sendMessage(plugin.getMessageManager().getMessage("commands.help-types"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("help", "cancel", "info", "edit"));
            for (CircuitType type : CircuitType.values()) {
                subCommands.add(type.name().toLowerCase());
            }
            
            String currentArg = args[0].toLowerCase();
            for (String s : subCommands) {
                if (s.startsWith(currentArg)) {
                    completions.add(s);
                }
            }
        }
        
        return completions;
    }
}