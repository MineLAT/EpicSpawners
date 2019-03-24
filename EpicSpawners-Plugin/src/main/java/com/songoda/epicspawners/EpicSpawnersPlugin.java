package com.songoda.epicspawners;

import com.google.common.base.Preconditions;
import com.songoda.epicspawners.api.EpicSpawners;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import com.songoda.epicspawners.hook.HookManager;
import com.songoda.epicspawners.api.utils.SpawnerDataBuilder;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostManager;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.command.CommandManager;
import com.songoda.epicspawners.handlers.AppearanceHandler;
import com.songoda.epicspawners.handlers.BlacklistHandler;
import com.songoda.epicspawners.hologram.Hologram;
import com.songoda.epicspawners.hologram.HologramArconix;
import com.songoda.epicspawners.hologram.HologramHolographicDisplays;
import com.songoda.epicspawners.listeners.*;
import com.songoda.epicspawners.player.PlayerActionManager;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.SpawnManager;
import com.songoda.epicspawners.spawners.condition.*;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.spawners.spawner.ESpawnerManager;
import com.songoda.epicspawners.spawners.spawner.ESpawnerStack;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageRow;
import com.songoda.epicspawners.storage.types.StorageMysql;
import com.songoda.epicspawners.storage.types.StorageYaml;
import com.songoda.epicspawners.tasks.SpawnerParticleTask;
import com.songoda.epicspawners.tasks.SpawnerSpawnTask;
import com.songoda.epicspawners.utils.*;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class EpicSpawnersPlugin extends JavaPlugin implements EpicSpawners {

    private static final Set<Biome> BIOMES = EnumSet.allOf(Biome.class);
    private static EpicSpawnersPlugin INSTANCE;
    private References references;


    private ConfigWrapper spawnerFile = new ConfigWrapper(this, "", "spawners.yml");

    private SpawnManager spawnManager;
    private PlayerActionManager playerActionManager;
    private SpawnerManager spawnerManager;
    private BoostManager boostManager;
    private SettingsManager settingsManager;
    private CommandManager commandManager;
    private HookManager hookManager;

    private BlacklistHandler blacklistHandler;
    private AppearanceHandler appearanceHandler;

    private Hologram hologram;

    private SpawnerParticleTask particleTask;
    private SpawnerSpawnTask spawnerCustomSpawnTask;

    private Heads heads;
    private Locale locale;

    private Storage storage;

    public static EpicSpawnersPlugin getInstance() {
        return INSTANCE;
    }

    private boolean checkVersion() {
        int workingVersion = 13;
        int currentVersion = Integer.parseInt(Bukkit.getServer().getClass()
                .getPackage().getName().split("\\.")[3].split("_")[1]);

        if (currentVersion < workingVersion) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                Bukkit.getConsoleSender().sendMessage("");
                Bukkit.getConsoleSender().sendMessage(String.format("%sYou installed the 1.%s only version of %s on a 1.%s server. Since you are on the wrong version we disabled the plugin for you. Please install correct version to continue using %s.", ChatColor.RED, workingVersion, this.getDescription().getName(), currentVersion, this.getDescription().getName()));
                Bukkit.getConsoleSender().sendMessage("");
            }, 20L);
            return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Check to make sure the Bukkit version is compatible.
        if (!checkVersion()) return;

        EpicSpawnersAPI.setImplementation(this);

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText(String.format("&7%s %s by &5Songoda <3&7!", this.getName(), this.getDescription().getVersion())));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.heads = new Heads();
        this.settingsManager = new SettingsManager(this);

        this.setupConfig();
        this.setupSpawners();
        this.setupLanguage();

        if (SettingsManager.Setting.DOWNLOAD_FILES.getBoolean()) this.update();

        this.references = new References();
        this.boostManager = new BoostManager();
        this.spawnManager = new SpawnManager();
        this.spawnerManager = new ESpawnerManager();
        this.hookManager = new HookManager(this);
        this.commandManager = new CommandManager(this);
        this.blacklistHandler = new BlacklistHandler();
        this.playerActionManager = new PlayerActionManager();

        this.loadSpawnersFromFile();
        this.checkStorage();

        this.appearanceHandler = new AppearanceHandler();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Event registration
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new SpawnerListeners(this), this);
        pluginManager.registerEvents(new PlayerJoinListeners(this), this);

        AbstractGUI.initializeListeners(this);

        // Register Hologram Plugin
        if (SettingsManager.Setting.SPAWNER_HOLOGRAMS.getBoolean()) {
            if (pluginManager.isPluginEnabled("HolographicDisplays"))
                hologram = new HologramHolographicDisplays(this);
            else if (pluginManager.isPluginEnabled("Arconix")) hologram = new HologramArconix(this);
        }


        int timeout = SettingsManager.Setting.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

        // Start tasks
        this.particleTask = SpawnerParticleTask.startTask(this);
        this.spawnerCustomSpawnTask = SpawnerSpawnTask.startTask(this);

        // Load Spawners
        Bukkit.getScheduler().runTaskLater(this, this::loadData, 10);

        // Start Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));

    }

    @Override
    public void onDisable() {
        this.saveToFile();
        this.storage.closeConnection();
        this.particleTask.cancel();
        this.spawnerCustomSpawnTask.cancel();
        if (hologram != null)
            this.hologram.unloadHolograms();

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicSpawners " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    private void loadData() {
        // Adding in spawners.
        if (storage.containsGroup("spawners")) {
            for (StorageRow row : storage.getRowsByGroup("spawners")) {
                try {
                    if (row.get("location") == null) continue;
                    Location location = Methods.unserializeLocation(row.getKey());

                    ESpawner spawner = new ESpawner(location);

                    for (String stackKey : row.get("stacks").asString().split(";")) {
                        if (stackKey == null) continue;
                        String[] stack = stackKey.split(":");
                        if (!spawnerManager.isSpawnerData(stack[0].toLowerCase())) continue;
                        spawner.addSpawnerStack(new ESpawnerStack(spawnerManager.getSpawnerData(stack[0]), Integer.parseInt(stack[1])));
                    }

                    if (row.getItems().containsKey("placedby"))
                        spawner.setPlacedBy(UUID.fromString(row.get("placedby").asString()));

                    spawner.setSpawnCount(row.get("spawns").asInt());
                    this.spawnerManager.addSpawnerToWorld(location, spawner);
                } catch (Exception e) {
                    System.out.println("Failed to load spawner.");
                    e.printStackTrace();
                }
            }
        }

        // Adding in Boosts
        if (storage.containsGroup("boosts")) {
            for (StorageRow row : storage.getRowsByGroup("boosts")) {
                if (row.get("boosttype").asObject() != null)
                    continue;

                BoostData boostData = new BoostData(
                        BoostType.valueOf(row.get("boosttype").asString()),
                        row.get("amount").asInt(),
                        Long.parseLong(row.getKey()),
                        row.get("data").asObject());

                this.boostManager.addBoostToSpawner(boostData);
            }
        }

        // Adding in Player Data
        if (storage.containsGroup("players")) {
            for (StorageRow row : storage.getRowsByGroup("players")) {
                PlayerData playerData = playerActionManager.getPlayerAction(UUID.fromString(row.getKey()));

                Map<EntityType, Integer> entityKills = new HashMap<>();
                if (row.get("entitykills").asObject() == null) continue;
                for (String entityKillsKey : row.get("entitykills").asString().split(";")) {
                    if (entityKillsKey == null) continue;
                    String[] entityKills2 = entityKillsKey.split(":");
                    if (entityKills2[0] == null || entityKills2[0].equals("")) continue;
                    EntityType entityType = EntityType.valueOf(entityKills2[0]);
                    int amt = Integer.parseInt(entityKills2[1]);
                    entityKills.put(entityType, amt);
                }
                playerData.setEntityKills(entityKills);
            }
        }
        if (hologram != null)
            hologram.loadHolograms();

        //Register Crafting Recipe
        System.out.println("[" + getDescription().getName() + "] Loading Crafting Recipes");
        this.enabledRecipe();

        // Save data initially so that if the person reloads again fast they don't lose all their data.
        this.saveToFile();
    }

    @SuppressWarnings("unchecked")
    private void loadSpawnersFromFile() {
        // Register spawner data into SpawnerRegistry from configuration.
        FileConfiguration spawnerConfig = spawnerFile.getConfig();
        if (!spawnerConfig.contains("Entities")) return;
        for (String key : spawnerConfig.getConfigurationSection("Entities").getKeys(false)) {
            ConfigurationSection currentSection = spawnerConfig.getConfigurationSection("Entities." + key);

            List<EntityType> entities = new ArrayList<>();
            List<Material> blocks = new ArrayList<>();
            List<Material> spawnBlocks = new ArrayList<>();
            List<ItemStack> itemDrops = (List<ItemStack>) currentSection.getList("itemDrops", new ArrayList<>());
            List<ItemStack> items = (List<ItemStack>) currentSection.getList("items", new ArrayList<>());
            List<String> commands = currentSection.getStringList("command");

            for (String block : currentSection.getStringList("blocks")) {
                blocks.add(Material.matchMaterial(block.toUpperCase()));
            }
            for (String block : currentSection.getString("Spawn-Block").split(",")) {
                spawnBlocks.add(Material.matchMaterial(block.toUpperCase()));
            }
            for (String entity : currentSection.getStringList("entities")) {
                entities.add(EntityType.valueOf(entity));
            }

            SpawnerDataBuilder dataBuilder = new ESpawnerDataBuilder(key).uuid(currentSection.getInt("uuid"))
                    .entities(entities).blocks(blocks).items(items).entityDroppedItems(itemDrops).commands(commands)
                    .spawnBlocks(spawnBlocks)
                    .active(currentSection.getBoolean("Allowed"))
                    .spawnOnFire(currentSection.getBoolean("Spawn-On-Fire"))
                    .upgradeable(currentSection.getBoolean("Upgradable"))
                    .convertible(currentSection.getBoolean("Convertible"))
                    .convertRatio(currentSection.getString("Convert-Ratio"))
                    .inShop(currentSection.getBoolean("In-Shop"))
                    .pickupCost(currentSection.getDouble("Pickup-Cost"))
                    .craftable(currentSection.getBoolean("Craftable"))
                    .recipe(currentSection.getString("Recipe-Layout"))
                    .recipeIngredients(currentSection.getStringList("Recipe-Ingredients"))
                    .shopPrice(currentSection.getDouble("Shop-Price"))
                    .killGoal(currentSection.getInt("CustomGoal"))
                    .upgradeCostEconomy(currentSection.getInt("Custom-ECO-Cost"))
                    .upgradeCostExperience(currentSection.getInt("Custom-XP-Cost"))
                    .tickRate(currentSection.getString("Tick-Rate"))
                    .particleEffect(ParticleEffect.valueOf(currentSection.getString("Spawn-Effect", "HALO")))
                    .spawnEffectParticle(ParticleType.valueOf(currentSection.getString("Spawn-Effect-Particle", "REDSTONE")))
                    .entitySpawnParticle(ParticleType.valueOf(currentSection.getString("Entity-Spawn-Particle", "SMOKE")))
                    .spawnerSpawnParticle(ParticleType.valueOf(currentSection.getString("Spawner-Spawn-Particle", "FIRE")))
                    .particleDensity(ParticleDensity.valueOf(currentSection.getString("Particle-Amount", "NORMAL")))
                    .particleEffectBoostedOnly(currentSection.getBoolean("Particle-Effect-Boosted-Only"));

            if (currentSection.contains("custom")) {
                dataBuilder.isCustom(currentSection.getBoolean("custom"));
            } else {
                dataBuilder.isCustom(key.toLowerCase().contains("custom"));
            }

            if (currentSection.contains("Display-Name")) {
                dataBuilder.displayName(currentSection.getString("Display-Name"));
            }
            if (currentSection.contains("Display-Item")) {
                dataBuilder.displayItem(Material.valueOf(currentSection.getString("Display-Item")));
            }

            SpawnerData data = dataBuilder.build();

            if (currentSection.contains("Conditions")) {
                String biomeString = currentSection.getString("Conditions.Biomes");
                Set<Biome> biomes;
                if (biomeString.toLowerCase().equals("all"))
                    biomes = EnumSet.copyOf(BIOMES);
                else {
                    biomes = new HashSet<>();
                    for (String string : biomeString.split(", ")) {
                        biomes.add(Biome.valueOf(string));
                    }
                }

                String[] heightString = currentSection.getString("Conditions.Height").split(":");
                String[] playerString = currentSection.getString("Conditions.Required Player Distance And Amount").split(":");

                data.addCondition(new SpawnConditionNearbyPlayers(Integer.parseInt(playerString[0]), Integer.parseInt(playerString[1])));
                data.addCondition(new SpawnConditionNearbyEntities(currentSection.getInt("Conditions.Max Entities Around Spawner")));
                data.addCondition(new SpawnConditionBiome(biomes));
                data.addCondition(new SpawnConditionHeight(Integer.parseInt(heightString[0]), Integer.parseInt(heightString[1])));
                data.addCondition(new SpawnConditionLightDark(SpawnConditionLightDark.Type.valueOf(currentSection.getString("Conditions.Light"))));
                data.addCondition(new SpawnConditionStorm(currentSection.getBoolean("Conditions.Storm Only")));
            }

            this.spawnerManager.addSpawnerData(key, data);
        }
    }

    private void update() {
        try {
            URL url = new URL("http://update.songoda.com/index.php?plugin=" + getDescription().getName() + "&version=" + getDescription().getVersion());
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String jsonString = sb.toString();
            JSONObject json = (JSONObject) new JSONParser().parse(jsonString);

            JSONArray files = (JSONArray) json.get("neededFiles");
            for (Object o : files) {
                JSONObject file = (JSONObject) o;

                switch ((String) file.get("type")) {
                    case "locale":
                        InputStream in = new URL((String) file.get("link")).openStream();
                        Locale.saveDefaultLocale(in, (String) file.get("name"));
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to update.");
            //e.printStackTrace();
        }
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    private void saveToFile() {
        checkStorage();

        //ToDO: If the defaults are set correctly this could do the initial config save.

        // Save spawner settings

        FileConfiguration spawnerConfig = spawnerFile.getConfig();
        spawnerConfig.set("Entities", null);

        ConfigurationSection entitiesSection = spawnerConfig.createSection("Entities");
        for (SpawnerData spawnerData : spawnerManager.getAllSpawnerData()) {
            ConfigurationSection currentSection = entitiesSection.createSection(spawnerData.getIdentifyingName());

            currentSection.set("uuid", spawnerData.getUUID());
            currentSection.set("Display-Name", spawnerData.getDisplayName());

            currentSection.set("blocks", getStrings(spawnerData.getBlocks()));
            currentSection.set("entities", getStrings(spawnerData.getEntities()));
            currentSection.set("itemDrops", spawnerData.getEntityDroppedItems());
            currentSection.set("items", spawnerData.getItems());
            currentSection.set("command", spawnerData.getCommands());

            currentSection.set("custom", spawnerData.isCustom());
            currentSection.set("Spawn-Block", String.join(", ", getStrings(spawnerData.getSpawnBlocksList())));
            currentSection.set("Allowed", spawnerData.isActive());
            currentSection.set("Spawn-On-Fire", spawnerData.isSpawnOnFire());
            currentSection.set("Upgradable", spawnerData.isUpgradeable());
            currentSection.set("Convertible", spawnerData.isConvertible());
            currentSection.set("Convert-Ratio", spawnerData.getConvertRatio());
            currentSection.set("In-Shop", spawnerData.isInShop());
            currentSection.set("Shop-Price", spawnerData.getShopPrice());
            currentSection.set("CustomGoal", spawnerData.getKillGoal());
            currentSection.set("Custom-ECO-Cost", spawnerData.getUpgradeCostEconomy());
            currentSection.set("Custom-XP-Cost", spawnerData.getUpgradeCostExperience());
            currentSection.set("Tick-Rate", spawnerData.getTickRate());
            currentSection.set("Pickup-cost", spawnerData.getPickupCost());
            currentSection.set("Craftable", spawnerData.isCraftable());
            currentSection.set("Recipe-Layout", spawnerData.getRecipe());
            currentSection.set("Recipe-Ingredients", spawnerData.getRecipeIngredients());

            currentSection.set("Spawn-Effect", spawnerData.getParticleEffect().name());
            currentSection.set("Spawn-Effect-Particle", spawnerData.getSpawnEffectParticle().name());
            currentSection.set("Entity-Spawn-Particle", spawnerData.getEntitySpawnParticle().name());
            currentSection.set("Spawner-Spawn-Particle", spawnerData.getSpawnerSpawnParticle().name());
            currentSection.set("Particle-Amount", spawnerData.getParticleDensity().name());
            currentSection.set("Particle-Effect-Boosted-Only", spawnerData.isParticleEffectBoostedOnly());


            for (SpawnCondition spawnCondition : spawnerData.getConditions()) {
                if (spawnCondition instanceof SpawnConditionBiome) {
                    if (BIOMES.equals(((SpawnConditionBiome) spawnCondition).getBiomes())) {
                        currentSection.set("Conditions.Biomes", "ALL");
                    } else {
                        currentSection.set("Conditions.Biomes", String.join(", ", getStrings(((SpawnConditionBiome) spawnCondition).getBiomes())));
                    }
                }
                if (spawnCondition instanceof SpawnConditionHeight)
                    currentSection.set("Conditions.Height", ((SpawnConditionHeight) spawnCondition).getMin() + ":" + ((SpawnConditionHeight) spawnCondition).getMax());
                if (spawnCondition instanceof SpawnConditionLightDark)
                    currentSection.set("Conditions.Light", ((SpawnConditionLightDark) spawnCondition).getType().name());
                if (spawnCondition instanceof SpawnConditionStorm)
                    currentSection.set("Conditions.Storm Only", ((SpawnConditionStorm) spawnCondition).isStormOnly());
                if (spawnCondition instanceof SpawnConditionNearbyEntities)
                    currentSection.set("Conditions.Max Entities Around Spawner", ((SpawnConditionNearbyEntities) spawnCondition).getMax());
                if (spawnCondition instanceof SpawnConditionNearbyPlayers)
                    currentSection.set("Conditions.Required Player Distance And Amount", ((SpawnConditionNearbyPlayers) spawnCondition).getDistance() + ":" + ((SpawnConditionNearbyPlayers) spawnCondition).getAmount());
            }

            if (spawnerData.getDisplayItem() != null) {
                currentSection.set("Display-Item", spawnerData.getDisplayItem().name());
            }
        }

        this.spawnerFile.saveConfig();

        storage.doSave();
    }

    private <T extends Enum<T>> String[] getStrings(List<T> mats) {
        List<String> strings = new ArrayList<>();

        for (Object object : mats) {
            if (object instanceof Material) {
                strings.add(((Material) object).name());
            } else if (object instanceof EntityType) {
                strings.add(((EntityType) object).name());
            }
        }

        return strings.toArray(new String[strings.size()]);
    }

    private String[] getStrings(Set<Biome> biomes) {
        List<String> strings = new ArrayList<>();

        for (Biome biome : biomes) {
            strings.add(biome.name());
        }

        return strings.toArray(new String[strings.size()]);
    }

    private void setupConfig() {
        this.settingsManager.updateSettings();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    private void setupSpawners() {
        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().toLowerCase().contains("armor")) {
                processDefault(value.name());
            }
        }

        this.processDefault("Omni");
        this.spawnerFile.getConfig().options().copyDefaults(true);
        this.spawnerFile.saveConfig();
    }

    private void setupLanguage() {
        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
    }

    private void enabledRecipe() {
        top:
        for (SpawnerData spawnerData : spawnerManager.getAllSpawnerData()) {
            if (!spawnerData.isCraftable()) continue;

            String recipe = spawnerData.getRecipe();

            String type = spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_").replace("MUSHROOM_COW", "MOOSHROOM");

            ShapedRecipe spawnerRecipe = new ShapedRecipe(new NamespacedKey(this, "SPAWNER_RECIPE_" + type), newSpawnerItem(spawnerData, 1));

            if (recipe.length() != 9) return;

            String[] split = Methods.splitStringEvery(recipe, 3);
            spawnerRecipe.shape(split[0], split[1], split[2]);

            List<String> ingredients = spawnerData.getRecipeIngredients();

            if (ingredients.isEmpty()) return;

            for (String ingredient : ingredients) {
                try {
                    if (!ingredient.contains(",")) return;
                    String[] s = ingredient.split(",");
                    char letter = s[0].trim().toCharArray()[0];
                    String materialStr = s[1].trim();

                    Material material;

                    if (materialStr.equals("SPAWN_EGG")) {
                        try {
                            material = Material.valueOf(type + "_SPAWN_EGG");
                        } catch (Exception ignored) {
                            continue top;
                        }
                    } else {
                        material = Material.valueOf(materialStr);
                    }

                    spawnerRecipe.setIngredient(letter, material);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            getServer().addRecipe(spawnerRecipe);
        }
    }

    private void processDefault(String value) {
        FileConfiguration spawnerConfig = spawnerFile.getConfig();

        String type = Methods.getTypeFromString(value);
        Random rn = new Random();
        int uuid = rn.nextInt(9999);
        spawnerConfig.addDefault("Entities." + type + ".uuid", uuid);

        if (!spawnerConfig.contains("Entities." + type + ".Display-Name")) {
            spawnerConfig.set("Entities." + type + ".Display-Name", type);
        }
        if (!spawnerConfig.contains("Entities." + type + ".Pickup-cost")) {
            spawnerConfig.addDefault("Entities." + type + ".Pickup-cost", 0);
        }

        String spawnBlock = "AIR";
        if (value.equalsIgnoreCase("pig") || value.equalsIgnoreCase("sheep") || value.equalsIgnoreCase("chicken") ||
                value.equalsIgnoreCase("cow") || value.equalsIgnoreCase("rabbit") || value.equalsIgnoreCase("llamma") ||
                value.equalsIgnoreCase("horse") || value.equalsIgnoreCase("OCELOT")) {
            spawnBlock = "GRASS_BLOCK";
        }

        if (value.equalsIgnoreCase("MUSHROOM_COW")) {
            spawnBlock = "MYCELIUM";
        }

        if (value.equalsIgnoreCase("SQUID") || value.equalsIgnoreCase("ELDER_GUARDIAN") || value.equalsIgnoreCase("COD") ||
                value.equalsIgnoreCase("SALMON") || value.toUpperCase().contains("FISH")) {
            spawnBlock = "WATER";
        }

        if (value.equalsIgnoreCase("OCELOT")) {
            spawnBlock += ", LEAVES";
        }

        EntityType entityType = null;

        for (EntityType val : EntityType.values()) {
            if (val.isSpawnable() && val.isAlive()) {
                if (val.name().equals(value)) {
                    entityType = val;
                    List<String> list = new ArrayList<>();
                    list.add(value);
                    if (!spawnerConfig.contains("Entities." + type + ".entities"))
                        spawnerConfig.addDefault("Entities." + type + ".entities", list);
                }
            }
        }

        spawnerConfig.addDefault("Entities." + type + ".custom", false);
        spawnerConfig.addDefault("Entities." + type + ".Spawn-Block", spawnBlock);
        spawnerConfig.addDefault("Entities." + type + ".Allowed", true);
        spawnerConfig.addDefault("Entities." + type + ".Spawn-On-Fire", false);
        spawnerConfig.addDefault("Entities." + type + ".Upgradable", true);
        spawnerConfig.addDefault("Entities." + type + ".Convertible", true);
        spawnerConfig.addDefault("Entities." + type + ".Convert-Ratio", "45%");
        spawnerConfig.addDefault("Entities." + type + ".In-Shop", true);
        spawnerConfig.addDefault("Entities." + type + ".Shop-Price", 1000.00);
        spawnerConfig.addDefault("Entities." + type + ".CustomGoal", 0);
        spawnerConfig.addDefault("Entities." + type + ".Custom-ECO-Cost", 0);
        spawnerConfig.addDefault("Entities." + type + ".Custom-XP-Cost", 0);
        spawnerConfig.addDefault("Entities." + type + ".Tick-Rate", "800:200");
        spawnerConfig.addDefault("Entities." + type + ".Spawn-Effect", "NONE");
        spawnerConfig.addDefault("Entities." + type + ".Spawn-Effect-Particle", "REDSTONE");
        spawnerConfig.addDefault("Entities." + type + ".Entity-Spawn-Particle", "SMOKE");
        spawnerConfig.addDefault("Entities." + type + ".Spawner-Spawn-Particle", "FIRE");
        spawnerConfig.addDefault("Entities." + type + ".Particle-Amount", "NORMAL");
        spawnerConfig.addDefault("Entities." + type + ".Particle-Effect-Boosted-Only", true);
        spawnerConfig.addDefault("Entities." + type + ".Craftable", false);
        spawnerConfig.addDefault("Entities." + type + ".Recipe-Layout", "AAAABAAAA");
        spawnerConfig.addDefault("Entities." + type + ".Recipe-Ingredients", Arrays.asList("A, IRON_BARS", "B, SPAWN_EGG"));

        if (entityType == EntityType.SLIME) {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Biomes", Biome.SWAMP);
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Height", "50:70");
        } else {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Biomes", "ALL");
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Height", "0:256");
        }
        if (entityType != null && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Light", "DARK");
        } else {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Light", "BOTH");
        }
        spawnerConfig.addDefault("Entities." + type + ".Conditions.Storm Only", false);
        spawnerConfig.addDefault("Entities." + type + ".Conditions.Max Entities Around Spawner", 6);
        spawnerConfig.addDefault("Entities." + type + ".Conditions.Required Player Distance And Amount", 16 + ":" + 1);
    }

    public void reload() {
        String langMode = getConfig().getString("System.Language Mode");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
        this.locale.reloadMessages();
        this.spawnerFile.createNewFile("Loading Spawners File", "EpicSpawners Spawners File");
        this.hookManager = new HookManager(this);
        this.references = new References();
        this.blacklistHandler.reload();
        this.loadSpawnersFromFile();
        this.setupConfig();
    }

    public Locale getLocale() {
        return locale;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public PlayerActionManager getPlayerActionManager() {
        return playerActionManager;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public AppearanceHandler getAppearanceHandler() {
        return appearanceHandler;
    }

    public BlacklistHandler getBlacklistHandler() {
        return blacklistHandler;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Heads getHeads() {
        return heads;
    }
    public HookManager getHookManager() {
        return hookManager;
    }

    @Override
    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    @Override
    public ItemStack newSpawnerItem(SpawnerData data, int amount) {
        return newSpawnerItem(data, amount, 1);
    }

    @Override
    public ItemStack newSpawnerItem(SpawnerData data, int amount, int stackSize) {
        Preconditions.checkArgument(stackSize > 0, "Stack size must be greater than or equal to 0");

        ItemStack item = new ItemStack(Material.SPAWNER, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.compileName(data, stackSize, true));
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public SpawnerData identifySpawner(String sid) {
        int id = Integer.parseInt(sid.replace(";", ""));
        for (SpawnerData data : spawnerManager.getAllSpawnerData()) {
            if (data.getUUID() == id)
                return data;
        }
        return null;
    }

    @Override
    public SpawnerData getSpawnerDataFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        String name = item.getItemMeta().getDisplayName();
        if (name == null) return null;

        if (name.contains(":")) {
            String value = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace(";", "").split(":")[0];
            if (Methods.isInt(value)) {
                return identifySpawner(value);
            }
            return spawnerManager.getSpawnerData(value.toLowerCase().replace("_", " "));
        }

        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
        return spawnerManager.getSpawnerData(cs.getSpawnedType());
    }

    @Override
    public SpawnerDataBuilder createSpawnerData(String identifier) {
        return new ESpawnerDataBuilder(identifier);
    }

    @Override
    public int getStackSizeFromItem(ItemStack item) {
        Preconditions.checkNotNull(item, "Cannot get stack size of null item");
        if (!item.hasItemMeta()) return 1;

        String name = item.getItemMeta().getDisplayName();
        if (name == null || !name.contains(":")) return 1;

        String amount = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace(";", "").split(":")[1];
        return NumberUtils.toInt(amount, 1);
    }
}
