package org.mrdarkimc.satanicraids.weapi;


import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;

@Deprecated
public interface WorldPaster {
    public void paste(Location loc);
    public CompletableFuture<Void> pasteAsync(Location loc);

}