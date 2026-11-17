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

    static void ember(ShardV30Main var0, World var1, double var2, double var4, double var6, double var8) {
        var0.ember(var1, var2, var4, var6, var8);
        long var10 = var0.tick;
        var0.rune(var1, var2, var4, var6, var8 * 0.45, 1.55, 22, 16734720, 16765562, 0.8F);
        var0.helix(var1, var2, var4, var6, -var8 * 1.3, 2, 0.8, 2.1, 0.06, 16747008, 16769162, 1.0F, Particle.SMALL_FLAME);

        for (int var12 = 0; var12 < 10; var12++) {
            double var13 = var8 * 1.4 + var12 * (Math.PI / 5);
            var0.trans(var1, var2 + Math.cos(var13) * 0.42, var4 + 2.35, var6 + Math.sin(var13) * 0.42, 16738816, 16765562, 0.75F);
        }

        for (int var21 = 0; var21 < 4; var21++) {
            double var22 = var8 * 0.8 + var21 * (Math.PI / 2);
            double var15 = (var10 * 0.05 + var21 * 0.5) % 2.0;
            double var17 = var2 + Math.cos(var22) * 0.95;
            double var19 = var6 + Math.sin(var22) * 0.95;
            var0.trans(var1, var17, var4 + 0.1 + var15, var19, 16734720, 16762954, 0.9F);
            var0.trans(var1, var17, var4 + 0.1 + var15 * 0.9 - 0.12, var19, 16726528, 16757575, 0.6F);
        }

        if (var10 % 4L == 0L) {
            var0.fx(var1, Particle.FLAME, var2, var4 + 1.0, var6, 3, 0.6, 0.9, 0.6, 0.02);
        }

        if (var10 % 6L == 0L) {
            var0.fx(var1, Particle.SMALL_FLAME, var2, var4 + 2.35, var6, 2, 0.45, 0.1, 0.45, 0.03);
        }
    }

    static void sculk(ShardV30Main var0, World var1, double var2, double var4, double var6, double var8) {
        var0.sculk(var1, var2, var4, var6, var8);
        long var10 = var0.tick;
        var0.rune(var1, var2, var4, var6, -var8 * 0.5, 1.5, 22, 1820856, 669008, 0.8F);
        var0.helix(var1, var2, var4, var6, var8 * 1.2, 2, 0.7, 2.2, 0.04, 4190944, 403516, 1.0F, Particle.SCULK_SOUL);

        for (int var12 = 0; var12 < 8; var12++) {
            double var13 = -var8 * 1.3 + var12 * (Math.PI / 4);
            var0.trans(var1, var2 + Math.cos(var13) * 0.45, var4 + 2.35, var6 + Math.sin(var13) * 0.45, 4190944, 403516, 0.7F);
        }

        for (int var21 = 0; var21 < 4; var21++) {
            double var22 = -var8 * 0.7 + var21 * (Math.PI / 2);
            double var15 = (var10 * 0.04 + var21 * 0.5) % 2.0;
            double var17 = var2 + Math.cos(var22) * 1.0;
            double var19 = var6 + Math.sin(var22) * 1.0;
            var0.trans(var1, var17, var4 + 0.1 + var15, var19, 2418384, 403516, 0.9F);
            var0.trans(var1, var17, var4 + 0.1 + var15 * 0.9 - 0.12, var19, 1820856, 669008, 0.6F);
        }

        if (var10 % 4L == 0L) {
            var0.fx(var1, Particle.SOUL, var2, var4 + 1.0, var6, 2, 0.6, 0.9, 0.6, 0.02);
        }

        if (var10 % 6L == 0L) {
            var0.fx(var1, Particle.SCULK_SOUL, var2, var4 + 1.4, var6, 2, 0.5, 0.7, 0.5, 0.03);
        }
    }
}
