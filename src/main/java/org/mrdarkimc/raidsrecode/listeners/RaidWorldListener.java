package org.mrdarkimc.raidsrecode.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.NotifyAPI.KeyedMessage;
import org.mrdarkimc.SatanicRespawner.SatanicRespawner;
import org.mrdarkimc.SatanicRespawner.services.RespawnerService;
import org.mrdarkimc.raidsrecode.EventListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            KeyedMessage.of("raids-no-commands").send(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player leftPlayer = event.getPlayer();
        World playerWorld = leftPlayer.getWorld();
        if (playerWorld.getName().equals(world.getName())) {
            RespawnerService respawnerService = SatanicRespawner.getInstance().getRespawnerService();
            respawnerService.fakeKillAndRespawn(leftPlayer);
            KeyedMessage.of("raids-death-by-quit").withPlaceholders(Map.of("{player}", leftPlayer.getName())).broadcast();
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
