package com.songoda.epicspawners.Handlers;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Lang;
import com.songoda.epicspawners.Spawners.Shop;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import com.songoda.epicspawners.Utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by songoda on 2/24/2017.
 */
public class CommandHandler implements CommandExecutor {

    private final EpicSpawners plugin;

    public CommandHandler(final EpicSpawners plugin) {
        this.plugin = plugin;
    }


    public void help(CommandSender sender, int page) {
        sender.sendMessage("");
        int of = 3;
        if (!sender.hasPermission("epicspawners.admin")) {
            of = 1;
        }

        sender.sendMessage(Arconix.pl().format().formatText("&7Page: &a" + page + " of " + of + " ======================"));
        if (page == 1) {
            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&7" + plugin.getDescription().getVersion() + " Created by &5&l&oBrianna"));
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes help &7Displays this page."));
            if (page == 2 && sender.hasPermission("epicspawners.admin")) {
                sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes editor &7Opens the spawner editor."));
            }
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aspawnershop &7Opens the spawner shop."));
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aspawnerstats &7Allows a player to view their current EpicSpawners stats and see how many kills they have left to get a specific spawner drop."));
        } else if (page == 2 && sender.hasPermission("epicspawners.admin")) {
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes change <type> &7Changes the entity for the spawner you are looking at."));
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes give [player] [spawnertype/random] [multiplier] [amount] &7Gives an operator the ability to spawn a spawner of his or her choice."));
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes setshop <type> &7Assigns a spawner shop to the block you are looking at."));
        } else if (page == 3 && sender.hasPermission("epicspawners.admin")) {
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes settings &7Edit the EpicSpawners Settings."));
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes boost <p:player, f:faction, t:town, i:islandOwner> <amount> [m:minute, h:hour, d:day, y:year] &7This allows you to boost the amount of spawns that are got from placed spawners."));
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes removeboosts <p:player, f:faction, t:town, i:islandOwner> &7This allows you to remove boosts."));
            sender.sendMessage(Arconix.pl().format().formatText(" &8- &aes removeshop &7Unassigns a spawner shop to the block you are looking at."));
        } else {
            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "That page does not exist!"));
        }
        sender.sendMessage("");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            if (cmd.getName().equalsIgnoreCase("EpicSpawners")) {
                if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                    if (args.length == 2) {
                        help(sender, Integer.parseInt(args[1]));
                    } else {
                        help(sender, 1);
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        plugin.reload();
                        sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&8Configuration and Language files reloaded."));
                    }
                } else if (args[0].equalsIgnoreCase("change")) {
                    if (!sender.hasPermission("epicspawners.admin") && !sender.hasPermission("epicspawners.change.*") && !sender.hasPermission("epicspawners.change." + args[1].toUpperCase())) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        Player p = (Player) sender;
                        Block b = p.getTargetBlock((Set<Material>) null, 200);

                        if (b.getType().equals(Material.MOB_SPAWNER)) {
                            Spawner eSpawner = new Spawner(b);

                            try {
                                eSpawner.getSpawner().setSpawnedType(EntityType.valueOf(args[1].toUpperCase()));
                                eSpawner.update();
                                plugin.holo.processChange(b);
                                sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&7Successfully changed this spawner to &6"+args[1]+"&7."));
                            } catch (Exception ee) {
                                sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&7That Entity does not exist."));
                            }
                        } else {
                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThis is not a spawner."));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("editor")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        plugin.editor.open((Player)sender, 1);
                    }
                } else if (args[0].equalsIgnoreCase("removeboosts")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        if (args.length == 2) {
                            if (plugin.dataFile.getConfig().contains("data.boosts")) {
                                if (args[1].contains("p:") || args[1].contains("player:") ||
                                        args[1].contains("f:") || args[1].contains("faction:") ||
                                        args[1].contains("t:") || args[1].contains("town:") ||
                                    args[1].contains("i:") || args[1].contains("island:")) {
                                    String[] arr = (args[1]).split(":");

                                    String type = "";
                                    String att = "";

                                    if (arr[0].equalsIgnoreCase("p") || arr[0].equalsIgnoreCase("player")) {
                                        if (Bukkit.getOfflinePlayer(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat player does not exist..."));
                                        } else {
                                            type = "player";
                                            att = Bukkit.getOfflinePlayer(arr[1]).getUniqueId().toString();
                                        }
                                    } else if (arr[0].equalsIgnoreCase("f") || arr[0].equalsIgnoreCase("faction")) {
                                        if (plugin.hooks.getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat faction does not exist..."));
                                            return true;
                                        } else {
                                            type = "faction";
                                            att = plugin.hooks.getFactionId(arr[1]);
                                        }
                                    } else if (arr[0].equalsIgnoreCase("t") || arr[0].equalsIgnoreCase("town")) {
                                        if (plugin.hooks.getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat town does not exist..."));
                                            return true;
                                        } else {
                                            type = "town";
                                            att = plugin.hooks.getTownId(arr[1]);
                                        }
                                    } else if (arr[0].equalsIgnoreCase("i") || arr[0].equalsIgnoreCase("island")) {
                                        if (plugin.hooks.getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat island does not exist..."));
                                            return true;
                                        } else {
                                            type = "island";
                                            att = plugin.hooks.getIslandId(arr[1]);
                                        }
                                    }

                                    int removes = plugin.getApi().removeBoosts(type, att);

                                    if (removes == 0) {
                                        sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cNo boosts were found matching your search."));
                                    } else {
                                        if (removes == 1)
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + removes + " &7boost was removed."));
                                        else
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + removes + " &7boosts were removed."));
                                    }
                                } else {
                                    sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + args[1] + " &7this is incorrect"));
                                }
                            } else {
                                sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThe boost database is currently empty."));
                            }
                        } else {
                            sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText("&7Syntax error..."));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("boost")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        if (args.length >= 3) {
                            if (args[1].contains("p:") || args[1].contains("player:") ||
                                    args[1].contains("f:") || args[1].contains("faction:") ||
                                    args[1].contains("t:") || args[1].contains("town:") ||
                                    args[1].contains("i:") || args[1].contains("island:")) {
                                String[] arr = (args[1]).split(":");
                                if (!Arconix.pl().doMath().isNumeric(args[2])) {
                                    sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + args[2] + " &7is not a number..."));
                                } else {

                                    Calendar c = Calendar.getInstance();
                                    Date currentDate = new Date();
                                    c.setTime(currentDate);

                                    String response = " &6" + arr[1] + "&7 has been given a spawner boost of &6" + args[2];

                                    if (args.length > 3) {
                                        if (args[3].contains("m:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.MINUTE, Integer.parseInt(arr2[1]));
                                            response += " &7for &6" + arr2[1] + " minutes&7.";
                                        } else if (args[3].contains("h:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.HOUR, Integer.parseInt(arr2[1]));
                                            response += " &7for &6" + arr2[1] + " hours&7.";
                                        } else if (args[3].contains("d:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.HOUR, Integer.parseInt(arr2[1]) * 24);
                                            response += " &7for &6" + arr2[1] + " days&7.";
                                        } else if (args[3].contains("y:")) {
                                            String[] arr2 = (args[3]).split(":");
                                            c.add(Calendar.YEAR, Integer.parseInt(arr2[1]));
                                            response += " &7for &6" + arr2[1] + " years&7.";
                                        } else {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&7" + args[3] + " &7is invalid."));
                                            return true;
                                        }
                                    } else {
                                        c.add(Calendar.YEAR, 10);
                                        response += "&6.";
                                    }
                                    String uuid = UUID.randomUUID().toString();

                                    String start = "&7";

                                    if (arr[0].equalsIgnoreCase("p") || arr[0].equalsIgnoreCase("player")) {
                                        if (Bukkit.getOfflinePlayer(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat player does not exist..."));
                                        } else {
                                            start += "The player";
                                            plugin.dataFile.getConfig().set("data.boosts." + uuid + ".player", Bukkit.getOfflinePlayer(arr[1]).getUniqueId().toString());
                                        }
                                    } else if (arr[0].equalsIgnoreCase("f") || arr[0].equalsIgnoreCase("faction")) {
                                        if (plugin.hooks.getFactionId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat faction does not exist..."));
                                            return true;
                                        } else {
                                            start += "The faction";
                                            plugin.dataFile.getConfig().set("data.boosts." + uuid + ".faction", plugin.hooks.getFactionId(arr[1]));
                                        }
                                    } else if (arr[0].equalsIgnoreCase("t") || arr[0].equalsIgnoreCase("town")) {
                                        if (plugin.hooks.getTownId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat town does not exist..."));
                                            return true;
                                        } else {
                                            start += "The town";
                                            plugin.dataFile.getConfig().set("data.boosts." + uuid + ".town", plugin.hooks.getTownId(arr[1]));
                                        }
                                    } else if (arr[0].equalsIgnoreCase("i") || arr[0].equalsIgnoreCase("island")) {
                                        if (plugin.hooks.getIslandId(arr[1]) == null) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat island does not exist..."));
                                            return true;
                                        } else {
                                            start += "The island";
                                            plugin.dataFile.getConfig().set("data.boosts." + uuid + ".island", plugin.hooks.getIslandId(arr[1]));
                                        }
                                    }

                                    plugin.dataFile.getConfig().set("data.boosts." + uuid + ".boosted", Integer.parseInt(args[2]));

                                    plugin.dataFile.getConfig().set("data.boosts." + uuid + ".end", c.getTime().getTime());
                                    sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + start + response));
                                }
                            } else {
                                sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + args[1] + " &7this is incorrect"));
                            }
                        } else {
                            sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText("&7Syntax error..."));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("settings")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        Player p = (Player) sender;
                        plugin.sm.openEditor(p);
                    }
                } else if (args[0].equalsIgnoreCase("setshop")) {
                    if (args.length >= 2) {
                        Player p = (Player) sender;
                        if (!sender.hasPermission("epicspawners.admin")) {
                            sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                        } else {
                            if (plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(args[1]) + ".Allowed") == null) {
                                sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText(plugin.references.getPrefix() + "&7The entity type &6" + args[1] + " &7does not exist. Try one of these:"));
                                String list = "";
                                for (final EntityType value : EntityType.values()) {
                                    if (value.isSpawnable() && value.isAlive()) {
                                        list += value.toString() + "&7, &6";
                                    }
                                }
                                sender.sendMessage(Arconix.pl().format().formatText("&6" + list));
                            } else {
                                Entity ent = null;
                                if (Arconix.pl().getPlayer(p).getTarget() != null) {
                                    if (ent instanceof ItemFrame) {
                                        ent = Arconix.pl().getPlayer(p).getTarget();
                                    }
                                }
                                if (ent != null) {
                                    plugin.dataFile.getConfig().set("data.entityshop." + ent.getUniqueId().toString(), args[1]);
                                    sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText(plugin.references.getPrefix() + "&aShop setup successfully."));
                                } else {
                                    if (p.getTargetBlock((Set<Material>) null, 200) != null) {
                                        Block b = p.getTargetBlock((Set<Material>) null, 200);
                                        String loc = Arconix.pl().serialize().serializeLocation(b);
                                        plugin.dataFile.getConfig().set("data.blockshop." + loc, args[1]);
                                        sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText(plugin.references.getPrefix() + "&aShop setup successfully."));
                                    }
                                }
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removeshop")) {
                    if (!sender.hasPermission("epicspawners.admin")) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        Player p = (Player) sender;
                        Entity ent = null;
                        if (Arconix.pl().getPlayer(p).getTarget() != null) {
                            if (ent.getType() == EntityType.ITEM_FRAME) {
                                ent = Arconix.pl().getPlayer(p).getTarget();
                            }
                        }
                        if (ent != null) {
                            plugin.dataFile.getConfig().set("data.entityshop." + ent.getUniqueId().toString(), null);
                        } else {
                            Block b = p.getTargetBlock((Set<Material>) null, 200);
                            String loc = Arconix.pl().serialize().serializeLocation(b);
                            plugin.dataFile.getConfig().set("data.blockshop." + loc, null);
                        }
                        sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText(plugin.references.getPrefix() + "&aShop removed successfully."));
                    }
                } else if (args[0].equalsIgnoreCase("shop")) {
                    if (!sender.hasPermission("epicspawners.openshop")) {
                        sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                    } else {
                        Player p = (Player) sender;
                        plugin.shop.open(p, 1);
                    }
                } else if (args[0].equalsIgnoreCase("give")) {
                    if (args.length <= 3 && args.length != 6) {
                        sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText("&7Syntax error..."));
                    } else {
                        if (!sender.hasPermission("epicspawners.admin")) {
                            sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                        } else {
                            if (Bukkit.getPlayerExact(args[1]) == null) {
                                sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&cThat username does not exist, or the user is not online!"));
                            } else {
                                String type = args[2].toUpperCase();
                                int multi = 0;
                                int amt = 1;
                                if (plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(args[2]) + ".Allowed") == null && !args[2].equalsIgnoreCase("random")) {
                                    sender.sendMessage(plugin.references.getPrefix() + Arconix.pl().format().formatText(plugin.references.getPrefix() + "&7The entity type &6" + args[2] + " &7does not exist. Try one of these:"));
                                    String list = "";

                                    ConfigurationSection cs = plugin.spawnerFile.getConfig().getConfigurationSection("Entities");
                                    for (String key : cs.getKeys(false)) {
                                        key = key.toUpperCase().replace(" ", "_");
                                        list += key + "&7, &6";
                                    }
                                    sender.sendMessage(Arconix.pl().format().formatText("&6" + list));
                                } else {
                                    if (type.equalsIgnoreCase("random")) {
                                        ConfigurationSection cs = plugin.spawnerFile.getConfig().getConfigurationSection("Entities");
                                        for (String key : cs.getKeys(false)) {
                                            key = key.toUpperCase().replace(" ", "_");
                                            if (plugin.spawnerFile.getConfig().getBoolean("Entities." + key + ".Allowed")) {
                                                if (type.equalsIgnoreCase("random")) {
                                                    type = key;
                                                }
                                            }

                                        }
                                    }
                                    if (args.length == 4) {
                                        if (!Arconix.pl().doMath().isNumeric(args[3])) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + args[3] + "&7 is not a number."));
                                        } else {
                                            ItemStack spawnerItem = plugin.api.newSpawnerItem(type, 0, Integer.parseInt(args[3]));
                                            Player pl = Bukkit.getPlayerExact(args[1]);
                                            pl.getInventory().addItem(spawnerItem);
                                            Bukkit.getPlayerExact(args[1]).sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + Lang.GIVE.getConfigValue(Integer.toString(amt), Methods.compileName(type, multi, false))));

                                        }
                                    } else {
                                        if (!Arconix.pl().doMath().isNumeric(args[3])) {
                                            sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + args[3] + "&7 is not a number."));
                                        } else {
                                            if (!Arconix.pl().doMath().isNumeric(args[3])) {
                                                sender.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&6" + args[4] + "&7 is not a number."));
                                            } else {
                                                amt = Integer.parseInt(args[4]);
                                                multi = Integer.parseInt(args[3]);
                                                ItemStack spawnerItem = plugin.api.newSpawnerItem(type, multi, amt);
                                                Player pl = Bukkit.getPlayerExact(args[1]);
                                                pl.getInventory().addItem(spawnerItem);
                                                Bukkit.getPlayerExact(args[1]).sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + Lang.GIVE.getConfigValue(Integer.toString(amt), Methods.compileName(type, multi, false))));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (cmd.getName().equalsIgnoreCase("SpawnerShop")) {
                if (!sender.hasPermission("epicspawners.openshop")) {
                    sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                } else {
                    Player p = (Player) sender;
                    plugin.shop.open(p, 1);
                }
            } else if (cmd.getName().equalsIgnoreCase("SpawnerStats")) {
                if (!sender.hasPermission("epicspawners.stats")) {
                    sender.sendMessage(plugin.references.getPrefix() + Lang.NO_PERMS.getConfigValue());
                } else {
                    Player p = (Player) sender;
                    String uuid = p.getUniqueId().toString();

                    int size = 0;

                    if (plugin.dataFile.getConfig().contains("data.kills")) {
                        for (String u : plugin.dataFile.getConfig().getConfigurationSection("data.kills." + uuid).getKeys(false)) {
                            if (plugin.spawnerFile.getConfig().getInt("Entities." + u + ".CustomGoal") != 0) {
                                size++;
                            }
                        }
                    }

                    Inventory i = Bukkit.createInventory(null, 54, Arconix.pl().format().formatTitle(Lang.SSTATS_TITLE.getConfigValue()));
                    int max2 = 54;
                    if (size <= 9) {
                        i = Bukkit.createInventory(null, 18, Arconix.pl().format().formatTitle(Lang.SSTATS_TITLE.getConfigValue()));
                        max2 = 18;
                    } else if (size <= 9) {
                        i = Bukkit.createInventory(null, 27, Arconix.pl().format().formatTitle(Lang.SSTATS_TITLE.getConfigValue()));
                        max2 = 27;
                    } else if (size <= 18) {
                        i = Bukkit.createInventory(null, 36, Arconix.pl().format().formatTitle(Lang.SSTATS_TITLE.getConfigValue()));
                        max2 = 36;
                    } else if (size <= 27) {
                        i = Bukkit.createInventory(null, 45, Arconix.pl().format().formatTitle(Lang.SSTATS_TITLE.getConfigValue()));
                        max2 = 45;
                    }

                    int num = 0;
                    while (num != 9) {
                        i.setItem(num, Methods.getGlass());
                        num++;
                    }
                    ItemStack exit = new ItemStack(Material.valueOf(plugin.getConfig().getString("settings.Exit-Icon")), 1);
                    ItemMeta exitmeta = exit.getItemMeta();
                    exitmeta.setDisplayName(Lang.EXIT.getConfigValue());
                    exit.setItemMeta(exitmeta);
                    i.setItem(8, exit);

                    short place = 9;
                    p.sendMessage("");
                    if (plugin.dataFile.getConfig().getString("data.kills." + uuid) == null) {
                        p.sendMessage(plugin.references.getPrefix() + Lang.NO_KILLS.getConfigValue());
                    } else {
                        p.sendMessage(plugin.references.getPrefix());
                        p.sendMessage(Arconix.pl().format().formatText(Lang.SSTATS.getConfigValue()));
                        for (String u : plugin.dataFile.getConfig().getConfigurationSection("data.kills." + uuid).getKeys(false)) {

                            int goal = plugin.getConfig().getInt("settings.Goal");
                            if (plugin.spawnerFile.getConfig().getInt("Entities." + u + ".CustomGoal") != 0) {
                                goal = plugin.spawnerFile.getConfig().getInt("Entities." + u + ".CustomGoal");
                            }


                            ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

                            ItemStack item = plugin.heads.addTexture(it, Methods.restoreType(u));

                            ItemMeta itemmeta = item.getItemMeta();
                            ArrayList<String> lore = new ArrayList<>();
                            itemmeta.setLore(lore);
                            itemmeta.setDisplayName(Arconix.pl().format().formatText("&6" + u + "&7: &e" + plugin.dataFile.getConfig().getInt("data.kills." + uuid + "." + u) + "&7/&e" + goal));
                            item.setItemMeta(itemmeta);
                            i.setItem(place, item);

                            place++;
                            p.sendMessage(Arconix.pl().format().formatText("    &7- &6" + u + "&7: &e" + plugin.dataFile.getConfig().getInt("data.kills." + uuid + "." + u) + "&7/&e" + goal));
                        }
                        p.sendMessage(Lang.ON_GOAL.getConfigValue(Integer.toString(plugin.getConfig().getInt("settings.Goal"))));
                    }
                    p.sendMessage("");

                    if (plugin.dataFile.getConfig().getString("data.kills." + uuid) != null)
                        p.openInventory(i);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }
}
