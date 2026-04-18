package org.mrdarkimc.raidsrecode.hooks.we.pasters;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;

import java.util.Optional;
@Deprecated
public class WorldPasterImpl implements WorldPaster {
    private final Clipboard clipboard;

    public WorldPasterImpl(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * refactor to abstractPaster. Сделать fluent api настроек
     */
    public Optional<EditSession> paste(Location loc) {
        World adaptedWorld = BukkitAdapter.adapt(loc.getWorld());
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld);) {


            BlockVector3 pasteLocation = BlockVector3.at(
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ()
            );

            Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession)
                    .to(pasteLocation)
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);
            return Optional.empty();
        }
    }
}
