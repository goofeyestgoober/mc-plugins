package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class ShardAbilities implements Listener {
    private static final String BAR_FULL = "█";
    private static final String BAR_EMPTY = "░";
    private static final String[] ARROWS = new String[]{"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
    private final SuperhumanShard plugin;
    private final NamespacedKey fireballKey;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<String> waiting = new HashSet<>();
    private final Map<UUID, UUID> chosen = new HashMap<>();
    private final Map<UUID, ShardAbilities.Tracking> tracking = new HashMap<>();
    private boolean beamActive = false;
    private final Map<UUID, UUID> blazeOwners = new HashMap<>();
    private final Map<UUID, UUID> blazeTargets = new HashMap<>();
    private final Map<UUID, Blaze> liveBlazes = new HashMap<>();

    ShardAbilities(SuperhumanShard var1) {
        this.plugin = var1;
        this.fireballKey = new NamespacedKey(var1, "ember_fireball");
    }

    void start() {
        Bukkit.getScheduler().runTaskTimer(this.plugin, this::tickDisplay, 10L, 2L);
    }

    void shutdown() {
        for (Blaze var2 : new ArrayList<>(this.liveBlazes.values())) {
            if (var2 != null) {
                var2.remove();
            }
        }

        this.liveBlazes.clear();
        this.blazeOwners.clear();
        this.blazeTargets.clear();
        this.tracking.clear();
        this.chosen.clear();
    }

    private FileConfiguration cfg() {
        return this.plugin.getConfig();
    }

    private long cdMs(String var1) {
        switch (var1) {
            case "EMBER1":
                return this.cfg().getLong("abilities.ember.fireball-cooldown", 20L) * 1000L;
            case "EMBER2":
                return this.cfg().getLong("abilities.ember.swap-cooldown", 300L) * 1000L;
            case "SCULK1":
                return this.cfg().getLong("abilities.sculk.beam-cooldown", 20L) * 1000L;
            default:
                return this.cfg().getLong("abilities.sculk.track-cooldown", 60L) * 1000L;
        }
    }

    private long remaining(Player var1, String var2) {
        Long var3 = this.cooldowns.get(var1.getUniqueId() + ":" + var2);
        return var3 == null ? 0L : Math.max(0L, var3 - System.currentTimeMillis());
    }

    private void startCooldown(Player var1, String var2) {
        String var3 = var1.getUniqueId() + ":" + var2;
        this.cooldowns.put(var3, System.currentTimeMillis() + this.cdMs(var2));
        this.waiting.add(var3);
    }

    private static String secs(long var0) {
        return (var0 + 999L) / 1000L + "s";
    }

    void use(Player var1, ShardType var2, boolean var3) {
        NamedTextColor var4 = var2.color();
        if (var2 == ShardType.EMBER) {
            String var5 = var3 ? "EMBER2" : "EMBER1";
            String var6 = var3 ? "Ember Arena" : "Fireball";
            long var7 = this.remaining(var1, var5);
            if (var7 > 0L) {
                var1.sendActionBar(cat(t(var6, var4), t(" ready in " + secs(var7), NamedTextColor.GRAY)));
                return;
            }

            this.startCooldown(var1, var5);
            if (var3) {
                this.blazeSquad(var1);
            } else {
                this.fireball(var1);
            }

            this.flair(var1, var2);
            var1.sendActionBar(t(var6.toUpperCase(Locale.ROOT), var4));
        } else if (var2 == ShardType.SCULK) {
            String var9 = var3 ? "SCULK2" : "SCULK1";
            String var10 = var3 ? "Sculk Tracker" : "Supersonic Beam";
            long var11 = this.remaining(var1, var9);
            if (var11 > 0L) {
                var1.sendActionBar(cat(t(var10, var4), t(" ready in " + secs(var11), NamedTextColor.GRAY)));
                return;
            }

            if (var3) {
                if (!this.startTracking(var1)) {
                    return;
                }

                this.startCooldown(var1, var9);
            } else {
                this.startCooldown(var1, var9);
                this.beam(var1);
                this.flair(var1, var2);
                var1.sendActionBar(t(var10.toUpperCase(Locale.ROOT), var4));
            }
        }
    }

    private void flair(Player var1, ShardType var2) {
        World var3 = var1.getWorld();
        ShardPatch.particle(var3, var2.particle(), var1.getLocation().add(0.0, 1.0, 0.0), 12, 0.35, 0.45, 0.35, 0.15);
        var3.playSound(var1.getLocation(), var2.sound(), 0.8F, 1.2F);
    }

    private void fireball(Player var1) {
        Fireball var2 = (Fireball)var1.launchProjectile(Fireball.class);
        var2.setYield(4.0F);
        var2.setIsIncendiary(true);
        var2.getPersistentDataContainer().set(this.fireballKey, PersistentDataType.BYTE, (byte)1);
    }

    private void blazeSquad(Player var1) {
        EmberSwap.cast(var1, this.cooldowns, this.waiting);
    }

    private void retarget(Player var1, List<Blaze> var2) {
        double var3 = this.cfg().getDouble("abilities.ember.blaze-range", 24.0);
        Location var5 = var1.getLocation();
        LivingEntity var6 = null;
        double var7 = Double.MAX_VALUE;
        LivingEntity var9 = null;
        double var10 = Double.MAX_VALUE;

        for (Entity var13 : var1.getWorld().getNearbyEntities(var5, var3, var3, var3)) {
            if (var13 instanceof LivingEntity var14
                && !var14.isDead()
                && !var14.getUniqueId().equals(var1.getUniqueId())
                && !this.blazeOwners.containsKey(var14.getUniqueId())) {
                double var15 = var14.getLocation().distanceSquared(var5);
                if (var14 instanceof Player) {
                    GameMode var17 = ((Player)var14).getGameMode();
                    if (var17 != GameMode.CREATIVE && var17 != GameMode.SPECTATOR && var15 < var7) {
                        var7 = var15;
                        var6 = var14;
                    }
                } else if (var14 instanceof Monster && var15 < var10) {
                    var10 = var15;
                    var9 = var14;
                }
            }
        }

        LivingEntity var18 = var6 != null ? var6 : var9;
        if (var18 == null) {
            this.blazeTargets.remove(var1.getUniqueId());
        } else {
            this.blazeTargets.put(var1.getUniqueId(), var18.getUniqueId());
        }

        for (Blaze var20 : var2) {
            if (!var20.isDead()) {
                var20.setTarget(var18);
            }
        }
    }

    @EventHandler
    public void onBlazeTarget(EntityTargetEvent var1) {
        UUID var2 = this.blazeOwners.get(var1.getEntity().getUniqueId());
        if (var2 != null) {
            Entity var3 = var1.getTarget();
            if (var3 != null) {
                UUID var4 = this.blazeTargets.get(var2);
                if (var4 == null || !var4.equals(var3.getUniqueId())) {
                    var1.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlazeShoot(ProjectileLaunchEvent var1) {
        Projectile var2 = var1.getEntity();
        if (var2 instanceof SmallFireball) {
            ProjectileSource var3 = var2.getShooter();
            if (var3 instanceof Blaze && this.blazeOwners.containsKey(((Blaze)var3).getUniqueId())) {
                ((Fireball)var2).setIsIncendiary(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlazeDamage(EntityDamageByEntityEvent var1) {
        Entity var2 = var1.getDamager();
        Blaze var3 = null;
        if (var2 instanceof Blaze) {
            var3 = (Blaze)var2;
        } else if (var2 instanceof Projectile) {
            ProjectileSource var4 = ((Projectile)var2).getShooter();
            if (var4 instanceof Blaze) {
                var3 = (Blaze)var4;
            }
        }

        if (var3 != null) {
            UUID var8 = this.blazeOwners.get(var3.getUniqueId());
            if (var8 != null) {
                Entity var5 = var1.getEntity();
                if (!var5.getUniqueId().equals(var8) && !this.blazeOwners.containsKey(var5.getUniqueId())) {
                    double var6 = this.cfg().getDouble("abilities.ember.blaze-damage-hearts", 0.5) * 2.0;
                    var1.setDamage(var6);
                    PierceDamage.pierce(var1, var6);
                } else {
                    var1.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlazeIgnite(EntityCombustByEntityEvent var1) {
        if (!this.cfg().getBoolean("abilities.ember.blaze-ignite", false)) {
            Entity var2 = var1.getCombuster();
            if (var2 instanceof SmallFireball) {
                ProjectileSource var3 = ((SmallFireball)var2).getShooter();
                if (var3 instanceof Blaze && this.blazeOwners.containsKey(((Blaze)var3).getUniqueId())) {
                    var1.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlazeDeath(EntityDeathEvent var1) {
        UUID var2 = var1.getEntity().getUniqueId();
        if (this.blazeOwners.remove(var2) != null) {
            this.liveBlazes.remove(var2);
            var1.getDrops().clear();
            var1.setDroppedExp(0);
        }
    }

    private void beam(Player var1) {
        World var2 = var1.getWorld();
        Location var3 = var1.getEyeLocation();
        Vector var4 = var3.getDirection().normalize();
        double var5 = this.cfg().getDouble("abilities.sculk.beam-range", 24.0);
        double var7 = var5;
        Block var9 = var1.getTargetBlockExact((int)var5);
        if (var9 != null) {
            double var10 = Math.sqrt(var9.getLocation().add(0.5, 0.5, 0.5).distanceSquared(var3));
            var7 = Math.min(var5, var10);
        }

        double var29 = this.cfg().getDouble("abilities.sculk.beam-damage-hearts", 6.0) * 2.0;

        for (double var12 = 1.0; var12 <= var7; var12 += 2.0) {
            ShardFX.spawn(var2, Particle.SONIC_BOOM, var3.clone().add(var4.clone().multiply(var12)), 1, 0.0, 0.0, 0.0, 0.0);
        }

        for (double var30 = 0.5; var30 <= var7; var30 += 0.75) {
            ShardFX.spawn(var2, Particle.SCULK_SOUL, var3.clone().add(var4.clone().multiply(var30)), 1, 0.05, 0.05, 0.05, 0.0);
        }

        var2.playSound(var1.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0F, 1.0F);
        double var31 = var7 / 2.0 + 2.0;
        Location var14 = var3.clone().add(var4.clone().multiply(var7 / 2.0));
        ArrayList<LivingEntity> var15 = new ArrayList<>();

        for (Entity var17 : var2.getNearbyEntities(var14, var31, var31, var31)) {
            if (var17 instanceof LivingEntity && !var17.isDead()) {
                UUID var18 = var17.getUniqueId();
                if (!var18.equals(var1.getUniqueId()) && (var18 == null || !var1.getUniqueId().equals(this.blazeOwners.get(var18)))) {
                    if (var17 instanceof Player) {
                        GameMode var19 = ((Player)var17).getGameMode();
                        if (var19 == GameMode.CREATIVE || var19 == GameMode.SPECTATOR) {
                            continue;
                        }
                    }

                    Vector var34 = var17.getLocation().add(0.0, var17.getHeight() / 2.0, 0.0).toVector().subtract(var3.toVector());
                    double var20 = var34.dot(var4);
                    if (!(var20 < 0.0) && !(var20 > var7 + 1.0)) {
                        double var22 = var34.lengthSquared() - var20 * var20;
                        double var24 = Math.max(var17.getWidth(), var17.getHeight()) / 2.0 + 0.5;
                        if (var22 <= var24 * var24) {
                            var15.add((LivingEntity)var17);
                        }
                    }
                }
            }
        }

        this.beamActive = true;

        try {
            for (LivingEntity var33 : var15) {
                PierceDamage.hit(var33, var29, var1);
                var33.setVelocity(var4.clone().multiply(1.1).setY(0.35));
            }
        } finally {
            this.beamActive = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSculkHit(EntityDamageByEntityEvent var1) {
        if (!this.beamActive) {
            if (var1.getDamager() instanceof Player && var1.getEntity() instanceof Player) {
                Player var2 = (Player)var1.getDamager();
                Player var3 = (Player)var1.getEntity();
                if (!var2.getUniqueId().equals(var3.getUniqueId())) {
                    if (this.plugin.getShardType(var2.getInventory().getItemInMainHand()) == ShardType.SCULK) {
                        UUID var4 = this.chosen.put(var2.getUniqueId(), var3.getUniqueId());
                        if (var4 == null || !var4.equals(var3.getUniqueId())) {
                            var2.sendActionBar(
                                cat(
                                    t("Target chosen: ", NamedTextColor.GRAY),
                                    t(var3.getName(), ShardType.SCULK.color()),
                                    t("  (/ability2 to track)", NamedTextColor.DARK_GRAY)
                                )
                            );
                            var2.getWorld().playSound(var2.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.8F, 1.0F);
                        }
                    }
                }
            }
        }
    }

    private boolean startTracking(Player var1) {
        UUID var2 = this.chosen.get(var1.getUniqueId());
        if (var2 == null) {
            var1.sendActionBar(t("Hit a player with the Sculk Shard to choose who to track first.", NamedTextColor.GRAY));
            return false;
        }

        Player var3 = Bukkit.getPlayer(var2);
        if (var3 != null && var3.isOnline()) {
            long var4 = Math.max(1L, this.cfg().getLong("abilities.sculk.track-seconds", 30L));
            this.tracking.put(var1.getUniqueId(), new ShardAbilities.Tracking(var2, System.currentTimeMillis() + var4 * 1000L));
            World var6 = var1.getWorld();
            var6.playSound(var1.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0F, 1.0F);
            ShardFX.spawn(var6, ShardType.SCULK.particle(), var1.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.6, 0.4, 0.02);
            if (this.cfg().getBoolean("abilities.sculk.notify-target", false)) {
                var3.sendActionBar(t("You are being tracked...", ShardType.SCULK.color()));
            }

            return true;
        } else {
            var1.sendActionBar(t("Your chosen target is offline.", NamedTextColor.GRAY));
            return false;
        }
    }

    private static String arrowTo(Player var0, Location var1) {
        Location var2 = var0.getLocation();
        double var3 = var1.getX() - var2.getX();
        double var5 = var1.getZ() - var2.getZ();
        double var7 = Math.toDegrees(Math.atan2(-var3, var5));
        double var9 = var7 - var2.getYaw();
        var9 = (var9 % 360.0 + 540.0) % 360.0 - 180.0;
        int var11 = (int)Math.round(var9 / 45.0);
        var11 = (var11 % 8 + 8) % 8;
        return ARROWS[var11];
    }

    private void tickDisplay() {
        long var1 = System.currentTimeMillis();

        for (Player var4 : Bukkit.getOnlinePlayers()) {
            UUID var5 = var4.getUniqueId();

            for (String var9 : new String[]{"EMBER1", "EMBER2", "SCULK1", "SCULK2"}) {
                String var10 = var5 + ":" + var9;
                if (this.waiting.contains(var10)) {
                    Long var11 = this.cooldowns.get(var10);
                    if (var11 == null || var11 <= var1) {
                        this.waiting.remove(var10);
                        this.sendReady(var4, var9);
                    }
                }
            }

            ShardAbilities.Tracking var12 = this.tracking.get(var5);
            if (var12 != null) {
                Player var13 = Bukkit.getPlayer(var12.target);
                if (var1 < var12.until && var13 != null && var13.isOnline() && !var13.isDead()) {
                    var4.sendActionBar(this.trackingBar(var4, var13, var12, var1));
                    continue;
                }

                this.tracking.remove(var5);
                var4.sendActionBar(t("Tracking ended.", NamedTextColor.GRAY));
            }

            ShardType var14 = this.plugin.getShardType(var4.getInventory().getItemInMainHand());
            if (var14 == ShardType.EMBER) {
                var4.sendActionBar(
                    cat(
                        this.bar(var4, "EMBER1", "Fireball", var14.color()),
                        t("  ", NamedTextColor.DARK_GRAY),
                        this.bar(var4, "EMBER2", "Ember Arena", var14.color())
                    )
                );
            } else if (var14 == ShardType.SCULK) {
                var4.sendActionBar(
                    cat(this.bar(var4, "SCULK1", "Beam", var14.color()), t("  ", NamedTextColor.DARK_GRAY), this.bar(var4, "SCULK2", "Tracker", var14.color()))
                );
            }
        }
    }

    private Component trackingBar(Player var1, Player var2, ShardAbilities.Tracking var3, long var4) {
        NamedTextColor var6 = ShardType.SCULK.color();
        Location var7 = var2.getLocation();
        Location var8 = var1.getLocation();
        String var9 = "  (" + secs(var3.until - var4) + ")";
        if (var7.getWorld() != var8.getWorld()) {
            return cat(
                t("TRACKING ", var6),
                t(var2.getName(), NamedTextColor.WHITE),
                t("  in " + var7.getWorld().getName(), NamedTextColor.GRAY),
                t(var9, NamedTextColor.DARK_GRAY)
            );
        }

        long var10 = Math.round(Math.sqrt(var7.distanceSquared(var8)));
        double var12 = var7.getY() - var8.getY();
        String var14 = var12 >= 6.0 ? " ▲" : (var12 <= -6.0 ? " ▼" : "");
        return cat(
            t("TRACKING ", var6),
            t(var2.getName(), NamedTextColor.WHITE),
            t("  ", var6),
            t(arrowTo(var1, var7), NamedTextColor.WHITE),
            t("  " + var10 + "m" + var14, NamedTextColor.WHITE),
            t(var9, NamedTextColor.DARK_GRAY)
        );
    }

    private Component bar(Player var1, String var2, String var3, TextColor var4) {
        return ShardHud.slot(var4, this.remaining(var1, var2));
    }

    private void sendReady(Player var1, String var2) {
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent var1) {
        UUID var2 = var1.getPlayer().getUniqueId();
        this.tracking.remove(var2);
        this.chosen.remove(var2);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent var1) {
        Player var2 = var1.getPlayer();
        ItemStack[] var3 = var2.getInventory().getContents();

        for (int var4 = 0; var4 < var3.length; var4++) {
            ItemStack var5 = var3[var4];
            if (var5 != null) {
                ShardType var6 = this.plugin.getShardType(var5);
                if (var6 == ShardType.EMBER || var6 == ShardType.SCULK) {
                    ItemMeta var7 = var5.getItemMeta();
                    if (var7 != null) {
                        var7.lore(lore(var6, this.cfg()));
                        var5.setItemMeta(var7);
                        var2.getInventory().setItem(var4, var5);
                    }
                }
            }
        }
    }

    static List<Component> lore(ShardType var0, FileConfiguration var1) {
        ArrayList var2 = new ArrayList();
        var2.add(plain(var0.description(), NamedTextColor.GRAY));
        var2.add(Component.empty());
        var2.add(cat(plain("Passive: ", NamedTextColor.DARK_GRAY), plain(var0.passiveSummary(), NamedTextColor.WHITE)));
        if (var0 == ShardType.EMBER) {
            var2.add(
                cat(
                    plain("Ability 1 (/ability1): ", NamedTextColor.DARK_GRAY),
                    plain("Fireball (" + var1.getLong("abilities.ember.fireball-cooldown", 20L) + "s cooldown)", var0.color())
                )
            );
            var2.add(
                cat(
                    plain("Ability 2 (/ability2): ", NamedTextColor.DARK_GRAY),
                    plain("Ember Arena (" + var1.getLong("abilities.ember.swap-cooldown", 300L) + "s cooldown)", var0.color())
                )
            );
        } else if (var0 == ShardType.SCULK) {
            var2.add(
                cat(
                    plain("Ability 1 (/ability1): ", NamedTextColor.DARK_GRAY),
                    plain("Supersonic Beam (" + var1.getLong("abilities.sculk.beam-cooldown", 20L) + "s cooldown)", var0.color())
                )
            );
            var2.add(
                cat(
                    plain("Ability 2 (/ability2): ", NamedTextColor.DARK_GRAY),
                    plain("Sculk Tracker (" + var1.getLong("abilities.sculk.track-cooldown", 60L) + "s cooldown)", var0.color())
                )
            );
            var2.add(plain("Hit a player with the shard to choose them.", NamedTextColor.DARK_GRAY));
        }

        return var2;
    }

    private static Component t(String var0, TextColor var1) {
        return Component.text(var0, var1);
    }

    private static Component plain(String var0, TextColor var1) {
        return Component.text(var0, var1).decoration(TextDecoration.ITALIC, false);
    }

    private static Component cat(Component... var0) {
        Component var1 = var0[0];

        for (int var2 = 1; var2 < var0.length; var2++) {
            var1 = var1.append(var0[var2]);
        }

        return var1;
    }

    private final class SquadTask extends BukkitRunnable {
        private final UUID ownerId;
        private final List<Blaze> squad;
        private int ticksLeft;

        SquadTask(UUID nullx, List<Blaze> nullxx, int nullxxx) {
            this.ownerId = nullx;
            this.squad = nullxx;
            this.ticksLeft = nullxxx;
        }

        public void run() {
            Player var1 = Bukkit.getPlayer(this.ownerId);
            this.ticksLeft -= 10;
            boolean var2 = false;

            for (Blaze var4 : this.squad) {
                if (!var4.isDead()) {
                    var2 = true;
                    break;
                }
            }

            if (this.ticksLeft > 0 && var2 && var1 != null && var1.isOnline() && !var1.isDead()) {
                ShardAbilities.this.retarget(var1, this.squad);
                Location var7 = var1.getLocation();

                for (Blaze var5 : this.squad) {
                    if (!var5.isDead()) {
                        Location var6 = var5.getLocation();
                        if (var6.getWorld() != var7.getWorld() || var6.distanceSquared(var7) > 900.0) {
                            var5.teleport(var7.clone().add(0.0, 2.0, 0.0));
                        }
                    }
                }
            } else {
                this.finish();
            }
        }

        private void finish() {
            for (Blaze var2 : this.squad) {
                if (!var2.isDead()) {
                    ShardFX.spawn(var2.getWorld(), Particle.FLAME, var2.getLocation(), 10, 0.3, 0.3, 0.3, 0.02);
                    var2.remove();
                }

                ShardAbilities.this.blazeOwners.remove(var2.getUniqueId());
                ShardAbilities.this.liveBlazes.remove(var2.getUniqueId());
            }

            ShardAbilities.this.blazeTargets.remove(this.ownerId);
            this.cancel();
        }
    }

    private static final class Tracking {
        final UUID target;
        final long until;

        Tracking(UUID var1, long var2) {
            this.target = var1;
            this.until = var2;
        }
    }
}
