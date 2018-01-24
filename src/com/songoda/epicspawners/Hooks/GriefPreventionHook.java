package com.songoda.epicspawners.Hooks;

import com.songoda.epicspawners.Utils.Debugger;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class GriefPreventionHook implements Hooks {
    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            if (claim != null) {
                if (claim.allowBuild(p, Material.STONE) == null) {
                    return true;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    @Override
    public boolean canInteract(Player p, Location location) {
        return canBuild(p, location);
    }

    @Override
    public boolean isOnLand(String id, Location location) {
        return false;
    }

    @Override
    public String getFactionId(String name) {
        return null;
    }
}
