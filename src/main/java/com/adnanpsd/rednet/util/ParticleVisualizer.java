package com.adnanpsd.rednet.util;

import com.adnanpsd.rednet.RedNet;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ParticleVisualizer {

    public static void drawLine(RedNet plugin, Location point1, Location point2, int seconds) {
        // Lokasyonları tam blokların ortasına hizala
        Location start = point1.clone().add(0.5, 0.5, 0.5);
        Location end = point2.clone().add(0.5, 0.5, 0.5);

        // İki nokta arasındaki yönü ve mesafeyi bul
        Vector vector = end.toVector().subtract(start.toVector());
        double distance = vector.length();
        vector.normalize();

        // Parçacık rengi (Kırmızı)
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0F);

        // Parçacıkları 3 saniye boyunca saniyede 4 kez (5 tick'te bir) yenile
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = seconds * 20;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    this.cancel();
                    return;
                }

                // Çizgi üzerinde belirli aralıklarla parçacık oluştur
                for (double i = 0; i <= distance; i += 0.5) {
                    Location particleLoc = start.clone().add(vector.clone().multiply(i));
                    start.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dustOptions);
                }

                ticks += 5; // Her çalışmada 5 tick ekle
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 5L);
    }
}