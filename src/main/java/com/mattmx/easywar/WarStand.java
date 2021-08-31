package com.mattmx.easywar;

import com.mattmx.easygui.Utils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class WarStand {
    public static int maxTimer;
    public ArmorStand stand;
    public int timer;
    public boolean captured;
    public boolean capturing;
    public String name;

    public WarStand(String uuid, int timer, boolean captured, boolean capturing, String name) {
        this.stand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
        this.timer = timer;
        this.captured = captured;
        this.capturing = capturing;
        this.name = name;
        Main.STANDS.add(this);
    }

    public WarStand(ArmorStand e, int timer, boolean captured, boolean capturing, String name) {
        this.stand = e;
        this.timer = timer;
        this.captured = captured;
        this.capturing = capturing;
        this.name = name;
        Main.STANDS.add(this);
    }

    public static void init(Main plugin) {
        maxTimer = plugin.getConfig().getInt("captured-max-time");
    }

    public void doSecond(Main plugin) {
        if (captured) return;
        this.stand.setCustomName(Utils.chat(name + " &c&l" + timer + "&f&l / &a&l" + maxTimer));
        if (!capturing) {
            if (timer != 0) {
                timer--;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(stand.getLocation(), Sound.BLOCK_ANVIL_STEP, 0.5f, 2.0f);
                }
                for (Location l : getHollowCube(new Location(
                        stand.getWorld(),
                        stand.getLocation().getBlockX(),
                        stand.getLocation().getBlockY(),
                        stand.getLocation().getBlockZ()
                ), new Location(
                        stand.getWorld(),
                        stand.getLocation().getBlockX()+1,
                        stand.getLocation().getBlockY()+1,
                        stand.getLocation().getBlockZ()+1
                ))) {
                    stand.getWorld().spawnParticle(Particle.REDSTONE, l, 0, 0.001, 0, 0, 1, new Particle.DustOptions(Color.LIME, 1));
                }
            }
        } else {
            timer++;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(stand.getLocation(), Sound.BLOCK_ANVIL_STEP, 0.5f, 1.0f);
            }
            for (Location l : getHollowCube(new Location(
                    stand.getWorld(),
                    stand.getLocation().getBlockX(),
                    stand.getLocation().getBlockY(),
                    stand.getLocation().getBlockZ()
            ), new Location(
                    stand.getWorld(),
                    stand.getLocation().getBlockX()+1,
                    stand.getLocation().getBlockY()+1,
                    stand.getLocation().getBlockZ()+1
            ))) {
                stand.getWorld().spawnParticle(Particle.REDSTONE, l, 0, 0.001, 0, 0, 1, new Particle.DustOptions(Color.RED, 1));
            }
        }
        if (this.timer >= maxTimer) {
            stand.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, stand.getLocation(), 20);
            Bukkit.broadcastMessage(Utils.chat(plugin.getConfig().getString("captured-message").replace("%name%",this.name).replace("%prefix%",Main.CHAT_PREFIX)));
            this.captured = true;
            this.stand.setCustomName(Utils.chat(name + " &c&lCAPTURED"));
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 1.0f);
            }
        }
    }
    /*
    Stands:
        1:
            uuid:   UUID STRING
            timer:  INTEGER
            captured:   BOOLEAN
            capturing:  BOOLEAN
            name:   STRING
        2:
            [...]
     */

    public static void registerStands(Main plugin) {
        String basePath = "Stands";
        if (plugin.getConfig().getConfigurationSection(basePath) != null) {
            for (String c : plugin.getConfig().getConfigurationSection(basePath).getKeys(false)) {
                String standPath = basePath + "." + c;
                WarStand warStand = new WarStand(
                        plugin.getConfig().getString(standPath + ".uuid"),
                        plugin.getConfig().getInt(standPath + ".timer"),
                        plugin.getConfig().getBoolean(standPath + ".captured"),
                        plugin.getConfig().getBoolean(standPath + ".capturing"),
                        plugin.getConfig().getString(standPath + ".name"));
                if (warStand.stand == null) {
                    plugin.getLogger().info("Could not find Armor stand entity " + warStand.name);
                    Main.STANDS.remove(warStand);
                }
            }
        }
    }

    public static void saveStands(Main plugin) {
        plugin.getLogger().info("Saving config.yml");
        plugin.getConfig().set("Stands", null);
        for (WarStand stand : Main.STANDS) {
            plugin.getConfig().set("Stands." + stand.stand.getUniqueId().toString() + ".uuid", stand.stand.getUniqueId().toString());
            plugin.getConfig().set("Stands." + stand.stand.getUniqueId().toString() + ".timer", stand.timer);
            plugin.getConfig().set("Stands." + stand.stand.getUniqueId().toString() + ".captured", stand.captured);
            plugin.getConfig().set("Stands." + stand.stand.getUniqueId().toString() + ".capturing", stand.capturing);
            plugin.getConfig().set("Stands." + stand.stand.getUniqueId().toString() + ".name", stand.name);
        }
        plugin.saveConfig();
        plugin.getLogger().info("Saved config.yml");
    }

    public List<Location> getHollowCube(Location corner1, Location corner2) {
        List<Location> result = new ArrayList<Location>();
        World world = corner1.getWorld();
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    int components = 0;
                    if (x == minX || x == maxX) components++;
                    if (y == minY || y == maxY) components++;
                    if (z == minZ || z == maxZ) components++;
                    if (components >= 2) {
                        result.add(new Location(world, x, y, z));
                    }
                }
            }
        }

        return result;
    }
}
