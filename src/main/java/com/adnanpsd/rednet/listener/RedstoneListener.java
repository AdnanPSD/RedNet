package com.adnanpsd.rednet.listener;

import com.adnanpsd.rednet.RedNet;
import com.adnanpsd.rednet.model.Circuit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.List;
import java.util.Random;

public class RedstoneListener implements Listener {

    private final RedNet plugin;
    private final Random random;

    public RedstoneListener(RedNet plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onRedstoneChange(BlockRedstoneEvent event) {
        Block senderBlock = event.getBlock();
        Location senderLoc = senderBlock.getLocation();

        List<Circuit> circuits = plugin.getCircuitManager().getCircuitsBySender(senderLoc);
        if (circuits == null || circuits.isEmpty()) {
            return;
        }

        boolean oldPower = event.getOldCurrent() > 0;
        boolean newPower = event.getNewCurrent() > 0;

        if (oldPower == newPower) {
            return;
        }

        boolean isTurningOn = !oldPower && newPower;

        for (Circuit circuit : circuits) {
            long delayTicks = (long) (circuit.getDelay() * 20L);

            if (delayTicks > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> processCircuit(circuit, newPower, isTurningOn), delayTicks);
            } else {
                processCircuit(circuit, newPower, isTurningOn);
            }
        }
    }

    public void triggerCircuits(Location senderLoc, boolean isTurningOn) {
        List<Circuit> circuits = plugin.getCircuitManager().getCircuitsBySender(senderLoc);
        if (circuits == null || circuits.isEmpty()) return;

        for (Circuit circuit : circuits) {
            long delayTicks = (long) (circuit.getDelay() * 20L);
            if (delayTicks > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> processCircuit(circuit, isTurningOn, isTurningOn), delayTicks);
            } else {
                processCircuit(circuit, isTurningOn, isTurningOn);
            }
        }
    }

    public void processCircuit(Circuit circuit, boolean isOn, boolean isTurningOn) {
        Location receiverLoc = circuit.getReceiver();
        Block receiverBlock = receiverLoc.getBlock();
        
        boolean currentReceiverState = getReceiverPower(receiverBlock);
        boolean newState = currentReceiverState;
        boolean updateNeeded = false;

        switch (circuit.getType()) {
            case QUANTUM:
                newState = isOn;
                updateNeeded = true;
                break;
            case REVERSE:
                newState = !isOn;
                updateNeeded = true;
                break;
            case ON:
                if (isTurningOn) {
                    newState = true;
                    updateNeeded = true;
                }
                break;
            case OFF:
                if (isTurningOn) {
                    newState = false;
                    updateNeeded = true;
                }
                break;
            case TOGGLE:
                if (isTurningOn) {
                    newState = !currentReceiverState;
                    updateNeeded = true;
                }
                break;
            case RANDOM:
                if (isTurningOn) {
                    newState = random.nextBoolean();
                    updateNeeded = true;
                }
                break;
            case IMPULSE:
                if (isTurningOn) {
                    setReceiverPower(receiverBlock, true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> setReceiverPower(receiverBlock, false), 4L);
                }
                return;
        }

        if (updateNeeded) {
            setReceiverPower(receiverBlock, newState);
        }
    }

    private boolean getReceiverPower(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Powerable) {
            return ((Powerable) data).isPowered();
        } else if (data instanceof Lightable) {
            return ((Lightable) data).isLit();
        } else if (data instanceof Openable) {
            return ((Openable) data).isOpen();
        } else if (data instanceof Switch) {
            return ((Switch) data).isPowered();
        }
        return false;
    }

    private void setReceiverPower(Block block, boolean power) {
        BlockData data = block.getBlockData();
        boolean updated = false;

        if (data instanceof Powerable) {
            ((Powerable) data).setPowered(power);
            updated = true;
        } else if (data instanceof Lightable) {
            ((Lightable) data).setLit(power);
            updated = true;
        } else if (data instanceof Openable) {
            ((Openable) data).setOpen(power);
            updated = true;
        } else if (data instanceof Switch) {
            ((Switch) data).setPowered(power);
            updated = true;
        }

        if (updated) {
            block.setBlockData(data);
            block.getState().update(true, true);
        }
    }
}