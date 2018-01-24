package com.songoda.epicspawners.Hooks;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 3/17/2017.
 */
public class ASkyBlockHook implements Hooks {

    private EpicSpawners plugin = EpicSpawners.pl();

    ASkyBlockAPI as = ASkyBlockAPI.getInstance();

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            if (as.getIslandAt(location) != null) {
                UUID owner = as.getOwner(location);
                List<UUID> list = as.getTeamMembers(owner);
                if (owner != null) {
                    for (UUID uuid : list) {
                        if (uuid.equals(p.getUniqueId())) {
                            return true;
                        }
                    }
                    if (owner.equals(p.getUniqueId())) {
                        return true;
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

    @Override
    public boolean canInteract(Player p, Location location) {
        return canBuild(p, location);
    }

    @Override
    public boolean isOnLand(String uuid, Location location) {
        String owner = as.getOwner(location).toString();
        if (uuid.equals(owner))
            return true;
        return false;
    }

    @Override
    public String getFactionId(String name) {
        return null;
    }

}
