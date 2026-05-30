package org.mrdarkimc.raidsrecode.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.raidsrecode.EventTimer;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.events.raidevent.RaidEvent;
import org.mrdarkimc.raidsrecode.manager.Undoable;

import java.util.*;
import java.util.stream.Collectors;

//Обновляет голограммы и содержимое сундуков
//todo убрать листы с класса, сделать, что бы 1 сундук - 1 класс + 1 голограмма.
//клиент будет использовать как List
//todo багулька. Дисплеи не удаляются
public class ChestUpdater implements EventTimer.TimerTask, Undoable {
    private final List<Location> chestLocations;
    //private final List<MiniTextDisplay> holograms;
    private final List<EventTimer.TimerTask> hologramUpdaters;
    private final Random random = new Random();
    private final int refillChestTime = 60; //каждую минуту
    private final World world;
    private boolean isFirstSpawn = true;
//    private boolean isHolosSpawned = false;


    public ChestUpdater(MiniTextDisplay chestHoloTemplate) {
//        super(SatanicRaids.getInstance(), lifetime);
        this.world = RaidEvent.getRaidWorldLocation().getWorld();
        this.chestLocations = loadChestLocations();
        Bukkit.getLogger().info("World for chest Updater: " + world);
        //this.holograms = new ArrayList<>(this.chestLocations.size());
        this.hologramUpdaters = new ArrayList<>();

        for (Location chestLocation : this.chestLocations) {
//            holograms.add(chestHoloTemplate.makeCopy());
            if (chestLocation == null) {
                Bukkit.getLogger().warning("Chest location is null! Ignoring this holo updater!!!");
                continue;
            }
            HoloUpdater holoUpdater = new HoloUpdater(chestHoloTemplate.makeCopy(), chestLocation, refillChestTime);
            hologramUpdaters.add(holoUpdater);
        }

    }

    @Override
    public void nextSecound(EventTimer timer) {
        //ensureHologramsExists();
//        if (isFirstSpawn) {
//            spawnAndFillChests();
//            isFirstSpawn = false;
//        }
        updateHolos(timer);
        if (timer.getCurrentTime() % refillChestTime != 0) {
            return;
        }
        spawnAndFillChests();
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

    public void spawnAndFillChests() {
        chestLocations.forEach(loc -> {
            Bukkit.getLogger().info("spawning chest to: " + loc);
                    loc.getBlock().setType(Material.CHEST);
                    create3by3Zone(loc.clone().subtract(0, 1, 0), Material.BEDROCK);
                }
        );
        fillChests();
    }

    private void create3by3Zone(Location loc, Material blocktype) {
        int centerX = loc.getBlockX();
        int centerY = loc.getBlockY();
        int centerZ = loc.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = loc.getWorld().getBlockAt(centerX + x, centerY, centerZ + z);
                block.setType(blocktype);
            }
        }
    }

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
//
//    //todo прикрутить формат времени
//    private void updateHologram(MiniTextDisplay holo, EventTimer eventTimer) {
//        List<String> rawContents = holo.getRawContents();
//        rawContents.replaceAll((s) -> s.replace("{time}", calculateLocalTime(eventTimer)));
//        holo.applyText(rawContents);
//    }

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
        FileConfiguration config = SatanicRaids.getInstance().getLootsConfig().get();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");

        if (itemsSection == null) return;

        Set<String> lootKeys = itemsSection.getKeys(false);
        if (lootKeys.isEmpty()) return;

        List<ItemStack> stackList = new ArrayList<>();
        for (String s : lootKeys) {
            ItemStack itemStack = config.getItemStack("items." + s);
            if (itemStack != null) {
                stackList.add(itemStack);
            }
        }

        if (stackList.isEmpty()) return;

        for (Location chestLocation : chestLocations) {
            if (chestLocation.getBlock().getType() != Material.CHEST) {
                continue;
            }

            Chest chest = (Chest) chestLocation.getBlock().getState();
            Inventory chestInventory = chest.getInventory();
            chestInventory.clear();

            int itemCount = random.nextInt(7) + 3;

            List<Integer> freeSlots = new ArrayList<>();
            for (int i = 0; i < chestInventory.getSize(); i++) {
                freeSlots.add(i);
            }

            for (int i = 0; i < itemCount && !freeSlots.isEmpty(); i++) {
                ItemStack randomItem = stackList.get(random.nextInt(stackList.size()));

                int randomSlotIndex = random.nextInt(freeSlots.size());
                int slot = freeSlots.remove(randomSlotIndex);

                chestInventory.setItem(slot, randomItem.clone());
            }
        }
    }


    @Override
    public void undo() {
        Undoable.undoEach(hologramUpdaters);
    }
}
