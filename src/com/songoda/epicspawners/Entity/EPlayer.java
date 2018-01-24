package com.songoda.epicspawners.Entity;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Lang;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Spawners.SpawnerDropEvent;
import com.songoda.epicspawners.Spawners.SpawnerItem;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class EPlayer {
    Player p;

    EpicSpawners plugin = EpicSpawners.pl();

    public EPlayer(Player p) {
        this.p = p;
    }

    public void plus(Entity entity, int amt) {
        try {
            if (plugin.getConfig().getInt("settings.Goal") != 0 && plugin.getConfig().getBoolean("settings.Mob-kill-counting") && p.hasPermission("epicspawners.Killcounter")) {
                String type = Methods.getType(entity.getType());
                if (plugin.spawnerFile.getConfig().getBoolean("Entities." + type + ".Allowed")) {
                    String uuid = p.getUniqueId().toString();
                    int total = 0;
                    if (plugin.dataFile.getConfig().getInt("data.kills." + uuid + "." + type) != 0)
                        total = plugin.dataFile.getConfig().getInt("data.kills." + uuid + "." + type);
                    int goal = plugin.getConfig().getInt("settings.Goal");
                    if (plugin.spawnerFile.getConfig().getInt("Entities." + type + ".CustomGoal") != 0) {
                        goal = plugin.spawnerFile.getConfig().getInt("Entities." + type + ".CustomGoal");
                    }
                    if (total > goal)
                        total = 1;
                    total = amt + total;

                    if (plugin.getConfig().getInt("settings.Alert-every") != 0) {
                        if (total % plugin.getConfig().getInt("settings.Alert-every") == 0 && total != goal) {
                            Arconix.pl().packetLibrary.getActionBarManager().sendActionBar(p, Lang.ALERT.getConfigValue(Integer.toString(goal - total), type));
                        }

                    }
                    if (total % goal == 0) {
                        dropSpawner(entity.getLocation(), 0, entity.getType().name());
                        plugin.dataFile.getConfig().set("data.kills." + uuid + "." + type, 0);
                        Arconix.pl().packetLibrary.getActionBarManager().sendActionBar(p, Lang.DROPPED.getConfigValue(type));
                    } else
                        plugin.dataFile.getConfig().set("data.kills." + uuid + "." + type, total);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void dropSpawner(Location location, int multi, String type) {
        try {
            SpawnerDropEvent event = new SpawnerDropEvent(location, p);
            Bukkit.getPluginManager().callEvent(event);
            ItemStack item;

            if (!event.isCancelled()) {
                if (!type.toUpperCase().equals("OMNI")) {
                    item = plugin.getApi().newSpawnerItem(Methods.restoreType(type), multi, 1);
                } else {
                    if (!p.isSneaking() || p.isSneaking() && !plugin.getConfig().getBoolean("settings.Sneak-for-stack")) {
                        List<SpawnerItem> spawners = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".entities"));
                        List<ItemStack> items = plugin.getApi().removeOmni(plugin.getApi().newOmniSpawner(spawners));
                        item = items.get(0);
                        if (plugin.getApi().getType(items.get(1)).equals("OMNI"))
                            plugin.getApi().saveCustomSpawner(items.get(1), location.getBlock());
                    } else {
                        List<SpawnerItem> spawners = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(location) + ".entities"));
                        item = plugin.getApi().newOmniSpawner(spawners);
                    }
                }

                if (plugin.getConfig().getBoolean("settings.Add-Spawner-To-Inventory-On-Drop") && p != null) {
                    if (p.getInventory().firstEmpty() == -1)
                        location.getWorld().dropItemNaturally(location, item);
                    else
                        p.getInventory().addItem(item);
                } else
                    location.getWorld().dropItemNaturally(location, item);
            }


        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public Player getP() {
        return p;
    }
}
