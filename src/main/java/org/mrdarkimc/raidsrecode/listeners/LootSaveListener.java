package org.mrdarkimc.raidsrecode.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.mrdarkimc.SatanicLib.configsetups.Configs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.commands.LootInventoryHolder;

//todo зарегать и имплементировать в эвент


public class LootSaveListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof LootInventoryHolder)) {
            return;
        }

        Configs lootsConfig = SatanicRaids.getInstance().getLootsConfig();
        FileConfiguration config = lootsConfig.get();

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            itemsSection = config.createSection("items");
        }

        int nextIndex = itemsSection.getKeys(false).stream()
                .mapToInt(s -> {
                    try { return Integer.parseInt(s); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max().orElse(-1) + 1;

        boolean added = false;
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                itemsSection.set(String.valueOf(nextIndex), item);
                nextIndex++;
                added = true;
            }
        }

        if (added) {
            Configs.startAsyncSavingTask(lootsConfig);
        }
    }
}