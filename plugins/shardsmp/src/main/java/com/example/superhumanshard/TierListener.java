package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

final class TierListener implements Listener {
    private final ShardV25Main plugin;
    private final TierStore tiers;

    TierListener(ShardV25Main var1, TierStore var2) {
        this.plugin = var1;
        this.tiers = var2;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent var1) {
        Player var2 = var1.getEntity();
        Player var3 = var2.getKiller();
        if (var3 != null && !var3.equals(var2)) {
            for (ShardType var7 : ShardV25Main.NORMAL) {
                if (this.plugin.hasShard(var3, var7)) {
                    this.credit(var3, var2, var7);
                }
            }
        }
    }

    private void credit(Player var1, Player var2, ShardType var3) {
        UUID var4 = var1.getUniqueId();
        int var5 = this.tiers.tier(var4, var3);
        if (var5 < 3) {
            List var6 = this.tiers.victims(var4, var3);
            String var7 = var2.getUniqueId().toString();
            String var8 = "§6" + var3.displayName() + "§r";
            if (var6.contains(var7)) {
                var1.sendActionBar("§7" + var2.getName() + " was already counted for your " + var3.displayName() + " - kill a different player.");
            } else {
                var6.add(var7);
                int var9 = this.plugin.need2();
                int var10 = this.plugin.need3();
                boolean var11 = this.plugin.resetOnUpgrade();
                int var12 = var5 == 1 ? var9 : var10;
                if (var6.size() >= var12) {
                    int var13 = var5 + 1;
                    this.tiers.setTier(var4, var3, var13);
                    this.tiers.setVictims(var4, var3, var11 ? new ArrayList<>() : var6);
                    this.tiers.save();
                    Location var14 = var1.getLocation();
                    var1.getWorld().playSound(var14, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    ShardFX.spawn(var1.getWorld(), Particle.HAPPY_VILLAGER, var14.clone().add(0.0, 1.0, 0.0), 40, 0.6, 0.8, 0.6, 0.0);
                    if (var13 == 2) {
                        var1.sendMessage(
                            "§a§lSHARD UPGRADED! "
                                + var8
                                + " §ais now §e§lTier 2§r§a - /ability2 to use your second ability: §f"
                                + ShardV25Main.abilityName(var3)
                                + "§a."
                        );
                    } else {
                        var1.sendMessage(
                            "§a§lSHARD UPGRADED! "
                                + var8
                                + " §ais now §d§lTier 3§r§a - you gained a permanent buff while you carry it: §f"
                                + ShardV25Main.buffName(var3)
                                + "§a."
                        );
                    }
                } else {
                    this.tiers.setVictims(var4, var3, var6);
                    this.tiers.save();
                    var1.sendActionBar("§a" + var3.displayName() + " upgrade: §e" + var6.size() + "/" + var12 + " §akills (tier " + (var5 + 1) + ")");
                }
            }
        }
    }
}
