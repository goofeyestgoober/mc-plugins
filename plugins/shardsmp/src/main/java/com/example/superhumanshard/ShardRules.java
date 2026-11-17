package com.example.superhumanshard;

final class ShardRules {
    private ShardRules() {
    }

    static boolean isBound(ShardType var0) {
        if (var0 == null) {
            return false;
        }

        String var1 = var0.id();
        return !var1.equals("dragon") && !var1.equals("sculk") && !var1.equals("ember") && !var1.equals("evoker");
    }
}
