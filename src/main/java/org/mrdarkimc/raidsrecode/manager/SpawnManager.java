package org.mrdarkimc.raidsrecode.manager;

//todo next iteration
public class SpawnManager {
    public <T extends Spawnable & Undoable> void scheduleSpawnable(T object) {
        object.spawn(null);
        object.undo();
    }
}
