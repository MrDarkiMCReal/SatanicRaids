package org.mrdarkimc.satanicraids.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * todo вынести в либу
 */
public interface Disableable {
    public static final List<Disableable> disableables = new ArrayList<>();

    public static void register(Disableable instance) {
        disableables.add(instance);
    }

    default void disableAll() {
        foreachdisable();
    }

    private void foreachdisable() {
        for (Disableable disableable : disableables) {
            disableable.disable();
        }
    }

    public void disable();
}
