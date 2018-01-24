package com.songoda.epicspawners.Hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public interface Hooks {

    boolean canBuild(Player p, Location location);

    boolean canInteract(Player p, Location location);

    boolean isOnLand(String id, Location location);

    String getFactionId(String name);

}
