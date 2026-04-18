package org.mrdarkimc.raidsrecode.hooks.we.pasters;

import com.sk89q.worldedit.EditSession;
import org.bukkit.Location;

import java.awt.datatransfer.Clipboard;
import java.util.Optional;
@Deprecated
public interface WorldPaster {
    public Optional<EditSession> paste(Location loc);
}
