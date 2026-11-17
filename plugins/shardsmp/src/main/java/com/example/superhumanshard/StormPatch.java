package com.example.superhumanshard;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

final class StormPatch {
    static final double RADIUS = 2.0;
    static final double DAMAGE = 4.0;
    static final int STUN_TICKS = 60;
    static final int SLOW_AMPLIFIER = 49;

    static void stormStrike(Player var0, Location var1) {
        World var2 = var1.getWorld();
        var2.strikeLightningEffect(var1);

        for (int var3 = 0; var3 < 4; var3++) {
            double var4 = (Math.PI / 2) * var3;
            var2.strikeLightningEffect(new Location(var2, var1.getX() + Math.cos(var4) * 2.0, var1.getY(), var1.getZ() + Math.sin(var4) * 2.0));
        }

        for (int var10 = 0; var10 < 48; var10++) {
            double var12 = (Math.PI * 2) * var10 / 48.0;
            ShardFX.spawn(
                var2,
                Particle.ELECTRIC_SPARK,
                new Location(var2, var1.getX() + Math.cos(var12) * 2.0, var1.getY() + 0.2, var1.getZ() + Math.sin(var12) * 2.0),
                6,
                0.1,
                0.4,
                0.1,
                0.05
            );
        }

        ShardFX.spawn(var2, Particle.ELECTRIC_SPARK, new Location(var2, var1.getX(), var1.getY() + 0.5, var1.getZ()), 120, 1.0, 0.6, 1.0, 0.1);

        for (Entity var13 : var2.getNearbyEntities(var1, 2.0, 4.0, 2.0)) {
            if (var13 instanceof LivingEntity var5 && !var5.equals(var0) && !var5.isDead()) {
                double var6 = var5.getLocation().getX() - var1.getX();
                double var8 = var5.getLocation().getZ() - var1.getZ();
                if (!(var6 * var6 + var8 * var8 > 4.0)) {
                    PierceDamage.hit(var5, 4.0, var0);
                    if (var5 instanceof Player) {
                        stun((Player)var5);
                    }
                }
            }
        }
    }

    static void stun(final Player var0) {
        if (var0.getGameMode() != GameMode.SPECTATOR) {
            ShardFreeze.freeze(var0);
            final Location var1 = var0.getLocation().clone();
            final boolean var2 = var0.isOnGround();
            (new BukkitRunnable() {
                int ticks = 0;

                public void run() {
                    if (this.ticks++ < 60 && var0.isOnline() && !var0.isDead()) {
                        Location var1x = var0.getLocation();
                        if (var2) {
                            double var2x = var1x.getX() - var1.getX();
                            double var4 = var1x.getZ() - var1.getZ();
                            boolean var6 = var2x * var2x + var4 * var4 > 0.0025 || var1x.getY() > var1.getY() + 0.05;
                            if (var6) {
                                Location var7 = var1.clone();
                                var7.setYaw(var1x.getYaw());
                                var7.setPitch(var1x.getPitch());
                                var0.teleport(var7);
                            }
                        } else {
                            Vector var8 = var0.getVelocity();
                            var0.setVelocity(new Vector(0.0, var8.getY(), 0.0));
                        }
                    } else {
                        this.cancel();
                    }
                }
            }).runTaskTimer(JavaPlugin.getProvidingPlugin(StormPatch.class), 1L, 1L);
        }
    }
}
