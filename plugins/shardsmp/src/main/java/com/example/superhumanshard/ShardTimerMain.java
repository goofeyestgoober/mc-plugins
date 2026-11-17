package com.example.superhumanshard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShardTimerMain extends ShardCraftingMain {
    private static final int BAR_SEGMENTS = 10;
    private Map<String, Long> cooldownMap;
    private final Set<String> waiting = new HashSet<>();
    private static final long DRAGON_SCALES_MS = 20000L;
    private Set<UUID> dragonActive;
    private final Map<UUID, Long> dragonStart = new HashMap<>();
    private boolean locatorFailed;
    private boolean gameRuleFailed;

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            Field var1 = SuperhumanShard.class.getDeclaredField("cooldowns");
            var1.setAccessible(true);
            this.cooldownMap = (Map<String, Long>)var1.get(this);
        } catch (ReflectiveOperationException var3) {
            this.getLogger().warning("Cooldown timer disabled: could not read the shard cooldowns.");
            return;
        }

        try {
            Field var4 = SuperhumanShard.class.getDeclaredField("dragonScalesActive");
            var4.setAccessible(true);
            this.dragonActive = (Set<UUID>)var4.get(this);
        } catch (ReflectiveOperationException var2) {
            this.getLogger().warning("Dragon ability effects disabled: could not read dragonScalesActive.");
        }

        Bukkit.getScheduler().runTaskTimer(this, this::tickTimers, 5L, 2L);
        Bukkit.getScheduler().runTaskTimer(this, this::tickLocator, 20L, 20L);
    }

    public void onDisable() {
        this.resetLocatorBars();
        super.onDisable();
    }

    private void tickLocator() {
        if (!this.locatorFailed) {
            try {
                if (!this.gameRuleFailed) {
                    try {
                        for (World var2 : Bukkit.getWorlds()) {
                            var2.setGameRule(GameRule.LOCATOR_BAR, Boolean.TRUE);
                        }
                    } catch (Throwable var6) {
                        this.gameRuleFailed = true;
                        this.getLogger().warning("Could not enable the locator bar game rule; run /gamerule locator_bar true yourself.");
                    }
                }

                for (Player var9 : Bukkit.getOnlinePlayers()) {
                    AttributeInstance var3 = var9.getAttribute(Attribute.WAYPOINT_RECEIVE_RANGE);
                    if (var3 != null) {
                        double var4 = this.hasShard(var9, ShardType.DRAGON) ? var3.getDefaultValue() : 0.0;
                        if (var3.getBaseValue() != var4) {
                            var3.setBaseValue(var4);
                        }
                    }
                }
            } catch (Throwable var7) {
                this.locatorFailed = true;
                this.getLogger().warning("Dragon locator bar disabled (needs Minecraft 1.21.6+): " + var7);
            }
        }
    }

    private void resetLocatorBars() {
        try {
            for (Player var2 : Bukkit.getOnlinePlayers()) {
                AttributeInstance var3 = var2.getAttribute(Attribute.WAYPOINT_RECEIVE_RANGE);
                if (var3 != null) {
                    var3.setBaseValue(var3.getDefaultValue());
                }
            }
        } catch (Throwable var4) {
        }
    }

    private void tickDragonEffects() {
        if (this.dragonActive != null) {
            long var1 = System.currentTimeMillis();
            this.dragonStart.keySet().removeIf(var1x -> !this.dragonActive.contains(var1x));

            for (UUID var4 : new ArrayList<>(this.dragonActive)) {
                long var5 = this.dragonStart.computeIfAbsent(var4, var2 -> var1);
                if (var1 - var5 >= 20000L) {
                    this.dragonActive.remove(var4);
                    this.dragonStart.remove(var4);
                }
            }

            if (!this.dragonActive.isEmpty()) {
                for (Player var8 : Bukkit.getOnlinePlayers()) {
                    if (this.dragonActive.contains(var8.getUniqueId())) {
                        var8.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 8, 2, true, false, true));
                        var8.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 8, 0, true, false, true));
                    }
                }
            }
        }
    }

    private void tickTimers() {
        this.tickDragonEffects();
        long var1 = System.currentTimeMillis();
        ShardType[] var3 = ShardType.values();

        for (Player var5 : Bukkit.getOnlinePlayers()) {
            String var6 = var5.getUniqueId() + ":";
            ArrayList var7 = new ArrayList();
            ShardType var8 = this.getShardType(var5.getInventory().getItemInMainHand());
            ShardType var9 = this.getShardType(var5.getInventory().getItemInOffHand());
            if (var8 != null) {
                var7.add(var8);
            }

            if (var9 != null && var9 != var8) {
                var7.add(var9);
            }

            ShardType var10 = null;
            long var11 = 0L;
            boolean var13 = false;
            boolean var14 = false;

            for (ShardType var18 : var3) {
                String var19 = var6 + var18.name();
                Long var20 = this.cooldownMap.get(var19);
                if (var20 != null) {
                    long var21 = var20 - var1;
                    if (var21 > 0L) {
                        this.waiting.add(var19);
                        boolean var23 = var7.contains(var18);
                        boolean var24 = var10 == null || var23 && !var13 || var23 == var13 && var21 > var11;
                        if (var24) {
                            var10 = var18;
                            var11 = var21;
                            var13 = var23;
                        }
                    } else if (this.waiting.remove(var19)) {
                        this.sendReady(var5, var18);
                        var14 = true;
                    }
                }
            }

            ShardHud.tail(var5, var7, var10, var11, var14);
        }
    }

    private void sendTimer(Player var1, ShardType var2, long var3) {
        ShardHud.timer(var1, var2, var3);
    }

    private void sendReady(Player var1, ShardType var2) {
        ShardHud.ready(var1, var2);
    }

    private static String formatTime(long var0) {
        if (var0 >= 60000L) {
            long var2 = var0 / 60000L;
            long var4 = var0 % 60000L / 1000L;
            return var2 + ":" + (var4 < 10L ? "0" : "") + var4;
        } else {
            return String.format(Locale.ROOT, "%.1fs", var0 / 1000.0);
        }
    }
}
