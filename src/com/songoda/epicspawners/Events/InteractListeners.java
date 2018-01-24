package com.songoda.epicspawners.Events;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Lang;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import com.songoda.epicspawners.Utils.Reflection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;

/**
 * Created by songoda on 2/25/2017.
 */
public class InteractListeners implements Listener {

    private EpicSpawners plugin = EpicSpawners.pl();

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEventEgg(PlayerInteractEvent e) {

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player p = e.getPlayer();
            Block b = e.getClickedBlock();
            ItemStack i = e.getItem();
            Material is = null;
            if (e.getItem() != null) {
                is = i.getType();
            }
            if (e.getItem() != null) {
                if (is.equals(Material.WATER_BUCKET)) {
                    if (plugin.getConfig().getBoolean("settings.spawners-repel-liquid")) {
                        Block block = e.getClickedBlock();
                        int bx = block.getX();
                        int by = block.getY();
                        int bz = block.getZ();
                        int radius = plugin.getConfig().getInt("settings.spawners-repel-radius");
                        for (int fx = -radius; fx <= radius; fx++) {
                            for (int fy = -radius; fy <= radius; fy++) {
                                for (int fz = -radius; fz <= radius; fz++) {
                                    Block b2 = e.getClickedBlock().getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                                    if (b2.getType().equals(Material.MOB_SPAWNER)) {
                                        e.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MONSTER_EGG && plugin.blacklist.isBlacklisted(p, true))
                e.setCancelled(true);
            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MONSTER_EGG && !plugin.blacklist.isBlacklisted(p, true)) {
                Spawner eSpawner = new Spawner(b);
                String btype = Methods.getType(eSpawner.getSpawner().getSpawnedType());

                if (plugin.getConfig().getBoolean("settings.Eggs-convert-spawners") && plugin.spawnerFile.getConfig().getBoolean("Entities." + btype + ".Allowed")) {
                    int bmulti = 1;
                    if (plugin.dataFile.getConfig().getInt("data.spawner." + Arconix.pl().serialize().serializeLocation(b)) != 0)
                        bmulti = plugin.dataFile.getConfig().getInt("data.spawner." + Arconix.pl().serialize().serializeLocation(b));
                    int amt = p.getInventory().getItemInHand().getAmount();
                    EntityType itype;

                    if (plugin.v1_7 || plugin.v1_8)
                        itype = ((SpawnEgg) i.getData()).getSpawnedType();
                    else {
                        String str = Reflection.getNBTTagCompound(Reflection.getNMSItemStack(i)).toString();
                        if (str.contains("minecraft:"))
                            itype = EntityType.fromName(str.substring(str.indexOf("minecraft:") + 10, str.indexOf("\"}")));
                        else
                            itype = EntityType.fromName(str.substring(str.indexOf("EntityTag:{id:") + 15, str.indexOf("\"}")));
                    }

                    if (p.hasPermission("epicspawners.egg." + itype) || p.hasPermission("epicspawners.egg.*")) {
                        if (amt < bmulti) {
                            p.sendMessage(Lang.NEED_MORE.getConfigValue(Integer.toString(bmulti)));
                        } else {
                            if (btype.equals(Methods.getType(itype))) {
                                p.sendMessage(Lang.SAME_TYPE.getConfigValue(btype));
                            } else {
                                eSpawner.getSpawner().setSpawnedType(itype);
                                eSpawner.update();
                                plugin.holo.processChange(b);
                                if (p.getGameMode() != GameMode.CREATIVE) {
                                    Methods.takeItem(p, bmulti);
                                }
                            }
                        }
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerInteractEvent e) {
        try {
            if (!e.isCancelled()) {
                if (!Methods.isOffhand(e)) {
                    Player p = e.getPlayer();
                    Block b = e.getClickedBlock();
                    ItemStack i = e.getItem();
                    String loc = Arconix.pl().serialize().serializeLocation(b);
                    if (plugin.dataFile.getConfig().getString("data.blockshop." + loc) != null) {
                        e.setCancelled(true);
                        plugin.shop.show(plugin.dataFile.getConfig().getString("data.blockshop." + loc).toUpperCase(), 1, p);
                        return;
                    }
                    if (plugin.hooks.canInteract(e.getPlayer(), e.getClickedBlock().getLocation())) {
                        if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            Material is = null;
                            if (e.getItem() != null) {
                                is = i.getType();
                            }
                            if (is == Material.MONSTER_EGG)
                                return;
                            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MOB_SPAWNER && !plugin.blacklist.isBlacklisted(p, true)) {
                                if (b != null) {
                                    Spawner eSpawner = new Spawner(b);
                                    if (!p.isSneaking() && i.getItemMeta().getDisplayName() != null) {
                                        String itype = plugin.getApi().getIType(i);
                                        if (p.hasPermission("epicspawners.combine." + itype) || p.hasPermission("epicspawners.combine.*")) {
                                            eSpawner.processCombine(p, i, null);
                                            e.setCancelled(true);
                                        }
                                    }
                                }
                            } else if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && !plugin.blacklist.isBlacklisted(p, false)) {
                                if (!p.isSneaking()) {
                                    Spawner eSpawner = new Spawner(b);
                                    eSpawner.view(p, 1);
                                    plugin.holo.processChange(b);
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}
