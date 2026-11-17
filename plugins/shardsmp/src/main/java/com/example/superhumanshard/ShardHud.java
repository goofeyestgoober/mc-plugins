package com.example.superhumanshard;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class ShardHud {
    private static final String ICON = "✦ ";

    private ShardHud() {
    }

    private static boolean own(ShardType var0) {
        String var1 = var0.name();
        return var1.equals("EMBER") || var1.equals("SCULK") || var1.equals("EVOKER");
    }

    static Component slot(TextColor var0, long var1) {
        TextComponent var3 = Component.text("✦ ", var0);
        if (var1 <= 0L) {
            return var3.append(Component.text("Ready!", NamedTextColor.GREEN));
        }

        long var4 = Math.max(1L, (var1 + 999L) / 1000L);
        return var3.append(Component.text(var4 + "s", NamedTextColor.WHITE));
    }

    static Component state(String var0, boolean var1, long var2, TextColor var4) {
        if (var0.equals("EVOKER1")) {
            return Component.text("", NamedTextColor.WHITE);
        } else if (var1) {
            TextComponent var5 = Component.text("✦ ", var4);
            return var5.append(Component.text("Active", NamedTextColor.GREEN));
        } else {
            return slot(var4, var2);
        }
    }

    static void timer(Player var0, ShardType var1, long var2) {
        if (!own(var1) && var2 <= var1.cooldownMs()) {
            var0.sendActionBar(slot(hudColor(var1), var2));
        }
    }

    static void ready(Player var0, ShardType var1) {
        if (!own(var1)) {
            var0.sendActionBar(slot(hudColor(var1), 0L));
        }
    }

    private static ShardV25Main core() {
        Plugin var0 = Bukkit.getPluginManager().getPlugin("SuperhumanShard");
        return var0 instanceof ShardV25Main ? (ShardV25Main)var0 : null;
    }

    private static boolean a2Unlocked(ShardV25Main var0, Player var1, ShardType var2) {
        if (var0 != null && var2 != null && var0.tiers != null && ShardV25Main.isNormal(var2)) {
            try {
                return var0.tiers.tier(var1.getUniqueId(), var2) >= 2;
            } catch (Throwable var4) {
                return false;
            }
        } else {
            return false;
        }
    }

    private static long a2Left(ShardV25Main var0, Player var1, ShardType var2) {
        Long var3 = var0.cd2.get(var1.getUniqueId() + ":" + var2.id());
        return var3 == null ? 0L : Math.max(0L, var3 - System.currentTimeMillis());
    }

    private static Component both(TextColor var0, long var1, long var3) {
        return slot(var0, var1).append(Component.text("  ", NamedTextColor.DARK_GRAY)).append(slot(var0, var3));
    }

    static void tail(Player var0, List<ShardType> var1, ShardType var2, long var3, boolean var5) {
        if (!var5) {
            ShardV25Main var6 = core();
            boolean var7 = var1 != null && !var1.isEmpty();
            ShardType var8 = null;
            long var9 = 0L;
            if (var2 != null && var7 && var1.contains(var2)) {
                var8 = var2;
                var9 = var3;
            } else if (var7) {
                for (ShardType var12 : var1) {
                    if (a2Unlocked(var6, var0, var12) && a2Left(var6, var0, var12) > 0L) {
                        var8 = var12;
                        break;
                    }
                }
            }

            if (var8 == null) {
                if (var2 != null) {
                    var8 = var2;
                    var9 = var3;
                } else if (var7) {
                    var8 = (ShardType)var1.get(0);
                }
            }

            if (var8 != null && !own(var8) && var9 <= var8.cooldownMs()) {
                if (a2Unlocked(var6, var0, var8)) {
                    var0.sendActionBar(both(hudColor(var8), var9, a2Left(var6, var0, var8)));
                } else {
                    var0.sendActionBar(slot(hudColor(var8), var9));
                }
            }
        }
    }

    static TextColor hudColor(ShardType var0) {
        return (TextColor)(var0 == ShardType.STORM ? TextColor.color(4251856) : var0.color());
    }
}
