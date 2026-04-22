package org.mrdarkimc.raidsrecode.tasks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.SatanicLib.Utils;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.MessageInterface;
import org.mrdarkimc.SatanicRespawner.SatanicRespawner;
import org.mrdarkimc.SatanicRespawner.services.RespawnerService;
import org.mrdarkimc.raidsrecode.SatanicRaids;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class RaidWorldKillAllTimer extends BukkitRunnable {

    private final World eventWorld;
    private final RespawnerService killService;
    private final MessageInterface message;
    private Consumer<Void> consumer;
    private int timeToDeath;
    private final int initialTimeToDeath;
    private BukkitTask thisTask;


    public RaidWorldKillAllTimer(World raidWorld, int timeToDeath) {
        this.eventWorld = raidWorld;
        this.initialTimeToDeath = timeToDeath;
        this.timeToDeath = timeToDeath;
        this.message = new KeyedMessage(null, "messages.time-to-death", Map.of("{time}", String.valueOf(timeToDeath)));
        this.killService = SatanicRespawner.getInstance().getRespawnerService();
    }

    @Override
    public void run() {
        if ((initialTimeToDeath/3) == timeToDeath) {
            startKillingPlayersTask();
        }
        if (timeToDeath <= 0) {
            cancelAndConsume();
        }
        if (timeToDeath % 10 == 0 || timeToDeath <= 5) {
            eventWorld.getPlayers().forEach(e ->
                    new KeyedMessage(e, "messages.time-to-death", Map.of("{time}", String.valueOf(timeToDeath))).send()
            );
        }

        timeToDeath--;
    }

    private void cancelAndConsume() {
        if (consumer != null) {
            consumer.accept(null);
        }
        this.thisTask.cancel();
    }

    public void runTaskAndThen(Consumer<Void> consumer) {
        this.consumer = consumer;
        this.thisTask = this.runTaskTimer(SatanicRaids.getInstance(), 0, 20);
    }

    private void startKillingPlayersTask() {
        World raidWorld = eventWorld;
        if (raidWorld == null) {
            Bukkit.getLogger().warning("Unable to kill all players. RaidWorld is null");
            return;
        }
        List<Player> players = raidWorld.getPlayers();
        for (Player player : players) {
            player.sendTitle(" ", Utils.hexAndPAPI("%design_clr_main%Спасайся через портал или умри", player), 10, initialTimeToDeath/3 * 20, 20);
        }
        new BukkitRunnable() {
            double damage = 1;

            @Override
            public void run() {
                List<Player> players = raidWorld.getPlayers();
                if (players.isEmpty()) {
                    cancel();
                }
                if (damage >= 687194767) {
                    cancel();
                }
                for (Player player : players) {
                    if (player.getWorld().getName().equals(raidWorld.getName())) {
                        player.damage(damage);
                        Bukkit.getLogger().info("[RaidKiller] Dealing " + damage + " to a: " + player.getName());
                        player.getWorld().strikeLightning(player.getLocation());
                    }
                }
                damage = damage * 2;
            }
        }.runTaskTimer(SatanicRaids.getInstance(), 40, 40);
    }
}
