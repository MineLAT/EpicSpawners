package com.songoda.epicspawners.Hooks;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class TownyHook implements Hooks {

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            if (TownyUniverse.isWilderness(location.getBlock())) {
                return true;
            } else {
                if (TownyUniverse.getTownBlock(location).hasTown()) {
                    Resident r = TownyUniverse.getDataSource().getResident(p.getName());
                    if (r.hasTown()) {
                        if (TownyUniverse.getTownName(location).equals(r.getTown().getName())) {
                            return true;
                        }
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
    public boolean isOnLand(String id, Location location) {
        try {
            if (TownyUniverse.isWilderness(location.getBlock())) {
                return false;
            }
            if (TownyUniverse.getTownBlock(location).getTown().getUID().equals(Integer.parseInt(id))) {
                return true;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    @Override
    public String getFactionId(String name) {
        try {
        return TownyUniverse.getDataSource().getTown(name).getUID().toString();
        } catch (Exception e) {
        }
        return null;
    }
}
