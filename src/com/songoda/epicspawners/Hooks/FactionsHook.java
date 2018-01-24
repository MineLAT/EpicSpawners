package com.songoda.epicspawners.Hooks;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class FactionsHook implements Hooks {

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            MPlayer mp = MPlayer.get(p);

            Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));

            if (mp.getFaction().equals(faction)) {
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
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));

        if (faction.getId().equals(id)) {
            return true;
        }
        return false;
    }

    @Override
    public String getFactionId(String name) {
        try {
            Faction faction = FactionColl.get().getByName(name);

            return faction.getId();
        } catch (Exception e) {
        }
        return null;
    }

}
