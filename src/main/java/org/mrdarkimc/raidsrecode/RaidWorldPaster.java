package org.mrdarkimc.raidsrecode;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.mrdarkimc.SatanicLib.worldedit.pasters.PartitionWePaster;

import java.util.function.Consumer;

public class RaidWorldPaster extends PartitionWePaster {
    protected RaidWorldPaster(JavaPlugin plugin, String name, int chunkSize, long delay, @Nullable Consumer<PasteBuilder> pasteConfig) {
        super(plugin, name, chunkSize, delay, (c) -> c.ignoreAirBlocks(false), (s) -> s.setFastMode(true));
    }

    @Override
    protected void saveForUndo(EditSession editSession) {
        editSession.flushQueue();
    }

    public static class RaidWorldPasterBuilder extends PartitionPasterBuilder {

        @Override
        public PartitionWePaster build() {
            if (this.plugin != null && this.name != null) {
                return new RaidWorldPaster(this.plugin, this.name, this.chunkSize, this.delay, this.pasteConfig);
            } else {
                throw new IllegalStateException("Plugin and Name are required");
            }
        }
    }
}
