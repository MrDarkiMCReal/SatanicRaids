package org.mrdarkimc.satanicraids;

import com.sk89q.worldedit.extent.clipboard.Clipboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.mrdarkimc.satanicraids.hooks.WEPaster;
import org.mrdarkimc.satanicraids.hooks.WGSchemLoader;

public class SchemPasterImpl implements SchemPaster {
    private final String schemName;
    private WEPaster paster;

    public SchemPasterImpl(String schemName) {
        this.schemName = schemName;
    }

    @Override
    public void paste(Location loc) {
        if (paster == null) {
            Clipboard clipboard = WGSchemLoader.clipboardMap.get(schemName);
            if (clipboard == null) {
                Bukkit.getLogger().warning("[TimedWorldPaster] Схема " + schemName + " не найдена в кэше!");
                return;
            }
            paster = new WEPaster(clipboard, 120);
        }
        paster.pasteAndRemove(loc);

    }
}


