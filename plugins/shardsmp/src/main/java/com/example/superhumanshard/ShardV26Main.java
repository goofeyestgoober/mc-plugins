package com.example.superhumanshard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ShardV26Main extends ShardV25Main {
    private int cmdDepth = 0;
    private NamespacedKey sigKey;
    private TierStore tierStore;
    private boolean warned = false;

    @Override
    public void onEnable() {
        super.onEnable();
        this.sigKey = new NamespacedKey(this, "lore_sig");

        try {
            Field var1 = ShardV25Main.class.getDeclaredField("tiers");
            var1.setAccessible(true);
            this.tierStore = (TierStore)var1.get(this);
        } catch (Throwable var2) {
            this.getLogger().warning("Could not read tiers; shard tooltips will show Tier 1: " + var2);
        }

        Runnable var3 = new Runnable() {
            @Override
            public void run() {
                ShardV26Main.this.sweepLore();
            }
        };
        Bukkit.getScheduler().runTaskTimer(this, var3, 20L, 20L);
        this.getLogger().info("Shard abilities are now activated with /ability1 and /ability2.");
    }

    @EventHandler
    @Override
    public void onShardUse(PlayerInteractEvent var1) {
        if (this.cmdDepth > 0) {
            super.onShardUse(var1);
        } else if (this.getShardType(var1.getItem()) == null) {
            super.onShardUse(var1);
        }
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        String var5 = var2.getName().toLowerCase(Locale.ROOT);
        byte var7 = 0;
        int var6;
        if (var5.equals("ability1")) {
            var6 = 1;
        } else if (var5.equals("ability2")) {
            var6 = 2;
        } else {
            if (!var5.equals("ability")) {
                return super.onCommand(var1, var2, var3, var4);
            }

            if (var4.length < 1 || !var4[0].equals("1") && !var4[0].equals("2")) {
                var1.sendMessage("§cUsage: /ability <1|2> [shard]   (or /ability1, /ability2)");
                return true;
            }

            var6 = Integer.parseInt(var4[0]);
            var7 = 1;
        }

        if (!(var1 instanceof Player var8)) {
            var1.sendMessage("Only players can use shard abilities.");
            return true;
        } else {
            ShardType var9 = null;
            if (var4.length > var7) {
                var9 = ShardType.fromId(var4[var7].toLowerCase(Locale.ROOT));
                if (var9 == null) {
                    var8.sendMessage("§cUnknown shard '" + var4[var7] + "'.");
                    return true;
                }
            }

            this.cast(var8, var6, var9);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        String var5 = var2.getName().toLowerCase(Locale.ROOT);
        boolean var6 = var5.equals("ability1") || var5.equals("ability2");
        if (!var6 && !var5.equals("ability")) {
            return super.onTabComplete(var1, var2, var3, var4);
        }

        ArrayList var7 = new ArrayList();
        int var8 = var6 ? 0 : 1;
        if (var4.length == var8 + 1) {
            String var9 = var4[var8].toLowerCase(Locale.ROOT);
            if (var1 instanceof Player) {
                PlayerInventory var10 = ((Player)var1).getInventory();

                for (int var11 = 0; var11 < var10.getSize(); var11++) {
                    ItemStack var12 = var10.getItem(var11);
                    if (var12 != null) {
                        ShardType var13 = this.getShardType(var12);
                        if (var13 != null && var13.id().startsWith(var9) && !var7.contains(var13.id())) {
                            var7.add(var13.id());
                        }
                    }
                }
            }
        } else if (!var6 && var4.length == 1) {
            if ("1".startsWith(var4[0])) {
                var7.add("1");
            }

            if ("2".startsWith(var4[0])) {
                var7.add("2");
            }
        }

        return var7;
    }

    private ItemStack findShard(Player var1, ShardType var2) {
        PlayerInventory var3 = var1.getInventory();
        ItemStack var4 = var3.getItemInMainHand();
        ItemStack var5 = var3.getItemInOffHand();
        ItemStack[] var6 = new ItemStack[]{var4, var5};

        for (ItemStack var10 : var6) {
            if (var10 != null) {
                ShardType var11 = this.getShardType(var10);
                if (var11 != null && (var2 == null || var11 == var2)) {
                    return var10;
                }
            }
        }

        for (int var12 = 0; 127 < var3.getSize(); var12++) {
            ItemStack var13 = var3.getItem(var12);
            if (var13 != null) {
                ShardType var14 = this.getShardType(var13);
                if (var14 != null && (var2 == null || var14 == var2)) {
                    return var13;
                }
            }
        }

        return null;
    }

    private int tierOf(Player var1, ShardType var2) {
        if (isNormal(var2) && this.tierStore != null) {
            try {
                return Math.max(1, this.tierStore.tier(var1.getUniqueId(), var2));
            } catch (Throwable var4) {
                return 1;
            }
        } else {
            return 1;
        }
    }

    private void cast(Player var1, int var2, ShardType var3) {
        ItemStack var4 = this.findShard(var1, var3);
        if (var4 == null) {
            var1.sendMessage(
                var3 == null
                    ? "§cYou need a shard in your main hand or offhand to use abilities."
                    : "§cYou must hold the " + var3.displayName() + " in your main hand or offhand."
            );
        } else {
            ShardType var5 = this.getShardType(var4);
            if (var2 == 2 && isNormal(var5) && this.tierOf(var1, var5) < 2) {
                var1.sendMessage("§cAbility 2 unlocks at Tier 2 — get " + this.need2() + " kills on different players with the " + var5.displayName() + ".");
            } else {
                boolean var6 = var1.isSneaking();
                this.cmdDepth++;

                try {
                    var1.setSneaking(var2 == 2);
                    PlayerInteractEvent var7 = new PlayerInteractEvent(var1, Action.RIGHT_CLICK_AIR, var4, (Block)null, BlockFace.SELF);
                    this.onShardUse(var7);
                } finally {
                    this.cmdDepth--;
                    var1.setSneaking(var6);
                }
            }
        }
    }

    @Override
    protected ItemStack createShard(ShardType var1) {
        ItemStack var2 = super.createShard(var1);

        try {
            if (var2 != null) {
                this.applyLore(var2, var1, 1);
            }
        } catch (Throwable var4) {
        }

        return var2;
    }

    private boolean applyLore(ItemStack var1, ShardType var2, int var3) {
        ItemMeta var4 = var1.getItemMeta();
        if (var4 == null) {
            return false;
        }

        String var5 = ShardLore.signature(var2, this.getConfig(), var3, this.need2(), this.need3());
        String var6 = (String)var4.getPersistentDataContainer().get(this.sigKey, PersistentDataType.STRING);
        if (var5.equals(var6)) {
            return false;
        }

        ShardLore var7 = ShardLore.build(var2, this.getConfig(), var3, this.need2(), this.need3());
        var4.displayName(var7.name);
        var4.lore(var7.lines);
        var4.getPersistentDataContainer().set(this.sigKey, PersistentDataType.STRING, var7.sig);
        var1.setItemMeta(var4);
        return true;
    }

    private void sweepLore() {
        try {
            for (Player var2 : Bukkit.getOnlinePlayers()) {
                PlayerInventory var3 = var2.getInventory();
                int var4 = var3.getSize();

                for (int var5 = 0; var5 < var4; var5++) {
                    ItemStack var6 = var3.getItem(var5);
                    if (var6 != null) {
                        ShardType var7 = this.getShardType(var6);
                        if (var7 != null && this.applyLore(var6, var7, this.tierOf(var2, var7))) {
                            var3.setItem(var5, var6);
                        }
                    }
                }
            }
        } catch (Throwable var8) {
            if (!this.warned) {
                this.warned = true;
                this.getLogger().warning("Shard tooltip refresh failed: " + var8);
            }
        }
    }
}
