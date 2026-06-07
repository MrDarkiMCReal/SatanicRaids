package org.mrdarkimc.raidsrecode.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.mrdarkimc.raidsrecode.EventDeserializer;

import java.util.function.Supplier;

public abstract class EventSupplier implements Supplier<RunnableEvent> {

    private String displayName;
    private String type;

    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }


    public abstract EventSupplier fromConfig(FileConfiguration config, EventDeserializer deserializer);
}
