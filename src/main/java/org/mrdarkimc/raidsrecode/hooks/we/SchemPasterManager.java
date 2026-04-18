package org.mrdarkimc.raidsrecode.hooks.we;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.TaskHelper;
import org.mrdarkimc.raidsrecode.hooks.we.pasters.TimedWorldPaster;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SchemPasterManager implements PasterManager {
    protected final Map<UUID, EditSession> placedObjects = new ConcurrentHashMap<>();
    protected final List<BukkitTask> tasks = new ArrayList<>();
    @Deprecated
    public void paste(Clipboard clipboard, Location loc, int time) {
        UUID uuid = UUID.randomUUID();
        TimedWorldPaster schemPaster = new TimedWorldPaster(clipboard);
        Optional<EditSession> optionalEditSession = schemPaster.paste(loc);
        optionalEditSession.ifPresent(result -> scheduleUndoTask(uuid,result, time));

    }
protected void scheduleUndoTask(UUID uuid, EditSession result, int time){
    placedObjects.put(uuid, result);
    BukkitTask bukkitTask = new BukkitRunnable() {

        @Override
        public void run() {
            undo(uuid);
        }
    }.runTaskLater(SatanicRaids.getInstance(), time * 20L);
    tasks.add(bukkitTask);
}
    public void undo(UUID uuid) {
        EditSession session = placedObjects.remove(uuid);
        if (session != null) {
            EditSession newEditSession = WorldEdit.getInstance().newEditSession(session.getWorld());
            session.undo(newEditSession);
            session.close();
        }
    }

    public void undoAll() {
        tasks.forEach(TaskHelper::cancelTask);
        placedObjects.keySet().forEach(this::undo);
    }
}
