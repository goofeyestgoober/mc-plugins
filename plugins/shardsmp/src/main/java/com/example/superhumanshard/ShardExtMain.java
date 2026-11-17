package com.example.superhumanshard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShardExtMain extends ShardTimerMain {
    private ShardAbilities abilities;
    private PlayerInteractEvent lastUse;

    @Override
    public void onEnable() {
        super.onEnable();
        this.abilities = new ShardAbilities(this);
        Bukkit.getPluginManager().registerEvents(this.abilities, this);
        this.abilities.start();
    }

    @Override
    public void onDisable() {
        if (this.abilities != null) {
            this.abilities.shutdown();
        }

        super.onDisable();
    }

    @EventHandler
    @Override
    public void onShardUse(PlayerInteractEvent var1) {
        ShardType var2 = this.getShardType(var1.getItem());
        if (var2 != ShardType.EMBER && var2 != ShardType.SCULK) {
            super.onShardUse(var1);
        } else if (var1 != this.lastUse) {
            this.lastUse = var1;
            EquipmentSlot var3 = var1.getHand();
            if (var3 == EquipmentSlot.HAND || var3 == EquipmentSlot.OFF_HAND) {
                Action var4 = var1.getAction();
                if (var4 == Action.RIGHT_CLICK_AIR || var4 == Action.RIGHT_CLICK_BLOCK) {
                    if (var2 == ShardType.EMBER) {
                        var1.setCancelled(true);
                    }

                    Player var5 = var1.getPlayer();
                    if (var3 != EquipmentSlot.OFF_HAND || this.getShardType(var5.getInventory().getItemInMainHand()) == null) {
                        this.abilities.use(var5, var2, var5.isSneaking());
                    }
                }
            }
        }
    }

    @Override
    protected ItemStack createShard(ShardType var1) {
        ItemStack var2 = super.createShard(var1);
        if (var2 != null && (var1 == ShardType.EMBER || var1 == ShardType.SCULK)) {
            ItemMeta var3 = var2.getItemMeta();
            if (var3 != null) {
                var3.lore(ShardAbilities.lore(var1, this.getConfig()));
                var2.setItemMeta(var3);
            }
        }

        return var2;
    }
}
