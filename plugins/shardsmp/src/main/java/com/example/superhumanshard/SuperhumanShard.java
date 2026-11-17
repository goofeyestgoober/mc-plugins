package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SuperhumanShard extends JavaPlugin implements Listener {
    private NamespacedKey typeKey;
    private NamespacedKey legacyKey;
    private NamespacedKey fireballKey;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<UUID, List<ItemStack>> deathShards = new HashMap<>();
    private final Set<UUID> groundPounding = new HashSet<>();
    private final Set<UUID> canDoubleJump = new HashSet<>();
    private final Set<UUID> hasDoubleJumped = new HashSet<>();
    private final Set<UUID> dragonScalesActive = new HashSet<>();
    private final Map<Block, Long> frostedWater = new HashMap<>();

    public void onEnable() {
        this.typeKey = new NamespacedKey(this, "shard_type");
        this.legacyKey = new NamespacedKey(this, "superhuman_shard");
        this.fireballKey = new NamespacedKey(this, "ember_fireball");
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        (new BukkitRunnable() {
            public void run() {
                for (Player var2 : Bukkit.getOnlinePlayers()) {
                    SuperhumanShard.this.applyPassives(var2);
                    SuperhumanShard.this.updateDoubleJump(var2);
                    SuperhumanShard.this.updateDragonHearts(var2);
                }
            }
        }).runTaskTimer(this, 20L, 20L);
        (new BukkitRunnable() {
            public void run() {
                Iterator var1 = Bukkit.getOnlinePlayers().iterator();

                while (var1.hasNext()) {
                    SuperhumanShard.this.spawnShardParticles((Player)var1.next());
                }
            }
        }).runTaskTimer(this, 10L, 8L);
        Bukkit.getPluginManager().registerEvents(new ShardFireproof(this), this);
        Bukkit.getScheduler().runTaskTimer(this, this::meltFrostedWater, 20L, 20L);
        Bukkit.getPluginManager().registerEvents(new PierceDamage(), this);
    }

    protected boolean hasShard(Player var1, ShardType var2) {
        ItemStack[] var3 = var1.getInventory().getContents();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; var5++) {
            if (this.getShardType(var3[var5]) == var2) {
                return true;
            }
        }

        return false;
    }

    private ShardType getHeldOrAnyShard(Player var1) {
        ShardType var2 = this.getShardType(var1.getInventory().getItemInMainHand());
        if (var2 == null) {
            var2 = this.getShardType(var1.getInventory().getItemInOffHand());
        }

        if (var2 == null) {
            ItemStack[] var3 = var1.getInventory().getContents();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; var5++) {
                var2 = this.getShardType(var3[var5]);
                if (var2 != null) {
                    break;
                }
            }
        }

        return var2;
    }

    private void applyPassives(Player var1) {
        ItemStack[] var2 = var1.getInventory().getContents();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; var4++) {
            ShardType var5 = this.getShardType(var2[var4]);
            if (var5 != null) {
                for (ShardType.Passive var9 : var5.passives()) {
                    var1.addPotionEffect(new PotionEffect(var9.type(), 100, var9.amplifier(), true, false, true));
                }
            }
        }
    }

    private void updateDoubleJump(Player var1) {
        UUID var2 = var1.getUniqueId();
        if (this.hasShard(var1, ShardType.VELOCITY)) {
            if (var1.isOnGround()) {
                this.hasDoubleJumped.remove(var2);
                var1.setAllowFlight(true);
                this.canDoubleJump.add(var2);
            }
        } else {
            this.canDoubleJump.remove(var2);
            this.hasDoubleJumped.remove(var2);
            if (var1.getGameMode() != GameMode.CREATIVE && var1.getGameMode() != GameMode.SPECTATOR) {
                var1.setAllowFlight(false);
                var1.setFlying(false);
            }
        }
    }

    private void updateDragonHearts(Player var1) {
        AttributeInstance var2 = var1.getAttribute(Attribute.MAX_HEALTH);
        if (var2 != null) {
            double var3 = this.hasShard(var1, ShardType.DRAGON) ? 40.0 : (this.hasShard(var1, ShardType.TITAN) ? 24.0 : 20.0);
            double var5 = var2.getBaseValue();
            if (var5 < var3) {
                var2.setBaseValue(var3);
                var1.setHealth(Math.min(var3, var1.getHealth() + 4.0));
            } else if (var5 > var3) {
                var2.setBaseValue(var3);
                if (var1.getHealth() > var3) {
                    var1.setHealth(var3);
                }
            }
        }
    }

    private void spawnShardParticles(Player var1) {
        ShardType var2 = this.getHeldOrAnyShard(var1);
        if (null == var2) {
            if (var2 != null) {
                Location var3 = var1.getLocation().add(0.0, 1.0, 0.0);
                switch (var2) {
                    case VELOCITY:
                        ShardFX.spawn(var1.getWorld(), Particle.PORTAL, var3, 8, 0.4, 0.5, 0.4, 0.05);
                        ShardFX.spawn(var1.getWorld(), Particle.ENCHANT, var3, 3, 0.35, 0.4, 0.35, 0.0);
                        break;
                    case TITAN:
                        ShardFX.spawn(var1.getWorld(), Particle.CRIT, var3, 5, 0.35, 0.4, 0.35, 0.02);
                        ShardFX.spawn(var1.getWorld(), Particle.SMOKE, var3, 2, 0.25, 0.3, 0.25, 0.01);
                        break;
                    case TIDE:
                        ShardFX.spawn(var1.getWorld(), Particle.SPLASH, var3, 6, 0.4, 0.4, 0.4, 0.02);
                        ShardFX.spawn(var1.getWorld(), Particle.BUBBLE, var3, 3, 0.3, 0.35, 0.3, 0.01);
                        break;
                    case PHANTOM:
                        ShardFX.spawn(var1.getWorld(), Particle.CLOUD, var3, 3, 0.3, 0.35, 0.3, 0.01);
                        ShardFX.spawn(var1.getWorld(), Particle.SOUL, var3, 2, 0.25, 0.4, 0.25, 0.01);
                        break;
                    case EMBER:
                        ShardFX.spawn(var1.getWorld(), Particle.FLAME, var3, 4, 0.3, 0.4, 0.3, 0.01);
                        ShardFX.spawn(var1.getWorld(), Particle.LAVA, var3, 1, 0.2, 0.25, 0.2, 0.0);
                        break;
                    case DRAGON:
                        ShardFX.spawn(var1.getWorld(), Particle.PORTAL, var3, 6, 0.4, 0.5, 0.4, 0.02);
                        ShardFX.spawn(var1.getWorld(), Particle.PORTAL, var3, 4, 0.3, 0.4, 0.3, 0.02);
                        break;
                    case FROST:
                        ShardFX.spawn(var1.getWorld(), Particle.SNOWFLAKE, var3, 4, 0.35, 0.45, 0.35, 0.01);
                        break;
                    case VERDANT:
                        ShardFX.spawn(var1.getWorld(), Particle.HAPPY_VILLAGER, var3, 2, 0.35, 0.45, 0.35, 0.0);
                        break;
                    case STORM:
                        ShardFX.spawn(var1.getWorld(), Particle.ELECTRIC_SPARK, var3, 4, 0.35, 0.45, 0.35, 0.02);
                }
            }
        }
    }

    private void applyEffect(Player var1, PotionEffectType var2, int var3, int var4) {
        var1.addPotionEffect(new PotionEffect(var2, var3, var4, false, true, true));
    }

    private static Component text(String var0, TextColor var1) {
        return Component.text(var0, var1);
    }

    protected ItemStack createShard(ShardType var1) {
        ItemStack var2 = new ItemStack(this.getConfig().getBoolean("use-custom-textures", true) ? var1.material() : Material.AMETHYST_SHARD);
        ItemMeta var3 = var2.getItemMeta();
        var3.displayName(Component.text(var1.displayName(), var1.color()).decoration(TextDecoration.ITALIC, false));
        ArrayList var4 = new ArrayList();
        var4.add(Component.text(var1.description(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        var4.add(Component.empty());
        var4.add(
            ((TextComponent)Component.text("Passive: ", NamedTextColor.DARK_GRAY).append(Component.text(var1.passiveSummary(), NamedTextColor.WHITE)))
                .decoration(TextDecoration.ITALIC, false)
        );
        var4.add(
            ((TextComponent)Component.text("Ability: ", NamedTextColor.DARK_GRAY).append(Component.text(var1.abilityName() + " (/ability1)", var1.color())))
                .decoration(TextDecoration.ITALIC, false)
        );
        var4.add(Component.text("Cooldown: " + var1.cooldownMs() / 1000L, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        var3.lore(var4);
        var3.getPersistentDataContainer().set(this.typeKey, PersistentDataType.STRING, var1.id());
        var2.setItemMeta(var3);
        return var2;
    }

    public ShardType getShardType(ItemStack var1) {
        if (var1 != null && var1.hasItemMeta()) {
            ItemMeta var2 = var1.getItemMeta();
            String var3 = (String)var2.getPersistentDataContainer().get(this.typeKey, PersistentDataType.STRING);
            if (var3 != null) {
                return ShardType.fromId(var3.toLowerCase(Locale.ROOT));
            }

            Byte var4 = (Byte)var2.getPersistentDataContainer().get(this.legacyKey, PersistentDataType.BYTE);
            return var4 != null && var4 == 1 ? ShardType.VELOCITY : null;
        } else {
            return null;
        }
    }

    private void giveItem(Player var1, ItemStack var2) {
        var1.getInventory().addItem(new ItemStack[]{var2}).values().forEach(var1x -> var1.getWorld().dropItemNaturally(var1.getLocation(), var1x));
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent var1) {
        Player var2 = var1.getPlayer();
        if (var2.getGameMode() != GameMode.CREATIVE && var2.getGameMode() != GameMode.SPECTATOR) {
            UUID var3 = var2.getUniqueId();
            if (this.canDoubleJump.contains(var3) && this.hasShard(var2, ShardType.VELOCITY)) {
                if (this.hasDoubleJumped.contains(var3)) {
                    var1.setCancelled(true);
                    var2.setAllowFlight(false);
                    var2.setFlying(false);
                } else {
                    var1.setCancelled(true);
                    var2.setFlying(false);
                    var2.setAllowFlight(false);
                    this.hasDoubleJumped.add(var3);
                    Vector var4 = var2.getVelocity();
                    var4.setY(0.65);
                    var4.add(var2.getLocation().getDirection().normalize().multiply(0.25));
                    var2.setVelocity(var4);
                    ShardFX.spawn(var2.getWorld(), Particle.CLOUD, var2.getLocation(), 12, 0.3, 0.1, 0.3, 0.02);
                    ShardFX.spawn(var2.getWorld(), Particle.PORTAL, var2.getLocation().add(0.0, 0.2, 0.0), 15, 0.3, 0.2, 0.3, 0.05);
                    var2.getWorld().playSound(var2.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, 1.6F);
                }
            }
        }
    }

    @EventHandler
    public void onMoveResetJump(PlayerMoveEvent var1) {
        Player var2 = var1.getPlayer();
        if (var2.isOnGround() && this.hasShard(var2, ShardType.VELOCITY)) {
            this.hasDoubleJumped.remove(var2.getUniqueId());
            var2.setAllowFlight(true);
            this.canDoubleJump.add(var2.getUniqueId());
        }

        UUID var3 = var2.getUniqueId();
        if (this.groundPounding.contains(var3) && var2.isOnGround()) {
            this.groundPounding.remove(var3);
            Location var4 = var2.getLocation();
            ShardFX.spawn(var2.getWorld(), Particle.EXPLOSION, var4, 3, 0.2, 0.1, 0.2, 0.01);
            ShardFX.spawn(var2.getWorld(), Particle.BLOCK, var4, 40, 1.5, 0.3, 1.5, 0.1, Material.DIRT.createBlockData());
            ShardFX.spawn(var2.getWorld(), Particle.CRIT, var4, 30, 1.2, 0.4, 1.2, 0.2);
            var2.getWorld().playSound(var4, Sound.ENTITY_GENERIC_EXPLODE, 0.7F, 0.8F);
            var2.getWorld().playSound(var4, Sound.ENTITY_IRON_GOLEM_DAMAGE, 1.0F, 0.5F);

            for (Entity var6 : var2.getWorld().getNearbyEntities(var4, 5.0, 2.5, 5.0)) {
                if (var6 instanceof LivingEntity var7 && !var7.equals(var2) && !var7.isDead()) {
                    PierceDamage.hit(var7, 6.0, var2);
                    Vector var8 = var7.getLocation().toVector().subtract(var4.toVector()).normalize().multiply(1.35);
                    var8.setY(0.55);
                    var7.setVelocity(var8);
                }
            }

            var2.sendActionBar("GROUND POUND!");
        }
    }

    @EventHandler
    public void onShardUse(PlayerInteractEvent var1) {
        EquipmentSlot var2 = var1.getHand();
        if ((var2 == EquipmentSlot.HAND || var2 == EquipmentSlot.OFF_HAND)
            && (var1.getAction() == Action.RIGHT_CLICK_AIR || var1.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            ShardType var3 = this.getShardType(var1.getItem());
            if (var3 != null) {
                if (var3 == ShardType.EMBER || var3 == ShardType.DRAGON) {
                    var1.setCancelled(true);
                }

                Player var4 = var1.getPlayer();
                if (var2 != EquipmentSlot.OFF_HAND || this.getShardType(var4.getInventory().getItemInMainHand()) == null) {
                    String var5 = var4.getUniqueId() + ":" + var3.name();
                    long var6 = System.currentTimeMillis();
                    long var8 = this.cooldowns.getOrDefault(var5, 0L);
                    if (var6 < var8) {
                        var4.sendActionBar(var3.abilityName() + " cooldown: " + Math.max(1L, (Math.min(var8 - var6, var3.cooldownMs()) + 999L) / 1000L));
                    } else {
                        this.cooldowns.put(var5, var6 + CooldownPatch.total(var3));
                        this.activate(var4, var3);
                        ShardPatch.particle(var4.getWorld(), var3.particle(), var4.getLocation().add(0.0, 1.0, 0.0), 35, 0.35, 0.45, 0.35, 0.15);
                        var4.getWorld().playSound(var4.getLocation(), var3.sound(), 0.8F, 1.2F);
                        var4.sendActionBar(var3.abilityName().toUpperCase(Locale.ROOT));
                    }
                }
            }
        }
    }

    private void activate(final Player var1, ShardType var2) {
        Vector var3 = var1.getLocation().getDirection().normalize();
        switch (var2) {
            case VELOCITY:
                Vector var4 = var3.multiply(1.8);
                var4.setY(Math.max(0.45, var4.getY() + 0.35));
                var1.setVelocity(var4);
                break;
            case TITAN:
                this.groundPounding.add(var1.getUniqueId());
                var1.setVelocity(new Vector(0.0, 0.9, 0.0));
                (new BukkitRunnable() {
                    public void run() {
                        if (var1.isOnline() && SuperhumanShard.this.groundPounding.contains(var1.getUniqueId())) {
                            var1.setVelocity(new Vector(0.0, -1.8, 0.0));
                            var1.getWorld().playSound(var1.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0F, 0.6F);
                        }
                    }
                }).runTaskLater(this, 8L);
                break;
            case TIDE:
                var1.setVelocity(var3.multiply(2.2));
                this.applyEffect(var1, PotionEffectType.DOLPHINS_GRACE, 160, 0);
                new TideDashTask(var1).runTaskTimer(this, 0L, 1L);
                break;
            case PHANTOM:
                PhantomSummon.summon(var1);
                break;
            case EMBER:
                Fireball var5 = (Fireball)var1.launchProjectile(Fireball.class);
                var5.setYield(4.0F);
                var5.setIsIncendiary(true);
                var5.getPersistentDataContainer().set(this.fireballKey, PersistentDataType.BYTE, (byte)1);
                break;
            case DRAGON:
                final UUID var6 = var1.getUniqueId();
                this.dragonScalesActive.add(var6);
                var1.sendActionBar(Component.text("Dragon Scales: Strength III, Resist I!!     ", NamedTextColor.DARK_PURPLE));
                ShardFX.spawn(var1.getWorld(), Particle.PORTAL, var1.getLocation().add(0.0, 1.0, 0.0), 40, 0.6, 0.8, 0.6, 0.05);
                var1.getWorld().playSound(var1.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8F, 1.4F);
                (new BukkitRunnable() {
                    public void run() {
                        SuperhumanShard.this.dragonScalesActive.remove(var6);
                        if (var1.isOnline()) {
                            var1.sendActionBar(Component.text("Dragon Scales faded", NamedTextColor.GRAY));
                        }
                    }
                }).runTaskLater(this, 400L);
                break;
            case FROST:
                FrostBreath.breath(var1);
                break;
            case VERDANT:
                AttributeInstance var8 = var1.getAttribute(Attribute.MAX_HEALTH);
                double var9 = var8 != null ? var8.getValue() : 20.0;
                var1.setHealth(Math.min(var9, var1.getHealth() + 6.0));
                this.applyEffect(var1, PotionEffectType.ABSORPTION, 200, 0);
                ShardFX.spawn(var1.getWorld(), Particle.HAPPY_VILLAGER, var1.getLocation().add(0.0, 1.0, 0.0), 30, 0.6, 0.8, 0.6, 0.0);
                var1.getAttribute(Attribute.SCALE).setBaseValue(0.75);
                new SuperhumanShard$6(var1.getUniqueId()).runTaskLater(this, 400L);
                break;
            case STORM:
                Block var11 = var1.getTargetBlockExact(25);
                Location var12 = var11 != null
                    ? var11.getLocation().add(0.5, 1.0, 0.5)
                    : var1.getEyeLocation().add(var1.getLocation().getDirection().normalize().multiply(25.0));
                StormPatch.stormStrike(var1, var12);
        }
    }

    @EventHandler
    public void onFrostWalk(PlayerMoveEvent var1) {
        Player var2 = var1.getPlayer();
        if (var1.getTo() != null && !var1.getFrom().getBlock().equals(var1.getTo().getBlock())) {
            if (var2.isOnGround() && this.hasShard(var2, ShardType.FROST)) {
                Block var3 = var1.getTo().getBlock().getRelative(BlockFace.DOWN);

                for (int var4 = -2; var4 <= 2; var4++) {
                    for (int var5 = -2; var5 <= 2; var5++) {
                        if (var4 * var4 + var5 * var5 <= 5) {
                            Block var6 = var3.getRelative(var4, 0, var5);
                            if (var6.getType() == Material.WATER
                                && var6.getBlockData() instanceof Levelled var7
                                && var7.getLevel() == 0
                                && var6.getRelative(BlockFace.UP).getType().isAir()) {
                                var6.setType(Material.FROSTED_ICE);
                                this.frostedWater.put(var6, System.currentTimeMillis());
                            }
                        }
                    }
                }
            }
        }
    }

    private void meltFrostedWater() {
        long var1 = System.currentTimeMillis();
        this.frostedWater.entrySet().removeIf(var2 -> {
            if (var1 - var2.getValue() < 6000L) {
                return false;
            }

            if (var2.getKey().getType() == Material.FROSTED_ICE) {
                var2.getKey().setType(Material.WATER);
            }

            return true;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onShardPassiveDamage(EntityDamageEvent var1) {
        if (var1.getEntity() instanceof Player var2) {
            DamageCause var4 = var1.getCause();
            if (var4 == DamageCause.FREEZE && this.hasShard(var2, ShardType.FROST)) {
                var1.setCancelled(true);
                var2.setFreezeTicks(0);
            } else if (var4 == DamageCause.LIGHTNING && this.hasShard(var2, ShardType.STORM)) {
                var1.setCancelled(true);
            } else if (var4 == DamageCause.FALL && this.hasShard(var2, ShardType.STORM)) {
                var1.setDamage(var1.getDamage() * 0.5);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVerdantHunger(FoodLevelChangeEvent var1) {
        if (var1.getEntity() instanceof Player var2
            && var1.getFoodLevel() < var2.getFoodLevel()
            && this.hasShard(var2, ShardType.VERDANT)
            && Math.random() < 0.5) {
            var1.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDragonScalesDamage(EntityDamageEvent var1) {
        if (var1.getEntity() instanceof Player var2 && this.dragonScalesActive.contains(var2.getUniqueId())) {
            var1.setDamage(var1.getDamage() * 1.0);
            ShardFX.spawn(var2.getWorld(), Particle.PORTAL, var2.getLocation().add(0.0, 1.0, 0.0), 8, 0.4, 0.5, 0.4, 0.02);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFireballDamage(EntityDamageByEntityEvent var1) {
        if (var1.getDamager() instanceof Fireball var2 && var2.getPersistentDataContainer().get(this.fireballKey, PersistentDataType.BYTE) != null) {
            if (var1.getEntity() instanceof Player var9 && var2.getShooter() != var9) {
                int var10 = 0;

                for (ItemStack var8 : var9.getInventory().getArmorContents()) {
                    if (var8 != null && var8.getType() != Material.AIR) {
                        var10++;
                    }
                }

                if (var10 != 0) {
                    var1.setDamage(var1.getDamage() * (1.0 + 0.3 * var10));
                }
            }

            PierceDamage.ember(var1);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent var1) {
        var1.getEntity().getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        var1.getEntity().getAttribute(Attribute.SCALE).setBaseValue(1.0);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent var1) {
        Player var2 = var1.getPlayer();
        List<ItemStack> var3 = this.deathShards.remove(var2.getUniqueId());
        if (var3 != null) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (ItemStack var4 : var3) {
                    this.giveItem(var2, var4);
                }

                this.updateDragonHearts(var2);
            }, 5L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent var1) {
        UUID var2 = var1.getPlayer().getUniqueId();
        this.canDoubleJump.remove(var2);
        this.hasDoubleJumped.remove(var2);
        this.dragonScalesActive.remove(var2);
        this.groundPounding.remove(var2);
        AttributeInstance var3 = var1.getPlayer().getAttribute(Attribute.MAX_HEALTH);
        if (var3 != null) {
            var3.setBaseValue(20.0);
        }

        var1.getPlayer().getAttribute(Attribute.SCALE).setBaseValue(1.0);
    }

    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shard")) {
            return false;
        }

        if (!var1.hasPermission("superhumanshard.admin")) {
            var1.sendMessage("No permission.");
            return true;
        }

        if (var4.length < 1) {
            var1.sendMessage("Usage: /shard <velocity|titan|tide|phantom|ember|dragon|frost|verdant|storm|all> [player]");
            return true;
        }

        ArrayList<ShardType> var5 = new ArrayList<>();
        if (var4[0].equalsIgnoreCase("all")) {
            Collections.addAll(var5, ShardType.values());
        } else {
            ShardType var6 = ShardType.fromId(var4[0]);
            if (var6 == null) {
                var1.sendMessage("Unknown shard. Choose: velocity, titan, tide, phantom, ember, dragon, frost, verdant, storm, all.");
                return true;
            }

            var5.add(var6);
        }

        Player var9;
        if (var4.length < 2) {
            if (!(var1 instanceof Player var7)) {
                var1.sendMessage("Usage: /shard <type|all> <player>");
                return true;
            }

            var9 = var7;
        } else {
            var9 = Bukkit.getPlayerExact(var4[1]);
            if (var9 == null) {
                var1.sendMessage("That player is not online.");
                return true;
            }
        }

        for (ShardType var8 : var5) {
            this.giveItem(var9, this.createShard(var8));
            var9.sendMessage("You received a " + var8.displayName());
        }

        if (var1 != var9) {
            var1.sendMessage("Gave " + var5.size() + " shard(s) to " + var9.getName());
        }

        return true;
    }

    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        if (var4.length != 1) {
            return null;
        }

        ArrayList<String> var5 = new ArrayList<>();
        ShardType[] var6 = ShardType.values();
        int var7 = var6.length;

        for (int var8 = 0; var8 < var7; var8++) {
            var5.add(var6[var8].id());
        }

        var5.add("all");
        String var12 = var4[0].toLowerCase(Locale.ROOT);
        ArrayList var9 = new ArrayList();

        for (String var11 : var5) {
            if (var11.startsWith(var12)) {
                var9.add(var11);
            }
        }

        return var9;
    }
}
