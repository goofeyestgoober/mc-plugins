package com.example.superhumanshard;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.projectiles.ProjectileSource;

public final class PierceDamage implements Listener {
    static final double EMBER_CAP = 12.0;
    private static LivingEntity pendingVictim;
    private static double pendingCap;

    static void hit(LivingEntity var0, double var1, Entity var3) {
        pendingVictim = var0;
        pendingCap = var1;

        try {
            var0.damage(var1, var3);
        } finally {
            pendingVictim = null;
            pendingCap = 0.0;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent var1) {
        if (pendingVictim != null && var1.getEntity() == pendingVictim) {
            pierce(var1, pendingCap);
        }
    }

    static void ember(EntityDamageByEntityEvent var0) {
        Entity var1 = var0.getDamager();
        boolean var2 = false;
        if (var1 instanceof Projectile) {
            ProjectileSource var3 = ((Projectile)var1).getShooter();
            var2 = var3 == var0.getEntity();
        }

        if (var2) {
            var0.setDamage(Math.min(var0.getDamage(), 12.0));
        } else {
            pierce(var0, 12.0);
        }
    }

    static void pierce(EntityDamageEvent var0, double var1) {
        var0.setDamage(Math.min(var0.getDamage(), var1));

        try {
            zero(var0, DamageModifier.ARMOR);
            zero(var0, DamageModifier.MAGIC);
        } catch (Throwable var4) {
        }
    }

    private static void zero(EntityDamageEvent var0, DamageModifier var1) {
        if (var0.isApplicable(var1)) {
            var0.setDamage(var1, 0.0);
        }
    }
}
