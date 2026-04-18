package org.mrdarkimc.raidsrecode.hooks.we;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;

import java.util.UUID;

public interface PasterManager {
    public abstract void paste(Clipboard clipboard, Location loc, int time);
    public void undo(UUID uuid);
    public void undoAll();
}
