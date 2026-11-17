package com.example.superhumanshard;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public class ShardV28Main extends ShardV27Main {
    @Override
    public void onEnable() {
        super.onEnable();
        this.getServer().getScheduler().runTaskTimer(this, this::syncGlint, 20L, 20L);
    }

    private void syncGlint() {
        boolean var1 = this.getConfig().getBoolean("shard-glint", true);

        for (Player var3 : this.getServer().getOnlinePlayers()) {
            PlayerInventory var4 = var3.getInventory();
            ItemStack[] var5 = var4.getContents();

            for (int var6 = 0; var6 < var5.length; var6++) {
                ItemStack var7 = var5[var6];
                if (var7 != null) {
                    ShardType var8 = this.getShardType(var7);
                    if (var8 != null) {
                        int var9 = this.tiers.tier(var3.getUniqueId(), var8);
                        boolean var10 = var1 && var9 >= 2;
                        boolean var11 = var9 >= 3;
                        ItemMeta var12 = var7.getItemMeta();
                        if (var12 != null) {
                            boolean var13 = false;
                            if (!var12.hasEnchantmentGlintOverride() || var12.getEnchantmentGlintOverride() != var10) {
                                var12.setEnchantmentGlintOverride(var10);
                                var13 = true;
                            }

                            CustomModelDataComponent var14 = var12.getCustomModelDataComponent();
                            List var15 = var14.getFloats();
                            boolean var16 = var15.size() >= 2 && (Float)var15.get(1) >= 1.0F;
                            if (var16 != var11) {
                                ArrayList var17 = new ArrayList();
                                var17.add((float)(771001 + var8.ordinal()));
                                if (var11) {
                                    var17.add(1.0F);
                                }

                                var14.setFloats(var17);
                                var12.setCustomModelDataComponent(var14);
                                var13 = true;
                            }

                            if (var13) {
                                var7.setItemMeta(var12);
                                var4.setItem(var6, var7);
                            }
                        }
                    }
                }
            }
        }
    }
}
