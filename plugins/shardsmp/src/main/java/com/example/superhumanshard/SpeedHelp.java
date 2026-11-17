package com.example.superhumanshard;

import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

final class SpeedHelp {
    private SpeedHelp() {
    }

    static boolean isSpeedItem(Plugin var0, ItemStack var1) {
        return var1 != null && var1.getType() == Material.FEATHER && var1.hasItemMeta()
            ? var1.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(var0, "speed_shard"), PersistentDataType.BYTE)
            : false;
    }

    static int heldTier(ShardV29Main var0, Player var1) {
        PlayerInventory var2 = var1.getInventory();
        int var3 = 0;
        ItemStack var4 = var2.getItemInMainHand();
        if (var0.isSpeed(var4)) {
            var3 = Math.max(var3, var0.tierOf(var4));
        }

        ItemStack var5 = var2.getItemInOffHand();
        if (var0.isSpeed(var5)) {
            var3 = Math.max(var3, var0.tierOf(var5));
        }

        return var3;
    }

    static boolean holdsOtherShard(ShardV29Main var0, Player var1) {
        PlayerInventory var2 = var1.getInventory();
        ItemStack var3 = var2.getItemInMainHand();
        if (var3 != null) {
            if (var0.isSpeed(var3)) {
                return false;
            }

            if (var0.getShardType(var3) != null) {
                return true;
            }
        }

        ItemStack var4 = var2.getItemInOffHand();
        if (var4 != null) {
            if (var0.isSpeed(var4)) {
                return false;
            }

            if (var0.getShardType(var4) != null) {
                return true;
            }
        }

        return false;
    }

    static void refresh(ShardV29Main var0, Player var1) {
        PlayerInventory var2 = var1.getInventory();
        ItemStack[] var3 = var2.getContents();
        NamespacedKey var4 = new NamespacedKey(var0, "speed_lore");

        for (int var5 = 0; var5 < var3.length; var5++) {
            ItemStack var6 = var3[var5];
            if (var0.isSpeed(var6)) {
                ItemMeta var7 = var6.getItemMeta();
                if (var7 == null || !var7.getPersistentDataContainer().has(var4, PersistentDataType.BYTE)) {
                    var0.writeSpeed(var6, var0.tierOf(var6), var0.victimsOf(var6));
                    var2.setItem(var5, var6);
                }
            }
        }
    }

    private static String roman(int var0) {
        String[] var1 = new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return var0 >= 0 && var0 < var1.length ? var1[var0] : String.valueOf(var0);
    }

    static void applyLore(ShardV29Main var0, ItemMeta var1, int var2, int var3, int var4, int var5) {
        long var6 = Math.max(1L, var0.getConfig().getLong("speed-shard.dash-cooldown", 20L));
        long var8 = Math.max(1L, var0.getConfig().getLong("speed-shard.surge-cooldown", 30L));
        var1.displayName(ShardLore.bold("SPEED", NamedTextColor.YELLOW).append(ShardLore.line(" SHARD", NamedTextColor.WHITE)));
        ArrayList var10 = new ArrayList();
        var10.add(ShardLore.line("(Tier " + var2 + ")", NamedTextColor.LIGHT_PURPLE));
        var10.add(ShardLore.bold("FASTER THAN THE WIND", NamedTextColor.WHITE));
        var10.add(ShardLore.blank());
        var10.add(ShardLore.header("PASSIVES"));
        var10.add(ShardLore.bullet(ShardLore.line("Speed " + roman(var3), NamedTextColor.GRAY)));
        Component var11 = ShardLore.line("Speed " + roman(var4) + " + Haste " + roman(var5), NamedTextColor.GRAY)
            .append(ShardLore.line("  (Tier 3 · " + var0.need3() + " kills)", NamedTextColor.DARK_GRAY));
        if (var2 >= 3) {
            var11 = var11.append(ShardLore.line(" ✔", NamedTextColor.GREEN));
        }

        var10.add(ShardLore.bullet(var11));
        var10.add(ShardLore.blank());
        var10.add(ShardLore.header("ABILITY"));
        var10.add(ShardLore.bullet(ShardLore.line("Dash", NamedTextColor.GRAY)));
        var10.add(ShardLore.blank());
        var10.add(ShardLore.header("POWERS"));
        var10.add(
            ShardLore.bullet(
                ShardLore.bold("DASH", NamedTextColor.WHITE)
                    .append(ShardLore.line("  /ability1", NamedTextColor.YELLOW))
                    .append(ShardLore.line("  " + var6 + "s", NamedTextColor.DARK_GRAY))
            )
        );
        Component var12 = ShardLore.bold("SPEED SURGE", NamedTextColor.WHITE)
            .append(ShardLore.line("  /ability2", NamedTextColor.YELLOW))
            .append(ShardLore.line("  " + var8 + "s", NamedTextColor.DARK_GRAY));
        if (var2 >= 2) {
            var12 = var12.append(ShardLore.line(" ✔", NamedTextColor.GREEN));
        } else {
            var12 = var12.append(ShardLore.line(" ✖ Tier 2 · " + var0.need2() + " kills", NamedTextColor.RED));
        }

        var10.add(ShardLore.bullet(var12));
        var1.lore(var10);
        var1.getPersistentDataContainer().set(new NamespacedKey(var0, "speed_lore"), PersistentDataType.BYTE, (byte)1);
    }
}
