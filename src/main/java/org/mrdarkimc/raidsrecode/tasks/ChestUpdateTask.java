package org.mrdarkimc.raidsrecode.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.mrdarkimc.SatanicLib.Utils;
import org.mrdarkimc.SatanicLib.tasks.CountDownTask;
import org.mrdarkimc.enhancedtextdisplays.EnhancedTextDisplays;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.displays.interfaces.DisplayHandler;
import org.mrdarkimc.enhancedtextdisplays.tasks.UpdateHoloTask;
import org.mrdarkimc.raidsrecode.SatanicRaids;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

//Обновляет голограммы и содержимое сундуков
public class ChestUpdateTask extends CountDownTask {
    private final List<Location> chestLocations;
    private final List<MiniTextDisplay> holograms;
    private final Random random = new Random();
    private final int refillChestTime = 60; //каждую минуту
    private final World world;

    @Deprecated
    public ChestUpdateTask(MiniTextDisplay chestHoloTemplate, int lifetime) {
        super(SatanicRaids.getInstance(), lifetime);
        this.chestLocations = loadChestLocations();
        this.world = Bukkit.getWorld("RaidWorld");
        this.holograms = new ArrayList<>(this.chestLocations.size());
        for (int i = 0; i < this.chestLocations.size(); i++) {
            holograms.add(chestHoloTemplate.makeCopy());
        }
    }
    public ChestUpdateTask(World world, MiniTextDisplay chestHoloTemplate, int lifetime) {
        super(SatanicRaids.getInstance(), lifetime);
        this.chestLocations = loadChestLocations();
        this.world = world;
        this.holograms = new ArrayList<>(this.chestLocations.size());
        for (int i = 0; i < this.chestLocations.size(); i++) {
            holograms.add(chestHoloTemplate.makeCopy());
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

    @Override
    public void work() {
        updateHolos();//обновляем таймер на голограммах
        if (current % refillChestTime != 0) {
            return;
        }
        fillChests();
    }


    private void updateHolos() {
        for (MiniTextDisplay holo : holograms) {
            updateHologram(holo);
        }
    }

    //todo прикрутить формат времени
    private void updateHologram(MiniTextDisplay holo) {
        List<String> rawContents = holo.getRawContents();
        rawContents.replaceAll((s) -> s.replace("{time}", getFormattedTime()));
        holo.applyText(rawContents);
    }

    public String getFormattedTime() {
        int secondsPassedInCurrentCycle = current % refillChestTime;
        int timeLeft = refillChestTime - secondsPassedInCurrentCycle;
        return formatTime(timeLeft);
    }

    public String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return "сейчас";

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append(" дн. ");
        if (hours > 0) sb.append(hours).append(" час. ");
        if (minutes > 0) sb.append(minutes).append(" мин. ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append(" сек.");

        return sb.toString().trim();
    }

    @Override
    public void startTask() {
        DisplayHandler displayHandler = EnhancedTextDisplays.getInstance().getDisplayHandler();
        for (int i = 0; i < holograms.size(); i++) {
            MiniTextDisplay holo = holograms.get(i);
            Location location = chestLocations.get(i);
            displayHandler.spawnDisplay(holo, location);
        }
        super.startTask();
    }

    public void fillChests() {
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
