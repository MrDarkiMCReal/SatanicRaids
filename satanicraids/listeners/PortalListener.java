package org.mrdarkimc.satanicraids.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.mrdarkimc.satanicraids.listeners.events.ChestItemTakeEvent;
import org.mrdarkimc.satanicraids.portals.PortalExt;
import org.mrdarkimc.satanicraids.portals.PortalToEventWorldHandler;
import org.mrdarkimc.satanicraids.portals.PortalToOverworldHandler;
import org.mrdarkimc.satanicraids.utils.WorthCalculator;

import java.util.Iterator;
import java.util.List;


public class PortalListener implements Listener {
    private final String RAID_WORLD = "raidworld"; //todo hardcode + no oop = fired
    private final PortalToOverworldHandler portalToOverworld;//= new PortalToOverworldHandler();
    private final PortalToEventWorldHandler eventPortalHandler;// = new PortalToEventWorldHandler();
    private final List<Location> chests;
    //private PortalExt portalExt;
    //2 класса заменить на 1 класс - portal или portalExt(2е)
    public PortalListener(PortalToOverworldHandler portalToOverworld, PortalToEventWorldHandler eventPortalHandler, List<Location> chestLocations) {
        this.portalToOverworld = portalToOverworld;
        this.eventPortalHandler = eventPortalHandler;
        //this.portalExt = new PortalExt(portalToOverworld.getPortal(),eventPortalHandler.getPortal());
        this.chests = chestLocations;
    }

    /**
     * Обрабатывает телепортации и запрещает команды телепортации в эвентовый мир
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        boolean portalTpEvent = e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL);
        if (!portalTpEvent) {
            return;
        }
        Player player = e.getPlayer();
        World from = e.getFrom().getWorld();

        if ("world".equals(from.getName())) {
            boolean isTpEvent = eventPortalHandler.isTeleportUsingLocalPortal(player);
            if (isTpEvent) {
                e.setCancelled(true);
                eventPortalHandler.enter(player);
                return;
            }
            return;
        }
        if (RAID_WORLD.equals(from.getName().toLowerCase())) {
            boolean teleportUsingPortal = portalToOverworld.isTeleportUsingLocalPortal(player);
            if (teleportUsingPortal) {
                e.setCancelled(true);
                leaveEvent(player);
                return;
            }
        }
    }
    public void leaveEvent(Player player){
        eventPortalHandler.exit(player); //todo двухсторонний канал портала
    }
    @EventHandler
    public void onChestItemTake(InventoryClickEvent event){
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory instanceof DoubleChestInventory chestInventory){
            Location location = chestInventory.getLocation();
            if (location.getWorld().getName().equalsIgnoreCase(RAID_WORLD)){ //todo hardcode
                if (chests.contains(location)){
                    Bukkit.getPluginManager().callEvent(new ChestItemTakeEvent(event));
                    //todo use service instead of event
                }
            }
        }
    }



}
