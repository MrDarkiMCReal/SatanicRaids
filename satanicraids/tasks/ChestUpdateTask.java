package org.mrdarkimc.satanicraids.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.satanicraids.SatanicRaids;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Класс заполняем ресурсами сундуки по времени и обновляет голограмму <br>
 * Класс ожидает, что сундуки уже были созданы
 */
@Deprecated
public class ChestUpdateTask extends BukkitRunnable { //todo нельзя extends. т.к он будет каждую секу апдейтить
    private List<Location> chestLocations;
    private Random random = new Random();

    public ChestUpdateTask(List<Location> chestLocations) {
        this.chestLocations = chestLocations;
    }

    @Override
    public void run() {
        fillChests();
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
