package com.adnanpsd.rednet.manager;

import com.adnanpsd.rednet.RedNet;
import com.adnanpsd.rednet.model.Circuit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VisualizerManager {

    private final RedNet plugin;

    public VisualizerManager(RedNet plugin) {
        this.plugin = plugin;
    }

    public void showCircuit(Circuit circuit) {
        // Blokların tam ortasından çizgi başlatmak için x,y,z koordinatlarına 0.5 ekliyoruz
        Location start = circuit.getSender().clone().add(0.5, 0.5, 0.5);
        Location end = circuit.getReceiver().clone().add(0.5, 0.5, 0.5);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // 5 saniye = 100 tick. Her 5 tickte bir çizgiyi yenileyeceğiz.
                if (ticks > 100) {
                    this.cancel();
                    return;
                }

                drawLine(start.clone(), end.clone());
                ticks += 5; // Her çalışmada 5 tick ekle
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void drawLine(Location point1, Location point2) {
        World world = point1.getWorld();
        if (world == null || !world.equals(point2.getWorld())) {
            return;
        }

        double distance = point1.distance(point2);
        // İki nokta arasındaki yön vektörünü hesapla
        Vector vector = point2.toVector().subtract(point1.toVector()).normalize().multiply(0.5);

        // Kırmızı renkli, normal boyutlu bir kızıltaş parçacığı ayarı
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0F);

        // Mesafeyi 0.5 blok aralıklarla dolaş ve her adıma parçacık koy
        for (double length = 0; length < distance; length += 0.5) {
            world.spawnParticle(Particle.DUST, point1, 1, 0, 0, 0, 0, dustOptions);
            point1.add(vector);
        }
    }
}