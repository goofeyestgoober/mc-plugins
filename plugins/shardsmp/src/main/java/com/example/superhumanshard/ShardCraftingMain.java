package com.example.superhumanshard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Crafter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Allay;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class ShardCraftingMain extends SuperhumanShard implements Listener {
    private File craftedFile;
    private YamlConfiguration crafted;
    private File startersFile;
    private YamlConfiguration starters;
    private static final Set<ShardType> CRAFTABLE = EnumSet.of(ShardType.DRAGON);
    public static final int MODEL_BASE = 771001;
    private final Map<UUID, List<ItemStack>> keptOnDeath = new HashMap<>();
    private static final Map<ShardType, Material[]> GRIDS = new EnumMap<>(ShardType.class);
    private final Set<UUID> busy = new HashSet<>();

    @Override
    public void onEnable() {
        super.onEnable();
        this.getConfig().addDefault("craft-once", true);
        this.getConfig().addDefault("announce-crafts", true);
        this.getConfig().addDefault("shard-glint", true);
        this.getConfig().addDefault("starter-shard.enabled", true);
        this.getConfig().addDefault("starter-shard.pool", List.of("velocity", "titan", "tide", "phantom", "ember", "frost", "verdant", "storm"));
        this.getConfig().addDefault("resource-pack.url", "");
        this.getConfig().addDefault("resource-pack.sha1", "");
        this.getConfig().addDefault("resource-pack.required", false);
        this.getConfig().addDefault("resource-pack.prompt", "This server uses custom shard textures.");
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.craftedFile = new File(this.getDataFolder(), "crafted-shards.yml");
        this.crafted = YamlConfiguration.loadConfiguration(this.craftedFile);
        this.startersFile = new File(this.getDataFolder(), "starter-shards.yml");
        this.starters = YamlConfiguration.loadConfiguration(this.startersFile);

        for (ShardType var2 : CRAFTABLE) {
            if (!this.isLocked(var2)) {
                this.addRecipe(var2);
            }
        }

        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player var2x : Bukkit.getOnlinePlayers()) {
                this.upgradeInventory(var2x);
            }
        }, 20L, 20L);
        this.getLogger().info("Shard recipes loaded (craft-once=" + this.craftOnce() + ").");
    }

    private NamespacedKey recipeKey(ShardType var1) {
        return new NamespacedKey(this, "craft_" + var1.id());
    }

    private ItemStack makeShard(ShardType var1) {
        return this.styleShard(this.createShard(var1), var1);
    }

    private int modelFor(ShardType var1) {
        return 771001 + var1.ordinal();
    }

    private ItemStack styleShard(ItemStack var1, ShardType var2) {
        if (var1 == null) {
            return null;
        }

        ItemMeta var3 = var1.getItemMeta();
        if (var3 == null) {
            return var1;
        }

        boolean var4 = false;
        if (!var3.hasCustomModelData() || var3.getCustomModelData() != this.modelFor(var2)) {
            var3.setCustomModelData(this.modelFor(var2));
            var4 = true;
        }

        if (this.getConfig().getBoolean("shard-glint", true) && !var3.hasEnchantmentGlintOverride()) {
            var3.setEnchantmentGlintOverride(true);
            var4 = true;
        }

        if (!var3.hasMaxStackSize() || var3.getMaxStackSize() != 1) {
            var3.setMaxStackSize(1);
            var4 = true;
        }

        if (var4) {
            var1.setItemMeta(var3);
        }

        return var1;
    }

    private void upgradeInventory(Player var1) {
        ItemStack[] var2 = var1.getInventory().getContents();

        for (ItemStack var6 : var2) {
            ShardType var7 = this.typeOf(var6);
            if (var7 != null
                && (
                    !var6.getItemMeta().hasCustomModelData()
                        || var6.getItemMeta().getCustomModelData() != this.modelFor(var7)
                        || !var6.getItemMeta().hasMaxStackSize()
                )) {
                this.styleShard(var6, var7);
            }
        }

        ItemStack var8 = var1.getItemOnCursor();
        ShardType var9 = this.typeOf(var8);
        if (var9 != null) {
            this.styleShard(var8, var9);
        }
    }

    private void sendPack(Player var1) {
        String var2 = this.getConfig().getString("resource-pack.url", "");
        if (var2 != null && !var2.isBlank()) {
            String var3 = this.getConfig().getString("resource-pack.sha1", "");
            byte[] var4 = null;
            if (var3 != null && var3.length() == 40) {
                var4 = new byte[20];

                for (int var5 = 0; var5 < 20; var5++) {
                    var4[var5] = (byte)Integer.parseInt(var3.substring(var5 * 2, var5 * 2 + 2), 16);
                }
            }

            String var7 = this.getConfig().getString("resource-pack.prompt", "This server uses custom shard textures.");
            boolean var6 = this.getConfig().getBoolean("resource-pack.required", false);
            var1.setResourcePack(var2, var4, var7, var6);
        }
    }

    private void addRecipe(ShardType var1) {
        NamespacedKey var2 = this.recipeKey(var1);
        if (Bukkit.getRecipe(var2) == null) {
            ShapedRecipe var3 = new ShapedRecipe(var2, this.makeShard(var1));
            if (var1 == ShardType.DRAGON) {
                var3.shape(new String[]{"D"}).setIngredient('D', Material.DRAGON_EGG);
                Bukkit.addRecipe(var3);

                for (Player var5 : Bukkit.getOnlinePlayers()) {
                    var5.discoverRecipe(var2);
                }
            }
        }
    }

    private void removeRecipe(ShardType var1) {
        NamespacedKey var2 = this.recipeKey(var1);

        for (Player var4 : Bukkit.getOnlinePlayers()) {
            var4.undiscoverRecipe(var2);
        }

        Bukkit.removeRecipe(var2);
    }

    private boolean craftOnce() {
        return this.getConfig().getBoolean("craft-once", true);
    }

    private boolean isLocked(ShardType var1) {
        return this.craftOnce() && this.crafted.contains(var1.id());
    }

    private void lock(ShardType var1, Player var2) {
        this.crafted.set(var1.id() + ".player", var2.getName());
        this.crafted.set(var1.id() + ".uuid", var2.getUniqueId().toString());
        this.crafted.set(var1.id() + ".time", System.currentTimeMillis());
        this.saveCrafted();
    }

    private void saveCrafted() {
        try {
            this.crafted.save(this.craftedFile);
        } catch (IOException var2) {
            this.getLogger().severe("Could not save crafted-shards.yml: " + var2);
        }
    }

    private ShardType typeOf(ItemStack var1) {
        return var1 == null ? null : this.getShardType(var1);
    }

    private void giveStarter(Player var1) {
        if (this.getConfig().getBoolean("starter-shard.enabled", true)) {
            String var2 = var1.getUniqueId().toString();
            if (!this.starters.contains(var2)) {
                ArrayList var3 = new ArrayList();

                for (String var5 : this.getConfig().getStringList("starter-shard.pool")) {
                    ShardType var6 = ShardType.fromId(var5);
                    if (var6 != null && var6 != ShardType.DRAGON && !var3.contains(var6)) {
                        var3.add(var6);
                    }
                }

                if (!var3.isEmpty()) {
                    ShardType var11 = null;

                    for (ItemStack var8 : var1.getInventory().getContents()) {
                        ShardType var9 = this.typeOf(var8);
                        if (var9 != null && var9 != ShardType.DRAGON) {
                            var11 = var9;
                            break;
                        }
                    }

                    ShardType var13 = var11 != null ? var11 : (ShardType)var3.get(ThreadLocalRandom.current().nextInt(var3.size()));
                    this.starters.set(var2 + ".name", var1.getName());
                    this.starters.set(var2 + ".shard", var13.id());

                    try {
                        this.starters.save(this.startersFile);
                    } catch (IOException var10) {
                        this.getLogger().severe("Could not save starter-shards.yml: " + var10);
                    }

                    if (var11 == null) {
                        ItemStack var15 = this.makeShard(var13);

                        for (ItemStack var17 : var1.getInventory().addItem(new ItemStack[]{var15}).values()) {
                            var1.getWorld().dropItemNaturally(var1.getLocation(), var17);
                        }

                        var1.sendMessage(
                            ChatColor.GOLD
                                + "You have been given the "
                                + ChatColor.LIGHT_PURPLE
                                + var13.displayName()
                                + ChatColor.GOLD
                                + "! "
                                + ChatColor.GRAY
                                + var13.passiveSummary()
                                + " | "
                                + var13.abilityName()
                                + " (/ability1)"
                        );
                        var1.playSound(var1.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                    }
                }
            }
        }
    }

    private boolean bound(ItemStack var1) {
        if (SpeedHelp.isSpeedItem(this, var1)) {
            return true;
        }

        ShardType var2 = this.typeOf(var1);
        return var2 != null && ShardRules.isBound(var2);
    }

    private void denyMsg(HumanEntity var1) {
        var1.sendMessage(ChatColor.RED + "Your shard is bound to you. Only the Dragon Shard can be dropped.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBoundDrop(PlayerDropItemEvent var1) {
        if (this.bound(var1.getItemDrop().getItemStack())) {
            var1.setCancelled(true);
            this.denyMsg(var1.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBoundClick(InventoryClickEvent var1) {
        InventoryAction var2 = var1.getAction();
        if ((var2 != InventoryAction.DROP_ALL_SLOT && var2 != InventoryAction.DROP_ONE_SLOT || !this.bound(var1.getCurrentItem()))
            && (var2 != InventoryAction.DROP_ALL_CURSOR && var2 != InventoryAction.DROP_ONE_CURSOR || !this.bound(var1.getCursor()))) {
            Inventory var3 = var1.getView().getTopInventory();
            if (var3.getType() != InventoryType.CRAFTING) {
                boolean var4 = var1.getClickedInventory() == var3;
                boolean var5 = false;
                if (var4 && this.bound(var1.getCursor())) {
                    var5 = true;
                }

                if (!var4 && var1.isShiftClick() && this.bound(var1.getCurrentItem())) {
                    var5 = true;
                }

                if (var4 && var1.getClick() == ClickType.NUMBER_KEY && this.bound(var1.getWhoClicked().getInventory().getItem(var1.getHotbarButton()))) {
                    var5 = true;
                }

                if (var4 && var1.getClick() == ClickType.SWAP_OFFHAND && this.bound(var1.getWhoClicked().getInventory().getItemInOffHand())) {
                    var5 = true;
                }

                if (var2 == InventoryAction.COLLECT_TO_CURSOR && this.bound(var1.getCursor())) {
                    var5 = true;
                }

                if (var5) {
                    var1.setCancelled(true);
                    this.denyMsg(var1.getWhoClicked());
                }
            }
        } else {
            var1.setCancelled(true);
            this.denyMsg(var1.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBoundDrag(InventoryDragEvent var1) {
        if (this.bound(var1.getOldCursor())) {
            Inventory var2 = var1.getView().getTopInventory();
            if (var2.getType() != InventoryType.CRAFTING) {
                for (int var4 : var1.getRawSlots()) {
                    if (var4 < var2.getSize()) {
                        var1.setCancelled(true);
                        this.denyMsg(var1.getWhoClicked());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBoundEntity(PlayerInteractEntityEvent var1) {
        if (var1.getRightClicked() instanceof ItemFrame || var1.getRightClicked() instanceof Allay) {
            ItemStack var2 = var1.getPlayer().getInventory().getItem(var1.getHand());
            if (this.bound(var2)) {
                var1.setCancelled(true);
                this.denyMsg(var1.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBoundArmorStand(PlayerArmorStandManipulateEvent var1) {
        if (this.bound(var1.getPlayerItem())) {
            var1.setCancelled(true);
            this.denyMsg(var1.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBoundDeath(PlayerDeathEvent var1) {
        if (!var1.getKeepInventory()) {
            ArrayList var2 = new ArrayList();
            var1.getDrops().removeIf(var2x -> {
                if (this.bound(var2x)) {
                    var2.add(var2x.clone());
                    return true;
                } else {
                    return false;
                }
            });
            if (!var2.isEmpty()) {
                this.keptOnDeath.computeIfAbsent(var1.getEntity().getUniqueId(), var0 -> new ArrayList<>()).addAll(var2);
            }
        }
    }

    @EventHandler
    public void onBoundRespawn(PlayerRespawnEvent var1) {
        Player var2 = var1.getPlayer();
        List<ItemStack> var3 = this.keptOnDeath.remove(var2.getUniqueId());
        if (var3 != null) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (ItemStack var3x : var3) {
                    for (ItemStack var5 : var2.getInventory().addItem(new ItemStack[]{var3x}).values()) {
                        var2.getInventory().setItemInOffHand(var5);
                    }
                }
            }, 2L);
        }
    }

    private void returnKept(Player var1) {
        if (!var1.isDead()) {
            List<ItemStack> var2 = this.keptOnDeath.remove(var1.getUniqueId());
            if (var2 != null) {
                for (ItemStack var4 : var2) {
                    var1.getInventory().addItem(new ItemStack[]{var4});
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent var1) {
        this.upgradeInventory(var1.getPlayer());
        this.sendPack(var1.getPlayer());
        this.giveStarter(var1.getPlayer());
        this.returnKept(var1.getPlayer());

        for (ShardType var3 : CRAFTABLE) {
            NamespacedKey var4 = this.recipeKey(var3);
            if (this.isLocked(var3)) {
                var1.getPlayer().undiscoverRecipe(var4);
            } else {
                var1.getPlayer().discoverRecipe(var4);
            }
        }
    }

    private ShardType matchGrid(ItemStack[] var1) {
        return ShardPatch.matchDragon(var1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(PrepareItemCraftEvent var1) {
        CraftingInventory var2 = var1.getInventory();

        for (ItemStack var6 : var2.getMatrix()) {
            if (this.typeOf(var6) != null) {
                var2.setResult(null);
                return;
            }
        }

        ShardType var7 = this.matchGrid(var2.getMatrix());
        if (var7 != null) {
            var2.setResult(this.isLocked(var7) ? null : this.makeShard(var7));
        } else if (this.typeOf(var2.getResult()) != null) {
            var2.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onResultClick(InventoryClickEvent var1) {
        if (var1.getInventory() instanceof CraftingInventory var2) {
            if (var1.getSlotType() == SlotType.RESULT) {
                ShardType var7 = this.typeOf(var2.getResult());
                ShardType var4 = this.matchGrid(var2.getMatrix());
                if (var7 != null || var4 != null) {
                    var1.setCancelled(true);
                    if (var1.getWhoClicked() instanceof Player var5) {
                        if (var4 == null) {
                            var2.setResult(null);
                        } else if (this.busy.add(var5.getUniqueId())) {
                            boolean var8 = var1.isShiftClick()
                                || var1.getClick() == ClickType.NUMBER_KEY
                                || var1.getClick() == ClickType.DROP
                                || var1.getClick() == ClickType.CONTROL_DROP;
                            Bukkit.getScheduler()
                                .runTask(
                                    this,
                                    () -> {
                                        try {
                                            ShardType var4x = this.matchGrid(var2.getMatrix());
                                            if (var4x == null) {
                                                return;
                                            }

                                            if (!this.isLocked(var4x)) {
                                                ItemStack[] var5x = var2.getMatrix();

                                                for (int var6 = 0; var6 < var5x.length; var6++) {
                                                    if (var5x[var6] != null) {
                                                        int var7x = var5x[var6].getAmount() - 1;
                                                        var5x[var6] = var7x <= 0 ? null : var5x[var6].clone();
                                                        if (var5x[var6] != null) {
                                                            var5x[var6].setAmount(var7x);
                                                        }
                                                    }
                                                }

                                                var2.setMatrix(var5x);
                                                ItemStack var13 = this.makeShard(var4x);
                                                ItemStack var14 = var5.getItemOnCursor();
                                                if (var8 || var14 != null && !var14.getType().isAir()) {
                                                    for (ItemStack var9 : var5.getInventory().addItem(new ItemStack[]{var13}).values()) {
                                                        var5.getWorld().dropItemNaturally(var5.getLocation(), var9);
                                                    }
                                                } else {
                                                    var5.setItemOnCursor(var13);
                                                }

                                                if (this.craftOnce()) {
                                                    this.lock(var4x, var5);
                                                    this.removeRecipe(var4x);
                                                    if (this.getConfig().getBoolean("announce-crafts", true)) {
                                                        Bukkit.broadcastMessage(
                                                            ChatColor.GOLD
                                                                + var5.getName()
                                                                + ChatColor.YELLOW
                                                                + " has crafted the "
                                                                + ChatColor.LIGHT_PURPLE
                                                                + var4x.displayName()
                                                                + ChatColor.YELLOW
                                                                + "! Its recipe is now locked."
                                                        );

                                                        for (Player var17 : Bukkit.getOnlinePlayers()) {
                                                            var17.playSound(var17.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                                                        }
                                                    }
                                                }

                                                ShardType var16 = this.matchGrid(var2.getMatrix());
                                                var2.setResult(var16 != null && !this.isLocked(var16) ? this.makeShard(var16) : null);
                                                var5.updateInventory();
                                                return;
                                            }

                                            var5.sendMessage(ChatColor.RED + "The " + var4x.displayName() + " has already been crafted.");
                                            var2.setResult(null);
                                        } finally {
                                            this.busy.remove(var5.getUniqueId());
                                        }
                                    }
                                );
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCrafter(CrafterCraftEvent var1) {
        if (this.typeOf(var1.getResult()) != null) {
            var1.setCancelled(true);
        } else {
            if (var1.getBlock().getState() instanceof Crafter var2) {
                for (ItemStack var6 : var2.getInventory().getContents()) {
                    if (this.typeOf(var6) != null) {
                        var1.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShardPlace(PlayerInteractEvent var1) {
        if (var1.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (this.typeOf(var1.getItem()) != null) {
                var1.setUseItemInHand(Result.DENY);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent var1) {
        if (this.typeOf(var1.getItem()) != null) {
            var1.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBrew(BrewEvent var1) {
        if (this.typeOf(var1.getContents().getIngredient()) != null) {
            var1.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBrewingClick(InventoryClickEvent var1) {
        if (var1.getView().getTopInventory().getType() == InventoryType.BREWING) {
            boolean var2 = var1.getClickedInventory() == var1.getView().getTopInventory();
            if (var2 && this.typeOf(var1.getCursor()) != null) {
                var1.setCancelled(true);
            }

            if (!var2 && var1.isShiftClick() && this.typeOf(var1.getCurrentItem()) != null) {
                var1.setCancelled(true);
            }

            if (var2 && var1.getClick() == ClickType.NUMBER_KEY && this.typeOf(var1.getWhoClicked().getInventory().getItem(var1.getHotbarButton())) != null) {
                var1.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent var1) {
        if (var1.getDestination().getType() == InventoryType.BREWING && this.typeOf(var1.getItem()) != null) {
            var1.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvil(PrepareAnvilEvent var1) {
        for (ItemStack var5 : var1.getInventory().getContents()) {
            if (this.typeOf(var5) != null) {
                var1.setResult(null);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithing(PrepareSmithingEvent var1) {
        for (ItemStack var5 : var1.getInventory().getContents()) {
            if (this.typeOf(var5) != null) {
                var1.setResult(null);
                return;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shardrecipes")) {
            return super.onCommand(var1, var2, var3, var4);
        }

        if (!var1.hasPermission("superhumanshard.admin")) {
            var1.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (var4.length != 0 && !var4[0].equalsIgnoreCase("status")) {
            if (var4[0].equalsIgnoreCase("reset") && var4.length >= 2) {
                ArrayList<ShardType> var8 = new ArrayList<>();
                if (var4[1].equalsIgnoreCase("all")) {
                    var8.addAll(CRAFTABLE);
                } else {
                    ShardType var9 = ShardType.fromId(var4[1]);
                    if (var9 == null || !CRAFTABLE.contains(var9)) {
                        var1.sendMessage(ChatColor.RED + "Not a craftable shard: " + var4[1]);
                        return true;
                    }

                    var8.add(var9);
                }

                for (ShardType var11 : var8) {
                    this.crafted.set(var11.id(), null);
                    this.addRecipe(var11);
                }

                this.saveCrafted();
                var1.sendMessage(ChatColor.GREEN + "Reset recipe(s): " + var4[1].toLowerCase(Locale.ROOT));
                return true;
            } else {
                var1.sendMessage(ChatColor.YELLOW + "Usage: /shardrecipes [status | reset <shard|all>]");
                return true;
            }
        } else {
            var1.sendMessage(ChatColor.GOLD + "Shard recipe status (craft-once=" + this.craftOnce() + "):");

            for (ShardType var6 : CRAFTABLE) {
                String var7 = this.crafted.getString(var6.id() + ".player");
                var1.sendMessage(
                    ChatColor.GRAY + " - " + var6.displayName() + ": " + (var7 == null ? ChatColor.GREEN + "available" : ChatColor.RED + "crafted by " + var7)
                );
            }

            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shardrecipes")) {
            return super.onTabComplete(var1, var2, var3, var4);
        }

        ArrayList<String> var5 = new ArrayList<>();
        if (var4.length == 1) {
            var5.add("status");
            var5.add("reset");
        } else if (var4.length == 2 && var4[0].equalsIgnoreCase("reset")) {
            var5.add("all");

            for (ShardType var7 : CRAFTABLE) {
                var5.add(var7.id());
            }
        }

        var5.removeIf(var1x -> !var1x.startsWith(var4[var4.length - 1].toLowerCase(Locale.ROOT)));
        return var5;
    }

    static {
        Material var0 = Material.OBSIDIAN;
        Material var1 = Material.GHAST_TEAR;
        Material var2 = Material.DRAGON_EGG;
        GRIDS.put(ShardType.DRAGON, new Material[]{var0, var1, var0, var1, var2, var1, var0, var1, var0});
    }
}
