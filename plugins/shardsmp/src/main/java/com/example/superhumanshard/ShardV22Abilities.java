package com.example.superhumanshard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

final class ShardV22Abilities implements Listener {
    static final String EV1 = "EVOKER1";
    static final String EV2 = "EVOKER2";
    static final String DR2 = "DRAGON2";
    private static final double BREATH_RANGE = 7.0;
    private static final int FANG_BITE_DELAY = 7;
    private static boolean dealingAbilityDamage = false;
    private final ShardV22Main plugin;
    private final Map<String, Long> cdEnd = new HashMap<>();
    private final Map<String, Long> activeEnd = new HashMap<>();
    private final Set<String> waiting = new HashSet<>();
    private final Set<UUID> ourFangs = new HashSet<>();
    private Map<UUID, UUID> squadOwners;
    private boolean reflectFailed;

    ShardV22Abilities(ShardV22Main var1) {
        this.plugin = var1;
    }

    void start() {
        (new BukkitRunnable() {
            public void run() {
                ShardV22Abilities.this.tickDisplay();
            }
        }).runTaskTimer(this.plugin, 10L, 4L);
    }

    void shutdown() {
        this.ourFangs.clear();
    }

    private FileConfiguration cfg() {
        return this.plugin.getConfig();
    }

    private static String key(Player var0, String var1) {
        return var0.getUniqueId() + ":" + var1;
    }

    private long remaining(Player var1, String var2) {
        Long var3 = this.cdEnd.get(key(var1, var2));
        return var3 == null ? 0L : Math.max(0L, var3 - System.currentTimeMillis());
    }

    private boolean active(Player var1, String var2) {
        Long var3 = this.activeEnd.get(key(var1, var2));
        return var3 != null && var3 > System.currentTimeMillis();
    }

    private void begin(Player var1, String var2, long var3, long var5) {
        long var7 = System.currentTimeMillis();
        String var9 = key(var1, var2);
        this.activeEnd.put(var9, var7 + var3);
        this.cdEnd.put(var9, var7 + var3 + var5);
        this.waiting.add(var9);
    }

    private static String secs(long var0) {
        return (var0 + 999L) / 1000L + "s";
    }

    private static Component line(String var0, TextColor var1) {
        return Component.text(var0, var1).decoration(TextDecoration.ITALIC, false);
    }

    private static Component join(Component... var0) {
        Component var1 = var0[0];

        for (int var2 = 1; var2 < var0.length; var2++) {
            var1 = var1.append(var0[var2]);
        }

        return var1;
    }

    private boolean gate(Player var1, String var2, String var3, ShardType var4) {
        if (this.active(var1, var2)) {
            var1.sendActionBar(line(var3 + " is active", var4.color()));
            return false;
        } else {
            long var5 = this.remaining(var1, var2);
            if (var5 > 0L) {
                var1.sendActionBar(join(line(var3 + ": ", var4.color()), line(secs(var5), NamedTextColor.WHITE)));
                return false;
            } else {
                return true;
            }
        }
    }

    private static boolean untargetable(LivingEntity var0, Player var1) {
        if (!(var0 instanceof ArmorStand) && !var0.isDead()) {
            if (var0.getUniqueId().equals(var1.getUniqueId())) {
                return true;
            }

            if (!(var0 instanceof Player)) {
                return false;
            }

            GameMode var2 = ((Player)var0).getGameMode();
            return var2 == GameMode.CREATIVE || var2 == GameMode.SPECTATOR;
        } else {
            return true;
        }
    }

    private static void abilityHit(LivingEntity var0, double var1, Player var3) {
        dealingAbilityDamage = true;

        try {
            PierceDamage.hit(var0, var1, var3);
        } finally {
            dealingAbilityDamage = false;
        }
    }

    void use(Player var1, ShardType var2, boolean var3) {
        if (var2 == ShardType.EVOKER) {
            if (var3) {
                this.fangs(var1);
            } else {
                this.immortality(var1);
            }
        } else if (var2 == ShardType.DRAGON && var3) {
            this.dragonBreath(var1);
        }
    }

    private void immortality(final Player var1) {
        if (this.gate(var1, "EVOKER1", "Immortality", ShardType.EVOKER)) {
            final int var2 = (int)Math.max(1L, this.cfg().getLong("abilities.evoker.immortality-seconds", 3L));
            long var3 = Math.max(1L, this.cfg().getLong("abilities.evoker.immortality-cooldown", 45L)) * 1000L;
            this.begin(var1, "EVOKER1", var2 * 1000L, var3);
            var1.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, var2 * 20, 4, false, true, true));
            final World var5 = var1.getWorld();
            var5.playSound(var1.getLocation(), Sound.ITEM_TOTEM_USE, 0.8F, 1.2F);
            (new BukkitRunnable() {
                int t = 0;

                public void run() {
                    if (this.t < var2 * 20 && var1.isOnline() && !var1.isDead()) {
                        if (this.t % 4 == 0) {
                            ShardFX.spawn(var5, Particle.TOTEM_OF_UNDYING, var1.getLocation().add(0.0, 1.0, 0.0), 6, 0.35, 0.5, 0.35, 0.05);
                        }

                        this.t++;
                    } else {
                        this.cancel();
                    }
                }
            }).runTaskTimer(this.plugin, 0L, 1L);
        }
    }

    private static double groundY(World var0, double var1, double var3, double var5) {
        int var7 = (int)Math.floor(var1);
        int var8 = (int)Math.floor(var3);
        int var9 = (int)Math.floor(var5) + 2;

        for (int var10 = var9; var10 >= var9 - 8; var10--) {
            if (var0.getBlockAt(var7, var10, var8).getType().isSolid() && !var0.getBlockAt(var7, var10 + 1, var8).getType().isSolid()) {
                return var10 + 1;
            }
        }

        return Double.NaN;
    }

    private void fangs(final Player var1) {
        if (this.gate(var1, "EVOKER2", "Curse Fangs", ShardType.EVOKER)) {
            long var2 = Math.max(1L, this.cfg().getLong("abilities.evoker.fangs-cooldown", 35L)) * 1000L;
            final double var4 = Math.max(0.0, this.cfg().getDouble("abilities.evoker.fang-damage-hearts", 5.0)) * 2.0;
            World var6 = var1.getWorld();
            Location var7 = var1.getLocation();
            double var8 = Math.toRadians(var7.getYaw());
            double var10 = -Math.sin(var8);
            double var12 = Math.cos(var8);
            double var14 = -var12;
            double var16 = var10;
            final ArrayList<ShardV22Abilities.Fang> var18 = new ArrayList<>();
            int var19 = 0;

            for (int var20 = 1; var20 <= 14; var20++) {
                int var21 = var20 * 2;

                for (double var25 : new double[]{0.0, -1.3, 1.3}) {
                    if (var25 == 0.0 || var20 >= 3) {
                        double var27 = var7.getX() + var10 * var20 + var14 * var25;
                        double var29 = var7.getZ() + var12 * var20 + var16 * var25;
                        double var31 = groundY(var6, var27, var29, var7.getY());
                        if (!Double.isNaN(var31)) {
                            var18.add(new ShardV22Abilities.Fang(var6, var27, var31, var29, var7.getYaw(), var21));
                            var19 = var21;
                        }
                    }
                }
            }

            if (var18.isEmpty()) {
                var1.sendActionBar(line("No ground in front of you", NamedTextColor.GRAY));
            } else {
                final int var33 = var19 + 7 + 2;
                this.begin(var1, "EVOKER2", var33 * 50L, var2);
                var6.playSound(var1.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0F, 1.0F);
                final HashSet var34 = new HashSet();
                final HashSet var35 = new HashSet();
                (new BukkitRunnable() {
                        int t = 0;

                        public void run() {
                            if (this.t > var33) {
                                ShardV22Abilities.this.ourFangs.removeAll(var35);
                                this.cancel();
                            } else {
                                for (ShardV22Abilities.Fang var2x : var18) {
                                    if (var2x.spawnTick == this.t) {
                                        Entity var3 = var2x.w
                                            .spawnEntity(new Location(var2x.w, var2x.x, var2x.y, var2x.z, var2x.yaw, 0.0F), EntityType.EVOKER_FANGS);
                                        if (var3 instanceof EvokerFangs) {
                                            ((EvokerFangs)var3).setOwner(var1);
                                            ShardV22Abilities.this.ourFangs.add(var3.getUniqueId());
                                            var35.add(var3.getUniqueId());
                                        }
                                    }

                                    if (var2x.spawnTick + 7 == this.t) {
                                        ShardV22Abilities.this.bite(var1, var2x, var34, var4);
                                    }
                                }

                                this.t++;
                            }
                        }
                    })
                    .runTaskTimer(this.plugin, 0L, 1L);
            }
        }
    }

    private void bite(Player var1, ShardV22Abilities.Fang var2, Set<UUID> var3, double var4) {
        Location var6 = new Location(var2.w, var2.x, var2.y + 0.5, var2.z);

        for (Entity var8 : var2.w.getNearbyEntities(var6, 0.9, 1.2, 0.9)) {
            if (var8 instanceof LivingEntity var9 && !untargetable(var9, var1) && !var3.contains(var9.getUniqueId())) {
                var3.add(var9.getUniqueId());
                abilityHit(var9, var4, var1);
            }
        }
    }

    @EventHandler
    public void onFangDamage(EntityDamageByEntityEvent var1) {
        if (this.ourFangs.contains(var1.getDamager().getUniqueId())) {
            var1.setCancelled(true);
        }
    }

    private void dragonBreath(final Player var1) {
        if (this.gate(var1, "DRAGON2", "Dragon's Breath", ShardType.DRAGON)) {
            int var2 = (int)Math.max(1L, this.cfg().getLong("abilities.dragon.breath-seconds", 4L));
            long var3 = Math.max(1L, this.cfg().getLong("abilities.dragon.breath-cooldown", 30L)) * 1000L;
            final double var5 = Math.max(0.0, this.cfg().getDouble("abilities.dragon.breath-damage-hearts", 0.75)) * 2.0;
            final boolean var7 = this.cfg().getBoolean("abilities.dragon.breath-hits-mobs", false);
            this.begin(var1, "DRAGON2", var2 * 1000L, var3);
            final int var8 = var2 * 20;
            (new BukkitRunnable() {
                int t = 0;

                public void run() {
                    if (this.t < var8 && var1.isOnline() && !var1.isDead()) {
                        Location var1x = var1.getEyeLocation();
                        World var2x = var1x.getWorld();
                        Vector var3x = var1x.getDirection();
                        double var4 = var3x.getX();
                        double var6 = var3x.getY();
                        double var8x = var3x.getZ();
                        double var10 = var1x.getX() + var4 * 0.3;
                        double var12 = var1x.getY() - 0.25 + var6 * 0.3;
                        double var14 = var1x.getZ() + var8x * 0.3;

                        for (double var16 = 0.4; var16 <= 7.0; var16 += 0.4) {
                            double var18 = 0.3 + 1.3 * (var16 / 7.0);
                            Location var20 = new Location(var2x, var10 + var4 * var16, var12 + var6 * var16, var14 + var8x * var16);
                            ShardPatch.particle(var2x, Particle.DRAGON_BREATH, var20, 2, var18 * 0.35, var18 * 0.35, var18 * 0.35, 0.01);
                        }

                        if (this.t == 0) {
                            var2x.playSound(var1.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6F, 1.0F);
                        }

                        if (this.t % 10 == 0) {
                            double var37 = 3.5;
                            Location var38 = new Location(var2x, var10 + var4 * var37, var12 + var6 * var37, var14 + var8x * var37);
                            double var19 = var37 + 2.5;

                            for (Entity var22 : var2x.getNearbyEntities(var38, var19, var19, var19)) {
                                if (var22 instanceof LivingEntity && (var22 instanceof Player || var7)) {
                                    LivingEntity var23 = (LivingEntity)var22;
                                    if (!ShardV22Abilities.untargetable(var23, var1)) {
                                        Location var24 = var23.getLocation();
                                        double var25 = var24.getX() - var10;
                                        double var27 = var24.getY() + 0.9 - var12;
                                        double var29 = var24.getZ() - var14;
                                        double var31 = var25 * var4 + var27 * var6 + var29 * var8x;
                                        if (!(var31 < 0.0) && !(var31 > 7.5)) {
                                            double var33 = Math.sqrt(Math.max(0.0, var25 * var25 + var27 * var27 + var29 * var29 - var31 * var31));
                                            double var35 = 0.3 + 1.3 * (var31 / 7.0);
                                            if (!(var33 > var35 + 0.5)) {
                                                ShardV22Abilities.abilityHit(var23, var5, var1);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        this.t++;
                    } else {
                        this.cancel();
                    }
                }
            }).runTaskTimer(this.plugin, 0L, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCurseHit(EntityDamageByEntityEvent var1) {
        if (!dealingAbilityDamage) {
            if (var1.getDamager() instanceof Player && var1.getEntity() instanceof Player) {
                DamageCause var2 = var1.getCause();
                if (var2 == DamageCause.ENTITY_ATTACK || var2 == DamageCause.ENTITY_SWEEP_ATTACK) {
                    Player var3 = (Player)var1.getDamager();
                    Player var4 = (Player)var1.getEntity();
                    if (this.plugin.hasShard(var3, ShardType.EVOKER)) {
                        double var5 = Math.max(0.0, Math.min(100.0, this.cfg().getDouble("abilities.evoker.curse-chance-percent", 15.0))) / 100.0;
                        if (!(ThreadLocalRandom.current().nextDouble() >= var5)) {
                            int var7 = (int)Math.max(1L, this.cfg().getLong("abilities.evoker.curse-seconds", 5L)) * 20;
                            var4.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, var7, 0, false, true, true));
                            var4.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, var7, 1, false, true, true));
                        }
                    }
                }
            }
        }
    }

    private Map<UUID, UUID> squadOwners() {
        if (this.squadOwners == null && !this.reflectFailed) {
            try {
                Field var1 = ShardExtMain.class.getDeclaredField("abilities");
                var1.setAccessible(true);
                Object var2 = var1.get(this.plugin);
                if (var2 == null) {
                    return null;
                }

                Field var3 = var2.getClass().getDeclaredField("blazeOwners");
                var3.setAccessible(true);
                this.squadOwners = (Map<UUID, UUID>)var3.get(var2);
            } catch (Throwable var4) {
                this.reflectFailed = true;
                this.plugin.getLogger().warning("Could not hook Blaze Squad for the overworld fix: " + var4);
            }

            return this.squadOwners;
        } else {
            return this.squadOwners;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSquadBlazeWater(EntityDamageEvent var1) {
        if (var1.getEntity() instanceof Blaze) {
            if (var1.getCause() == DamageCause.DROWNING) {
                Map var2 = this.squadOwners();
                if (var2 != null && var2.containsKey(var1.getEntity().getUniqueId())) {
                    var1.setCancelled(true);
                }
            }
        }
    }

    static List<Component> lore(ShardType var0, FileConfiguration var1) {
        ArrayList var2 = new ArrayList();
        var2.add(line(var0.description(), NamedTextColor.GRAY));
        var2.add(Component.empty());
        var2.add(join(line("Passive: ", NamedTextColor.DARK_GRAY), line(var0.passiveSummary(), NamedTextColor.WHITE)));
        if (var0 == ShardType.DRAGON) {
            var2.add(join(line("Ability 1 (/ability1): ", NamedTextColor.DARK_GRAY), line("Dragon Scales (" + var0.cooldownMs() / 1000L + "s)", var0.color())));
            var2.add(
                join(
                    line("Ability 2 (/ability2): ", NamedTextColor.DARK_GRAY),
                    line("Dragon's Breath (" + var1.getLong("abilities.dragon.breath-cooldown", 30L) + "s)", var0.color())
                )
            );
        } else if (var0 == ShardType.EVOKER) {
            var2.add(
                join(
                    line("Ability 1 (/ability1): ", NamedTextColor.DARK_GRAY),
                    line("Immortality (" + var1.getLong("abilities.evoker.immortality-cooldown", 45L) + "s)", var0.color())
                )
            );
            var2.add(
                join(
                    line("Ability 2 (/ability2): ", NamedTextColor.DARK_GRAY),
                    line("Curse Fangs (" + var1.getLong("abilities.evoker.fangs-cooldown", 35L) + "s)", var0.color())
                )
            );
        }

        return var2;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent var1) {
        final Player var2 = var1.getPlayer();
        (new BukkitRunnable() {
            public void run() {
                if (var2.isOnline()) {
                    ShardV22Abilities.this.refreshLore(var2);
                }
            }
        }).runTaskLater(this.plugin, 40L);
    }

    private void refreshLore(Player var1) {
        PlayerInventory var2 = var1.getInventory();
        ItemStack[] var3 = var2.getContents();

        for (int var4 = 0; var4 < var3.length; var4++) {
            ItemStack var5 = var3[var4];
            ShardType var6 = this.plugin.getShardType(var5);
            if (var6 != null) {
                List var7;
                if (var6 != ShardType.DRAGON && var6 != ShardType.EVOKER) {
                    if (var6 != ShardType.EMBER && var6 != ShardType.SCULK) {
                        continue;
                    }

                    var7 = ShardAbilities.lore(var6, this.cfg());
                } else {
                    var7 = lore(var6, this.cfg());
                }

                ItemMeta var8 = var5.getItemMeta();
                if (var8 != null) {
                    var8.lore(var7);
                    var5.setItemMeta(var8);
                    var2.setItem(var4, var5);
                }
            }
        }
    }

    private void tickDisplay() {
        long var1 = System.currentTimeMillis();

        for (Player var4 : Bukkit.getOnlinePlayers()) {
            for (String var8 : new String[]{"EVOKER1", "EVOKER2", "DRAGON2"}) {
                String var9 = key(var4, var8);
                if (this.waiting.contains(var9)) {
                    Long var10 = this.cdEnd.get(var9);
                    if (var10 == null || var1 >= var10) {
                        this.waiting.remove(var9);
                        String var11 = var8.equals("EVOKER1") ? "Immortality" : (var8.equals("EVOKER2") ? "Curse Fangs" : "Dragon's Breath");
                        NamedTextColor var12 = var8.equals("DRAGON2") ? ShardType.DRAGON.color() : ShardType.EVOKER.color();
                        join(line(var11, var12).decoration(TextDecoration.BOLD, true), line(" is ready!", NamedTextColor.GREEN));
                    }
                }
            }

            ShardType var13 = this.plugin.getShardType(var4.getInventory().getItemInMainHand());
            if (var13 == ShardType.EVOKER) {
                var4.sendActionBar(
                    join(this.stateText(var4, "EVOKER1", "Immortality"), line("", NamedTextColor.DARK_GRAY), this.stateText(var4, "EVOKER2", "Curse Fangs"))
                );
            }
        }
    }

    private Component stateText(Player var1, String var2, String var3) {
        return ShardHud.state(var2, this.active(var1, var2), this.remaining(var1, var2), ShardType.EVOKER.color());
    }

    private static final class Fang {
        final World w;
        final double x;
        final double y;
        final double z;
        final float yaw;
        final int spawnTick;

        Fang(World var1, double var2, double var4, double var6, float var8, int var9) {
            this.w = var1;
            this.x = var2;
            this.y = var4;
            this.z = var6;
            this.yaw = var8;
            this.spawnTick = var9;
        }
    }
}
