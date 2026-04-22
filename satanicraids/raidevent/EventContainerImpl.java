//package org.mrdarkimc.satanicraids.raidevent;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.World;
//import org.bukkit.block.Chest;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//import org.mrdarkimc.satanicraids.SatanicRaids;
//import org.mrdarkimc.satanicraids.raidevent.EventWorld;
//import org.mrdarkimc.satanicraids.finders.LocationFinder;
//import org.mrdarkimc.satanicraids.finders.PresetAndSafeFinder;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class EventContainerImpl implements EventContainer {
//    private final EventWorld eventWorld;
//    private final List<Location> spawnLocations;
//    private final List<Location> chestLocations;
//    private final List<Player> involvedPlayers = new ArrayList<>();
//    private final List<Location> hologramLocations = new ArrayList<>(); //todo
//    private ConfigurationSection config;
//    private LocationFinder eventSpawnPointFinder;
//
//    public EventContainerImpl(EventWorld world, List<Location> spawnLocations) {
//        this.eventWorld = world;
//        this.spawnLocations = spawnLocations;
//        this.chestLocations = fetchChestLocations();
//        this.config = SatanicRaids.getInstance().getConfig();
//        this.eventSpawnPointFinder = new PresetAndSafeFinder(world, spawnLocations);
//    }
//
//    @Override
//    public List<Player> getInvolvedPlayers() {
//        return new ArrayList<>(involvedPlayers);
//    }
//
//    public void addPlayer(Player player) {
//        if (!involvedPlayers.contains(player)) {
//            involvedPlayers.add(player);
//        }
//    }
//
//    public void removePlayer(Player player) {
//        involvedPlayers.remove(player);
//    }
//
//    public void spawnChests() {
//        chestLocations.forEach(e -> e.getBlock().setType(Material.CHEST)); //todo зарегать слушатель, что бы их нельзя было взорвать и ломать + создать площадку 3х3 из бедрока
//    }
//    private List<Location> fetchChestLocations(){
//        World world = eventWorld.getRaidWorld();
//
//        List<Map<?, ?>> chestsConfig = config.getMapList("chests");
//
//        return chestsConfig.stream()
//                .map(chestMap -> {
//                    int x = (int) chestMap.get("x");
//                    int y = (int) chestMap.get("y");
//                    int z = (int) chestMap.get("z");
//                    return new Location(world, x, y, z);
//                })
//                .collect(Collectors.toList());
//    }
//    public void prepareSpawnLocations(){
//        for (Location spawnLocation : spawnLocations) {
//            eventWorld.getRaidWorld().getBlockAt(spawnLocation).setType(Material.BEDROCK);
//        }
//    }
//
//    public void fillChests() {
//        if (config == null || eventWorld == null) {
//            return;
//        }
//
//        List<Map<?, ?>> lootConfig = config.getMapList("loot");
//        if (lootConfig.isEmpty()) {
//            return;
//        }
//
//        Random random = new Random();
//
//        for (Location chestLocation : chestLocations) {
//            if (chestLocation.getBlock().getType() != Material.CHEST) {
//                continue;
//            }
//
//            Chest chest = (Chest) chestLocation.getBlock().getState();
//            org.bukkit.inventory.Inventory chestInventory = chest.getInventory();
//            chestInventory.clear();
//
//            // Определяем количество предметов для сундука (от 3 до 9)
//            int itemCount = random.nextInt(7) + 3;
//
//            for (int i = 0; i < itemCount && i < chestInventory.getSize(); i++) {
//                // Выбираем случайный предмет из лута
//                Map<?, ?> randomLoot = lootConfig.get(random.nextInt(lootConfig.size()));
//
//                try {
//                    String materialName = (String) randomLoot.get("material");
//                    Material material = Material.matchMaterial(materialName);
//                    if (material == null) {
//                        continue;
//                    }
//
//                    int amount = randomLoot.containsKey("amount") ?
//                            ((Number) randomLoot.get("amount")).intValue() : 1;
//                    if (randomLoot.containsKey("amount-min") && randomLoot.containsKey("amount-max")) {
//                        int min = ((Number) randomLoot.get("amount-min")).intValue();
//                        int max = ((Number) randomLoot.get("amount-max")).intValue();
//                        amount = random.nextInt(max - min + 1) + min;
//                    }
//
//                    ItemStack item = new ItemStack(material, amount);
//                    chestInventory.addItem(item);
//                } catch (Exception e) {
//                    Bukkit.getLogger().warning("[EventContainer] Ошибка при заполнении сундука: " + e.getMessage());
//                }
//            }
//        }
//    }
//
//    public void spawnHolograms() {
//        // Это будет реализовываться через библиотеку EnhancedTextDisplays
//        // Пока оставляем заглушку
//        if (config == null || eventWorld == null) {
//            return;
//        }
//
//        List<Map<?, ?>> hologramsConfig = config.getMapList("holograms");
//        hologramLocations.clear();
//
//        for (Map<?, ?> hologramConfig : hologramsConfig) {
//            try {
//                int x = (Integer) hologramConfig.get("x");
//                int y = (Integer) hologramConfig.get("y");
//                int z = (Integer) hologramConfig.get("z");
//
//                Location hologramLocation = new Location(eventWorld.getRaidWorld(), x, y, z);
//                hologramLocations.add(hologramLocation);
//
//                // TODO: Создать голограмму через EnhancedTextDisplays
//                // Пример:
//                // EnhancedTextDisplay.create(hologramLocation, lines);
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[EventContainer] Ошибка при создании голограммы: " + e.getMessage());
//            }
//        }
//    }
//
//    public void cleanUp() {
//        // Удаляем сундуки
//        for (Location chestLocation : chestLocations) {
//            if (chestLocation.getBlock().getType() == Material.CHEST) {
//                chestLocation.getBlock().setType(Material.AIR);
//            }
//        }
//        chestLocations.clear();
//        hologramLocations.clear();
//        involvedPlayers.clear();
//    }
//
//    @Override
//    public List<Location> getSpawnLocations() {
//        return spawnLocations;
//    }
//
//    @Override
//    public LocationFinder getLocationFinder() {
//        return eventSpawnPointFinder;
//    }
//
//    @Override
//    public EventWorld getEventWorld() {
//        return null;
//    }
//}
