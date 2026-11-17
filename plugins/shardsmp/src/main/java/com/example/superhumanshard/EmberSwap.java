package com.example.superhumanshard;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class EmberSwap {
    private EmberSwap() {
    }

    private static Plugin plugin() {
        return Bukkit.getPluginManager().getPlugin("SuperhumanShard");
    }

    public static void cast(Player var0, Map<String, Long> var1, Set<String> var2) {
        Plugin var3 = plugin();
        FileConfiguration var4 = var3.getConfig();
        double var5 = Math.max(1.0, var4.getDouble("abilities.ember.swap-radius", 5.0));
        double var7 = var4.getDouble("abilities.ember.swap-x", 1970.0);
        double var9 = var4.getDouble("abilities.ember.swap-y", -55.0);
        double var11 = var4.getDouble("abilities.ember.swap-z", 1890.0);
        long var13 = Math.max(1L, var4.getLong("abilities.ember.swap-seconds", 60L));
        String var15 = var4.getString("abilities.ember.swap-world", "");
        Player var16 = null;
        double var17 = var5 * var5;
        Location var19 = var0.getLocation();

        for (Player var21 : var0.getWorld().getPlayers()) {
            if (!var21.getUniqueId().equals(var0.getUniqueId()) && !var21.isDead()) {
                double var22 = var21.getLocation().distanceSquared(var19);
                if (var22 <= var17) {
                    var17 = var22;
                    var16 = var21;
                }
            }
        }

        if (var16 == null) {
            String var27 = var0.getUniqueId() + ":EMBER2";
            var1.remove(var27);
            var2.remove(var27);
            var0.sendMessage("§cNo player within " + (int)var5 + " blocks.");
        } else {
            World var26 = var15 != null && !var15.isEmpty() ? Bukkit.getWorld(var15) : (World)Bukkit.getWorlds().get(0);
            if (var26 == null) {
                var26 = (World)Bukkit.getWorlds().get(0);
            }

            Location var28 = new Location(var26, var7, var9, var11);
            UUID var29 = var0.getUniqueId();
            UUID var23 = var16.getUniqueId();
            Location var24 = var0.getLocation().clone();
            Location var25 = var16.getLocation().clone();
            burst(var24);
            burst(var25);
            var0.teleport(var28);
            var16.teleport(var28);
            burst(var28);
            var0.sendMessage("§6You and §e" + var16.getName() + "§6 were pulled away. Returning in " + var13 + "s.");
            var16.sendMessage("§6" + var0.getName() + " pulled you away with Ember Arena. Returning in " + var13 + "s.");
            schedule(var3, var29, var24, var13 * 20L, 0);
            schedule(var3, var23, var25, var13 * 20L, 0);
        }
    }

    private static void burst(Location var0) {
        try {
            World var1 = var0.getWorld();
            if (var1 != null) {
                ShardFX.spawn(var1, Particle.FLAME, var0, 40, 0.5, 1.0, 0.5, 0.05);
            }
        } catch (Throwable var2) {
        }
    }

    private static void schedule(Plugin var0, UUID var1, Location var2, long var3, int var5) {
        Bukkit.getScheduler().runTaskLater(var0, () -> {
            Player var4 = Bukkit.getPlayer(var1);
            if (var4 != null && var4.isOnline()) {
                if (!var4.isDead()) {
                    burst(var4.getLocation());
                    var4.teleport(var2);
                    burst(var2);
                    var4.sendMessage("§6You returned from the Ember Arena.");
                }
            } else {
                if (var5 < 120) {
                    schedule(var0, var1, var2, 100L, var5 + 1);
                }
            }
        }, var3);
    }
}
