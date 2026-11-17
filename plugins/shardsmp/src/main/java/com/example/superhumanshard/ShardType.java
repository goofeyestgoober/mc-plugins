package com.example.superhumanshard;

import java.util.Locale;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffectType;

public enum ShardType {
    VELOCITY(
        "velocity",
        "Sky Shard",
        Material.AMETHYST_SHARD,
        NamedTextColor.LIGHT_PURPLE,
        Particle.PORTAL,
        Sound.ENTITY_ENDERMAN_TELEPORT,
        "Super Dash",
        10000L,
        "A shard crackling with raw speed.",
        "Speed II, Double Jump",
        new ShardType.Passive[]{new ShardType.Passive(PotionEffectType.SPEED, 1)}
    ),
    TITAN(
        "titan",
        "Titan Shard",
        Material.NETHERITE_SCRAP,
        NamedTextColor.RED,
        Particle.CRIT,
        Sound.ENTITY_IRON_GOLEM_ATTACK,
        "Ground Pound",
        15000L,
        "A shard heavy with unbreakable might.",
        "Strength II, 12 Hearts",
        new ShardType.Passive[]{new ShardType.Passive(PotionEffectType.STRENGTH, 1)}
    ),
    TIDE(
        "tide",
        "Tide Shard",
        Material.PRISMARINE_SHARD,
        NamedTextColor.AQUA,
        Particle.SPLASH,
        Sound.ENTITY_PLAYER_SPLASH,
        "Tidal Surge",
        18000L,
        "A shard that hums with the deep ocean.",
        "Water Breathing",
        new ShardType.Passive[]{new ShardType.Passive(PotionEffectType.WATER_BREATHING, 0)}
    ),
    PHANTOM(
        "phantom",
        "Ghost Shard",
        Material.ECHO_SHARD,
        NamedTextColor.DARK_AQUA,
        Particle.CLOUD,
        Sound.ENTITY_PHANTOM_FLAP,
        "Ghost Swarm",
        30000L,
        "A shard that whispers from the dark.",
        "Speed I",
        new ShardType.Passive[]{new ShardType.Passive(PotionEffectType.SPEED, 0), new ShardType.Passive(PotionEffectType.SPEED, 0)}
    ),
    EMBER(
        "ember",
        "Fire Shard",
        Material.FIRE_CHARGE,
        NamedTextColor.GOLD,
        Particle.FLAME,
        Sound.ENTITY_BLAZE_SHOOT,
        "Fireball",
        20000L,
        "A shard that burns with living flame.",
        "Fire Resistance",
        new ShardType.Passive[]{new ShardType.Passive(PotionEffectType.FIRE_RESISTANCE, 0)}
    ),
    DRAGON(
        "dragon",
        "Dragon Shard",
        Material.DRAGON_BREATH,
        NamedTextColor.DARK_PURPLE,
        Particle.DRAGON_BREATH,
        Sound.ENTITY_ENDER_DRAGON_GROWL,
        "Dragon Scales",
        120000L,
        "A shard forged from the Ender Dragon's power.",
        "20 Hearts",
        new ShardType.Passive[0]
    ),
    FROST(
        "frost",
        "Ice Shard",
        Material.QUARTZ,
        NamedTextColor.WHITE,
        Particle.SNOWFLAKE,
        Sound.BLOCK_GLASS_BREAK,
        "Ice Breath",
        25000L,
        "A shard cold enough to still the sea.",
        "Frost Walker, Freeze Immunity",
        new ShardType.Passive[0]
    ),
    VERDANT(
        "verdant",
        "Nature Shard",
        Material.TURTLE_SCUTE,
        NamedTextColor.GREEN,
        Particle.HAPPY_VILLAGER,
        Sound.ITEM_BONE_MEAL_USE,
        "Bloom",
        30000L,
        "A shard that pulses with living growth.",
        "Luck, Slower Hunger, Hero of the Village",
        new ShardType.Passive[]{new ShardType.Passive(PotionEffectType.LUCK, 0), new ShardType.Passive(PotionEffectType.HERO_OF_THE_VILLAGE, 0)}
    ),
    STORM(
        "storm",
        "Storm Shard",
        Material.BREEZE_ROD,
        NamedTextColor.YELLOW,
        Particle.ELECTRIC_SPARK,
        Sound.ITEM_TRIDENT_THUNDER,
        "Thunder Strike",
        20000L,
        "A shard humming with a gathering storm.",
        "Half Fall Damage, Lightning Immunity",
        new ShardType.Passive[0]
    ),
    SCULK(
        "sculk",
        "Sculk Shard",
        Material.DISC_FRAGMENT_5,
        NamedTextColor.BLUE,
        Particle.SCULK_SOUL,
        Sound.ENTITY_WARDEN_SONIC_BOOM,
        "Supersonic Beam",
        20000L,
        "A shard that listens to everything.",
        "Speed II, Strength II",
        new ShardType.Passive[]{new ShardType.Passive(PotionEffectType.SPEED, 1), new ShardType.Passive(PotionEffectType.STRENGTH, 1)}
    ),
    EVOKER(
        "evoker",
        "Evoker Shard",
        Material.GHAST_TEAR,
        NamedTextColor.DARK_RED,
        Particle.TOTEM_OF_UNDYING,
        Sound.ENTITY_EVOKER_CAST_SPELL,
        "Immortality",
        45000L,
        "A shard etched with illager sorcery.",
        "15% on-hit curse: Blindness + Slowness II",
        new ShardType.Passive[0]
    );

    private final String id;
    private final String displayName;
    private final Material material;
    private final NamedTextColor color;
    private final Particle particle;
    private final Sound sound;
    private final String abilityName;
    private final long cooldownMs;
    private final String description;
    private final String passiveSummary;
    private final ShardType.Passive[] passives;

    ShardType(
        String nullxx,
        String nullxxx,
        Material nullxxxx,
        NamedTextColor nullxxxxx,
        Particle nullxxxxxx,
        Sound nullxxxxxxx,
        String nullxxxxxxxx,
        long nullxxxxxxxxx,
        String nullxxxxxxxxxx,
        String nullxxxxxxxxxxx,
        ShardType.Passive[] nullxxxxxxxxxxxx
    ) {
        this.id = nullxx;
        this.displayName = nullxxx;
        this.material = nullxxxx;
        this.color = nullxxxxx;
        this.particle = nullxxxxxx;
        this.sound = nullxxxxxxx;
        this.abilityName = nullxxxxxxxx;
        this.cooldownMs = nullxxxxxxxxx;
        this.description = nullxxxxxxxxxx;
        this.passiveSummary = nullxxxxxxxxxxx;
        this.passives = nullxxxxxxxxxxxx;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public Material material() {
        return this.material;
    }

    public NamedTextColor color() {
        return this.color;
    }

    public Particle particle() {
        return this.particle;
    }

    public Sound sound() {
        return this.sound;
    }

    public String abilityName() {
        return this.abilityName;
    }

    public long cooldownMs() {
        return this.cooldownMs;
    }

    public String description() {
        return this.description;
    }

    public String passiveSummary() {
        return this.passiveSummary;
    }

    public ShardType.Passive[] passives() {
        return this.passives;
    }

    public static ShardType fromId(String var0) {
        if (var0 == null) {
            return null;
        }

        String var1 = var0.toLowerCase(Locale.ROOT);

        for (ShardType var5 : values()) {
            if (var5.id.equals(var1)) {
                return var5;
            }
        }

        return null;
    }

    record Passive(PotionEffectType type, int amplifier) {
    }
}
