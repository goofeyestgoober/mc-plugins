package com.example.superhumanshard;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class MythicShardMain extends ShardV24Main {
    private static boolean isMythic(ShardType var0) {
        return var0 == ShardType.EMBER || var0 == ShardType.DRAGON || var0 == ShardType.EVOKER || var0 == ShardType.SCULK;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                MythicShardMain.this.sweep();
            }
        }, 20L, 5L);
    }

    private void sweep() {
        for (Player var2 : this.getServer().getOnlinePlayers()) {
            PlayerInventory var3 = var2.getInventory();
            ItemStack[] var4 = var3.getContents();
            boolean var5 = false;

            for (ItemStack var9 : var4) {
                ShardType var10 = this.getShardType(var9);
                if (var10 != null && isMythic(var10)) {
                    var5 = true;
                    break;
                }
            }

            if (var5) {
                String var11 = null;

                for (int var12 = 0; var12 < var4.length; var12++) {
                    ShardType var13 = this.getShardType(var4[var12]);
                    if (var13 != null && !isMythic(var13)) {
                        var3.setItem(var12, null);
                        var11 = var13.displayName();
                    }
                }

                if (var11 != null) {
                    var2.sendMessage(Component.text("Your mythic shard consumed your regular shard."));
                }
            }
        }
    }
}
