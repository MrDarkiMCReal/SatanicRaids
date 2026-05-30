package org.mrdarkimc.raidsrecode.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.SatanicRespawner.SatanicRespawner;
import org.mrdarkimc.SatanicRespawner.services.RespawnerService;
import org.mrdarkimc.raidsrecode.EventListener;

public class RaidWorldListener extends EventListener {
    private World world = Bukkit.getWorld("RaidWorld");
    private final List<Location> chestLocation; //todo сравнивать только рейдовые сундуки. на следующей итерации сделать

    public RaidWorldListener(JavaPlugin plugin) {
        super(plugin);
        this.chestLocation = null;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().equals(world)) {
            if (player.hasPermission("satanic.admin")) {
                return;
            }
            event.setCancelled(true);
            new KeyedMessage(player, "messages.no-commands", null).send();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player leftPlayer = event.getPlayer();
        World playerWorld = leftPlayer.getWorld();
        if (playerWorld.getName().equals(world.getName())) {
            RespawnerService respawnerService = SatanicRespawner.getInstance().getRespawnerService();
            respawnerService.fakeKillAndRespawn(leftPlayer);
            new KeyedMessage(null, "messages.death-by-quit", Map.of("{player}", leftPlayer.getName())).broadcast();
        }

    }


    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!isRaidWorld(event.getLocation().getWorld())) {
            return;
        }

        EntityType entityType = event.getEntityType();
        if (entityType == EntityType.CREEPER || entityType == EntityType.TNT) {
            removeChestsFromExplosion(event.blockList());
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!isRaidWorld(event.getBlock().getWorld())) {
            return;
        }

        removeChestsFromExplosion(event.blockList());
    }

    public boolean isRaidWorld(World world) {
        return this.world.equals(world);
    }

    private void removeChestsFromExplosion(List<Block> blocks) {
        Iterator<Block> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();

            if (block.getType() == Material.CHEST ||
                    block.getType() == Material.TRAPPED_CHEST ||
                    block.getType() == Material.ENDER_CHEST ||
                    block.getState() instanceof Chest) {
                iterator.remove();
            }
        }
    }
}
