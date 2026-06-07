package org.mrdarkimc.raidsrecode.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.mrdarkimc.enhancedtextdisplays.EnhancedTextDisplays;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.displays.interfaces.DisplayHandler;
import org.mrdarkimc.raidsrecode.EventTimer;
import org.mrdarkimc.raidsrecode.manager.Undoable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//todo в EnhancedTextDisplays
public class HoloUpdater implements ChestUpdater.UndoableTimerMarker {
    private final Location loc;
    private final MiniTextDisplay holo;
    private final int maxDisplayTime;
    private boolean isHolosSpawned = false;
    private DisplayHandler displayHandler;

    public HoloUpdater(MiniTextDisplay display, Location loc, int maxDisplayTime) {
        this.loc = loc;
        this.holo = display;
        displayHandler = EnhancedTextDisplays.getInstance().getDisplayHandler();
        this.maxDisplayTime = maxDisplayTime;
        Bukkit.getLogger().info("Created HoloUpdater: ");
        Bukkit.getLogger().info(this.toString());
    }
//    public void work() {
//
//    }

//    public void startTask() {
//        this.handler.spawnDisplay(this.holo, this.loc);
//        super.startTask();
//    }
//
//    public void endTask() {
//        this.handler.removeDisplay(this.holo);
//        super.endTask();
//    }

    @Override
    public void nextSecound(EventTimer timer) {
        ensureHologramExists();
        String time = calculateLocalTime(timer);
        List<String> rawContents = new ArrayList<>(holo.getRawContents());
        rawContents.replaceAll(s -> s.replace("{time}", time));
        this.holo.applyText(rawContents);
    }


//    private void updateHologram(MiniTextDisplay holo, EventTimer eventTimer) {
//        List<String> rawContents = holo.getRawContents();
//        rawContents.replaceAll((s) -> s.replace("{time}", calculateLocalTime(eventTimer)));
//        holo.applyText(rawContents);
//    }

    private void ensureHologramExists() {
        if (isHolosSpawned) {
            return;
        }
        isHolosSpawned = true;

        displayHandler.spawnDisplay(holo, loc);

    }

    private String calculateLocalTime(EventTimer timer) {
        int current = timer.getCurrentTime();

        if (maxDisplayTime > 0) {
            int timeLeft = current % maxDisplayTime;

            if (timeLeft == 0 && current > 0) {
                return timer.getFormattedTime(maxDisplayTime);
            }

            return timer.getFormattedTime(timeLeft);
        }
        return timer.getFormattedTime(current);
    }

    @Override
    public void undo() {
        displayHandler.removeDisplay(holo);
    }

    @Override
    public String toString() {
        return "HoloUpdater{" +
                "loc=" + loc +
                ", holo=" + holo +
                ", maxDisplayTime=" + maxDisplayTime +
                ", isHolosSpawned=" + isHolosSpawned +
                ", displayHandler=" + displayHandler +
                '}';
    }
}
