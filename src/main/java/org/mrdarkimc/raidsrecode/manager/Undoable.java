package org.mrdarkimc.raidsrecode.manager;

import java.util.Collection;

@FunctionalInterface
public interface Undoable {
    public abstract void undo();

    public static void undoEach(Collection<?> collection) {
        collection.stream()
                .filter(e -> e instanceof Undoable)
                .map(e -> (Undoable) e)
                .forEach(Undoable::undo);
    }
}
