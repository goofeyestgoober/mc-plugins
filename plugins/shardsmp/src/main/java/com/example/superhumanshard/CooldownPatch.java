package com.example.superhumanshard;

final class CooldownPatch {
    static long total(ShardType var0) {
        return var0.cooldownMs() + abilityMs(var0.name());
    }

    static long abilityMs(String var0) {
        if (var0.equals("TITAN")) {
            return 1500L;
        } else if (var0.equals("TIDE")) {
            return 600L;
        } else if (var0.equals("PHANTOM")) {
            return 10000L;
        } else if (var0.equals("DRAGON")) {
            return 20000L;
        } else if (var0.equals("FROST")) {
            return 5000L;
        } else if (var0.equals("VERDANT")) {
            return 20000L;
        } else {
            return var0.equals("STORM") ? 3000L : 0L;
        }
    }
}
