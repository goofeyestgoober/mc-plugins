package com.example.superhumanshard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RestoreMain extends MythicShardMain {
    final Map<UUID, ShardType> regular = new HashMap<>();
    private File store;

    static boolean mythic(ShardType var0) {
        return var0 == ShardType.EMBER || var0 == ShardType.DRAGON || var0 == ShardType.EVOKER || var0 == ShardType.SCULK;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.getDataFolder().mkdirs();
        this.store = new File(this.getDataFolder(), "regular-shards.properties");
        this.load();
        Bukkit.getPluginManager().registerEvents(new RestoreListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, this::track, 1L, 1L);
    }

    private void track() {
        boolean var1 = false;

        for (Player var3 : Bukkit.getOnlinePlayers()) {
            for (ItemStack var7 : var3.getInventory().getContents()) {
                ShardType var8 = this.getShardType(var7);
                if (var8 != null && !mythic(var8) && this.regular.get(var3.getUniqueId()) != var8) {
                    this.regular.put(var3.getUniqueId(), var8);
                    var1 = true;
                }
            }
        }

        if (var1) {
            this.save();
        }
    }

    boolean holdsShard(Player var1) {
        for (ItemStack var5 : var1.getInventory().getContents()) {
            if (this.getShardType(var5) != null) {
                return true;
            }
        }

        return false;
    }

    private void load() {
        if (this.store.isFile()) {
            Properties var1 = new Properties();

            try (FileInputStream var2 = new FileInputStream(this.store)) {
                var1.load(var2);
            } catch (IOException var8) {
                this.getLogger().warning("Could not read " + this.store.getName() + ": " + var8);
                return;
            }

            for (String var3 : var1.stringPropertyNames()) {
                try {
                    ShardType var4 = ShardType.fromId(var1.getProperty(var3));
                    if (var4 != null && !mythic(var4)) {
                        this.regular.put(UUID.fromString(var3), var4);
                    }
                } catch (IllegalArgumentException var6) {
                }
            }
        }
    }

    void save() {
        Properties var1 = new Properties();

        for (Entry var3 : this.regular.entrySet()) {
            var1.setProperty(((UUID)var3.getKey()).toString(), ((ShardType)var3.getValue()).id());
        }

        try (FileOutputStream var8 = new FileOutputStream(this.store)) {
            var1.store(var8, "Regular shard each player had before a mythic consumed it");
        } catch (IOException var7) {
            this.getLogger().warning("Could not save " + this.store.getName() + ": " + var7);
        }
    }

    ItemStack create(ShardType var1) {
        return this.newShard(var1);
    }
}
