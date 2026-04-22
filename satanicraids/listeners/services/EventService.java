package org.mrdarkimc.satanicraids.listeners.services;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EventService {
    public void onChestTakeItem(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        //if (currentItem)
    }
    public void isCustomItem(){
        //pizda тут целая новая система нужна
    }
}
