package com.example.superhumanshard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class ShardV30Main extends ShardV29Main {
    private static final int CD_TOTAL = 1200;
    private static final int CD_STEP = 300;
    private BukkitTask auraTask;
    private BukkitTask cdTask;
    long tick;
    private final Map<UUID, ShardType> auraShard = new HashMap<>();
    private World cw;
    private Player cOwner;
    private final List<Player> cViewers = new ArrayList<>();
    private double eyeX;
    double eyeY;
    private double eyeZ;
    double lookX;
    private double lookY;
    double lookZ;
    private BukkitTask spriteTask;
    private ItemDisplay sprite;
    private TextDisplay label;
    private String labelText = "";
    private String shownText = null;
    private boolean hasSprite;
    private String spriteWorld = "";
    private double sx;
    private double sy;
    private double sz;
    private int spriteStep;
    private UUID droppedId;
    private int cdElapsed;
    private String cdName = "";
    private int[] cdXyz = new int[3];

    @Override
    public void onEnable() {
        super.onEnable();
        this.auraTask = Bukkit.getScheduler().runTaskTimer(this, this::auraTick, 20L, 2L);
        this.loadSprite();
        Bukkit.getPluginManager().registerEvents(new ShardV30Main.EmberPickup(), this);
        this.spriteTask = Bukkit.getScheduler().runTaskTimer(this, this::spriteTick, 20L, 10L);
    }

    @Override
    public void onDisable() {
        if (this.auraTask != null) {
            this.auraTask.cancel();
        }

        if (this.cdTask != null) {
            this.cdTask.cancel();
        }

        if (this.spriteTask != null) {
            this.spriteTask.cancel();
        }

        if (this.sprite != null) {
            this.sprite.remove();
        }

        if (this.label != null) {
            this.label.remove();
        }

        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
        String var5 = var2.getName().toLowerCase(Locale.ROOT);
        return !var5.equals("ember") && !var5.equals("embershard") ? super.onCommand(var1, var2, var3, var4) : this.emberCommand(var1, var4);
    }

    private boolean emberCommand(CommandSender var1, String[] var2) {
        if (!var1.isOp()) {
            var1.sendMessage("§cOnly operators can use this command.");
            return true;
        }

        ArrayList var3 = new ArrayList();
        ArrayList var4 = new ArrayList();

        for (String var8 : var2) {
            var3.add(var8.toLowerCase(Locale.ROOT));
            var4.add(var8);
        }

        if (!var3.isEmpty() && ((String)var3.get(0)).equals("shard")) {
            var3.remove(0);
            var4.remove(0);
        }

        if (var3.size() < 2
            || !((String)var3.get(0)).equals("countdown")
            || !((String)var3.get(1)).equals("start")
            || var3.size() != 2 && var3.size() != 5 && var3.size() != 6) {
            var1.sendMessage("§cUsage: /ember shard countdown start <x> <y> <z> [world]");
            var1.sendMessage("§7(or /ember shard countdown start while looking at a block)");
            return true;
        } else {
            this.startCountdown(var1, var4);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
        String var5 = var2.getName().toLowerCase(Locale.ROOT);
        if (!var5.equals("ember") && !var5.equals("embershard")) {
            return super.onTabComplete(var1, var2, var3, var4);
        }

        ArrayList var6 = new ArrayList();
        if (!var1.isOp()) {
            return var6;
        }

        boolean var7 = var5.equals("ember");
        int var8 = var4.length - 1;
        String var9 = var4[var8].toLowerCase(Locale.ROOT);
        ArrayList<String> var10 = new ArrayList<>();
        if (var7) {
            if (var8 == 0) {
                var10.add("shard");
            } else if (var8 == 1 && var4[0].equalsIgnoreCase("shard")) {
                var10.add("countdown");
            } else if (var8 == 2 && var4[0].equalsIgnoreCase("shard") && var4[1].equalsIgnoreCase("countdown")) {
                var10.add("start");
            }
        } else if (var8 == 0) {
            var10.add("countdown");
        } else if (var8 == 1 && var4[0].equalsIgnoreCase("countdown")) {
            var10.add("start");
        }

        for (String var12 : var10) {
            if (var12.startsWith(var9)) {
                var6.add(var12);
            }
        }

        return var6;
    }

    private void startCountdown(CommandSender var1, List<String> var2) {
        if (this.cdTask != null) {
            var1.sendMessage("§cAn Ember Shard countdown is already running (" + fmt(1200 - this.cdElapsed) + " left).");
        } else {
            Player var3 = var1 instanceof Player ? (Player)var1 : null;
            World var4;
            int var5;
            int var6;
            int var7;
            if (var2.size() == 2) {
                if (var3 == null) {
                    var1.sendMessage("§cFrom the console give coordinates and a world: /ember shard countdown start <x> <y> <z> <world>");
                    return;
                }

                Block var8 = var3.getTargetBlockExact(20);
                if (var8 == null) {
                    var1.sendMessage("§cLook at a block (within 20 blocks) or give coordinates: /ember shard countdown start <x> <y> <z> [world]");
                    return;
                }

                var4 = var8.getWorld();
                var5 = var8.getX();
                var6 = var8.getY();
                var7 = var8.getZ();
            } else {
                Location var12 = var3 == null ? null : var3.getLocation();
                Integer var9 = coord((String)var2.get(2), var12 == null ? null : var12.getBlockX());
                Integer var10 = coord((String)var2.get(3), var12 == null ? null : var12.getBlockY());
                Integer var11 = coord((String)var2.get(4), var12 == null ? null : var12.getBlockZ());
                if (var9 == null || var10 == null || var11 == null) {
                    var1.sendMessage(
                        "§cCoordinates must be whole numbers (or ~ / ~5 relative to you, in-game). Example: /ember shard countdown start 1000 100 1000"
                    );
                    return;
                }

                if (var2.size() >= 6) {
                    var4 = Bukkit.getWorld((String)var2.get(5));
                    if (var4 == null) {
                        var1.sendMessage("§cUnknown world: " + (String)var2.get(5));
                        return;
                    }
                } else {
                    if (var3 == null) {
                        var1.sendMessage("§cFrom the console, add a world name: /ember shard countdown start <x> <y> <z> <world>");
                        return;
                    }

                    var4 = var3.getWorld();
                }

                var5 = var9;
                var6 = var10;
                var7 = var11;
            }

            this.cdXyz = new int[]{var5, var6, var7};
            this.cdElapsed = 0;
            this.labelText = timeText(0);
            this.placeSprite(var4, var5, var6, var7);
            announce(gradient("The Ember Shard countdown has begun", 16718362, 16751135));
            announce(this.coordLine(0));
            this.cdTask = Bukkit.getScheduler().runTaskTimer(this, this::cdTick, 20L, 20L);
        }
    }

    private void cdTick() {
        this.cdElapsed++;
        this.labelText = timeText(this.cdElapsed);
        this.applyLabel();
        if (this.cdElapsed == 300) {
            announce(this.coordLine(1));
        } else if (this.cdElapsed == 600) {
            announce(this.coordLine(2));
        } else if (this.cdElapsed == 900) {
            announce(this.coordLine(3));
        }

        if (this.cdElapsed >= 1200) {
            if (this.cdTask != null) {
                this.cdTask.cancel();
            }

            this.cdTask = null;
            this.dropEmberShard();
        }
    }

    private String coordLine(int var1) {
        StringBuilder var2 = new StringBuilder();

        for (int var3 = 0; var3 < 3; var3++) {
            if (var3 > 0) {
                var2.append("§6 ");
            }

            String var4 = String.valueOf(this.cdXyz[var3]);
            if (var3 < var1) {
                var2.append("§6").append(var4);
            } else {
                var2.append("§6§k");

                for (int var5 = 0; var5 < var4.length(); var5++) {
                    var2.append('T');
                }
            }
        }

        return var2.toString();
    }

    private static String timeText(int var0) {
        return "§6§l" + fmt(var0);
    }

    private static String fmt(int var0) {
        if (var0 < 0) {
            var0 = 0;
        }

        return var0 / 60 + ":" + (var0 % 60 < 10 ? "0" : "") + var0 % 60;
    }

    private static void announce(String var0) {
        for (Player var2 : Bukkit.getOnlinePlayers()) {
            var2.sendMessage(var0);
        }

        Bukkit.getConsoleSender().sendMessage(var0);
    }

    private File spriteFile() {
        File var1 = this.getDataFolder();
        var1.mkdirs();
        return new File(var1, "ember-sprite.properties");
    }

    private void loadSprite() {
        File var1 = this.spriteFile();
        if (var1.exists()) {
            try (FileInputStream var2 = new FileInputStream(var1)) {
                Properties var3 = new Properties();
                var3.load(var2);
                String var4 = var3.getProperty("dropped", "");
                if (!var4.isEmpty()) {
                    this.droppedId = UUID.fromString(var4);
                }
            } catch (Exception var7) {
            }
        }
    }

    private void saveSprite() {
        Properties var1 = new Properties();
        var1.setProperty("dropped", this.droppedId == null ? "" : this.droppedId.toString());

        try (FileOutputStream var2 = new FileOutputStream(this.spriteFile())) {
            var1.store(var2, "Ember shard dropped item");
        } catch (Exception var7) {
        }
    }

    private static Integer coord(String var0, Integer var1) {
        try {
            if (var0.startsWith("~")) {
                return var1 == null ? null : var1 + (var0.length() == 1 ? 0 : Integer.parseInt(var0.substring(1)));
            } else {
                return Integer.parseInt(var0);
            }
        } catch (NumberFormatException var3) {
            return null;
        }
    }

    private void placeSprite(World var1, int var2, int var3, int var4) {
        if (this.sprite != null) {
            this.sprite.remove();
            this.sprite = null;
        }

        if (this.label != null) {
            this.label.remove();
            this.label = null;
        }

        this.spriteWorld = var1.getName();
        this.sx = var2 + 0.5;
        this.sy = var3 + 1.7;
        this.sz = var4 + 0.5;
        this.hasSprite = true;
        this.shownText = null;
        this.spriteTick();
    }

    private void applyLabel() {
        if (this.label != null && this.label.isValid() && !this.labelText.equals(this.shownText)) {
            this.label.setText(this.labelText);
            this.shownText = this.labelText;
        }
    }

    private void spriteTick() {
        if (this.hasSprite) {
            World var1 = Bukkit.getWorld(this.spriteWorld);
            if (var1 != null) {
                if (!var1.isChunkLoaded((int)Math.floor(this.sx) >> 4, (int)Math.floor(this.sz) >> 4)) {
                    this.sprite = null;
                    this.label = null;
                } else {
                    if (this.sprite == null || !this.sprite.isValid()) {
                        ItemDisplay var2 = (ItemDisplay)var1.spawn(new Location(var1, this.sx, this.sy, this.sz), ItemDisplay.class);
                        var2.setItemStack(this.newShard(ShardType.EMBER));
                        var2.setItemDisplayTransform(ItemDisplayTransform.FIXED);
                        var2.setPersistent(false);
                        var2.setInvulnerable(true);
                        this.sprite = var2;
                    }

                    if (this.label == null || !this.label.isValid()) {
                        TextDisplay var3 = (TextDisplay)var1.spawn(new Location(var1, this.sx, this.sy + 0.95, this.sz), TextDisplay.class);
                        var3.setBillboard(Billboard.CENTER);
                        var3.setPersistent(false);
                        var3.setInvulnerable(true);
                        this.label = var3;
                        this.shownText = null;
                    }

                    this.applyLabel();
                    this.spriteStep++;
                    float var4 = (float)Math.toRadians(this.spriteStep % 32 * 45.0);
                    this.sprite.setInterpolationDelay(0);
                    this.sprite.setInterpolationDuration(10);
                    this.sprite
                        .setTransformation(
                            new Transformation(
                                new Vector3f(0.0F, 0.0F, 0.0F),
                                new AxisAngle4f(var4, 0.0F, 1.0F, 0.0F),
                                new Vector3f(1.2F, 1.2F, 1.2F),
                                new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F)
                            )
                        );
                }
            }
        }
    }

    private void dropEmberShard() {
        if (this.hasSprite) {
            World var1 = Bukkit.getWorld(this.spriteWorld);
            if (var1 != null) {
                Location var2 = new Location(var1, this.sx, this.sy, this.sz);
                if (this.sprite != null) {
                    this.sprite.remove();
                    this.sprite = null;
                }

                if (this.label != null) {
                    this.label.remove();
                    this.label = null;
                }

                Item var3 = var1.dropItem(var2, this.newShard(ShardType.EMBER));
                var3.setUnlimitedLifetime(true);
                var3.setInvulnerable(true);
                var3.setGlowing(true);
                this.droppedId = var3.getUniqueId();
                this.hasSprite = false;
                this.saveSprite();
            }
        }
    }

    private static String gradient(String var0, int var1, int var2) {
        StringBuilder var3 = new StringBuilder();
        int var4 = var0.length();

        for (int var5 = 0; var5 < var4; var5++) {
            double var6 = var4 <= 1 ? 0.0 : (double)var5 / (var4 - 1);
            int var8 = (int)Math.round((var1 >> 16 & 0xFF) * (1.0 - var6) + (var2 >> 16 & 0xFF) * var6);
            int var9 = (int)Math.round((var1 >> 8 & 0xFF) * (1.0 - var6) + (var2 >> 8 & 0xFF) * var6);
            int var10 = (int)Math.round((var1 & 0xFF) * (1.0 - var6) + (var2 & 0xFF) * var6);
            String var11 = String.format("%02x%02x%02x", var8, var9, var10);
            var3.append("§x");

            for (int var12 = 0; var12 < var11.length(); var12++) {
                var3.append('§').append(var11.charAt(var12));
            }

            var3.append("§l").append(var0.charAt(var5));
        }

        return var3.toString();
    }

    private static ShardType pick(ShardType var0) {
        return var0 != ShardType.DRAGON && var0 != ShardType.EMBER && var0 != ShardType.SCULK && var0 != ShardType.EVOKER ? null : var0;
    }

    private ShardType look(ItemStack var1) {
        if (var1 == null) {
            return null;
        }

        try {
            return pick(this.getShardType(var1));
        } catch (RuntimeException var3) {
            return null;
        }
    }

    private ShardType shardOf(Player var1) {
        PlayerInventory var2 = var1.getInventory();
        ShardType var3 = this.look(var2.getItemInMainHand());
        if (var3 != null) {
            return var3;
        }

        var3 = this.look(var2.getItemInOffHand());
        if (var3 != null) {
            return var3;
        }

        ItemStack[] var4 = var2.getContents();
        if (var4 != null) {
            for (ItemStack var8 : var4) {
                var3 = this.look(var8);
                if (var3 != null) {
                    return var3;
                }
            }
        }

        return null;
    }

    private void auraTick() {
        this.tick += 2L;
        if (this.tick % 1200L == 0L) {
            this.auraShard.clear();
        }

        boolean var1 = this.tick % 10L == 0L;

        for (Player var3 : Bukkit.getOnlinePlayers()) {
            if (!var3.isDead()) {
                UUID var4 = var3.getUniqueId();
                if (var1 || !this.auraShard.containsKey(var4)) {
                    this.auraShard.put(var4, this.shardOf(var3));
                }

                ShardType var5 = this.auraShard.get(var4);
                if (var5 != null) {
                    Location var6 = var3.getLocation();
                    World var7 = var3.getWorld();
                    double var8 = var6.getX();
                    double var10 = var6.getY();
                    double var12 = var6.getZ();
                    this.setContext(var3, var7, var8, var10, var12);
                    double var14 = Math.toRadians(var6.getYaw());
                    double var16 = this.tick * 0.12;
                    if (var5 == ShardType.DRAGON) {
                        this.dragon(var7, var8, var10, var12, var14, var16);
                    } else if (var5 == ShardType.EMBER) {
                        ShardV30Fx.ember(this, var7, var8, var10, var12, var16);
                    } else if (var5 == ShardType.SCULK) {
                        ShardV30Fx.sculk(this, var7, var8, var10, var12, var16);
                    } else if (var5 == ShardType.EVOKER) {
                        this.evoker(var7, var8, var10, var12, var16);
                    }
                }
            }
        }
    }

    private void dragon(World var1, double var2, double var4, double var6, double var8, double var10) {
        ShardV30Fx.wings(this, var1, var2, var4, var6, var8);
        this.helix(var1, var2, var4, var6, var10, 2, 0.65, 2.3, 0.05, 11796735, 16743423, 1.0F, null);
        this.rune(var1, var2, var4, var6, var10 * 0.5, 1.2, 16, 10092774, 16751615, 0.9F);
        if (this.tick % 4L == 0L) {
            this.fx(var1, Particle.END_ROD, var2, var4 + 0.7, var6, 1, 0.6, 0.6, 0.6, 0.01);
            this.fx(var1, Particle.PORTAL, var2, var4 + 0.7, var6, 6, 0.5, 0.6, 0.5, 0.5);
        }
    }

    private void wings(World var1, double var2, double var4, double var6, double var8) {
        double var10 = -Math.sin(var8);
        double var12 = Math.cos(var8);
        double var14 = -Math.cos(var8);
        double var16 = -Math.sin(var8);
        double var18 = Math.sin(this.tick * 0.09) * 0.28;

        for (byte var20 = -1; var20 <= 1; var20 += 2) {
            double[] var21 = null;

            for (int var22 = 0; var22 <= 8; var22++) {
                double var23 = var22 / 8.0;
                double var25 = 0.15 + 1.7 * var23 * Math.cos(var18 * var23);
                double var27 = 1.25 + 0.95 * Math.sin(var23 * Math.PI * 0.62) + var18 * var23 * 1.2;
                double var29 = 0.28 + 0.5 * var23;
                double var31 = var2 + var14 * var20 * var25 - var10 * var29;
                double var33 = var6 + var16 * var20 * var25 - var12 * var29;
                double var35 = var4 + var27;
                this.trans(var1, var31, var35, var33, 12721151, 16754943, 1.1F);
                if (var22 == 5) {
                    var21 = new double[]{var25, var27, var29};
                }

                if (var22 == 8 && this.tick % 4L == 0L) {
                    this.fx(var1, Particle.END_ROD, var31, var35, var33, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }

            if (var21 != null) {
                for (int var38 = 0; var38 < 3; var38++) {
                    double var39 = var21[0] + 0.55 + 0.25 * var38;
                    double var40 = var21[1] - 0.75 - 0.3 * var38 + var18 * 0.6;
                    double var41 = var21[2] + 0.3 + 0.12 * var38;

                    for (int var42 = 1; var42 <= 4; var42++) {
                        double var30 = var42 / 4.0;
                        double var32 = var21[0] + (var39 - var21[0]) * var30;
                        double var34 = var21[1] + (var40 - var21[1]) * var30;
                        double var36 = var21[2] + (var41 - var21[2]) * var30;
                        this.trans(
                            var1,
                            var2 + var14 * var20 * var32 - var10 * var36,
                            var4 + var34,
                            var6 + var16 * var20 * var32 - var12 * var36,
                            9044180,
                            14708991,
                            0.8F
                        );
                    }
                }
            }
        }
    }

    void ember(World var1, double var2, double var4, double var6, double var8) {
        this.helix(var1, var2, var4, var6, var8, 3, 0.6, 2.2, 0.045, 16734720, 16762954, 1.0F, Particle.FLAME);

        for (int var10 = 0; var10 < 3; var10++) {
            double var11 = this.tick * 0.16 + var10 * (Math.PI * 2.0 / 3.0);
            double var13 = var4 + 1.0 + Math.sin(this.tick * 0.1 + var10 * 2.1) * 0.35;

            for (int var15 = 0; var15 < 4; var15++) {
                double var16 = var11 - var15 * 0.22;
                double var18 = var2 + Math.cos(var16) * 0.95;
                double var20 = var6 + Math.sin(var16) * 0.95;
                if (var15 == 0) {
                    this.fx(var1, Particle.FLAME, var18, var13, var20, 2, 0.04, 0.04, 0.04, 0.0);
                    this.trans(var1, var18, var13, var20, 16726528, 16765562, 1.5F);
                } else {
                    this.trans(var1, var18, var13, var20, 16738816, 16722432, 1.1F - var15 * 0.2F);
                }
            }
        }

        this.rune(var1, var2, var4, var6, -var8 * 0.7, 1.15, 14, 16726528, 16757575, 0.9F);
        this.fx(var1, Particle.SMALL_FLAME, var2, var4 + 0.2, var6, 2, 0.35, 0.1, 0.35, 0.03);
        if (this.tick % 12L == 0L) {
            this.fx(var1, Particle.LAVA, var2, var4 + 0.7, var6, 1, 0.5, 0.3, 0.5, 0.0);
        }
    }

    void sculk(World var1, double var2, double var4, double var6, double var8) {
        for (int var10 = 0; var10 < 2; var10++) {
            double var11 = var10 == 0 ? 0.0 : Math.PI / 2;
            double var13 = var10 == 0 ? 0.9 : -0.9;
            double var15 = var8 * (var10 == 0 ? 1.4 : -1.4);

            for (int var17 = 0; var17 < 10; var17++) {
                double var18 = var15 + var17 * (Math.PI / 5);
                double[] var20 = ringPoint(0.95, var18, var13, var11);
                this.trans(var1, var2 + var20[0], var4 + 1.1 + var20[1], var6 + var20[2], 2418384, 669008, 1.0F);
            }
        }

        this.fx(var1, Particle.SCULK_SOUL, var2, var4 + 0.1, var6, 1, 0.35, 0.1, 0.35, 0.03);
        int var21 = (int)(this.tick % 60L);
        if (var21 < 24) {
            double var22 = var21 / 24.0 * 2.6;

            for (int var23 = 0; var23 < 16; var23++) {
                double var14 = var23 * (Math.PI / 8);
                this.trans(var1, var2 + Math.cos(var14) * var22, var4 + 0.08, var6 + Math.sin(var14) * var22, 4190944, 403516, 1.1F);
            }
        }

        this.rune(var1, var2, var4, var6, var8 * 0.6, 1.0, 10, 1820856, 669008, 0.8F);
        if (this.tick % 10L == 0L) {
            this.fx(var1, Particle.SOUL, var2, var4 + 0.5, var6, 1, 0.4, 0.5, 0.4, 0.01);
        }
    }

    private void evoker(World var1, double var2, double var4, double var6, double var8) {
        this.rune(var1, var2, var4, var6, var8 * 0.5, 1.0, 16, 15913820, 12165690, 0.9F);
        this.rune(var1, var2, var4, var6, -var8 * 0.5, 1.45, 22, 15263976, 8163206, 0.8F);

        for (int var10 = 0; var10 < 8; var10++) {
            double var11 = var8 * 0.25 + var10 * (Math.PI / 4);
            double var13 = var2 + Math.cos(var11) * 1.7;
            double var15 = var6 + Math.sin(var11) * 1.7;

            for (int var17 = 0; var17 < 3; var17++) {
                this.trans(var1, var13, var4 + 0.1 + var17 * 0.12, var15, 16777215, 12563307, (float)(0.9 - 0.2 * var17));
            }
        }

        this.helix(var1, var2, var4, var6, var8, 2, 0.7, 2.3, 0.04, 15913820, 6192242, 0.9F, Particle.ENCHANT);

        for (int var18 = 0; var18 < 8; var18++) {
            double var19 = -var8 * 1.2 + var18 * (Math.PI / 4);
            this.trans(var1, var2 + Math.cos(var19) * 0.45, var4 + 2.35, var6 + Math.sin(var19) * 0.45, 15913820, 16777215, 0.7F);
        }

        if (this.tick % 6L == 0L) {
            this.fx(var1, Particle.TOTEM_OF_UNDYING, var2, var4 + 0.7, var6, 2, 0.45, 0.5, 0.45, 0.1);
        }
    }

    void helix(
        World var1,
        double var2,
        double var4,
        double var6,
        double var8,
        int var10,
        double var11,
        double var13,
        double var15,
        int var17,
        int var18,
        float var19,
        Particle var20
    ) {
        for (int var21 = 0; var21 < var10; var21++) {
            for (int var22 = 0; var22 < 4; var22++) {
                double var23 = (this.tick * var15 + var22 * (var13 / 4.0)) % var13;
                double var25 = var8 + var23 * 3.0 + var21 * ((Math.PI * 2) / var10);
                double var27 = var11 * (1.0 - 0.35 * (var23 / var13));
                double var29 = var2 + Math.cos(var25) * var27;
                double var31 = var6 + Math.sin(var25) * var27;
                double var33 = var4 + 0.1 + var23;
                this.trans(var1, var29, var33, var31, var17, var18, var19);
                if (var20 != null && var22 == 0) {
                    this.fx(var1, var20, var29, var33, var31, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    void rune(World var1, double var2, double var4, double var6, double var8, double var10, int var12, int var13, int var14, float var15) {
        for (int var16 = 0; var16 < var12; var16++) {
            double var17 = var8 + var16 * ((Math.PI * 2) / var12);
            this.trans(var1, var2 + Math.cos(var17) * var10, var4 + 0.06, var6 + Math.sin(var17) * var10, var13, var14, var15);
        }
    }

    private static double[] ringPoint(double var0, double var2, double var4, double var6) {
        double var8 = var0 * Math.cos(var2);
        double var10 = var0 * Math.sin(var2);
        double var12 = -var10 * Math.sin(var4);
        double var14 = var10 * Math.cos(var4);
        double var16 = var8 * Math.cos(var6) + var14 * Math.sin(var6);
        double var18 = -var8 * Math.sin(var6) + var14 * Math.cos(var6);
        return new double[]{var16, var12, var18};
    }

    private void setContext(Player var1, World var2, double var3, double var5, double var7) {
        this.cw = var2;
        this.cOwner = var1;
        this.cViewers.clear();

        for (Player var10 : Bukkit.getOnlinePlayers()) {
            if (var10.getWorld().equals(var2)) {
                Location var11 = var10.getLocation();
                double var12 = var11.getX() - var3;
                double var14 = var11.getY() - var5;
                double var16 = var11.getZ() - var7;
                if (var12 * var12 + var14 * var14 + var16 * var16 <= 2304.0) {
                    this.cViewers.add(var10);
                }
            }
        }

        if (!this.cViewers.contains(var1)) {
            this.cViewers.add(var1);
        }

        Location var18 = var1.getEyeLocation();
        this.eyeX = var18.getX();
        this.eyeY = var18.getY();
        this.eyeZ = var18.getZ();
        double var19 = Math.toRadians(var18.getYaw());
        double var20 = Math.toRadians(var18.getPitch());
        this.lookX = -Math.sin(var19) * Math.cos(var20);
        this.lookY = -Math.sin(var20);
        this.lookZ = Math.cos(var19) * Math.cos(var20);
    }

    private boolean blocksOwnerView(double var1, double var3, double var5) {
        double var7 = var1 - this.eyeX;
        double var9 = var3 - this.eyeY;
        double var11 = var5 - this.eyeZ;
        double var13 = var7 * var7 + var9 * var9 + var11 * var11;
        if (var13 > 2.25) {
            return false;
        }

        if (var13 < 0.6400000000000001) {
            return true;
        }

        double var15 = Math.sqrt(var13);
        return (var7 * this.lookX + var9 * this.lookY + var11 * this.lookZ) / var15 > 0.5;
    }

    private void send(Particle var1, double var2, double var4, double var6, int var8, double var9, double var11, double var13, double var15, Object var17) {
        Location var18 = new Location(this.cw, var2, var4, var6);
        boolean var19 = this.blocksOwnerView(var2, var4, var6);

        for (Player var21 : this.cViewers) {
            if (!var19 || var21 != this.cOwner) {
                if (var17 == null) {
                    var21.spawnParticle(var1, var18, var8, var9, var11, var13, var15);
                } else {
                    var21.spawnParticle(var1, var18, var8, var9, var11, var13, var15, var17);
                }
            }
        }
    }

    void trans(World var1, double var2, double var4, double var6, int var8, int var9, float var10) {
        this.send(Particle.DUST_COLOR_TRANSITION, var2, var4, var6, 1, 0.0, 0.0, 0.0, 0.0, new DustTransition(Color.fromRGB(var8), Color.fromRGB(var9), var10));
    }

    void fx(World var1, Particle var2, double var3, double var5, double var7, int var9, double var10, double var12, double var14, double var16) {
        this.send(var2, var3, var5, var7, var9, var10, var12, var14, var16, null);
    }

    private class EmberPickup implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onPickup(EntityPickupItemEvent var1) {
            if (ShardV30Main.this.droppedId != null) {
                if (var1.getEntity() instanceof Player) {
                    if (ShardV30Main.this.droppedId.equals(var1.getItem().getUniqueId())) {
                        Player var2 = (Player)var1.getEntity();
                        ShardV30Main.this.droppedId = null;
                        ShardV30Main.this.saveSprite();
                        ShardV30Main.announce(ShardV30Main.gradient(var2.getName() + " got the Ember Shard!", 16718362, 16751135));
                    }
                }
            }
        }
    }
}
