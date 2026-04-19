package org.mrdarkimc.raidsrecode.tasks;

import org.bukkit.Location;
import org.mrdarkimc.enhancedtextdisplays.EnhancedTextDisplays;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.displays.interfaces.DisplayHandler;
import org.mrdarkimc.raidsrecode.EventTimer;
import org.mrdarkimc.raidsrecode.manager.Undoable;

import java.util.ArrayList;
import java.util.List;

//todo в EnhancedTextDisplays
public class HoloUpdater implements EventTimer.TimerTask {
    Location loc;
    MiniTextDisplay holo;
    DisplayHandler handler;
    int displayTimer;
    boolean isHolosSpawned = false;
    /** Неизменяемый снимок строк с плейсхолдером {time}; getRawContents() нельзя мутировать — иначе {time} пропадает после первого тика. */
    private final List<String> templateLinesWithTimePlaceholder;

    public HoloUpdater(MiniTextDisplay display, Location loc, int displayTimer) {
        this.loc = loc;
        this.holo = display;
        this.handler = EnhancedTextDisplays.getInstance().getDisplayHandler();
        this.displayTimer = displayTimer;
        List<String> raw = display.getRawContents();
        List<String> snap = new ArrayList<>(raw.size());
        for (String line : raw) {
            snap.add(line);
        }
        this.templateLinesWithTimePlaceholder = snap;
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
        List<String> lines = new ArrayList<>(templateLinesWithTimePlaceholder.size());
        for (String templateLine : templateLinesWithTimePlaceholder) {
            lines.add(templateLine.replace("{time}", time));
        }
        this.holo.applyText(lines);
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
        DisplayHandler displayHandler = EnhancedTextDisplays.getInstance().getDisplayHandler();
        displayHandler.spawnDisplay(holo, loc);

    }

    private String calculateLocalTime(EventTimer timer) {
        int secondsPassedInCurrentCycle = timer.getCurrentTime() % displayTimer;
        int timeLeft = displayTimer - secondsPassedInCurrentCycle;
        return timer.getFormattedTime(timeLeft);
    }
}
