package com.songoda.epicspawners.Hooks;

import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 3/17/2017.
 */
public class USkyBlockHook implements Hooks {

    Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");

    uSkyBlockAPI usb = (uSkyBlockAPI) plugin;

    @Override
    public boolean canBuild(Player p, Location location) {
        try {

            List<Player> list = usb.getIslandInfo(location).getOnlineMembers();

            for (Player pl : list) {
                if (pl.equals(p)) {
                    return true;
                }
            }

            if (usb.getIslandInfo(location).isLeader(p)) {
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
    public boolean isOnLand(String uuid, Location location) {
        String owner = usb.getIslandInfo(location).getLeader();
        if (uuid.equals(owner))
            return true;
        return false;
    }

    @Override
    public String getFactionId(String name) {
        return null;
    }
}
