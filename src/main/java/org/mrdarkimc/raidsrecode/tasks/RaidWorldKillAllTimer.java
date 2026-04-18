package org.mrdarkimc.raidsrecode.tasks;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.MessageInterface;
import org.mrdarkimc.SatanicRespawner.SatanicRespawner;
import org.mrdarkimc.SatanicRespawner.services.RespawnerService;
import org.mrdarkimc.raidsrecode.SatanicRaids;

import java.util.Map;
import java.util.function.Consumer;


public class RaidWorldKillAllTimer extends BukkitRunnable {

    private final World eventWorld;
    private final RespawnerService killService;
    private final MessageInterface message;
    private Consumer<Void> consumer;
    private int timeToDeath;
    private BukkitTask thisTask;


    public RaidWorldKillAllTimer(World raidWorld, int timeToDeath) {
        this.eventWorld = raidWorld;
        this.timeToDeath = timeToDeath;
        this.message = new KeyedMessage(null, "messages.time-to-death", Map.of("{time}", String.valueOf(timeToDeath)));
        this.killService = SatanicRespawner.getInstance().getRespawnerService();
    }

    @Override
    public void run() {
        if (timeToDeath <= 0) {
            killAllPlayers();
            cancelAndConsume();
            return;
        }
        if (timeToDeath % 10 == 0 || timeToDeath <= 5) {
            eventWorld.getPlayers().forEach(e ->
                    new KeyedMessage(e, "messages.time-to-death", Map.of("{time}", String.valueOf(timeToDeath))).send()
            );
        }
        timeToDeath--;
    }

    private void cancelAndConsume() {
        consumer.accept(null);
        this.thisTask.cancel();
    }

    public void runTaskAndThen(Consumer<Void> consumer) {
        this.consumer = consumer;
        this.thisTask = this.runTaskTimer(SatanicRaids.getInstance(), 0, 20);
    }

    private void killAllPlayers() {
        World raidWorld = eventWorld;
        if (raidWorld != null) {
            for (Player player : raidWorld.getPlayers()) {
                if (player.getWorld().getName().equals(raidWorld.getName())) {
                    killService.fakeKillAndRespawn(player);
                }
            }
        }

    }
}
