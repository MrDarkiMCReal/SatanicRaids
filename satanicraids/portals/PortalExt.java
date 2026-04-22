package org.mrdarkimc.satanicraids.portals;


public class PortalExt {
    private final Portal from;
    private final Portal to;

    public PortalExt(Portal from, Portal to) {
        this.from = from;
        this.to = to;
    }

    public Portal getFrom() {
        return from;
    }

    public Portal getTo() {
        return to;
    }
}
