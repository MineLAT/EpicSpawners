package com.songoda.epicspawners.Handlers;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 3/12/2017.
 */
public class HologramHandler {

    EpicSpawners plugin = EpicSpawners.pl();

    public HologramHandler() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> updateHolograms(), 5000L, 5000L);
        loadHolograms();
    }

    public void loadHolograms() {
        if ((boolean) plugin.getConfig().get("settings.Spawner-holograms")) {
            if (!plugin.v1_7) {
                    if (plugin.dataFile.getConfig().contains("data.spawner")) {
                    ConfigurationSection cs = plugin.dataFile.getConfig().getConfigurationSection("data.spawner");
                    for (String key : cs.getKeys(true)) {
                        if (Arconix.pl().serialize().unserializeLocation(key).getWorld() != null) {
                            updateHologram(Arconix.pl().serialize().unserializeLocation(key).getBlock());
                        }
                    }
                }
            }
        }
    }

    public void updateHolograms() {
        try {
            /*
            if (!plugin.v1_7) {
                    if (plugin.dataFile.getConfig().contains("data.holograms")) {
                        ConfigurationSection cs = plugin.dataFile.getConfig().getConfigurationSection("data.holograms");
                        for (String key : cs.getKeys(true)) {
                            if (Methods.getEntityByUniqueId(UUID.fromString(key)) != null) {
                                plugin.dataFile.getConfig().set("data.holograms." + key, null);
                                Methods.getEntityByUniqueId(UUID.fromString(key)).remove();
                            }
                        }
                    }

                if ((boolean) plugin.getConfig().get("settings.Spawner-holograms")) {
                    plugin.dataFile.getConfig().set("data.holograms", null);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if (plugin.dataFile.getConfig().contains("data.spawner")) {
                            ConfigurationSection cs = plugin.dataFile.getConfig().getConfigurationSection("data.spawner");
                            for (String key : cs.getKeys(true)) {
                                if (Arconix.pl().serialize().unserializeLocation(key).getWorld() != null) {
                                    updateHologram(Arconix.pl().serialize().unserializeLocation(key).getBlock());
                                }
                            }
                        }
                    }, 1L);
                }
            }
            */
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void updateHologram(Block b) {
        try {
            Location olocation = b.getLocation();
            Location location = null;
            String face = null;
            if (!plugin.v1_7 && !plugin.v1_8_R1) {
                Collection<Entity> nearbyEntites = olocation.getWorld().getNearbyEntities(olocation, 5, 5, 5);
                for (Entity entity : nearbyEntites) {
                    if (entity instanceof Player) {
                        face = Arconix.pl().getPlayer((Player) entity).getPlayerDirection();
                    }
                }
                Arconix.pl().packetLibrary.getHologramManager().despawnHologram(adjust("UP", b));
                Arconix.pl().packetLibrary.getHologramManager().despawnHologram(adjust("NORTH", b));
                Arconix.pl().packetLibrary.getHologramManager().despawnHologram(adjust("SOUTH", b));
                Arconix.pl().packetLibrary.getHologramManager().despawnHologram(adjust("EAST", b));
                Arconix.pl().packetLibrary.getHologramManager().despawnHologram(adjust("WEST", b));
                Arconix.pl().packetLibrary.getHologramManager().despawnHologram(adjust("DOWN", b));

            }
            if (b.getType() == Material.MOB_SPAWNER) {
                boolean go = true;
                if (face != null && b.getRelative(BlockFace.UP).getType() != Material.AIR) {
                    if (b.getRelative(BlockFace.valueOf(face.toUpperCase())).getType() == Material.AIR) {
                        location = adjust(face.toUpperCase(), b);
                        go = false;
                    }
                }

                if (go == true) {
                    if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        location = adjust("UP", b);
                    } else if (b.getRelative(BlockFace.NORTH).getType() == Material.AIR) {
                        location = adjust("NORTH", b);
                    } else if (b.getRelative(BlockFace.SOUTH).getType() == Material.AIR) {
                        location = adjust("SOUTH", b);
                    } else if (b.getRelative(BlockFace.EAST).getType() == Material.AIR) {
                        location = adjust("EAST", b);
                    } else if (b.getRelative(BlockFace.WEST).getType() == Material.AIR) {
                        location = adjust("WEST", b);
                    } else if (b.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                        location = adjust("DOWN", b);
                    } else {
                        location = null;
                    }
                }

                if (location != null) {
                    if ((boolean) plugin.getConfig().get("settings.Spawner-holograms")) {
                        addHologram(location, olocation);
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public Location adjust(String direction, Block b) {
        Location location = null;
        try {
            switch (direction) {
                case "UP":
                    location = b.getLocation().add(0.5, 1, 0.5);
                    break;
                case "DOWN":
                    location = b.getLocation().subtract(0, 0.75, 0);
                    location = location.add(0.5, 0, 0.5);
                    break;
                case "NORTH":
                    location = b.getLocation().subtract(0, 0, 0.5);
                    location = location.add(0.5, 0.15, 0);
                    break;
                case "SOUTH":
                    location = b.getLocation().add(0, 0, 1.5);
                    location = location.add(0.5, 0.15, 0);
                    break;
                case "EAST":
                    location = b.getLocation().add(1.5, 0, 0);
                    location = location.add(0, 0.15, 0.5);
                    break;
                case "WEST":
                    location = b.getLocation().subtract(0.5, 0, 0);
                    location = location.add(0, 0.15, 0.5);
                    break;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return location;
    }

    public void addHologram(Location location, Location olocation) {
        try {
            if (olocation.getBlock().getType() == Material.MOB_SPAWNER) {
                Spawner spawner = new Spawner(olocation.getBlock());
                int multi = plugin.dataFile.getConfig().getInt("data.spawner." + spawner.locationStr);


                String name;
                if (plugin.dataFile.getConfig().contains("data.spawnerstats." + spawner.locationStr + ".type")) {
                    name = Methods.compileName(plugin.dataFile.getConfig().getString("data.spawnerstats." + spawner.locationStr + ".type"), multi, true);
                } else {
                    name = Methods.formatName(spawner.spawner.getSpawnedType(), multi);
                }
                Arconix.pl().packetLibrary.getHologramManager().spawnHologram(location, name.trim());
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void processChange(Block b) {
        try {
            if (!plugin.v1_7) {
                Block spawner = null;
                if (b.getType() == Material.MOB_SPAWNER) {
                    spawner = b;
                } else if (b.getRelative(BlockFace.UP).getType() == Material.MOB_SPAWNER) {
                    spawner = b.getRelative(BlockFace.UP);
                } else if (b.getRelative(BlockFace.DOWN).getType() == Material.MOB_SPAWNER) {
                    spawner = b.getRelative(BlockFace.DOWN);
                } else if (b.getRelative(BlockFace.NORTH).getType() == Material.MOB_SPAWNER) {
                    spawner = b.getRelative(BlockFace.NORTH);
                } else if (b.getRelative(BlockFace.SOUTH).getType() == Material.MOB_SPAWNER) {
                    spawner = b.getRelative(BlockFace.SOUTH);
                } else if (b.getRelative(BlockFace.WEST).getType() == Material.MOB_SPAWNER) {
                    spawner = b.getRelative(BlockFace.WEST);
                } else if (b.getRelative(BlockFace.EAST).getType() == Material.MOB_SPAWNER) {
                    spawner = b.getRelative(BlockFace.EAST);
                }
                final Block spawn = spawner;
                if (spawner != null) {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.holo.updateHologram(spawn), 1L);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}