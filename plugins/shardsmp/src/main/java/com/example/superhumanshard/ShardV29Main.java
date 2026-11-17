package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ShardV29Main extends ShardV28Main {
    static final int SPEED_MODEL = 771013;
    private NamespacedKey keyId;
    private NamespacedKey keyTier;
    private NamespacedKey keyVictims;
    private final Map<UUID, Long> dashReady = new HashMap<>();
    private final Map<UUID, Long> surgeReady = new HashMap<>();
    private final Map<UUID, Long> noFallUntil = new HashMap<>();
    private final Map<UUID, List<ItemStack>> kept = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();
        this.keyId = new NamespacedKey(this, "speed_shard");
        this.keyTier = new NamespacedKey(this, "speed_tier");
        this.keyVictims = new NamespacedKey(this, "speed_victims");
        Bukkit.getPluginManager().registerEvents(new ShardV29Main.SpeedEvents(), this);
        Bukkit.getScheduler().runTaskTimer(this, () -> this.tickPassive(), 20L, 20L);
    }

    boolean isSpeed(ItemStack var1) {
        return var1 != null && var1.getType() == Material.FEATHER && var1.hasItemMeta()
            ? var1.getItemMeta().getPersistentDataContainer().has(this.keyId, PersistentDataType.BYTE)
            : false;
    }

    int tierOf(ItemStack var1) {
        Byte var2 = (Byte)var1.getItemMeta().getPersistentDataContainer().get(this.keyTier, PersistentDataType.BYTE);
        return var2 == null ? 1 : Math.max(1, Math.min(3, var2));
    }

    List<String> victimsOf(ItemStack var1) {
        String var2 = (String)var1.getItemMeta().getPersistentDataContainer().get(this.keyVictims, PersistentDataType.STRING);
        ArrayList var3 = new ArrayList();
        if (var2 != null && !var2.isEmpty()) {
            for (String var7 : var2.split(",")) {
                if (!var7.isEmpty()) {
                    var3.add(var7);
                }
            }
        }

        return var3;
    }

    ItemStack buildSpeed(int var1, List<String> var2) {
        ItemStack var3 = new ItemStack(Material.FEATHER);
        this.writeSpeed(var3, var1, var2);
        return var3;
    }

    private static Component line(String var0, TextColor var1) {
        return Component.text(var0, var1).decoration(TextDecoration.ITALIC, false);
    }

    private int lvl(String var1, int var2) {
        return Math.max(1, Math.min(10, this.getConfig().getInt(var1, var2)));
    }

    void writeSpeed(ItemStack var1, int var2, List<String> var3) {
        ItemMeta var4 = var1.getItemMeta();
        int var5 = this.lvl("speed-shard.speed-level", 2);
        int var6 = this.lvl("speed-shard.tier3-speed-level", 3);
        int var7 = this.lvl("speed-shard.tier3-haste-level", 3);
        SpeedHelp.applyLore(this, var4, var2, var5, var6, var7);
        Object var8 = null;
        Object var9 = null;
        PersistentDataContainer var10 = var4.getPersistentDataContainer();
        var10.set(this.keyId, PersistentDataType.BYTE, (byte)1);
        var10.set(this.keyTier, PersistentDataType.BYTE, (byte)var2);
        var10.set(this.keyVictims, PersistentDataType.STRING, String.join(",", var3));
        CustomModelDataComponent var11 = var4.getCustomModelDataComponent();
        var11.setFloats(List.of(771013.0F, var2 >= 3 ? 1.0F : 0.0F));
        var4.setCustomModelDataComponent(var11);
        if (this.getConfig().getBoolean("shard-glint", true)) {
            var4.setEnchantmentGlintOverride(true);
        }

        var4.setMaxStackSize(1);
        var1.setItemMeta(var4);
    }

    private static String roman(int var0) {
        String[] var1 = new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return var0 >= 0 && var0 < var1.length ? var1[var0] : String.valueOf(var0);
    }

    int carriedTier(Player var1) {
        int var2 = 0;

        for (ItemStack var6 : var1.getInventory().getContents()) {
            if (this.isSpeed(var6)) {
                var2 = Math.max(var2, this.tierOf(var6));
            }
        }

        return var2;
    }

    private boolean carriesOtherShard(Player var1) {
        for (ItemStack var5 : var1.getInventory().getContents()) {
            if (var5 != null && this.getShardType(var5) != null) {
                return true;
            }
        }

        return false;
    }

    private void give(Player var1, ItemStack var2) {
        HashMap<Integer, ItemStack> var3 = var1.getInventory().addItem(new ItemStack[]{var2});

        for (ItemStack var5 : var3.values()) {
            var1.getWorld().dropItemNaturally(var1.getLocation(), var5);
        }
    }

    private void effect(Player var1, PotionEffectType var2, int var3) {
        int var4 = var3 - 1;
        PotionEffect var5 = var1.getPotionEffect(var2);
        if (var5 == null || var5.getAmplifier() <= var4) {
            var1.addPotionEffect(new PotionEffect(var2, 60, var4, true, false, true));
        }
    }

    private void tickPassive() {
        for (Player var2 : Bukkit.getOnlinePlayers()) {
            if (!var2.isDead()) {
                SpeedHelp.refresh(this, var2);
                int var3 = this.carriedTier(var2);
                if (var3 != 0) {
                    if (var3 >= 3) {
                        this.effect(var2, PotionEffectType.SPEED, this.lvl("speed-shard.tier3-speed-level", 3));
                        this.effect(var2, PotionEffectType.HASTE, this.lvl("speed-shard.tier3-haste-level", 3));
                    } else {
                        this.effect(var2, PotionEffectType.SPEED, this.lvl("speed-shard.speed-level", 2));
                    }
                }
            }
        }
    }

    private void creditKill(Player var1, Player var2) {
        for (int var3 = 0; var3 < var1.getInventory().getSize(); var3++) {
            ItemStack var4 = var1.getInventory().getItem(var3);
            if (this.isSpeed(var4)) {
                int var5 = this.tierOf(var4);
                if (var5 >= 3) {
                    return;
                }

                List var6 = this.victimsOf(var4);
                String var7 = var2.getUniqueId().toString();
                if (var6.contains(var7)) {
                    return;
                }

                var6.add(var7);
                int var8 = var5;

                for (int var9 = 0; var9 < 2; var9++) {
                    if (var5 == 1 && var6.size() >= this.need2()) {
                        var5 = 2;
                        if (this.resetOnUpgrade()) {
                            var6.clear();
                        }
                    } else if (var5 == 2 && var6.size() >= this.need3()) {
                        var5 = 3;
                    }
                }

                this.writeSpeed(var4, var5, var6);
                var1.getInventory().setItem(var3, var4);
                if (var5 > var8) {
                    var1.sendMessage(Component.text("Your Speed Shard reached Tier " + var5 + "!", NamedTextColor.YELLOW));
                    var1.playSound(var1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.4F);
                } else {
                    var1.sendActionBar(Component.text("Speed Shard: kill counted", NamedTextColor.YELLOW));
                }
                break;
            }
        }
    }

    private void speedAbility(Player var1, int var2) {
        int var3 = SpeedHelp.heldTier(this, var1);
        if (var3 == 0) {
            var1.sendMessage(Component.text("You must hold the Speed Shard in your main hand or offhand.", NamedTextColor.RED));
        } else if (var2 == 2 && var3 < 2) {
            var1.sendMessage(Component.text("Speed Surge needs Tier 2 (" + this.need2() + " different player kills).", NamedTextColor.RED));
        } else {
            long var4 = System.currentTimeMillis();
            Map var6 = var2 == 1 ? this.dashReady : this.surgeReady;
            Long var7 = (Long)var6.get(var1.getUniqueId());
            if (var7 != null && var7 > var4) {
                long var16 = (var7 - var4 + 999L) / 1000L;
                var1.sendActionBar(Component.text((var2 == 1 ? "Dash" : "Speed Surge") + " ready in " + var16 + "s", NamedTextColor.RED));
            } else if (var2 == 2) {
                this.surge(var1, var4);
            } else {
                long var8 = Math.max(1L, this.getConfig().getLong("speed-shard.dash-cooldown", 20L));
                this.dashReady.put(var1.getUniqueId(), var4 + var8 * 1000L);
                this.noFallUntil.put(var1.getUniqueId(), var4 + 5000L);
                double var10 = Math.max(0.3, this.getConfig().getDouble("speed-shard.dash-strength", 1.8));
                Vector var12 = var1.getLocation().getDirection();
                Vector var13 = new Vector(var12.getX(), 0.0, var12.getZ());
                if (var13.lengthSquared() < 1.0E-4) {
                    var13 = var12.clone();
                }

                var13.normalize().multiply(var10);
                var13.setY(0.2 + Math.max(0.0, var12.getY()) * 0.5);
                var1.setVelocity(var13);
                var1.getWorld().playSound(var1.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.2F, 1.8F);
                var1.getWorld().playSound(var1.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.4F, 1.9F);
                var1.sendActionBar(Component.text("Dash!", NamedTextColor.YELLOW));
                DustOptions var14 = new DustOptions(Color.fromRGB(255, 225, 60), 1.4F);

                for (int var15 = 0; var15 < 12; var15++) {
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (var1.isOnline() && !var1.isDead()) {
                            var1.getWorld().spawnParticle(Particle.DUST, var1.getLocation().add(0.0, 0.9, 0.0), 12, 0.3, 0.4, 0.3, 0.0, var14);
                        }
                    }, var15);
                }
            }
        }
    }

    private void surge(Player var1, long var2) {
        long var4 = Math.max(1L, this.getConfig().getLong("speed-shard.surge-cooldown", 30L));
        int var6 = Math.max(1, this.getConfig().getInt("speed-shard.surge-seconds", 6));
        int var7 = this.lvl("speed-shard.surge-speed-level", 5);
        this.surgeReady.put(var1.getUniqueId(), var2 + var4 * 1000L);
        var1.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, var6 * 20, var7 - 1, false, true, true));
        var1.getWorld().playSound(var1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8F, 2.0F);
        var1.getWorld().playSound(var1.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.6F, 1.8F);
        var1.sendActionBar(Component.text("Speed Surge! Speed " + roman(var7) + " for " + var6 + "s", NamedTextColor.YELLOW));
        DustOptions var8 = new DustOptions(Color.fromRGB(255, 225, 60), 1.4F);

        for (int var9 = 0; var9 < var6 * 2; var9++) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (var1.isOnline() && !var1.isDead()) {
                    var1.getWorld().spawnParticle(Particle.DUST, var1.getLocation().add(0.0, 1.0, 0.0), 10, 0.35, 0.5, 0.35, 0.0, var8);
                    var1.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, var1.getLocation().add(0.0, 1.0, 0.0), 4, 0.4, 0.5, 0.4, 0.0);
                }
            }, var9 * 10);
        }
    }

    private boolean giveSpeedCommand(CommandSender var1, String[] var2) {
        if (!var1.hasPermission("superhumanshard.admin")) {
            var1.sendMessage(Component.text("You don't have permission.", NamedTextColor.RED));
            return true;
        }

        int var3 = 1;
        Player var4 = null;

        for (int var5 = 1; var5 < var2.length; var5++) {
            if (!var2[var5].equals("1") && !var2[var5].equals("2") && !var2[var5].equals("3")) {
                var4 = Bukkit.getPlayerExact(var2[var5]);
                if (var4 == null) {
                    var1.sendMessage(Component.text("That player is not online.", NamedTextColor.RED));
                    return true;
                }
            } else {
                var3 = Integer.parseInt(var2[var5]);
            }
        }

        if (var4 == null) {
            if (!(var1 instanceof Player var6)) {
                var1.sendMessage(Component.text("Usage: /shard speed [1|2|3] <player>", NamedTextColor.RED));
                return true;
            }

            var4 = var6;
        }

        this.give(var4, this.buildSpeed(var3, new ArrayList<>()));
        var1.sendMessage(Component.text("Gave a Tier " + var3 + " Speed Shard to " + var4.getName() + ".", NamedTextColor.YELLOW));
        if (var4 != var1) {
            var4.sendMessage(Component.text("You received a Speed Shard.", NamedTextColor.YELLOW));
        }

        return true;
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        String var5 = var2.getName().toLowerCase(Locale.ROOT);
        if (var5.equals("shard") && var4.length >= 1) {
            if (var4[0].equalsIgnoreCase("speed")) {
                return this.giveSpeedCommand(var1, var4);
            }

            if (var4[0].equalsIgnoreCase("all")) {
                boolean var11 = super.onCommand(var1, var2, var3, var4);
                if (var1.hasPermission("superhumanshard.admin")) {
                    String[] var12 = new String[var4.length];
                    var12[0] = "speed";
                    System.arraycopy(var4, 1, var12, 1, var4.length - 1);
                    boolean var13 = true;

                    for (int var14 = 1; var14 < var12.length; var14++) {
                        if (!var12[var14].matches("[123]") && Bukkit.getPlayerExact(var12[var14]) == null) {
                            var13 = false;
                        }
                    }

                    if (var13) {
                        this.giveSpeedCommand(var1, var12);
                    }
                }

                return var11;
            }
        }

        if ((var5.equals("ability1") || var5.equals("ability2") || var5.equals("ability")) && var1 instanceof Player var6) {
            int var7 = 0;
            String var8 = null;
            if (var5.equals("ability1")) {
                var7 = 1;
                if (var4.length > 0) {
                    var8 = var4[0];
                }
            } else if (var5.equals("ability2")) {
                var7 = 2;
                if (var4.length > 0) {
                    var8 = var4[0];
                }
            } else if (var4.length > 0 && (var4[0].equals("1") || var4[0].equals("2"))) {
                var7 = Integer.parseInt(var4[0]);
                if (var4.length > 1) {
                    var8 = var4[1];
                }
            }

            if (var7 != 0) {
                boolean var9 = var8 != null && var8.equalsIgnoreCase("speed");
                boolean var10 = var8 == null && SpeedHelp.heldTier(this, var6) > 0 && !SpeedHelp.holdsOtherShard(this, var6);
                if (var9 || var10) {
                    this.speedAbility(var6, var7);
                    return true;
                }
            }
        }

        return super.onCommand(var1, var2, var3, var4);
    }

    private static void addMatching(List<String> var0, String var1, String var2) {
        if (var2.toLowerCase(Locale.ROOT).startsWith(var1.toLowerCase(Locale.ROOT)) && !var0.contains(var2)) {
            var0.add(var2);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        List var5 = super.onTabComplete(var1, var2, var3, var4);
        String var6 = var2.getName().toLowerCase(Locale.ROOT);
        if (var6.equals("shard") && var4.length >= 1) {
            if (var4[0].equalsIgnoreCase("speed") && var4.length >= 2) {
                ArrayList var14 = new ArrayList();
                String var15 = var4[var4.length - 1];
                if (var4.length == 2) {
                    for (String var12 : new String[]{"1", "2", "3"}) {
                        addMatching(var14, var15, var12);
                    }
                }

                for (Player var18 : Bukkit.getOnlinePlayers()) {
                    addMatching(var14, var15, var18.getName());
                }

                return var14;
            }

            if (var4.length == 1) {
                ArrayList var13 = var5 == null ? new ArrayList() : new ArrayList(var5);
                addMatching(var13, var4[0], "speed");
                return var13;
            }
        }

        boolean var7 = (var6.equals("ability1") || var6.equals("ability2")) && var4.length == 1;
        boolean var8 = var6.equals("ability") && var4.length == 2;
        if (!var7 && !var8) {
            return var5;
        }

        ArrayList var9 = var5 == null ? new ArrayList() : new ArrayList(var5);
        addMatching(var9, var4[var4.length - 1], "speed");
        return var9;
    }

    private void returnKept(Player var1) {
        List<ItemStack> var2 = this.kept.remove(var1.getUniqueId());
        if (var2 != null) {
            for (ItemStack var4 : var2) {
                this.give(var1, var4);
            }
        }
    }

    public class SpeedEvents implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onDeath(PlayerDeathEvent var1) {
            Player var2 = var1.getEntity();
            Player var3 = var2.getKiller();
            if (var3 != null && !var3.equals(var2)) {
                ShardV29Main.this.creditKill(var3, var2);
            }

            if (!var1.getKeepInventory()) {
                ArrayList var4 = new ArrayList();
                Iterator var5 = var1.getDrops().iterator();

                while (var5.hasNext()) {
                    ItemStack var6 = (ItemStack)var5.next();
                    if (ShardV29Main.this.isSpeed(var6)) {
                        var4.add(var6);
                        var5.remove();
                    }
                }

                if (!var4.isEmpty()) {
                    ShardV29Main.this.kept.computeIfAbsent(var2.getUniqueId(), var0 -> new ArrayList<>()).addAll(var4);
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onRespawn(PlayerRespawnEvent var1) {
            Player var2 = var1.getPlayer();
            if (ShardV29Main.this.kept.containsKey(var2.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(ShardV29Main.this, () -> ShardV29Main.this.returnKept(var2), 6L);
            }
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent var1) {
            Player var2 = var1.getPlayer();
            if (ShardV29Main.this.kept.containsKey(var2.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(ShardV29Main.this, () -> {
                    if (!var2.isDead()) {
                        ShardV29Main.this.returnKept(var2);
                    }
                }, 20L);
            }
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onFall(EntityDamageEvent var1) {
            if (var1.getCause() == DamageCause.FALL) {
                if (var1.getEntity() instanceof Player var2) {
                    Long var4 = ShardV29Main.this.noFallUntil.get(var2.getUniqueId());
                    if (var4 != null && var4 > System.currentTimeMillis()) {
                        var1.setCancelled(true);
                    }
                }
            }
        }

        @EventHandler
        public void onCraft(PrepareItemCraftEvent var1) {
            for (ItemStack var5 : var1.getInventory().getMatrix()) {
                if (ShardV29Main.this.isSpeed(var5)) {
                    var1.getInventory().setResult(null);
                    return;
                }
            }
        }
    }
}
