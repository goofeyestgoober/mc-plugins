package com.example.superhumanshard;

import org.bukkit.Particle;
import org.bukkit.World;

final class ShardV30Fx {
    private static final int SEG = 16;
    private static final int KNUCKLE = 10;
    private static final int FSEG = 8;
    private static final float BONE_SIZE = 1.9F;
    private static final float BONE_SIDE = 1.5F;
    private static final float FINGER_SIZE = 1.4F;
    private static final float FINGER_SIDE = 1.2F;
    private static final double BONE_SPREAD = 0.12;
    private static final double FINGER_SPREAD = 0.1;

    private ShardV30Fx() {
    }

    static void wings(ShardV30Main var0, World var1, double var2, double var4, double var6, double var8) {
        double var10 = -Math.sin(var8);
        double var12 = Math.cos(var8);
        double var14 = -Math.cos(var8);
        double var16 = -Math.sin(var8);
        double var18 = Math.sin(var0.tick * 0.09) * 0.28;

        for (byte var20 = -1; var20 <= 1; var20 += 2) {
            double[] var21 = null;

            for (int var22 = 0; var22 <= 16; var22++) {
                double var23 = var22 / 16.0;
                double var25 = 0.15 + 1.7 * var23 * Math.cos(var18 * var23);
                double var27 = 1.25 + 0.95 * Math.sin(var23 * Math.PI * 0.62) + var18 * var23 * 1.2;
                double var29 = 0.28 + 0.5 * var23;
                double var31 = var2 + var14 * var20 * var25 - var10 * var29;
                double var33 = var6 + var16 * var20 * var25 - var12 * var29;
                double var35 = var4 + var27;
                var0.trans(var1, var31, var35, var33, 12721151, 16754943, 1.9F);
                if (var22 % 2 == 0) {
                    var0.trans(var1, var31, var35 + 0.12, var33, 12721151, 16754943, 1.5F);
                    var0.trans(var1, var31, var35 - 0.12, var33, 12721151, 16754943, 1.5F);
                }

                if (var22 == 10) {
                    var21 = new double[]{var25, var27, var29};
                }

                if (var22 == 16 && var0.tick % 4L == 0L) {
                    var0.fx(var1, Particle.END_ROD, var31, var35, var33, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }

            if (var21 != null) {
                for (int var44 = 0; var44 < 3; var44++) {
                    double var45 = var21[0] + 0.55 + 0.25 * var44;
                    double var46 = var21[1] - 0.75 - 0.3 * var44 + var18 * 0.6;
                    double var47 = var21[2] + 0.3 + 0.12 * var44;

                    for (int var48 = 1; var48 <= 8; var48++) {
                        double var30 = var48 / 8.0;
                        double var32 = var21[0] + (var45 - var21[0]) * var30;
                        double var34 = var21[1] + (var46 - var21[1]) * var30;
                        double var36 = var21[2] + (var47 - var21[2]) * var30;
                        double var38 = var2 + var14 * var20 * var32 - var10 * var36;
                        double var40 = var6 + var16 * var20 * var32 - var12 * var36;
                        double var42 = var4 + var34;
                        var0.trans(var1, var38, var42, var40, 9044180, 14708991, 1.4F);
                        if (var48 % 2 == 0) {
                            var0.trans(var1, var38, var42 - 0.1, var40, 9044180, 14708991, 1.2F);
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Ember: demon horns growing out of the top of the head, plus loose fire.
    // No rings or halos.
    // ---------------------------------------------------------------------
    static void ember(ShardV30Main fx, World world, double x, double y, double z, double spin) {
        long tick = fx.tick;
        Head head = Head.of(fx);
        demonHorns(fx, world, head, tick);

        if (tick % 4L == 0L) {
            fx.fx(world, Particle.FLAME, x, y + 1.0, z, 2, 0.35, 0.6, 0.35, 0.01);
        }
        if (tick % 6L == 0L) {
            fx.fx(world, Particle.SMALL_FLAME, x, y + 0.2, z, 2, 0.35, 0.1, 0.35, 0.02);
        }
        if (tick % 12L == 0L) {
            fx.fx(world, Particle.LAVA, x, y + 0.7, z, 1, 0.4, 0.3, 0.4, 0.0);
        }
    }

    // ---------------------------------------------------------------------
    // Sculk: Warden ears sticking out of the sides of the head, plus soul wisps.
    // No rings or halos.
    // ---------------------------------------------------------------------
    static void sculk(ShardV30Main fx, World world, double x, double y, double z, double spin) {
        long tick = fx.tick;
        Head head = Head.of(fx);
        wardenEars(fx, world, head, tick);

        if (tick % 6L == 0L) {
            fx.fx(world, Particle.SCULK_SOUL, x, y + 0.15, z, 1, 0.35, 0.1, 0.35, 0.02);
        }
        if (tick % 10L == 0L) {
            fx.fx(world, Particle.SOUL, x, y + 0.9, z, 1, 0.4, 0.5, 0.4, 0.01);
        }
    }

    // Colors (RGB hex)
    private static final int HORN_BASE = 0x3A0000;      // near-black red
    private static final int HORN_BASE_FADE = 0x8B0000; // dark red
    private static final int HORN_TIP = 0xFF5A00;       // glowing orange
    private static final int HORN_TIP_FADE = 0xFFD24A;  // yellow
    private static final int EAR_GLOW = 0x29DFEB;       // Warden cyan
    private static final int EAR_GLOW_FADE = 0x0A6E7A;
    private static final int EAR_EDGE = 0x0B3B4A;       // Warden dark teal
    private static final int EAR_EDGE_FADE = 0x06283C;

    /**
     * Where the player's head is and which way it faces (horizontally).
     * All offsets below are in blocks, measured from the eyes:
     * side = toward the right ear (+) or left ear (-), up = above the eyes, forward = toward the face.
     * A player's head is ~0.47 blocks wide and tall; it spans from ~0.21 below the eyes to ~0.26 above them.
     */
    private record Head(double x, double y, double z, double fwdX, double fwdZ) {
        static Head of(ShardV30Main fx) {
            double lx = fx.lookX;
            double lz = fx.lookZ;
            double len = Math.sqrt(lx * lx + lz * lz);
            if (len < 0.05) { // looking straight up/down: fall back to body yaw
                double yaw = Math.toRadians(fx.bodyYaw);
                lx = -Math.sin(yaw);
                lz = Math.cos(yaw);
                len = 1.0;
            }
            return new Head(fx.eyeX, fx.eyeY, fx.eyeZ, lx / len, lz / len);
        }

        double[] at(double side, double up, double forward) {
            double rightX = -fwdZ;
            double rightZ = fwdX;
            return new double[]{x + rightX * side + fwdX * forward, y + up, z + rightZ * side + fwdZ * forward};
        }
    }

    /** Two curved demon horns rising from the top front corners of the head, sweeping out, up and back. */
    private static void demonHorns(ShardV30Main fx, World world, Head head, long tick) {
        final int points = 12;
        for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
            double[] tip = null;
            for (int i = 0; i <= points; i++) {
                double t = i / (double) points;              // 0 = base, 1 = tip
                double side = 0.14 + 0.17 * t - 0.05 * t * t; // spreads outward, easing in at the tip
                double up = 0.24 + 0.38 * t - 0.05 * t * t;   // grows out of the top of the head
                double forward = 0.06 - 0.24 * t * t;         // starts near the front, curls back toward the tip
                double[] p = head.at(sideSign * side, up, forward);
                float size = (float) (1.6 - 1.05 * t);        // thick base, sharp tip
                boolean nearTip = t > 0.7;
                fx.trans(world, p[0], p[1], p[2],
                        nearTip ? HORN_TIP : HORN_BASE,
                        nearTip ? HORN_TIP_FADE : HORN_BASE_FADE, size);
                if (t < 0.5) { // thicken the base
                    double[] q = head.at(sideSign * (side + 0.04), up, forward - 0.03);
                    fx.trans(world, q[0], q[1], q[2], HORN_BASE, HORN_BASE_FADE, size * 0.85F);
                }
                if (i == points) {
                    tip = p;
                }
            }
            if (tip != null && tick % 4L == 0L) {
                fx.fx(world, Particle.SMALL_FLAME, tip[0], tip[1] + 0.04, tip[2], 1, 0.01, 0.01, 0.01, 0.003);
            }
        }
    }

    /**
     * Two Warden ears: flat, pointed fins coming out of the upper sides of the head,
     * angled outward and slightly up. Dark teal outline with a glowing cyan middle.
     */
    private static void wardenEars(ShardV30Main fx, World world, Head head, long tick) {
        // ear shape in (side, up) from the eyes; forward is fixed slightly behind the eyes
        final double baseSide = 0.24;   // the side of the head
        final double baseTop = 0.20;
        final double baseBottom = -0.02;
        final double tipSide = 0.72;
        final double tipUp = 0.32;
        final double forward = -0.02;
        final int points = 8;

        // glowing pulse, like the Warden's ears when it hears something
        float pulse = (float) (0.85 + 0.25 * Math.sin(tick * 0.15));

        for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
            for (int i = 0; i <= points; i++) {
                double t = i / (double) points;
                // top edge: from top of base, bulging up, to the tip
                double topSide = lerp(baseSide, tipSide, t);
                double topUp = lerp(baseTop, tipUp, t) + 0.08 * Math.sin(t * Math.PI);
                // bottom edge: from bottom of base, sagging slightly, to the tip
                double botSide = lerp(baseSide, tipSide, t);
                double botUp = lerp(baseBottom, tipUp, t) - 0.03 * Math.sin(t * Math.PI);
                double[] a = head.at(sideSign * topSide, topUp, forward);
                double[] b = head.at(sideSign * botSide, botUp, forward);
                fx.trans(world, a[0], a[1], a[2], EAR_EDGE, EAR_EDGE_FADE, 1.0F);
                if (i > 0 && i < points) {
                    fx.trans(world, b[0], b[1], b[2], EAR_EDGE, EAR_EDGE_FADE, 1.0F);
                }
                // glowing middle
                if (i > 0 && i < points) {
                    double midUp = (topUp + botUp) / 2.0;
                    double[] m = head.at(sideSign * topSide, midUp, forward);
                    fx.trans(world, m[0], m[1], m[2], EAR_GLOW, EAR_GLOW_FADE, 0.9F * pulse);
                }
            }
            if (tick % 6L == 0L) {
                double[] tip = head.at(sideSign * tipSide, tipUp, forward);
                fx.fx(world, Particle.SCULK_SOUL, tip[0], tip[1], tip[2], 1, 0.02, 0.02, 0.02, 0.005);
            }
        }
    }

    private static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }
}
