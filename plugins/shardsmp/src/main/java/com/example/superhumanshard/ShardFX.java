package com.example.superhumanshard;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public final class ShardFX {
    private static final Color VELOCITY = Color.fromRGB(255, 255, 255);
    private static final Color VELOCITY2 = Color.fromRGB(235, 240, 245);
    private static final Color TITAN = Color.fromRGB(230, 0, 0);
    private static final Color TITAN2 = Color.fromRGB(255, 45, 45);
    private static final Color TIDE = Color.fromRGB(63, 208, 255);
    private static final Color TIDE2 = Color.fromRGB(170, 240, 255);
    private static final Color PHANTOM = Color.fromRGB(47, 168, 184);
    private static final Color PHANTOM2 = Color.fromRGB(150, 225, 232);
    private static final Color EMBER = Color.fromRGB(255, 122, 24);
    private static final Color EMBER2 = Color.fromRGB(255, 208, 70);
    private static final Color DRAGON = Color.fromRGB(155, 48, 255);
    private static final Color DRAGON2 = Color.fromRGB(220, 130, 255);
    private static final Color FROST = Color.fromRGB(205, 240, 255);
    private static final Color FROST2 = Color.fromRGB(255, 255, 255);
    private static final Color VERDANT = Color.fromRGB(63, 217, 90);
    private static final Color VERDANT2 = Color.fromRGB(180, 255, 150);
    private static final Color STORM = Color.fromRGB(64, 224, 208);
    private static final Color STORM2 = Color.fromRGB(150, 255, 240);
    private static final Color SCULK = Color.fromRGB(47, 107, 255);
    private static final Color SCULK2 = Color.fromRGB(110, 220, 235);
    private static final Color EVOKER = Color.fromRGB(160, 24, 24);
    private static final Color EVOKER2 = Color.fromRGB(255, 215, 90);
    private static volatile long lastLoad = 0L;
    private static volatile boolean enabled = true;
    private static volatile float size = 1.1F;

    private ShardFX() {
    }

    private static void reload() {
        long var0 = System.currentTimeMillis();
        if (var0 - lastLoad >= 5000L) {
            lastLoad = var0;

            try {
                Plugin var2 = Bukkit.getPluginManager().getPlugin("SuperhumanShard");
                if (var2 != null) {
                    FileConfiguration var3 = var2.getConfig();
                    enabled = var3.getBoolean("custom-particles", true);
                    size = (float)var3.getDouble("custom-particle-size", 1.1);
                }
            } catch (Throwable var4) {
            }
        }
    }

    private static boolean paletteFor(Particle var0, Color[] var1) {
        if (var0 == Particle.PORTAL) {
            var1[0] = VELOCITY;
            var1[1] = VELOCITY2;
            return true;
        } else if (var0 == Particle.ENCHANT) {
            var1[0] = VELOCITY;
            var1[1] = VELOCITY2;
            return true;
        } else if (var0 == Particle.CRIT) {
            var1[0] = TITAN;
            var1[1] = TITAN2;
            return true;
        } else if (var0 == Particle.SPLASH || var0 == Particle.BUBBLE) {
            var1[0] = TIDE;
            var1[1] = TIDE2;
            return true;
        } else if (var0 == Particle.CLOUD || var0 == Particle.SMOKE) {
            var1[0] = PHANTOM;
            var1[1] = PHANTOM2;
            return true;
        } else if (var0 == Particle.FLAME) {
            var1[0] = EMBER;
            var1[1] = EMBER2;
            return true;
        } else if (var0 == Particle.DRAGON_BREATH) {
            var1[0] = DRAGON;
            var1[1] = DRAGON2;
            return true;
        } else if (var0 == Particle.SNOWFLAKE) {
            var1[0] = FROST;
            var1[1] = FROST2;
            return true;
        } else if (var0 == Particle.HAPPY_VILLAGER) {
            var1[0] = VERDANT;
            var1[1] = VERDANT2;
            return true;
        } else if (var0 == Particle.ELECTRIC_SPARK) {
            var1[0] = STORM;
            var1[1] = STORM2;
            return true;
        } else if (var0 == Particle.SCULK_SOUL || var0 == Particle.SOUL) {
            var1[0] = SCULK;
            var1[1] = SCULK2;
            return true;
        } else if (var0 == Particle.TOTEM_OF_UNDYING) {
            var1[0] = EVOKER;
            var1[1] = EVOKER2;
            return true;
        } else {
            return false;
        }
    }

    private static boolean dust(World var0, Particle var1, Location var2, int var3, double var4, double var6, double var8) {
        reload();
        if (!enabled) {
            return false;
        }

        Color[] var10 = new Color[2];
        if (!paletteFor(var1, var10)) {
            return false;
        }

        try {
            int var11 = Math.max(1, var3);
            int var12 = var11 / 3;
            int var13 = var11 - var12;
            if (var3 <= 0) {
                var4 = 0.0;
                var6 = 0.0;
                var8 = 0.0;
            }

            if (var13 > 0) {
                var0.spawnParticle(Particle.DUST, var2, var13, var4, var6, var8, 0.0, new DustOptions(var10[0], size));
            }

            if (var12 > 0) {
                var0.spawnParticle(Particle.DUST, var2, var12, var4, var6, var8, 0.0, new DustOptions(var10[1], Math.max(0.6F, size * 0.7F)));
            }

            return true;
        } catch (Throwable var14) {
            return false;
        }
    }

    public static void spawn(World var0, Particle var1, Location var2, int var3, double var4, double var6, double var8, double var10) {
        if (!dust(var0, var1, var2, var3, var4, var6, var8)) {
            var0.spawnParticle(var1, var2, var3, var4, var6, var8, var10);
        }
    }

    public static void spawn(World var0, Particle var1, Location var2, int var3, double var4, double var6, double var8, double var10, Object var12) {
        if (var12 != null || !dust(var0, var1, var2, var3, var4, var6, var8)) {
            var0.spawnParticle(var1, var2, var3, var4, var6, var8, var10, var12);
        }
    }

}
