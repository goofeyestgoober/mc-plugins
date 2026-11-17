package com.example.superhumanshard;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

final class FrostBreath {
    static final double RANGE = 6.0;
    static final int DURATION_TICKS = 100;
    static final int SLOW_AMPLIFIER = 2;
    static final double DAMAGE = 1.0;
    static final int DAMAGE_INTERVAL_TICKS = 10;
    static final int SLOW_TICKS = 60;
    static final double START_WIDTH = 0.3;
    static final double END_WIDTH = 1.6;

    static void breath(final Player var0) {
        JavaPlugin var1 = JavaPlugin.getProvidingPlugin(FrostBreath.class);
        (new BukkitRunnable() {
            int ticks = 0;

            public void run() {
                if (this.ticks < 100 && var0.isOnline() && !var0.isDead()) {
                    Location var1x = var0.getEyeLocation();
                    World var2 = var1x.getWorld();
                    Vector var3 = var1x.getDirection();
                    double var4 = var3.getX();
                    double var6 = var3.getY();
                    double var8 = var3.getZ();
                    double var10 = var1x.getX() + var4 * 0.3;
                    double var12 = var1x.getY() - 0.25 + var6 * 0.3;
                    double var14 = var1x.getZ() + var8 * 0.3;

                    for (double var16 = 0.4; var16 <= 6.0; var16 += 0.4) {
                        double var18 = 0.3 + 1.3 * (var16 / 6.0);
                        Location var20 = new Location(var2, var10 + var4 * var16, var12 + var6 * var16, var14 + var8 * var16);
                        ShardFX.spawn(var2, Particle.SNOWFLAKE, var20, 2, var18 * 0.35, var18 * 0.35, var18 * 0.35, 0.01);
                    }

                    if (this.ticks % 2 == 0) {
                        Location var36 = new Location(var2, var10 + var4 * 6.0 * 0.5, var12 + var6 * 6.0 * 0.5, var14 + var8 * 6.0 * 0.5);
                        double var17 = 5.0;

                        for (Entity var37 : var2.getNearbyEntities(var36, var17, var17, var17)) {
                            if (var37 instanceof Player var21 && !var21.equals(var0) && !var21.isDead() && var21.getGameMode() != GameMode.SPECTATOR) {
                                Location var22 = var21.getLocation();
                                double var23 = var22.getX() - var10;
                                double var25 = var22.getY() + 0.9 - var12;
                                double var27 = var22.getZ() - var14;
                                double var29 = var23 * var4 + var25 * var6 + var27 * var8;
                                if (!(var29 < 0.0) && !(var29 > 6.5)) {
                                    double var31 = var23 * var23 + var25 * var25 + var27 * var27 - var29 * var29;
                                    double var33 = 0.3 + 1.3 * (var29 / 6.0) + 0.5;
                                    if (var31 <= var33 * var33) {
                                        var21.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, true, true));
                                        if (this.ticks % 10 == 0) {
                                            PierceDamage.hit(var21, 1.0, var0);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (this.ticks == 0) {
                        try {
                            var2.playSound(var0.getLocation(), "entity.ender_dragon.growl", 2.0F, 0.5F);
                        } catch (Throwable var35) {
                        }
                    }

                    this.ticks++;
                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(var1, 0L, 1L);
    }
}
