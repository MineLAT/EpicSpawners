package com.songoda.epicspawners.Handlers;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Spawners.SpawnerItem;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Created by songo on 5/17/2017.
 */
public class OmniHandler {
    EpicSpawners plugin = EpicSpawners.pl();

    public OmniHandler() {
        try {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> displayItems(), 30L, 30L);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public static void displayItems() {
        try {
            EpicSpawners plugin = EpicSpawners.pl();

            if (plugin.getConfig().getBoolean("settings.OmniSpawners")) {
                if (plugin.dataFile.getConfig().contains("data.spawnerstats")) {
                    ConfigurationSection cs = plugin.dataFile.getConfig().getConfigurationSection("data.spawnerstats");
                    for (String key : cs.getKeys(false)) {
                        if (plugin.dataFile.getConfig().contains("data.spawnerstats." + key + ".type")) {
                            if (plugin.dataFile.getConfig().getString("data.spawnerstats." + key + ".type").equals("OMNI")) {

                                Location loc = Arconix.pl().serialize().unserializeLocation(key);
                                if (loc != null && loc.getWorld() != null) {
                                    int destx = loc.getBlockX() >> 4;
                                    int destz = loc.getBlockZ() >> 4;
                                    if (!loc.getWorld().isChunkLoaded(destx, destz)) {
                                        continue;
                                    }
                                    if (loc.getBlock().getType() == Material.MOB_SPAWNER) {
                                        Spawner eSpawner = new Spawner(loc);

                                        String last = null;
                                        String next = null;
                                        List<SpawnerItem> list = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + key + ".entities"));
                                        for (SpawnerItem item : list) {
                                            if (item.getType().equals(eSpawner.getOmniState())) {
                                                last = item.getType();
                                            } else if (last != null && next == null) {
                                                next = item.getType();
                                            }
                                        }
                                        if (next == null) {
                                            next = list.get(0).getType();
                                        }
                                        plugin.getApi().updateDisplayItem(next,loc);
                                        eSpawner.setOmniState(next);
                                    } else {
                                        plugin.dataFile.getConfig().set("data.spawnerstats." + key + ".type", null);
                                        plugin.dataFile.getConfig().set("data.spawnerstats." + key + ".entities", null);
                                    }
                                } else {
                                    plugin.dataFile.getConfig().set("data.spawnerstats." + key + ".type", null);
                                    plugin.dataFile.getConfig().set("data.spawnerstats." + key + ".entities", null);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
