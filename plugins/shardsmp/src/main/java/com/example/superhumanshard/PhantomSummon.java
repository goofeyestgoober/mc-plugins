package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public final class PhantomSummon implements Listener {
    static final int COUNT = 5;
    static final int LIFETIME_TICKS = 200;
    static final double MAX_RANGE_SQ = 4096.0;
    static final Map<UUID, UUID> OWNERS = new HashMap<>();
    static final Map<UUID, UUID> TARGETS = new HashMap<>();
    static boolean registered = false;

    public static void summon(final Player var0) {
        JavaPlugin var1 = JavaPlugin.getProvidingPlugin(PhantomSummon.class);
        if (!registered) {
            registered = true;
            Bukkit.getPluginManager().registerEvents(new PhantomSummon(), var1);
        }

        World var2 = var0.getWorld();
        Location var3 = var0.getLocation();
        final UUID var4 = var0.getUniqueId();
        final ArrayList<Phantom> var5 = new ArrayList<>();

        for (int var6 = 0; var6 < 5; var6++) {
            double var7 = (Math.PI * 2) * var6 / 5.0;
            Location var9 = var3.clone().add(Math.cos(var7) * 2.0, 2.5, Math.sin(var7) * 2.0);
            Entity var10 = var2.spawnEntity(var9, EntityType.PHANTOM);
            if (!(var10 instanceof Phantom var11)) {
                if (var10 != null) {
                    var10.remove();
                }
            } else {
                var11.setPersistent(false);
                var11.setRemoveWhenFarAway(false);
                var11.setShouldBurnInDay(false);
                var11.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 240, 0, false, false, false));
                var11.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 240, 4, false, false, false));
                OWNERS.put(var11.getUniqueId(), var4);
                var5.add(var11);
            }
        }

        if (!var5.isEmpty()) {
            retarget(var0, var5);
            (new BukkitRunnable() {
                int age = 0;

                public void run() {
                    this.age += 5;
                    if (this.age < 200) {
                        PhantomSummon.retarget(var0, var5);
                    } else {
                        for (Phantom var2x : var5) {
                            PhantomSummon.OWNERS.remove(var2x.getUniqueId());
                            var2x.remove();
                        }

                        PhantomSummon.TARGETS.remove(var4);
                        this.cancel();
                    }
                }
            }).runTaskTimer(var1, 5L, 5L);
        }
    }

    static Player nearestOther(Player var0) {
        Player var1 = null;
        double var2 = 4096.0;
        Location var4 = var0.getLocation();

        for (Player var6 : var0.getWorld().getPlayers()) {
            if (!var6.getUniqueId().equals(var0.getUniqueId()) && !var6.isDead() && var6.isOnline() && var6.getGameMode() != GameMode.SPECTATOR) {
                double var7 = var6.getLocation().distanceSquared(var4);
                if (var7 < var2) {
                    var2 = var7;
                    var1 = var6;
                }
            }
        }

        return var1;
    }

    static void retarget(Player var0, List<Phantom> var1) {
        Player var2 = nearestOther(var0);
        UUID var3 = var0.getUniqueId();
        if (var2 == null) {
            TARGETS.remove(var3);
        } else {
            TARGETS.put(var3, var2.getUniqueId());
        }

        for (Phantom var5 : var1) {
            if (!var5.isDead()) {
                var5.setTarget(var2);
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent var1) {
        UUID var2 = OWNERS.get(var1.getEntity().getUniqueId());
        if (var2 != null) {
            Entity var3 = var1.getTarget();
            if (var3 != null) {
                UUID var4 = TARGETS.get(var2);
                if (var4 == null || !var3.getUniqueId().equals(var4)) {
                    var1.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent var1) {
        UUID var2 = OWNERS.get(var1.getDamager().getUniqueId());
        if (var2 != null) {
            if (var1.getEntity().getUniqueId().equals(var2)) {
                var1.setCancelled(true);
            }

            PierceDamage.pierce(var1, 2.0);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent var1) {
        if (OWNERS.remove(var1.getEntity().getUniqueId()) != null) {
            var1.getDrops().clear();
            var1.setDroppedExp(0);
        }
    }
}
