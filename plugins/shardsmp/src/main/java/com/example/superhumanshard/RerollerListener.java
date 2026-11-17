package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

public class RerollerListener implements Listener {
    private final ShardV23Main plugin;

    public RerollerListener(ShardV23Main var1) {
        this.plugin = var1;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent var1) {
        if (var1.getAction() == Action.RIGHT_CLICK_AIR || var1.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack var2 = var1.getItem();
            if (this.plugin.isReroller(var2)) {
                var1.setCancelled(true);
                EquipmentSlot var3 = var1.getHand();
                if (var3 == EquipmentSlot.HAND || var3 == EquipmentSlot.OFF_HAND) {
                    Player var4 = var1.getPlayer();
                    if (!this.plugin.rerollerEnabled()) {
                        var4.sendMessage(ChatColor.RED + "The Reroller is disabled on this server.");
                    } else {
                        List var5 = this.plugin.rerollPool();
                        PlayerInventory var6 = var4.getInventory();
                        ItemStack[] var7 = var6.getContents();
                        int var8 = -1;
                        ShardType var9 = null;
                        ShardType var10 = null;

                        for (int var11 = 0; var11 < var7.length; var11++) {
                            ShardType var12 = var7[var11] == null ? null : this.plugin.getShardType(var7[var11]);
                            if (var12 != null) {
                                if (var5.contains(var12)) {
                                    var8 = var11;
                                    var9 = var12;
                                    break;
                                }

                                if (var10 == null) {
                                    var10 = var12;
                                }
                            }
                        }

                        if (var8 < 0) {
                            if (var10 != null) {
                                var4.sendMessage(ChatColor.RED + "Your " + var10.displayName() + " can't be rerolled.");
                            } else {
                                var4.sendMessage(ChatColor.RED + "You need a shard in your inventory to reroll.");
                            }

                            var4.playSound(var4.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        } else {
                            ArrayList var15 = new ArrayList(var5);
                            var15.remove(var9);
                            if (var15.isEmpty()) {
                                var4.sendMessage(ChatColor.RED + "There is nothing to reroll into.");
                            } else {
                                ShardType var16 = (ShardType)var15.get(ThreadLocalRandom.current().nextInt(var15.size()));
                                var6.setItem(var8, this.plugin.newShard(var16));
                                ItemStack var13 = var3 == EquipmentSlot.HAND ? var6.getItemInMainHand() : var6.getItemInOffHand();
                                ItemStack var14 = null;
                                if (var13 != null && var13.getAmount() > 1) {
                                    var14 = var13.clone();
                                    var14.setAmount(var13.getAmount() - 1);
                                }

                                if (var3 == EquipmentSlot.HAND) {
                                    var6.setItemInMainHand(var14);
                                } else {
                                    var6.setItemInOffHand(var14);
                                }

                                var4.sendMessage(
                                    ChatColor.GOLD
                                        + "Your "
                                        + ChatColor.LIGHT_PURPLE
                                        + var9.displayName()
                                        + ChatColor.GOLD
                                        + " was rerolled into "
                                        + ChatColor.LIGHT_PURPLE
                                        + var16.displayName()
                                        + ChatColor.GOLD
                                        + "!"
                                );
                                var4.playSound(var4.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.2F);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepare(PrepareItemCraftEvent var1) {
        if (var1.getInventory() != null) {
            if (var1.getRecipe() == null || !this.plugin.isReroller(var1.getRecipe().getResult())) {
                for (ItemStack var5 : var1.getInventory().getMatrix()) {
                    if (var5 != null && this.plugin.isReroller(var5)) {
                        var1.getInventory().setResult(null);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent var1) {
        if (this.plugin.headsEnabled()) {
            Player var2 = var1.getEntity();
            if (!this.plugin.headsPvpOnly() || var2.getKiller() != null) {
                ItemStack var3 = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta var4 = (SkullMeta)var3.getItemMeta();
                if (var4 != null) {
                    var4.setOwningPlayer(var2);
                    var3.setItemMeta(var4);
                }

                var1.getDrops().add(var3);
            }
        }
    }
}
