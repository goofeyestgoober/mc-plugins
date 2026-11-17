package com.example.superhumanshard;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class ShardPatch {
    static final double STORM_DAMAGE = 4.0;

    static ShardType matchDragon(ItemStack[] var0) {
        if (var0 == null) {
            return null;
        }

        int var1 = 0;

        for (ItemStack var5 : var0) {
            if (var5 != null && !var5.getType().isAir()) {
                if (var5.getType() != Material.DRAGON_EGG) {
                    return null;
                }

                var1++;
            }
        }

        return var1 == 1 ? ShardType.DRAGON : null;
    }

    static void stormStrike(Player var0, Location var1) {
        World var2 = var1.getWorld();
        var2.strikeLightningEffect(var1);

        for (Entity var4 : var2.getNearbyEntities(var1, 3.0, 4.0, 3.0)) {
            if (var4 instanceof LivingEntity var5 && !var5.equals(var0) && !var5.isDead()) {
                PierceDamage.hit(var5, 4.0, var0);
            }
        }
    }

    static void particle(World var0, Particle var1, Location var2, int var3, double var4, double var6, double var8, double var10) {
        try {
            ShardFX.spawn(var0, var1, var2, var3, var4, var6, var8, var10);
        } catch (IllegalArgumentException var15) {
            try {
                ShardFX.spawn(var0, var1, var2, var3, var4, var6, var8, var10, 1.0F);
            } catch (Throwable var14) {
            }
        }
    }
}
