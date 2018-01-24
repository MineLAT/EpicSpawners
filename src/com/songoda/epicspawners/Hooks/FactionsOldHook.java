package com.songoda.epicspawners.Hooks;

import com.songoda.epicspawners.Utils.Debugger;
import me.markeh.factionsframework.entities.FPlayer;
import me.markeh.factionsframework.entities.FPlayers;
import me.markeh.factionsframework.entities.Faction;
import me.markeh.factionsframework.entities.Factions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class FactionsOldHook implements Hooks {

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            FPlayer fp = FPlayers.getBySender(p);

            Faction faction = Factions.getFactionAt(location);

            if (fp.getFaction().equals(faction) || faction.isNone()) {
                return true;
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
        Faction faction = Factions.getFactionAt(location);

        if (faction.getId().equals(id)) {
            return true;
        }
        return false;
    }

    @Override
    public String getFactionId(String name) {
        try {
            Faction faction = Factions.getByName(name, "");

            return faction.getId();
        } catch (Exception e) {
        }
        return null;
    }

}
