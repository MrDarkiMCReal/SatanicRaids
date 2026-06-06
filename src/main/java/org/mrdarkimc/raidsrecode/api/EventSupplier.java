package org.mrdarkimc.raidsrecode.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.mrdarkimc.raidsrecode.EventDeserializer;

import java.util.function.Supplier;

public abstract class EventSupplier implements Supplier<RunnableEvent> {

    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public abstract EventSupplier fromConfig(FileConfiguration config, EventDeserializer deserializer);
}
