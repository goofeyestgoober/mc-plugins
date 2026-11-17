package com.example.superhumanshard;

import java.io.File;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShardV22Main extends ShardExtMain {
    private static final int CONFIG_VERSION = 22;
    private ShardV22Abilities v22;
    private PlayerInteractEvent lastUse;

    @Override
    public void onEnable() {
        this.migrateConfig();
        super.onEnable();
        this.v22 = new ShardV22Abilities(this);
        Bukkit.getPluginManager().registerEvents(this.v22, this);
        this.v22.start();
    }

    @Override
    public void onDisable() {
        if (this.v22 != null) {
            this.v22.shutdown();
        }

        super.onDisable();
    }

    private void migrateConfig() {
        File var1 = new File(this.getDataFolder(), "config.yml");
        if (var1.exists()) {
            FileConfiguration var2 = this.getConfig();
            if (var2.getInt("config-version", 0) < 22) {
                var2.set("abilities.ember.fireball-cooldown", 35);
                var2.set("abilities.ember.blaze-cooldown", 35);
                var2.set("abilities.sculk.beam-cooldown", 35);
                var2.set("abilities.sculk.track-cooldown", 35);
                setIfMissing(var2, "abilities.dragon.breath-cooldown", 30);
                setIfMissing(var2, "abilities.dragon.breath-seconds", 4);
                setIfMissing(var2, "abilities.dragon.breath-damage-hearts", 0.75);
                setIfMissing(var2, "abilities.dragon.breath-hits-mobs", false);
                setIfMissing(var2, "abilities.evoker.curse-chance-percent", 15);
                setIfMissing(var2, "abilities.evoker.curse-seconds", 5);
                setIfMissing(var2, "abilities.evoker.immortality-seconds", 3);
                setIfMissing(var2, "abilities.evoker.immortality-cooldown", 45);
                setIfMissing(var2, "abilities.evoker.fangs-cooldown", 35);
                setIfMissing(var2, "abilities.evoker.fang-damage-hearts", 5);
                List var3 = var2.getStringList("starter-shard.pool");
                if (var3 != null && !var3.isEmpty() && !var3.contains("evoker")) {
                    var3.add("evoker");
                    var2.set("starter-shard.pool", var3);
                }

                var2.set("config-version", 22);
                this.saveConfig();
                this.getLogger().info("Config upgraded to v22 (35s Ember/Sculk cooldowns, Evoker shard, Dragon's Breath).");
            }
        }
    }

    private static void setIfMissing(FileConfiguration var0, String var1, Object var2) {
        if (!var0.contains(var1)) {
            var0.set(var1, var2);
        }
    }

    @EventHandler
    @Override
    public void onShardUse(PlayerInteractEvent var1) {
        ShardType var2 = this.getShardType(var1.getItem());
        Player var3 = var1.getPlayer();
        boolean var4 = var2 == ShardType.EVOKER;
        boolean var5 = var2 == ShardType.DRAGON && var3.isSneaking();
        if (!var4 && !var5) {
            super.onShardUse(var1);
        } else if (var1 != this.lastUse) {
            this.lastUse = var1;
            EquipmentSlot var6 = var1.getHand();
            if (var6 == EquipmentSlot.HAND || var6 == EquipmentSlot.OFF_HAND) {
                Action var7 = var1.getAction();
                if (var7 == Action.RIGHT_CLICK_AIR || var7 == Action.RIGHT_CLICK_BLOCK) {
                    if (var6 != EquipmentSlot.OFF_HAND || this.getShardType(var3.getInventory().getItemInMainHand()) == null) {
                        this.v22.use(var3, var2, var3.isSneaking());
                    }
                }
            }
        }
    }

    @Override
    protected ItemStack createShard(ShardType var1) {
        ItemStack var2 = super.createShard(var1);
        if (var2 != null && (var1 == ShardType.DRAGON || var1 == ShardType.EVOKER)) {
            ItemMeta var3 = var2.getItemMeta();
            if (var3 != null) {
                var3.lore(ShardV22Abilities.lore(var1, this.getConfig()));
                var2.setItemMeta(var3);
            }
        }

        return var2;
    }
}
