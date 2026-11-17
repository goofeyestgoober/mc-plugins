package com.example.superhumanshard;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public final class ShardFireproof implements Listener {
    private final SuperhumanShard plugin;

    public ShardFireproof(SuperhumanShard var1) {
        this.plugin = var1;
    }

    private boolean isShardItem(Entity var1) {
        return var1 instanceof Item && SpeedHelp.isSpeedItem(this.plugin, ((Item)var1).getItemStack())
            ? true
            : var1 instanceof Item && this.plugin.getShardType(((Item)var1).getItemStack()) != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShardDamage(EntityDamageEvent var1) {
        Entity var2 = var1.getEntity();
        if (this.isShardItem(var2)) {
            DamageCause var3 = var1.getCause();
            if (var3 == DamageCause.FIRE || var3 == DamageCause.FIRE_TICK || var3 == DamageCause.LAVA || var3 == DamageCause.HOT_FLOOR) {
                var1.setCancelled(true);
                var2.setFireTicks(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShardCombust(EntityCombustEvent var1) {
        if (this.isShardItem(var1.getEntity())) {
            var1.setCancelled(true);
        }
    }
}
