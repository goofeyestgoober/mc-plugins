package com.example.superhumanshard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

final class TideDashTask extends BukkitRunnable {
    private static final double DAMAGE = 6.0;
    private static final int MAX_TICKS = 12;
    private final Player player;
    private final Set<UUID> hit = new HashSet<>();
    private int ticks;

    TideDashTask(Player var1) {
        this.player = var1;
    }

    public void run() {
        if (this.player.isOnline() && !this.player.isDead() && ++this.ticks <= 12) {
            for (Entity var2 : this.player.getNearbyEntities(1.5, 1.2, 1.5)) {
                if (var2 instanceof LivingEntity var3 && !var3.equals(this.player) && !var3.isDead() && this.hit.add(var3.getUniqueId())) {
                    PierceDamage.hit(var3, 6.0, this.player);
                }
            }
        } else {
            this.cancel();
        }
    }
}
