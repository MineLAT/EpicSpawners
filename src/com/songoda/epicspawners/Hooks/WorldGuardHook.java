package com.songoda.epicspawners.Hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class WorldGuardHook implements Hooks {
    @Override
    public boolean canBuild(Player p, Location location) {
        return WorldGuardPlugin.inst().canBuild(p, location);
    }

    @Override
    public boolean canInteract(Player p, Location location) {
        return true;
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
