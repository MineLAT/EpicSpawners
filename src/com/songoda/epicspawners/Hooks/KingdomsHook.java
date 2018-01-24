package com.songoda.epicspawners.Hooks;

import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.SimpleChunkLocation;
import org.kingdoms.constants.player.OfflineKingdomPlayer;
import org.kingdoms.manager.game.GameManagement;

/**
 * Created by songoda on 3/17/2017.
 */
public class KingdomsHook implements Hooks {

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            OfflineKingdomPlayer pl = GameManagement.getPlayerManager().getOfflineKingdomPlayer(p);
            if (pl.getKingdomPlayer().getKingdom() != null) {
                SimpleChunkLocation chunkLocation = new SimpleChunkLocation(location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ());
                Land land = GameManagement.getLandManager().getOrLoadLand(chunkLocation);
                String owner = land.getOwner();
                if (pl.getKingdomPlayer().getKingdom().getKingdomName().equals(owner)) {
                    return true;
                } else {
                    if (owner == null) {
                        return true;
                    }
                }
                return false;
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
        return false;
    }

    @Override
    public String getFactionId(String name) {
        return null;
    }
}
