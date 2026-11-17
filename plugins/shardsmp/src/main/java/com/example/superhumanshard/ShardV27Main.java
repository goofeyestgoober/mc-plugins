package com.example.superhumanshard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShardV27Main extends ShardV26Main {
    private static final String[] POOL = new String[]{"velocity", "titan", "tide", "phantom", "frost", "verdant", "storm"};
    private final Properties smp = new Properties();
    private File smpFile;

    @Override
    public void onEnable() {
        super.onEnable();
        this.smpFile = new File(this.getDataFolder(), "shardsmp-start.properties");
        this.loadSmp();
        Bukkit.getPluginManager().registerEvents(new ShardSmpJoinListener(this), this);
        this.getLogger()
            .info(
                "ShardSMP start: "
                    + (this.smpStarted() ? "ACTIVE (late joiners get a Tier " + this.smpTier() + " shard)" : "not started yet (use /shardsmp start <1|2|3>)")
            );
    }

    private void loadSmp() {
        this.smp.clear();
        if (this.smpFile != null && this.smpFile.isFile()) {
            try (FileInputStream var1 = new FileInputStream(this.smpFile)) {
                this.smp.load(var1);
            } catch (IOException var6) {
                this.getLogger().warning("Could not read shardsmp-start.properties: " + var6);
            }
        }
    }

    private void saveSmp() {
        try {
            this.getDataFolder().mkdirs();

            try (FileOutputStream var1 = new FileOutputStream(this.smpFile)) {
                this.smp.store(var1, "ShardSMP start state - delete this file to fully reset");
            }
        } catch (IOException var6) {
            this.getLogger().warning("Could not save shardsmp-start.properties: " + var6);
        }
    }

    boolean smpStarted() {
        return Boolean.parseBoolean(this.smp.getProperty("started", "false"));
    }

    int smpTier() {
        try {
            int var1 = Integer.parseInt(this.smp.getProperty("tier", "1").trim());
            return var1 < 1 ? 1 : (var1 > 3 ? 3 : var1);
        } catch (NumberFormatException var2) {
            return 1;
        }
    }

    private boolean isGiven(UUID var1) {
        return this.smp.getProperty("given." + var1) != null;
    }

    private void markGiven(UUID var1) {
        this.smp.setProperty("given." + var1, "true");
    }

    private boolean holdsAnyShard(Player var1) {
        for (ItemStack var5 : var1.getInventory().getContents()) {
            if (var5 != null && this.getShardType(var5) != null) {
                return true;
            }
        }

        return false;
    }

    private void giveItem(Player var1, ItemStack var2) {
        HashMap<Integer, ItemStack> var3 = var1.getInventory().addItem(new ItemStack[]{var2});

        for (ItemStack var5 : var3.values()) {
            var1.getWorld().dropItemNaturally(var1.getLocation(), var5);
        }
    }

    private ShardType giveStarter(Player var1, int var2) {
        ArrayList var3 = new ArrayList();

        for (String var7 : POOL) {
            ShardType var8 = ShardType.fromId(var7);
            if (var8 != null) {
                var3.add(var8);
            }
        }

        if (var3.isEmpty()) {
            return null;
        }

        ShardType var9 = (ShardType)var3.get(ThreadLocalRandom.current().nextInt(var3.size()));
        UUID var10 = var1.getUniqueId();
        this.tiers.setTier(var10, var9, var2);
        this.tiers.setVictims(var10, var9, new ArrayList<>());
        this.tiers.save();
        this.giveItem(var1, this.newShard(var9));
        var1.sendMessage("§6You received the §e" + var9.displayName() + "§6 (Tier " + var2 + ").");
        return var9;
    }

    void onPlayerJoin(final Player var1) {
        if (this.smpStarted()) {
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    if (var1.isOnline() && ShardV27Main.this.smpStarted()) {
                        UUID var1x = var1.getUniqueId();
                        if (!ShardV27Main.this.isGiven(var1x)) {
                            if (ShardV27Main.this.holdsAnyShard(var1)) {
                                ShardV27Main.this.markGiven(var1x);
                                ShardV27Main.this.saveSmp();
                            } else {
                                if (ShardV27Main.this.giveStarter(var1, ShardV27Main.this.smpTier()) != null) {
                                    ShardV27Main.this.markGiven(var1x);
                                    ShardV27Main.this.saveSmp();
                                }
                            }
                        }
                    }
                }
            }, 40L);
        }
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shardsmp")) {
            return super.onCommand(var1, var2, var3, var4);
        }

        if (!var1.hasPermission("superhumanshard.admin")) {
            var1.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (var4.length >= 1 && var4[0].equalsIgnoreCase("stop")) {
            this.smp.setProperty("started", "false");
            this.saveSmp();
            var1.sendMessage("§eShardSMP start turned off - new players will no longer get a starter shard.");
            return true;
        }

        if (var4.length != 0 && var4[0].equalsIgnoreCase("start")) {
            int var5 = 1;
            if (var4.length >= 2) {
                try {
                    var5 = Integer.parseInt(var4[1].trim());
                } catch (NumberFormatException var10) {
                    var5 = 0;
                }

                if (var5 < 1 || var5 > 3) {
                    var1.sendMessage("§cTier must be 1, 2 or 3.  Usage: /shardsmp start <1|2|3>");
                    return true;
                }
            }

            this.smp.setProperty("started", "true");
            this.smp.setProperty("tier", String.valueOf(var5));
            int var6 = 0;
            int var7 = 0;

            for (Player var9 : Bukkit.getOnlinePlayers()) {
                if (this.holdsAnyShard(var9)) {
                    this.markGiven(var9.getUniqueId());
                    var7++;
                } else if (this.giveStarter(var9, var5) != null) {
                    this.markGiven(var9.getUniqueId());
                    var6++;
                }
            }

            this.saveSmp();
            var1.sendMessage(
                "§aShardSMP started at Tier "
                    + var5
                    + ". Gave a starter shard to "
                    + var6
                    + " player(s)"
                    + (var7 > 0 ? " (" + var7 + " already had one)" : "")
                    + ". Anyone who joins later will get a Tier "
                    + var5
                    + " shard too."
            );
            return true;
        } else {
            var1.sendMessage("§cUsage: /shardsmp start <1|2|3>   (or /shardsmp stop)");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shardsmp")) {
            return super.onTabComplete(var1, var2, var3, var4);
        }

        ArrayList var5 = new ArrayList();
        if (var4.length == 1) {
            String var6 = var4[0].toLowerCase(Locale.ROOT);
            if ("start".startsWith(var6)) {
                var5.add("start");
            }

            if ("stop".startsWith(var6)) {
                var5.add("stop");
            }
        } else if (var4.length == 2 && var4[0].equalsIgnoreCase("start")) {
            for (String var9 : new String[]{"1", "2", "3"}) {
                if (var9.startsWith(var4[1])) {
                    var5.add(var9);
                }
            }
        }

        return var5;
    }
}
