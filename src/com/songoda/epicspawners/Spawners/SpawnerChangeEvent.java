package com.songoda.epicspawners.Spawners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

/**
 * Created by songoda on 4/22/2017.
 */
public class SpawnerChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Location location;

    private Player player;

    private int multi;
    private int oldMulti;

    public SpawnerChangeEvent(Location location, Player player, int multi, int oldMulti) {
        this.location = location;
        this.player = player;
        this.multi = multi;
        this.oldMulti = oldMulti;
    }

    public Block getSpawner() {
        return location.getBlock();
    }

    public int getCurrentMulti() {
        return multi;
    }

    public int getOldMulti() {
        return oldMulti;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
