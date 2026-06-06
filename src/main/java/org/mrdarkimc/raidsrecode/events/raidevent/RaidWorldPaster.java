package org.mrdarkimc.raidsrecode.events.raidevent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.session.PasteBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.mrdarkimc.SatanicLib.worldedit.WeSchemLoader;
import org.mrdarkimc.SatanicLib.worldedit.pasters.PartitionWePaster;

import java.util.function.Consumer;

public class RaidWorldPaster extends PartitionWePaster {
    //todo maybe new WeSchemLoader ????
    //а внутри WePaster -> loader.load();
    protected RaidWorldPaster(JavaPlugin plugin, WeSchemLoader loader, String name, int chunkSize, long delay, @Nullable Consumer<PasteBuilder> pasteConfig) {
        super(plugin, loader, name, chunkSize, delay, (c) -> c.ignoreAirBlocks(false), (s) -> s.setFastMode(true));
    }

    @Override
    protected void saveForUndo(EditSession editSession) {
        editSession.flushQueue();
    }

    public static class RaidWorldPasterBuilder extends PartitionPasterBuilder {

        @Override
        public PartitionWePaster build() {
            if (this.plugin != null && this.name != null) {
                return new RaidWorldPaster(this.plugin, loader, this.name, this.chunkSize, this.delay, this.pasteConfig);
            } else {
                throw new IllegalStateException("Plugin and Name are required");
            }
        }
    }
}
