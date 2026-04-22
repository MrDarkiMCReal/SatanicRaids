package org.mrdarkimc.satanicraids.portal2;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;
import org.mrdarkimc.satanicraids.portals.PasteOffset;
import org.mrdarkimc.satanicraids.weapi.WorldPaster;

public class PortalStructure {
    private final Location portalCenterLocation;
    private final PasteOffset schemPasteOffset; //todo refactor this. we dont need additional classes
    private final Clipboard clipboard;

    public PortalStructure(Clipboard clipboard, Location location, PasteOffset pasteOffset) {
        this.portalCenterLocation = location.clone();
        this.schemPasteOffset = pasteOffset;
        this.clipboard = clipboard;

    }
    public Location getPortalCenterLocation(){
        return portalCenterLocation.clone();
    }
    public Location getPasteLocation(){
        return portalCenterLocation.clone().add(schemPasteOffset.toVector());
    }
    public Clipboard getClipboard(){
        return clipboard;
    }
}