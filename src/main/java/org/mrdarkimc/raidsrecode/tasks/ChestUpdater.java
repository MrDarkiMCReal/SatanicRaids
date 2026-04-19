package org.mrdarkimc.raidsrecode.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.mrdarkimc.enhancedtextdisplays.EnhancedTextDisplays;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.displays.interfaces.DisplayHandler;
import org.mrdarkimc.raidsrecode.EventTimer;
import org.mrdarkimc.raidsrecode.SatanicRaids;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

//Обновляет голограммы и содержимое сундуков
public class ChestUpdater implements EventTimer.TimerTask {
    private final List<Location> chestLocations;
    //private final List<MiniTextDisplay> holograms;
    private final Random random = new Random();
    private final int refillChestTime = 60; //каждую минуту
    private final World world;
    private boolean isHolosSpawned = false;
    private List<EventTimer.TimerTask> hologramUpdaters;


    public ChestUpdater(MiniTextDisplay chestHoloTemplate) {
//        super(SatanicRaids.getInstance(), lifetime);
        this.chestLocations = loadChestLocations();
        this.world = Bukkit.getWorld("RaidWorld");
        //this.holograms = new ArrayList<>(this.chestLocations.size());
        this.hologramUpdaters = new ArrayList<>();

        for (Location chestLocation : this.chestLocations) {
//            holograms.add(chestHoloTemplate.makeCopy());
            HoloUpdater holoUpdater = new HoloUpdater(chestHoloTemplate.makeCopy(), chestLocation, refillChestTime);
            hologramUpdaters.add(holoUpdater);
        }

    }
    @Override
    public void nextSecound(EventTimer timer) {
        //ensureHologramsExists();
        updateHolos(timer);
        if (timer.getCurrentTime() % refillChestTime != 0) {
            return;
        }
        fillChests();
    }
//    public ChestUpdater(World world, MiniTextDisplay chestHoloTemplate) {
//        //super(SatanicRaids.getInstance(), lifetime);
//        this.chestLocations = loadChestLocations();
//        this.world = world;
//        this.holograms = new ArrayList<>(this.chestLocations.size());
//        for (int i = 0; i < this.chestLocations.size(); i++) {
//            holograms.add(chestHoloTemplate.makeCopy());
//        }
//    }

    private List<Location> loadChestLocations() {
        List<Map<?, ?>> chestsConfig = SatanicRaids.getInstance().getMainConfig().get().getMapList("chests");
        return mapsToListOfLocation(chestsConfig);
    }

    private List<Location> mapsToListOfLocation(List<Map<?, ?>> maps) {
        return maps.stream()
                .map(chestMap -> {
                    int x = (int) chestMap.get("x");
                    int y = (int) chestMap.get("y");
                    int z = (int) chestMap.get("z");
                    return new Location(world, x, y, z);
                }).collect(Collectors.toList());
    }

    private void updateHolos(EventTimer timer) {
        hologramUpdaters.forEach(e -> e.nextSecound(timer));
    }

    //todo прикрутить формат времени
    private void updateHologram(MiniTextDisplay holo, EventTimer eventTimer) {
        List<String> rawContents = holo.getRawContents();
        rawContents.replaceAll((s) -> s.replace("{time}", calculateLocalTime(eventTimer)));
        holo.applyText(rawContents);
    }

    private String calculateLocalTime(EventTimer timer) {
        int secondsPassedInCurrentCycle = timer.getCurrentTime() % refillChestTime;
        int timeLeft = refillChestTime - secondsPassedInCurrentCycle;
        return timer.getFormattedTime(timeLeft);
    }


//    private void ensureHologramsExists() {
//        if (isHolosSpawned) {
//            return;
//        }
//        isHolosSpawned = true;
//        DisplayHandler displayHandler = EnhancedTextDisplays.getInstance().getDisplayHandler();
//        for (int i = 0; i < holograms.size(); i++) {
//            MiniTextDisplay holo = holograms.get(i);
//            Location location = chestLocations.get(i);
//            displayHandler.spawnDisplay(holo, location);
//        }
//    }

    private void fillChests() {
        FileConfiguration config = SatanicRaids.getInstance().getConfig();
        List<Map<?, ?>> lootConfig = config.getMapList("loot");
        if (lootConfig.isEmpty()) {
            return;
        }

        for (Location chestLocation : chestLocations) {
            //System.out.println("Handling chest: " + chestLocation);
            if (chestLocation.getBlock().getType() != Material.CHEST) {
                continue;
            }

            Chest chest = (Chest) chestLocation.getBlock().getState();
            org.bukkit.inventory.Inventory chestInventory = chest.getInventory();
            chestInventory.clear();

            int itemCount = random.nextInt(7) + 3;

            List<Integer> freeSlots = new ArrayList<>();
            for (int i = 0; i < chestInventory.getSize(); i++) {
                freeSlots.add(i);
            }

            for (int i = 0; i < itemCount && !freeSlots.isEmpty(); i++) {
                Map<?, ?> randomLoot = lootConfig.get(random.nextInt(lootConfig.size()));

                try {
                    String materialName = (String) randomLoot.get("material");
                    Material material = Material.matchMaterial(materialName);
                    if (material == null) {
                        continue;
                    }

                    int amount = randomLoot.containsKey("amount") ?
                            ((Number) randomLoot.get("amount")).intValue() : 1;
                    if (randomLoot.containsKey("amount-min") && randomLoot.containsKey("amount-max")) {
                        int min = ((Number) randomLoot.get("amount-min")).intValue();
                        int max = ((Number) randomLoot.get("amount-max")).intValue();
                        amount = random.nextInt(max - min + 1) + min;
                    }

                    ItemStack item = new ItemStack(material, amount);

                    int randomSlotIndex = random.nextInt(freeSlots.size());
                    int slot = freeSlots.remove(randomSlotIndex);

                    chestInventory.setItem(slot, item);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[EventContainer] Ошибка при заполнении сундука: " + e.getMessage());
                }
            }
        }
    }
}
