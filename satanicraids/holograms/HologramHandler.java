package org.mrdarkimc.satanicraids.holograms;

import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;

import java.util.ArrayList;
import java.util.List;
@Deprecated
public class HologramHandler {
    private final int counterNominal;
    private int current;
    private List<MiniTextDisplay> holograms = new ArrayList<>();

    public HologramHandler(int counter) {
        this.counterNominal = counter;
        this.current = counter;
    }

    public void add(MiniTextDisplay hologram) {
        System.out.println("-------------------------");
        System.out.println("Добавляю голограмму " + hologram.getDisplay().getLocation());
        System.out.println("Добавляю голограмму " + hologram.getRawContents());
        System.out.println("-------------------------");
        holograms.add(hologram);
    }

    public void updateAll() {
        for (MiniTextDisplay hologram : holograms) {
            String name = hologram.getDisplay().getLocation().getWorld().getName();
            //System.out.println("Updated in world: "+ name);
            System.out.println("Updating holo: " + hologram.getDisplay().getLocation());
            System.out.println("is Chunk loaded?: " + hologram.getDisplay().getLocation().getChunk().isLoaded());
            System.out.println(" ");
            List<String> rawContents = hologram.getRawContents();
            rawContents.replaceAll(s -> s.replace("{time}", String.valueOf(current)));
            hologram.applyText(rawContents);
        }
        current--;
        if (current <= 0) {
            current = counterNominal;
        }
    }
    public void removeAll() {
        for (MiniTextDisplay hologram : holograms) {
            hologram.getDisplay().remove();
        }
        holograms.clear();
    }

}
