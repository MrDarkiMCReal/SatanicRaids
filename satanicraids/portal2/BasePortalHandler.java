package org.mrdarkimc.satanicraids.portal2;

import org.mrdarkimc.satanicraids.portals.Portal;

import java.util.List;

public class BasePortalHandler {
    private List<Portal> activePortals;
    public void registerPortal(Portal portal){
        activePortals.add(portal);
    }
}