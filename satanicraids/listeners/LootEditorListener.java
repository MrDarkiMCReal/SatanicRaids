package org.mrdarkimc.satanicraids.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.mrdarkimc.satanicraids.SatanicRaids;
import org.mrdarkimc.satanicraids.commands.LootInventoryHolder;
import org.mrdarkimc.satanicraids.commands.RaidsCommand;

import java.util.*;

public class LootEditorListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof LootInventoryHolder)) {
            return;
        }

        List<Map<String, Object>> loot = new ArrayList<>();

        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("material", item.getType().name());
            entry.put("amount-min", 1);
            entry.put("amount-max", item.getAmount());
            loot.add(entry);
        }

        SatanicRaids plugin = SatanicRaids.getInstance();
        plugin.getConfig().set("loot", loot);
        plugin.saveConfig();

        event.getPlayer().sendMessage("§aЛут обновлен. Предметов: §e" + loot.size());
    }
}





