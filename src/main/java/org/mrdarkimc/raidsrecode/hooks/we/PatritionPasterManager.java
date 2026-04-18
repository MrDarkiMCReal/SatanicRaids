package org.mrdarkimc.raidsrecode.hooks.we;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.hooks.we.pasters.WorldPaster;
import org.mrdarkimc.raidsrecode.hooks.we.pasters.WorldPasterImpl;

public class PatritionPasterManager  {
    private final Clipboard[] clipboards;
    private final int awaitTimeInSeconds;

    public PatritionPasterManager(int secondsBetweenPaste,Clipboard... clipboards) {
        this.awaitTimeInSeconds = secondsBetweenPaste;
        this.clipboards = clipboards;
    }


    /**
     * Метод !планирует! вставку всех схем.
     * @return Возвращает делей в секундах.<br>
     * После этого времени можно будет запустить обьявление о запуске эвента
     */
    public int pasteMultiple(Location loc) {
        int delay = 0;
        for (Clipboard clipboard : clipboards) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    WorldPaster schemPaster = new WorldPasterImpl(clipboard);
                    schemPaster.paste(loc);
                }
            }.runTaskLater(SatanicRaids.getInstance(), delay);
            delay = delay + awaitTimeInSeconds;
        }
        return delay;
    }
}
