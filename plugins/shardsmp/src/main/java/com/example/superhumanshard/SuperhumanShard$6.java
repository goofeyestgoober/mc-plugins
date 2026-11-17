package com.example.superhumanshard;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

class SuperhumanShard$6 extends BukkitRunnable {
    final UUID id;

    SuperhumanShard$6(UUID var1) {
        this.id = var1;
    }

    public void run() {
        Player var1;
        if ((var1 = Bukkit.getPlayer(this.id)) != null) {
            AttributeInstance var10000 = var1.getAttribute(Attribute.SCALE);
            if (var10000 != null) {
                var10000.setBaseValue(1.0);
            }
        }
    }
}
