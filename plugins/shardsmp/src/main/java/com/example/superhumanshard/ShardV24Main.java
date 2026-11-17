package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShardV24Main extends ShardV23Main {
    private static final String[] START_POOL = new String[]{"velocity", "titan", "tide", "phantom", "frost", "verdant", "storm"};
    private static final String[] NO_REROLL = new String[]{"dragon", "sculk", "ember", "evoker"};

    @Override
    public void onEnable() {
        super.onEnable();
        this.getConfig().set("starter-shard.enabled", false);
        Bukkit.getPluginManager().registerEvents(new ShardFreeze(), this);
    }

    @Override
    List<ShardType> rerollPool() {
        ArrayList<ShardType> var1 = new ArrayList<>(super.rerollPool());
        var1.removeIf(var0 -> contains(NO_REROLL, var0.id()));
        return var1;
    }

    private static boolean contains(String[] var0, String var1) {
        for (String var5 : var0) {
            if (var5.equals(var1)) {
                return true;
            }
        }

        return false;
    }

    private static Component text(String var0, NamedTextColor var1) {
        return Component.text(var0, var1);
    }

    private boolean hasAnyShard(Player var1) {
        for (ItemStack var5 : var1.getInventory().getContents()) {
            if (var5 != null && this.getShardType(var5) != null) {
                return true;
            }
        }

        return false;
    }

    private void give(Player var1, ItemStack var2) {
        for (ItemStack var4 : var1.getInventory().addItem(new ItemStack[]{var2}).values()) {
            var1.getWorld().dropItemNaturally(var1.getLocation(), var4);
        }
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shardsmp")) {
            return super.onCommand(var1, var2, var3, var4);
        }

        if (!var1.hasPermission("superhumanshard.admin")) {
            var1.sendMessage(text("You don't have permission.", NamedTextColor.RED));
            return true;
        }

        if (var4.length != 0 && var4[0].equalsIgnoreCase("start")) {
            ArrayList var5 = new ArrayList();

            for (String var9 : START_POOL) {
                ShardType var10 = ShardType.fromId(var9);
                if (var10 != null) {
                    var5.add(var10);
                }
            }

            if (var5.isEmpty()) {
                var1.sendMessage(text("No shards are available to hand out.", NamedTextColor.RED));
                return true;
            }

            int var11 = 0;
            int var12 = 0;

            for (Player var14 : Bukkit.getOnlinePlayers()) {
                if (this.hasAnyShard(var14)) {
                    var12++;
                } else {
                    ShardType var15 = (ShardType)var5.get(ThreadLocalRandom.current().nextInt(var5.size()));
                    this.give(var14, this.newShard(var15));
                    var14.sendMessage(text("The SMP has started! You received the " + var15.displayName() + ".", NamedTextColor.GOLD));
                    var11++;
                }
            }

            var1.sendMessage(text("Gave a shard to " + var11 + " player(s)" + (var12 > 0 ? " (" + var12 + " already had one)." : "."), NamedTextColor.GREEN));
            return true;
        } else {
            var1.sendMessage(text("Usage: /shardsmp start", NamedTextColor.RED));
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        if (var2.getName().equalsIgnoreCase("shardsmp")) {
            return var4.length == 1 && "start".startsWith(var4[0].toLowerCase(Locale.ROOT)) ? List.of("start") : List.of();
        } else {
            return super.onTabComplete(var1, var2, var3, var4);
        }
    }
}
