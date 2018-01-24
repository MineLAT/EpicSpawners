package com.songoda.epicspawners.Handlers;

import com.songoda.epicspawners.Utils.ConfigWrapper;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Lang;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private EpicSpawners plugin = EpicSpawners.pl();
    private ConfigWrapper blackFile = new ConfigWrapper(plugin, "", "blacklist.yml");

    public BlacklistHandler() {
        blackFile.createNewFile("Loading language file", "EpicSpawnesrs.java blacklist file");
        loadBlacklistFile();
    }

    public boolean isBlacklisted(Player p, boolean yell) {
        boolean blacklisted = false;
        try {
        List<String> list = blackFile.getConfig().getStringList("settings.blacklist");
        String cworld = p.getWorld().getName();
        for (int i = 0; i < list.size(); i++) {
            String world = list.get(i);
            if (cworld.equalsIgnoreCase(world)) {
                if (yell) {
                    p.sendMessage(Lang.BLACKLISTED.getConfigValue());
                }
                blacklisted = true;
            }
        }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return blacklisted;
    }

    private void loadBlacklistFile() {
        try {
        List<String> list = new ArrayList<>();
        list.add("world2");
        list.add("world3");
        list.add("world4");
        list.add("world5");
        blackFile.getConfig().addDefault("settings.blacklist", list);

        blackFile.getConfig().options().copyDefaults(true);
        blackFile.saveConfig();
    } catch (Exception e) {
        Debugger.runReport(e);
    }
    }

    public void reload() {
        blackFile.createNewFile("Loading blacklist file", "EpicSpawnesrs.java blacklist file");
        loadBlacklistFile();
    }
}
