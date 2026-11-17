package com.example.superhumanshard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

final class TierStore {
    private final File file;
    private final YamlConfiguration yml;

    TierStore(File var1) {
        var1.mkdirs();
        this.file = new File(var1, "tiers.yml");
        this.yml = YamlConfiguration.loadConfiguration(this.file);
    }

    private static String path(UUID var0, ShardType var1) {
        return "p." + var0 + "." + var1.id();
    }

    int tier(UUID var1, ShardType var2) {
        int var3 = this.yml.getInt(path(var1, var2) + ".tier", 1);
        return var3 < 1 ? 1 : Math.min(var3, 3);
    }

    List<String> victims(UUID var1, ShardType var2) {
        List var3 = this.yml.getStringList(path(var1, var2) + ".victims");
        return var3 == null ? new ArrayList<>() : new ArrayList<>(var3);
    }

    void setTier(UUID var1, ShardType var2, int var3) {
        this.yml.set(path(var1, var2) + ".tier", var3);
    }

    void setVictims(UUID var1, ShardType var2, List<String> var3) {
        this.yml.set(path(var1, var2) + ".victims", var3);
    }

    void save() {
        try {
            this.yml.save(this.file);
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }
}
