package org.mrdarkimc.raidsrecode.api;

import java.util.function.Supplier;

public class EventSupplier implements Supplier<RunnableEvent> {
    Supplier<RunnableEvent> eventSupplier;

    @Override
    public RunnableEvent get() {
        return eventSupplier.get();
    }
}
