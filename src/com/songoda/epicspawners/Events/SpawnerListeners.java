package com.songoda.epicspawners.Events;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Spawners.SpawnerItem;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by songoda on 2/25/2017.
 */
public class SpawnerListeners implements Listener {

    private EpicSpawners plugin = EpicSpawners.pl();

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent e) {
        try {
            String type = e.getEntityType().name();
            if (plugin.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(e.getSpawner().getBlock()) + ".type")) {
                if (!plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(e.getSpawner().getBlock()) + ".type").equals("OMNI"))
                    type = plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(e.getSpawner().getBlock()) + ".type");
            }

            if (e.getEntityType() == EntityType.FALLING_BLOCK)
                return;
            String sloc = Arconix.pl().serialize().serializeLocation(e.getSpawner().getBlock());
            Spawner eSpawner = new Spawner(e.getSpawner().getBlock());
            if ((!e.getEntityType().equals(EntityType.IRON_GOLEM) && !e.getEntityType().equals(EntityType.GHAST) || !plugin.getConfig().getBoolean("settings.Large-Entity-Safe-Spawning")) && !e.isCancelled() && e.getLocation() != null) {
                e.getEntity().remove();
                if (plugin.getConfig().getBoolean("settings.Mob-kill-counting")) {
                    plugin.dataFile.getConfig().set("data.Entities." + e.getEntity().getUniqueId(), true);
                }


                if (Methods.countEntitiesAroundLoation(e.getSpawner().getLocation()) < plugin.getConfig().getInt("settings.Max-Entities-Around-Single-Spawner")) {
                    long lastSpawn = 1001;
                    if (plugin.lastSpawn.containsKey(e.getSpawner().getLocation())) {
                        lastSpawn = (new Date()).getTime() - plugin.lastSpawn.get(e.getSpawner().getLocation()).getTime();
                    }
                    if (lastSpawn >= 1 * 1000) {
                        if (plugin.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(e.getSpawner().getLocation()) + ".type")) {
                            if (plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(e.getSpawner().getLocation()) + ".type").equals("OMNI")) {
                                List<SpawnerItem> list = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(e.getSpawner().getLocation()) + ".entities"));
                                for (SpawnerItem ent : list) {
                                    if (!ent.getType().equals(e.getSpawner().getSpawnedType())) {

                                        int high = eSpawner.getMulti();
                                        int low = eSpawner.getMulti();

                                        if (ent.getMulti() > eSpawner.getMulti()) {
                                            high = ent.getMulti();
                                        } else {
                                            low = ent.getMulti();
                                        }

                                        int rand = ThreadLocalRandom.current().nextInt(plugin.getConfig().getInt("settings.Random-Low"), plugin.getConfig().getInt("settings.Random-High"));
                                        int times = high - low + rand + eSpawner.getBoost();

                                        for (String entt : (ArrayList<String>)plugin.spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(ent.getType()) + ".entities")) {
                                            spawnEntity(e.getSpawner().getLocation(), EntityType.valueOf(entt), times);
                                        }
                                    }
                                }
                            }
                        }

                        int rand = ThreadLocalRandom.current().nextInt(plugin.getConfig().getInt("settings.Random-Low"), plugin.getConfig().getInt("settings.Random-High"));


                        String equation = plugin.getConfig().getString("settings.Spawner-Spawn-Equation");
                        equation = equation.replace("{RAND}", rand + "");
                        equation = equation.replace("{MULTI}", eSpawner.getMulti() + "");

                        int times;
                        if (!plugin.cache.containsKey(equation)) {
                            ScriptEngineManager mgr = new ScriptEngineManager();
                            ScriptEngine engine = mgr.getEngineByName("JavaScript");
                            times = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                            plugin.cache.put(equation, times);
                        } else {
                            times = plugin.cache.get(equation);
                        }
                        int size = 1;
                        try {
                            size = plugin.spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(type) + ".entities").size();
                        } catch (Exception ee) {
                        }
                        if (size == 0)
                            size = 1;
                        times = (int)Math.ceil((times + eSpawner.getBoost()) / size);

                        if (plugin.spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(type) + ".entities")) {
                            for (String ent : (ArrayList<String>) plugin.spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(type) + ".entities")) {
                                spawnEntity(e.getSpawner().getLocation(), EntityType.valueOf(ent), times);
                            }
                        }
                        plugin.lastSpawn.put(e.getSpawner().getLocation(), new Date());
                    }

                }
            } else {
                int spawnAmt = 1;
                if (plugin.dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") != 0) {
                    spawnAmt = plugin.dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") + 1;
                }
                plugin.dataFile.getConfig().set("data.spawnerstats." + sloc + ".spawns", spawnAmt);
            }
            if (plugin.dataFile.getConfig().getInt("data.spawner." + sloc) != 0) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> eSpawner.updateDelay(), 10L);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    public void spawnEntity(Location location, EntityType type, int times) {
        try {
            Block b = location.getBlock();
            if (b.isBlockPowered() && plugin.getConfig().getBoolean("settings.redstone-activate"))
                return;

            String sloc = Arconix.pl().serialize().serializeLocation(location);

            int spawnAmt = 1;
            if (plugin.dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") != 0) {
                spawnAmt = plugin.dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") + 1;
            }
            int stack = 0;
            if (plugin.getServer().getPluginManager().getPlugin("StackMob") != null) {
                stack = times;
                times = 1;
            }
            int num = 0;
            while (num != times) {
                Location spot = null;
                boolean in = false;

                int amt = 0;
                while (!in && amt <= 25) {
                    double testX = ThreadLocalRandom.current().nextDouble(-1, 1);
                    double testY = ThreadLocalRandom.current().nextDouble(-1, 2);
                    double testZ = ThreadLocalRandom.current().nextDouble(-1, 1);

                    double x = location.getX() + testX * (double) 3;
                    double y = location.getY() + testY;
                    double z = location.getZ() + testZ * (double) 3;

                    spot = new Location(location.getWorld(), x, y, z);
                    if (canSpawn(type, spot))
                        in = true;

                    amt++;
                }
                if (in) {
                    float x = (float) (0 + (Math.random() * 1));
                    float y = (float) (0 + (Math.random() * 2));
                    float z = (float) (0 + (Math.random() * 1));

                    if (spot != null) {
                        Arconix.pl().packetLibrary.getParticleManager().broadcastParticle(spot, x, y, z, 0, plugin.getConfig().getString("settings.SpawnEffect"), 5);

                        Location loc = spot.clone();
                        loc.subtract(0, 1, 0);
                        if (type.equals(EntityType.IRON_GOLEM)) {

                            spot.add(.5, .5, .5);

                        } else {
                            spot = spot.clone().getBlock().getLocation();

                            double spawnX = ThreadLocalRandom.current().nextDouble(0.4, 0.6);
                            double spawnZ = ThreadLocalRandom.current().nextDouble(0.4, 0.6);

                            spot.add(spawnX, .5, spawnZ);
                        }

                        spawnMob(location, spot, type, CreatureSpawnEvent.SpawnReason.SPAWNER, stack);
                    }
                }
                spawnAmt++;
                num++;
            }
            plugin.dataFile.getConfig().set("data.spawnerstats." + sloc + ".spawns", spawnAmt);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    public static Method spawnMethod = null;

    public Entity spawnMob(Location spawnerLoc, Location loc, EntityType type, CreatureSpawnEvent.SpawnReason reason, int stack) throws Exception {
        World world = loc.getWorld();
        Class<? extends Entity> clazz = type.getEntityClass();
        Entity e;
        if (plugin.v1_12 || plugin.v1_11) {
            if (spawnMethod == null)
                spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, Consumer.class, CreatureSpawnEvent.SpawnReason.class);
            e = (Entity) spawnMethod.invoke(world, loc, clazz, null, reason);
        } else {
            if (spawnMethod == null)
                spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, CreatureSpawnEvent.SpawnReason.class);
            e = (Entity) spawnMethod.invoke(world, loc, clazz, reason);
        }

        Spawner spawner = new Spawner(spawnerLoc);

        if (spawner.isSpawningOnFire())
            e.setFireTicks(160);

        if (plugin.getServer().getPluginManager().getPlugin("StackMob") != null && stack != 0) {
            uk.antiperson.stackmob.StackMob sm = ((uk.antiperson.stackmob.StackMob) Bukkit.getPluginManager().getPlugin("StackMob"));
            e.setMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.METATAG, new FixedMetadataValue(sm, stack));
            e.setMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.NO_SPAWN_STACK, new FixedMetadataValue(sm, true));
        }

        if (plugin.getConfig().getBoolean("settings.Mob-kill-counting")) {
            plugin.dataFile.getConfig().set("data.Entities." + e.getUniqueId(), true);
        }
        return e;
    }


    public boolean canSpawn(EntityType type, Location location) {
        boolean canSpawn = true;
        String spawnBlocks = plugin.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(Methods.getType(type)) + ".Spawn-Block");

        List<String> blocks = Arrays.asList(spawnBlocks.split("\\s*,\\s*"));

        if (!Methods.isAir(location.getBlock().getType()) && (!isWater(location.getBlock().getType()) && !blocks.contains("WATER"))) {
            canSpawn = false;
        }

        boolean canSpawnUnder = false;
        if (canSpawn != false) {
            for (String block : blocks) {
                Location loc = location.clone().subtract(0, 1, 0);
                if (loc.getBlock().getType().toString().equalsIgnoreCase(block) || isWater(loc.getBlock().getType()) && blocks.contains("WATER")) {
                        canSpawnUnder = true;
                }
            }
            canSpawn = canSpawnUnder;
        }
        return canSpawn;
    }

    public boolean isWater(Material type) {
        if (type == Material.WATER || type == Material.STATIONARY_WATER) {
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        try {
            if (plugin.getConfig().getBoolean("settings.Hostile-mobs-attack-second")) {
                if (event.getEntity().getLastDamageCause() != null) {
                    if (event.getEntity().getLastDamageCause().getCause().name().equals("ENTITY_ATTACK")) {
                        return;
                    }
                }
                event.setCancelled(true);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}
