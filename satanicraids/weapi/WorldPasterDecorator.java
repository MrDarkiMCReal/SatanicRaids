package org.mrdarkimc.satanicraids.weapi;

import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;
@Deprecated
public class WorldPasterDecorator implements WorldPaster{
    private WorldPaster paster;

    public WorldPasterDecorator(WorldPaster paster) {
        this.paster = paster;
    }

    @Override
    public void paste(Location loc) {
        paster.paste(loc);
    }

    @Override
    public CompletableFuture<Void> pasteAsync(Location loc) {
        return null;
    }
}
