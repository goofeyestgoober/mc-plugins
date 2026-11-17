package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ShardV23Main extends ShardV22Main {
    static final int REROLLER_MODEL = 771012;
    private NamespacedKey rerollerKey;
    private NamespacedKey recipeKey;

    @Override
    public void onEnable() {
        super.onEnable();
        this.rerollerKey = new NamespacedKey(this, "reroller");
        this.recipeKey = new NamespacedKey(this, "reroller_recipe");
        Bukkit.removeRecipe(this.recipeKey);
        if (this.getConfig().getBoolean("reroller.craftable", true)) {
            ShapelessRecipe var1 = new ShapelessRecipe(this.recipeKey, this.createReroller());
            var1.addIngredient(4, Material.IRON_BLOCK).addIngredient(4, Material.GOLD_BLOCK).addIngredient(1, Material.DIAMOND_BLOCK);
            Bukkit.addRecipe(var1);
        }

        Bukkit.getPluginManager().registerEvents(new RerollerListener(this), this);
    }

    @Override
    public void onDisable() {
        if (this.recipeKey != null) {
            Bukkit.removeRecipe(this.recipeKey);
        }

        super.onDisable();
    }

    boolean rerollerEnabled() {
        return this.getConfig().getBoolean("reroller.enabled", true);
    }

    boolean headsEnabled() {
        return this.getConfig().getBoolean("player-heads.drop-on-death", true);
    }

    boolean headsPvpOnly() {
        return this.getConfig().getBoolean("player-heads.pvp-only", false);
    }

    List<ShardType> rerollPool() {
        List<String> var1 = this.getConfig().getStringList("reroller.pool");
        if (var1 == null || var1.isEmpty()) {
            var1 = List.of("velocity", "titan", "tide", "phantom", "frost", "verdant", "storm");
        }

        ArrayList var2 = new ArrayList();

        for (String var4 : var1) {
            ShardType var5 = ShardType.fromId(var4);
            if (var5 != null && var5 != ShardType.DRAGON && !var2.contains(var5)) {
                var2.add(var5);
            }
        }

        return var2;
    }

    NamespacedKey rerollerKey() {
        return this.rerollerKey;
    }

    ItemStack createReroller() {
        ItemStack var1 = new ItemStack(Material.NETHER_STAR);
        ItemMeta var2 = var1.getItemMeta();
        var2.setDisplayName("" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + "Reroller");
        ArrayList var3 = new ArrayList();
        var3.add(ChatColor.GRAY + "Right-click to reroll your shard");
        var3.add(ChatColor.GRAY + "into a different random one.");
        var3.add("");
        var3.add(ChatColor.DARK_GRAY + "Cannot reroll Curse, Dragon, Fire or Sculk.");
        var2.setLore(var3);
        var2.getPersistentDataContainer().set(this.rerollerKey, PersistentDataType.BYTE, (byte)1);
        if (this.getConfig().getBoolean("use-custom-textures", true)) {
            var2.setCustomModelData(771012);
        }

        var2.setEnchantmentGlintOverride(Boolean.TRUE);
        var1.setItemMeta(var2);
        return var1;
    }

    boolean isReroller(ItemStack var1) {
        if (var1 != null && var1.getType() == Material.NETHER_STAR) {
            ItemMeta var2 = var1.getItemMeta();
            return var2 != null && var2.getPersistentDataContainer().has(this.rerollerKey, PersistentDataType.BYTE);
        } else {
            return false;
        }
    }

    ItemStack newShard(ShardType var1) {
        ItemStack var2 = this.createShard(var1);
        ItemMeta var3 = var2.getItemMeta();
        if (var3 != null) {
            var3.setCustomModelData(771001 + var1.ordinal());
            if (this.getConfig().getBoolean("shard-glint", true)) {
                var3.setEnchantmentGlintOverride(Boolean.TRUE);
            }

            var3.setMaxStackSize(1);
            var2.setItemMeta(var3);
        }

        return var2;
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shard") || var4.length < 1 || !var4[0].equalsIgnoreCase("reroller")) {
            return super.onCommand(var1, var2, var3, var4);
        }

        if (!var1.hasPermission("superhumanshard.admin")) {
            var1.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        Player var5 = var4.length > 1 ? Bukkit.getPlayerExact(var4[1]) : (var1 instanceof Player var6 ? var6 : null);
        if (var5 == null) {
            var1.sendMessage(ChatColor.RED + "Usage: /shard reroller <player>");
            return true;
        }

        for (ItemStack var7 : var5.getInventory().addItem(new ItemStack[]{this.createReroller()}).values()) {
            var5.getWorld().dropItemNaturally(var5.getLocation(), var7);
        }

        var1.sendMessage(ChatColor.GREEN + "Gave a Reroller to " + var5.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        List var5 = super.onTabComplete(var1, var2, var3, var4);
        ArrayList var6 = var5 == null ? new ArrayList() : new ArrayList(var5);
        if (var2.getName().equalsIgnoreCase("shard") && var1.hasPermission("superhumanshard.admin")) {
            if (var4.length == 1 && "reroller".startsWith(var4[0].toLowerCase()) && !var6.contains("reroller")) {
                var6.add("reroller");
            } else if (var4.length == 2 && var4[0].equalsIgnoreCase("reroller")) {
                for (Player var8 : Bukkit.getOnlinePlayers()) {
                    if (var8.getName().toLowerCase().startsWith(var4[1].toLowerCase())) {
                        var6.add(var8.getName());
                    }
                }
            }
        }

        return var6;
    }
}
