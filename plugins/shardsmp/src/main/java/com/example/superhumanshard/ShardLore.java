package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.file.FileConfiguration;

final class ShardLore {
    static final String VERSION = "L1";
    final Component name;
    final List<Component> lines;
    final String sig;

    private ShardLore(Component var1, List<Component> var2, String var3) {
        this.name = var1;
        this.lines = var2;
        this.sig = var3;
    }

    static String tagline(ShardType var0) {
        switch (var0) {
            case VELOCITY:
                return "OUTRUN EVERYTHING";
            case TITAN:
                return "HIT LIKE A MOUNTAIN";
            case TIDE:
                return "RULE THE DEEP";
            case PHANTOM:
                return "VANISH INTO THE DARK";
            case EMBER:
                return "BURN BRIGHT";
            case DRAGON:
                return "BOW TO THE DRAGON";
            case FROST:
                return "FREEZE THE FIELD";
            case VERDANT:
                return "LET EVERYTHING GROW";
            case STORM:
                return "CALL DOWN THE THUNDER";
            case SCULK:
                return "HEAR EVERYTHING";
            case EVOKER:
                return "CURSE YOUR ENEMIES";
            default:
                return "";
        }
    }

    private static long defaultTier2Cooldown(ShardType var0) {
        switch (var0) {
            case VELOCITY:
                return 20L;
            case TITAN:
            case TIDE:
                return 25L;
            case PHANTOM:
            case EMBER:
            case DRAGON:
            case FROST:
            default:
                return 30L;
            case VERDANT:
                return 40L;
        }
    }

    static ShardLore.Power[] powers(ShardType var0, FileConfiguration var1) {
        switch (var0) {
            case EMBER:
                return new ShardLore.Power[]{
                    new ShardLore.Power("Fireball", var1.getLong("abilities.ember.fireball-cooldown", 35L)),
                    new ShardLore.Power("Ember Arena", var1.getLong("abilities.ember.swap-cooldown", 300L))
                };
            case DRAGON:
                return new ShardLore.Power[]{
                    new ShardLore.Power("Dragon Scales", var0.cooldownMs() / 1000L),
                    new ShardLore.Power("Dragon's Breath", var1.getLong("abilities.dragon.breath-cooldown", 30L))
                };
            case FROST:
            case VERDANT:
            case STORM:
            default:
                return new ShardLore.Power[]{
                    new ShardLore.Power(var0.abilityName(), var0.cooldownMs() / 1000L),
                    new ShardLore.Power(ShardV25Main.abilityName(var0), var1.getLong("tiers.cooldown-seconds." + var0.id(), defaultTier2Cooldown(var0)))
                };
            case SCULK:
                return new ShardLore.Power[]{
                    new ShardLore.Power("Supersonic Beam", var1.getLong("abilities.sculk.beam-cooldown", 35L)),
                    new ShardLore.Power("Sculk Tracker", var1.getLong("abilities.sculk.track-cooldown", 35L))
                };
            case EVOKER:
                return new ShardLore.Power[]{
                    new ShardLore.Power("Immortality", var1.getLong("abilities.evoker.immortality-cooldown", 45L)),
                    new ShardLore.Power("Evoker Fangs", var1.getLong("abilities.evoker.fangs-cooldown", 35L))
                };
        }
    }

    static String signature(ShardType var0, FileConfiguration var1, int var2, int var3, int var4) {
        ShardLore.Power[] var5 = powers(var0, var1);
        return "L1|" + var0.id() + "|" + var2 + "|" + var3 + "|" + var4 + "|" + var5[0].cooldown + "|" + var5[1].cooldown;
    }

    static Component line(String var0, TextColor var1) {
        return Component.text(var0, var1).decoration(TextDecoration.ITALIC, false);
    }

    static Component bold(String var0, TextColor var1) {
        return Component.text(var0, var1).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false);
    }

    static Component blank() {
        return Component.text(" ").decoration(TextDecoration.ITALIC, false);
    }

    static Component header(String var0) {
        return Component.text("✦ ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false).append(bold(var0, NamedTextColor.YELLOW));
    }

    static Component bullet(Component var0) {
        return line("- ", NamedTextColor.DARK_GRAY).append(var0);
    }

    static ShardLore build(ShardType var0, FileConfiguration var1, int var2, int var3, int var4) {
        boolean var5 = ShardV25Main.isNormal(var0);
        NamedTextColor var6 = var0.color();
        ShardLore.Power[] var7 = powers(var0, var1);
        String var8 = var0.displayName();
        String var9 = var8.toUpperCase(Locale.ROOT);
        Component var10;
        if (var9.endsWith(" SHARD")) {
            var10 = bold(var9.substring(0, var9.length() - 6), var6).append(line(" SHARD", NamedTextColor.WHITE));
        } else {
            var10 = bold(var9, var6);
        }

        ArrayList var11 = new ArrayList();
        var11.add(line(var5 ? "(Tier " + var2 + ")" : "(Mythic)", NamedTextColor.LIGHT_PURPLE));
        var11.add(bold(tagline(var0), NamedTextColor.WHITE));
        var11.add(blank());
        ArrayList<String> var12 = new ArrayList<>();
        boolean var13 = false;

        for (String var17 : var0.passiveSummary().split(",\\s*")) {
            String var18 = var17.trim();
            if (!var18.isEmpty()) {
                if (var18.equalsIgnoreCase("Double Jump")) {
                    var13 = true;
                } else {
                    var12.add(var18);
                }
            }
        }

        var11.add(header("PASSIVES"));

        for (String var22 : var12) {
            var11.add(bullet(line(var22, NamedTextColor.GRAY)));
        }

        if (var5) {
            Component var20 = line(ShardV25Main.buffName(var0), NamedTextColor.GRAY).append(line("  (Tier 3 · " + var4 + " kills)", NamedTextColor.DARK_GRAY));
            if (var2 >= 3) {
                var20 = var20.append(line(" ✔", NamedTextColor.GREEN));
            }

            var11.add(bullet(var20));
        }

        var11.add(blank());
        var11.add(header("ABILITY"));
        if (var13) {
            var11.add(bullet(line("Double Jump", NamedTextColor.GRAY)));
        }

        var11.add(bullet(line(var7[0].name, NamedTextColor.GRAY)));
        var11.add(blank());
        var11.add(header("POWERS"));
        var11.add(
            bullet(
                bold(var7[0].name.toUpperCase(Locale.ROOT), NamedTextColor.WHITE)
                    .append(line("  /ability1", var6))
                    .append(line("  " + var7[0].cooldown + "s", NamedTextColor.DARK_GRAY))
            )
        );
        Component var21 = bold(var7[1].name.toUpperCase(Locale.ROOT), NamedTextColor.WHITE)
            .append(line("  /ability2", var6))
            .append(line("  " + var7[1].cooldown + "s", NamedTextColor.DARK_GRAY));
        if (var5) {
            if (var2 >= 2) {
                var21 = var21.append(line(" ✔", NamedTextColor.GREEN));
            } else {
                var21 = var21.append(line(" ✖ Tier 2 · " + var3 + " kills", NamedTextColor.RED));
            }
        }

        var11.add(bullet(var21));
        return new ShardLore(var10, var11, signature(var0, var1, var2, var3, var4));
    }

    static final class Power {
        final String name;
        final long cooldown;

        Power(String var1, long var2) {
            this.name = var1;
            this.cooldown = var2;
        }
    }
}
