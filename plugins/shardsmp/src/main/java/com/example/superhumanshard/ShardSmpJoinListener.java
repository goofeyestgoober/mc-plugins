package com.example.superhumanshard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class ShardSmpJoinListener implements Listener {
    private final ShardV27Main plugin;

    ShardSmpJoinListener(ShardV27Main var1) {
        this.plugin = var1;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent var1) {
        this.plugin.onPlayerJoin(var1.getPlayer());
    }
}
