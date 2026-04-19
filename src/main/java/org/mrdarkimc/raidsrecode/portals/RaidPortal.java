package org.mrdarkimc.raidsrecode.portals;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePaster;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.tasks.UpdateHoloTask;
import org.mrdarkimc.raidsrecode.TaskHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RaidPortal implements Portal {
    private final JavaPlugin plugin;
    private final WePaster paster;
    private final MiniTextDisplay portalTemplate;
    private final Random random = new Random();
    private final Offset hologramSpawnOffset;
    private final Offset portalEnteranceOffset;
    private final int portalEnteranceRadius = 3;
    private Location portalTeleportArea;
    @Deprecated //todo уйти от lifetime в классиках
    private int lifeTime = -1;
    private List<Location> destinationPoints;
    private UpdateHoloTask updateHoloTask;

    public BukkitTask undoTask;
    private Consumer<Player> afterTeleportation = player -> {};
    private Consumer<Player> beforeTeleportation = player -> {};
    private Predicate<Player> enterencePredicate = Objects::nonNull;
    private boolean isUndone = false;

    public RaidPortal(JavaPlugin plugin, WePaster paster, MiniTextDisplay portalTemplate, Offset holoOffset, Offset entenanceOffset) {
        this.plugin = plugin;
        this.portalTemplate = portalTemplate;
        this.paster = paster;
        this.hologramSpawnOffset = holoOffset;
        this.portalEnteranceOffset = entenanceOffset;
    }

    //спавн портала
    public void spawn(Location location) {

//        if (alreadyInitialized){
//            throw new RuntimeException();
//        }
        if (lifeTime <= 0) {
            throw new RuntimeException("0 as lifetime");
        }
        Location holoLocation = adjustHologramLocation(location);
        this.portalTeleportArea = adjustEnteranceLocation(location);
        updateHoloTask = new UpdateHoloTask(portalTemplate, holoLocation, lifeTime);

        //register();
        paster.paste(location);
        updateHoloTask.startTask();
        this.undoTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isUndone) {
                    Bukkit.getLogger().info("Portal is already undone. Do nothing");
                    return;
                }
                undo();
            }
        }.runTaskLater(plugin, lifeTime * 20L);
    }

    @Override
    public void undo() {
        //todo трекать состояние
        isUndone = true;
//        if (undone) {return;} // Если уже отменили, выходим
//        undone = true;
        TaskHelper.cancelTask(undoTask);
        if (updateHoloTask != null) {
            updateHoloTask.endTask();
        }
        //unregister();
        paster.undo();
    }

    @Override
    public void afterTeleportation(Consumer<Player> onEnter) {
        this.afterTeleportation = afterTeleportation.andThen(onEnter);
    }

    public void setDuration(int duration) {
        this.lifeTime = duration;
    }

    @Override
    public boolean enter(Player player) {
        if (!enterencePredicate.test(player)){
            return false;
        }
        int size = destinationPoints.size();
        int i = random.nextInt(0, size);
        Location loc = destinationPoints.get(i);
        player.teleport(loc);
        if (afterTeleportation != null) {
            afterTeleportation.accept(player);
        }
        return true;
    }

    public void addDestinationPoint(Location loc) {
        if (destinationPoints == null) {
            destinationPoints = new ArrayList<>();
        }
        this.destinationPoints.add(loc);
    }

    public void setDestinationPoints(List<Location> locations) {
        this.destinationPoints = locations;
    }

    @Override
    public void setTeleportRequirements(Predicate<Player> requirements) {
        this.enterencePredicate = enterencePredicate.and(requirements);
    }

//    public void register() {
//        //throw new RuntimeException("not implemented yet");
//    }
//
//    public void unregister() {
//        //throw new RuntimeException("not implemented yet");
//    }

    private Location adjustHologramLocation(Location location) {
        Location clone = location.clone();
        clone.add(hologramSpawnOffset.toVector());
        return clone;
    }

    private Location adjustEnteranceLocation(Location location) {
        Location clone = location.clone();
        clone.add(portalEnteranceOffset.toVector());
        return clone;
    }

    @Override
    public boolean isEnteringPortal(Player player) {
        if (!portalTeleportArea.getWorld().equals(player.getWorld())) {
            return false;
        }
        double distance = player.getLocation().distance(portalTeleportArea) - 1; //-1 т.к игрок стоит на блоке
        return distance <= portalEnteranceRadius;
    }

    @Override
    public boolean isAllowedToEnterPortal(Player player) {
        return false;
    }


}
