package com.example.superhumanshard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class RestoreListener implements Listener {
    private final RestoreMain plugin;
    private final Set<UUID> pending = new HashSet<>();

    RestoreListener(RestoreMain var1) {
        this.plugin = var1;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent var1) {
        if (!var1.getKeepInventory()) {
            Player var2 = var1.getEntity();
            boolean var3 = false;

            for (ItemStack var5 : var1.getDrops()) {
                ShardType var6 = this.plugin.getShardType(var5);
                if (var6 != null && RestoreMain.mythic(var6)) {
                    var3 = true;
                    break;
                }
            }

            if (var3 && this.plugin.regular.containsKey(var2.getUniqueId())) {
                this.pending.add(var2.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent var1) {
        UUID var2 = var1.getPlayer().getUniqueId();
        if (this.pending.remove(var2)) {
            ShardType var3 = this.plugin.regular.get(var2);
            if (var3 != null) {
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    Player var3x = Bukkit.getPlayer(var2);
                    if (var3x != null && !this.plugin.holdsShard(var3x)) {
                        for (ItemStack var5 : var3x.getInventory().addItem(new ItemStack[]{this.plugin.create(var3)}).values()) {
                            var3x.getWorld().dropItemNaturally(var3x.getLocation(), var5);
                        }

                        var3x.sendMessage("You lost your mythic shard - your " + var3.displayName() + " shard has returned to you.");
                    }
                }, 6L);
            }
        }
    }
}
