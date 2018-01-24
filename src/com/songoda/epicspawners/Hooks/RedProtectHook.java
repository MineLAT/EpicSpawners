package com.songoda.epicspawners.Hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 3/17/2017.
 */
public class RedProtectHook implements Hooks {

    private EpicSpawners plugin = EpicSpawners.pl();
    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            if (RedProtect.get().getAPI().getRegion(location) != null) {
                return RedProtect.get().getAPI().getRegion(location).canBuild(p);
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
        return false;
    }

    @Override
    public String getFactionId(String name) {
        return null;
    }

}
