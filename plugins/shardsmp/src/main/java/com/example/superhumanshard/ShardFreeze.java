package com.example.superhumanshard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

final class ShardFreeze implements Listener {
    static final long FREEZE_MS = 3000L;
    private static final Map<UUID, Long> FROZEN = new ConcurrentHashMap<>();

    static void freeze(Player var0) {
        FROZEN.put(var0.getUniqueId(), System.currentTimeMillis() + 3000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent var1) {
        Player var2 = var1.getPlayer();
        Long var3 = FROZEN.get(var2.getUniqueId());
        if (var3 != null) {
            if (System.currentTimeMillis() >= var3) {
                FROZEN.remove(var2.getUniqueId());
            } else {
                Location var4 = var1.getFrom();
                Location var5 = var1.getTo();
                if (var5 != null) {
                    if (var4.getX() != var5.getX() || var4.getY() != var5.getY() || var4.getZ() != var5.getZ()) {
                        Location var6 = var4.clone();
                        var6.setYaw(var5.getYaw());
                        var6.setPitch(var5.getPitch());
                        if (var5.getY() < var4.getY()) {
                            var6.setY(var5.getY());
                        }

                        var1.setTo(var6);
                    }
                }
            }
        }
    }
}
