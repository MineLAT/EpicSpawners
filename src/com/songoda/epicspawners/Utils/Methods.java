package com.songoda.epicspawners.Utils;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by songoda on 2/24/2017.
 */
public class Methods {

    public static String formatName(EntityType type, int multi) {
        if (multi <= 0)
            multi = 1;
        return compileName(type.name(), multi, true);
    }

    public static void takeItem(Player p, int amt) {
        if (p.getGameMode() != GameMode.CREATIVE) {
            int result = p.getInventory().getItemInHand().getAmount() - amt;
            if (result > 0) {
                ItemStack is = p.getItemInHand();
                is.setAmount(is.getAmount() - amt);
                p.setItemInHand(is);
            } else {
                p.setItemInHand(null);
            }
        }
    }

    public static boolean isOffhand(PlayerInteractEvent e) {
        EpicSpawners plugin = EpicSpawners.pl();
        if (!plugin.v1_8 && !plugin.v1_7) {
            if (e.getHand() == EquipmentSlot.OFF_HAND)
                return true;
        }
        return false;
    }

    public static boolean isOffhand(BlockPlaceEvent e) {
        EpicSpawners plugin = EpicSpawners.pl();
        if (!plugin.v1_8 && !plugin.v1_7) {
            if (e.getHand() == EquipmentSlot.OFF_HAND)
                return true;
        }
        return false;
    }

    public static String getBoostCost(int time, int amt) {
        EpicSpawners plugin = EpicSpawners.pl();

        String cost = "";

        String un = plugin.getConfig().getString("settings.Boost-cost");

        String[] parts = un.split(":");

        String type = parts[0];

        String multi = parts[1];

        int co = boostCost(multi, time, amt);
        if (!type.equals("ECO") && !type.equals("XP")) {
            cost += "&6&l" + co;
            cost += " &7" + type.substring(0, 1).toUpperCase() + type.toLowerCase().substring(1);
            if (co != 1)
                cost += "s";
        } else if (type.equals("ECO")) {
            cost += "&6&l$" + Arconix.pl().format().formatEconomy(co);
        } else if (type.equals("XP")) {
            cost += "&6&l" + co;
            cost += " &7Levels";
        }

        return cost;
    }

    public static int boostCost(String multi, int time, int amt) {
        return  (int) Math.ceil((Double.parseDouble(multi) * time) * amt);
    }

    public static String compileName(String type, int multi, boolean full) {
        EpicSpawners plugin = EpicSpawners.pl();

        String name = plugin.getConfig().getString("settings.Name-format");
        String nme = getTypeFromString(type);
        if (plugin.spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(type) + ".Display-Name")) {
            nme = plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Name");
        }
        name = name.replace("{TYPE}", nme);
        if (multi > 1 && multi >= 0 || plugin.getConfig().getBoolean("settings.Display-Level-One") && multi >= 0) {
            name = name.replace("{AMT}", Integer.toString(multi)).replace("[", "").replace("]", "");
        } else {
            name = name.replaceAll("\\[.*?\\]", "");
        }

        String info = "";
        if (full) {
            info += Arconix.pl().format().convertToInvisibleString(type.toUpperCase().replaceAll(" ", "_")+":"+multi+":");
        }

        return info + Arconix.pl().format().formatText(name).trim();
    }

    public static ItemStack getGlass() {
        EpicSpawners plugin = EpicSpawners.pl();
        return Arconix.pl().getGUI().getGlass(plugin.getConfig().getBoolean("settings.Rainbow-Glass"), plugin.getConfig().getInt("settings.Glass-Type-1"));
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        EpicSpawners plugin = EpicSpawners.pl();
        if (type)
            return Arconix.pl().getGUI().getGlass(false, plugin.getConfig().getInt("settings.Glass-Type-2"));
        else
            return Arconix.pl().getGUI().getGlass(false, plugin.getConfig().getInt("settings.Glass-Type-3"));
    }

    public static String properType(String type) {
        EpicSpawners plugin = EpicSpawners.pl();
        return plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Name");
    }

    public static String getType(EntityType typ) {
        String type = typ.toString().replaceAll("_", " ");
        type = ChatColor.stripColor(type.substring(0, 1).toUpperCase() + type.toLowerCase().substring(1));
        return type;
    }

    public static String getTypeFromString(String typ) {
        String type = typ.replaceAll("_", " ");
        type = ChatColor.stripColor(type.substring(0, 1).toUpperCase() + type.toLowerCase().substring(1));
        return type;
    }

    public static String restoreType(String typ) {
        String type = typ.replace(" ", "_");
        type = type.toUpperCase();
        return type;
    }

    public static boolean isAir(Material type) {
        if (type == Material.AIR || type == Material.WOOD_PLATE
                || type == Material.STONE_PLATE || type == Material.IRON_PLATE
                || type == Material.GOLD_PLATE)
            return true;
        return false;
    }

    public static int countEntitiesAroundLoation(Location location) {
        EpicSpawners plugin = EpicSpawners.pl();
        int amt = 0;
        if (!plugin.v1_7) {
            List<String> arr = Arrays.asList(plugin.getConfig().getString("settings.Search-Radius").split("x"));
            Collection <Entity> nearbyEntite = location.getWorld().getNearbyEntities(location.clone().add(0.5, 0.5, 0.5), Integer.parseInt(arr.get(0)), Integer.parseInt(arr.get(1)), Integer.parseInt(arr.get(2)));
            if (nearbyEntite.size() >= 1) {
                for (Entity ee : nearbyEntite) {
                    if (ee instanceof LivingEntity) {
                        if (ee instanceof Player || ee.getName().toLowerCase().contains("armor")) {
                        } else {
                            if (plugin.getServer().getPluginManager().getPlugin("StackMob") != null) {
                                if (ee.getMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.METATAG).size() != 0) {
                                    amt = amt + ee.getMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.METATAG).get(0).asInt();
                                }
                            }
                            amt++;
                        }
                    }
                }
            }
        }
        return amt;
    }
}
