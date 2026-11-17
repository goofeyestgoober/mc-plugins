package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ShardV25Main extends RestoreMain {
    static final ShardType[] NORMAL = new ShardType[]{
        ShardType.VELOCITY, ShardType.TITAN, ShardType.TIDE, ShardType.PHANTOM, ShardType.FROST, ShardType.VERDANT, ShardType.STORM
    };
    TierStore tiers;
    final Map<String, Long> cd2 = new HashMap<>();
    private PlayerInteractEvent lastAbility2;

    @Override
    public void onEnable() {
        super.onEnable();
        this.tiers = new TierStore(this.getDataFolder());
        Bukkit.getPluginManager().registerEvents(new TierListener(this, this.tiers), this);
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                ShardV25Main.this.tickBuffs();
            }
        }, 40L, 20L);
    }

    int need2() {
        return Math.max(1, this.getConfig().getInt("tiers.tier2-kills", 5));
    }

    int need3() {
        return Math.max(1, this.getConfig().getInt("tiers.tier3-kills", 10));
    }

    boolean resetOnUpgrade() {
        return this.getConfig().getInt("tiers.reset-victims-on-upgrade", 0) != 0;
    }

    static boolean isNormal(ShardType var0) {
        for (ShardType var4 : NORMAL) {
            if (var4 == var0) {
                return true;
            }
        }

        return false;
    }

    static String abilityName(ShardType var0) {
        switch (var0) {
            case VELOCITY:
                return "Gale Leap";
            case TITAN:
                return "Seismic Roar";
            case TIDE:
                return "Whirlpool";
            case PHANTOM:
                return "Shadow Veil";
            case FROST:
                return "Frost Nova";
            case VERDANT:
                return "Regrowth";
            case STORM:
                return "Chain Lightning";
            default:
                return "None";
        }
    }

    static String buffName(ShardType var0) {
        switch (var0) {
            case VELOCITY:
                return "Jump Boost II";
            case TITAN:
                return "Resistance I";
            case TIDE:
                return "Dolphin's Grace + Conduit Power";
            case PHANTOM:
                return "Night Vision";
            case FROST:
                return "None";
            case VERDANT:
                return "Regeneration I";
            case STORM:
                return "Haste II";
            default:
                return "None";
        }
    }

    private long cooldownSeconds(ShardType var1) {
        return this.getConfig().getLong("tiers.cooldown-seconds." + var1.id(), switch (var1) {
            case VELOCITY -> 20L;
            case TITAN -> 25L;
            case TIDE -> 25L;
            case PHANTOM -> 30L;
            case FROST -> 30L;
            case VERDANT -> 40L;
            default -> 30L;
        });
    }

    @EventHandler
    @Override
    public void onShardUse(PlayerInteractEvent var1) {
        Player var2 = var1.getPlayer();
        if (var2.isSneaking()) {
            ShardType var3 = this.getShardType(var1.getItem());
            if (var3 != null && isNormal(var3)) {
                Action var4 = var1.getAction();
                if ((var4 == Action.RIGHT_CLICK_AIR || var4 == Action.RIGHT_CLICK_BLOCK) && this.tiers.tier(var2.getUniqueId(), var3) >= 2) {
                    EquipmentSlot var5 = var1.getHand();
                    if (var5 == EquipmentSlot.OFF_HAND && this.getShardType(var2.getInventory().getItemInMainHand()) != null) {
                        return;
                    }

                    if (var1 == this.lastAbility2) {
                        return;
                    }

                    this.lastAbility2 = var1;
                    var1.setCancelled(true);
                    this.ability2(var2, var3);
                    return;
                }
            }
        }

        super.onShardUse(var1);
    }

    private List<LivingEntity> targets(Player var1, double var2) {
        ArrayList var4 = new ArrayList();

        for (Entity var6 : var1.getNearbyEntities(var2, var2, var2)) {
            if (var6 instanceof LivingEntity && !(var6 instanceof ArmorStand) && !var6.isDead()) {
                if (var6 instanceof Player) {
                    GameMode var7 = ((Player)var6).getGameMode();
                    if (var7 == GameMode.CREATIVE || var7 == GameMode.SPECTATOR) {
                        continue;
                    }
                }

                var4.add((LivingEntity)var6);
            }
        }

        return var4;
    }

    private static void effect(LivingEntity var0, PotionEffectType var1, int var2, int var3) {
        var0.addPotionEffect(new PotionEffect(var1, var2, var3, false, true, true));
    }

    private void ability2(Player var1, ShardType var2) {
        String var3 = var1.getUniqueId() + ":" + var2.id();
        long var4 = System.currentTimeMillis();
        Long var6 = this.cd2.get(var3);
        if (var6 != null && var6 > var4) {
            long var14 = (var6 - var4 + 999L) / 1000L;
            var1.sendActionBar(abilityName(var2) + " cooldown: " + var14);
        } else {
            Location var7 = var1.getLocation();
            boolean var8 = true;
            switch (var2) {
                case VELOCITY:
                    Vector var19 = var1.getEyeLocation().getDirection();
                    var1.setVelocity(var19.multiply(1.3).setY(1.1));
                    var1.getWorld().playSound(var7, Sound.ENTITY_PHANTOM_FLAP, 1.0F, 1.2F);
                    ShardFX.spawn(var1.getWorld(), Particle.CLOUD, var7, 30, 0.4, 0.1, 0.4, 0.05);
                    break;
                case TITAN:
                    for (LivingEntity var23 : this.targets(var1, 6.0)) {
                        var23.damage(8.0, var1);
                        Vector var25 = var23.getLocation().toVector().subtract(var7.toVector());
                        if (var25.lengthSquared() > 1.0E-4) {
                            var23.setVelocity(var25.normalize().multiply(1.5).setY(0.6));
                        }
                    }

                    effect(var1, PotionEffectType.RESISTANCE, 120, 1);
                    var1.getWorld().playSound(var7, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5F, 0.6F);
                    ShardFX.spawn(var1.getWorld(), Particle.EXPLOSION, var7, 6, 2.0, 0.3, 2.0, 0.0);
                    break;
                case TIDE:
                    for (LivingEntity var22 : this.targets(var1, 8.0)) {
                        Vector var24 = var7.toVector().subtract(var22.getLocation().toVector());
                        if (var24.lengthSquared() > 1.0E-4) {
                            var22.setVelocity(var24.normalize().multiply(1.2).setY(0.3));
                        }

                        effect(var22, PotionEffectType.SLOWNESS, 60, 1);
                    }

                    effect(var1, PotionEffectType.DOLPHINS_GRACE, 200, 1);
                    var1.getWorld().playSound(var7, Sound.ENTITY_PLAYER_SPLASH, 1.5F, 0.7F);
                    ShardFX.spawn(var1.getWorld(), Particle.BUBBLE, var7.clone().add(0.0, 1.0, 0.0), 80, 3.0, 1.0, 3.0, 0.1);
                    break;
                case PHANTOM:
                    for (LivingEntity var21 : this.targets(var1, 8.0)) {
                        effect(var21, PotionEffectType.BLINDNESS, 60, 0);
                    }

                    effect(var1, PotionEffectType.INVISIBILITY, 160, 0);
                    var1.getWorld().playSound(var7, Sound.ENTITY_PHANTOM_FLAP, 1.5F, 0.5F);
                    ShardFX.spawn(var1.getWorld(), Particle.SMOKE, var7.clone().add(0.0, 1.0, 0.0), 60, 0.6, 0.9, 0.6, 0.02);
                    break;
                case FROST:
                    for (LivingEntity var20 : this.targets(var1, 6.0)) {
                        var20.setFreezeTicks(300);
                        effect(var20, PotionEffectType.SLOWNESS, 80, 2);
                        var20.damage(4.0, var1);
                    }

                    var1.getWorld().playSound(var7, Sound.BLOCK_GLASS_BREAK, 1.5F, 0.8F);
                    ShardFX.spawn(var1.getWorld(), Particle.SNOWFLAKE, var7.clone().add(0.0, 1.0, 0.0), 120, 3.0, 0.8, 3.0, 0.05);
                    break;
                case VERDANT:
                    effect(var1, PotionEffectType.REGENERATION, 100, 3);
                    var1.getWorld().playSound(var7, Sound.ITEM_BONE_MEAL_USE, 1.5F, 1.0F);
                    ShardFX.spawn(var1.getWorld(), Particle.HAPPY_VILLAGER, var7.clone().add(0.0, 1.0, 0.0), 50, 0.8, 1.0, 0.8, 0.0);
                    break;
                case STORM:
                    final Location var9 = var7;
                    List var10 = this.targets(var1, 12.0);
                    Collections.sort(var10, new Comparator<LivingEntity>() {
                        public int compare(LivingEntity var1, LivingEntity var2x) {
                            return Double.compare(var1.getLocation().distanceSquared(var9), var2x.getLocation().distanceSquared(var9));
                        }
                    });
                    if (var10.isEmpty()) {
                        var1.sendActionBar("§cNo targets within 12 blocks.");
                        var8 = false;
                    } else {
                        int var11 = Math.min(3, var10.size());

                        for (int var12 = 0; var12 < var11; var12++) {
                            LivingEntity var13 = (LivingEntity)var10.get(var12);
                            var13.getWorld().strikeLightningEffect(var13.getLocation());
                            var13.damage(7.0, var1);
                            var13.setFireTicks(60);
                        }

                        var1.getWorld().playSound(var7, Sound.ITEM_TRIDENT_THUNDER, 1.5F, 1.0F);
                    }
                    break;
                default:
                    var8 = false;
            }

            if (var8) {
                this.cd2.put(var3, var4 + this.cooldownSeconds(var2) * 1000L);
                var1.sendActionBar("§e" + abilityName(var2) + "!");
            }
        }
    }

    private void tickBuffs() {
        for (Player var2 : Bukkit.getOnlinePlayers()) {
            if (!var2.isDead()) {
                for (ShardType var6 : NORMAL) {
                    if (this.tiers.tier(var2.getUniqueId(), var6) >= 3 && this.hasShard(var2, var6)) {
                        this.buff(var2, var6);
                    }
                }
            }
        }
    }

    private static void keep(Player var0, PotionEffectType var1, int var2, int var3) {
        PotionEffect var4 = var0.getPotionEffect(var1);
        if (var4 == null || var4.getAmplifier() <= var2) {
            var0.addPotionEffect(new PotionEffect(var1, var3, var2, true, false, true));
        }
    }

    private void buff(Player var1, ShardType var2) {
        switch (var2) {
            case VELOCITY:
                break;
            case TITAN:
                keep(var1, PotionEffectType.RESISTANCE, 0, 100);
                break;
            case TIDE:
                keep(var1, PotionEffectType.DOLPHINS_GRACE, 0, 100);
                keep(var1, PotionEffectType.CONDUIT_POWER, 0, 100);
                break;
            case PHANTOM:
                keep(var1, PotionEffectType.NIGHT_VISION, 0, 300);
            case FROST:
                break;
            case VERDANT:
                keep(var1, PotionEffectType.REGENERATION, 0, 100);
                break;
            case STORM:
                keep(var1, PotionEffectType.HASTE, 1, 100);
        }
    }

    private static boolean isTierArg(String var0) {
        return "1".equals(var0) || "2".equals(var0) || "3".equals(var0);
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        if (!var2.getName().equalsIgnoreCase("shard") || var4.length < 2 || !isTierArg(var4[1])) {
            return super.onCommand(var1, var2, var3, var4);
        }

        if (!var1.hasPermission("superhumanshard.admin")) {
            var1.sendMessage("§cYou don't have permission.");
            return true;
        }

        ShardType var5 = ShardType.fromId(var4[0]);
        if (var5 != null && isNormal(var5)) {
            Player var6 = var4.length >= 3 ? Bukkit.getPlayerExact(var4[2]) : (var1 instanceof Player ? (Player)var1 : null);
            if (var6 == null) {
                var1.sendMessage("§cUsage: /shard <shard> <1|2|3> [player]  (player must be online)");
                return true;
            }

            int var7 = Integer.parseInt(var4[1]);
            boolean var8 = super.onCommand(var1, var2, var3, new String[]{var4[0], var6.getName()});
            this.tiers.setTier(var6.getUniqueId(), var5, var7);
            this.tiers.setVictims(var6.getUniqueId(), var5, new ArrayList<>());
            this.tiers.save();
            var1.sendMessage("§aSet " + var6.getName() + "'s " + var5.displayName() + " to tier " + var7 + ".");
            if (!var6.equals(var1)) {
                var6.sendMessage("§aYour " + var5.displayName() + " is now tier " + var7 + ".");
            }

            if (!var8) {
            }

            return true;
        } else {
            var1.sendMessage("§cTiers only exist for: velocity, titan, tide, phantom, frost, verdant, storm.");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        List var5 = super.onTabComplete(var1, var2, var3, var4);
        if (!var2.getName().equalsIgnoreCase("shard")) {
            return var5;
        }

        if (var4.length == 2) {
            ShardType var6 = ShardType.fromId(var4[0]);
            if (var6 != null && isNormal(var6)) {
                ArrayList var7 = new ArrayList();
                if (var5 != null) {
                    var7.addAll(var5);
                }

                for (String var11 : new String[]{"1", "2", "3"}) {
                    if (var11.startsWith(var4[1])) {
                        var7.add(var11);
                    }
                }

                return var7;
            }
        } else if (var4.length == 3 && isTierArg(var4[1])) {
            ArrayList var12 = new ArrayList();

            for (Player var14 : Bukkit.getOnlinePlayers()) {
                if (var14.getName().toLowerCase().startsWith(var4[2].toLowerCase())) {
                    var12.add(var14.getName());
                }
            }

            return var12;
        }

        return var5;
    }
}
