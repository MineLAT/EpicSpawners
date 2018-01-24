package com.songoda.epicspawners.Hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.songoda.epicspawners.Utils.Debugger;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 3/17/2017.
 */
public class PlotSquaredHook implements Hooks {

    final Plugin plotsquared = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            PlotAPI api = new PlotAPI();
            if (api.getPlot(location) != null) {
                if (api.isInPlot(p)) {
                    if (api.getPlot(p) == api.getPlot(location)) {
                        return  true;
                    } else {
                        return false;
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
