package org.mrdarkimc.satanicraids.portal2;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mrdarkimc.raidsrecode.hooks.we.SchemPasterManager;

public class DoubleChannelPortal implements Portal { //todo ref to SchemPortal
    private final PortalStructure enterPortal;
    private final PortalStructure exitPortal;
    private final int radius;
    private SchemPasterManager paster = new SchemPasterManager();
    private final int activeTime;

    public DoubleChannelPortal(PortalStructure enterPortal, PortalStructure exitPortal, int radius, int activeTime) {
        this.enterPortal = enterPortal;
        this.exitPortal = exitPortal;
        this.radius = radius;
        this.activeTime = activeTime;
    }

    public void enter(Player player) {
        Location loc = findSpawnLocation();
        player.teleport(loc);
    }

    public void exit(Player player) {
        Location center = exitPortal.getPortalCenterLocation();
        Location loc = calculateLocationInRadius(60, center);
        player.teleport(loc);
    }

    private Location calculateLocationInRadius(int radius, Location from) {
        return null; //todo
    }

    public void create() {
        paste(enterPortal);
        paste(exitPortal);
    }

    private void paste(PortalStructure structure) {
        paster.paste(structure.getClipboard(), structure.getPasteLocation(), activeTime);
    }

    private void createHologram() {

    }

    private void removeHologram() {

    }

    private void startUpdatingTask() {

    }

    private void stopUpdatingTask() {

    }

    private Location findSpawnLocation() {
        return null;//todo
    }

    public boolean teleportPlayerIfTouchingPortal(Player player) {
        if (checkTouchingPortal(enterPortal, player)) {
            enter(player);
            return true;
        }
        if (checkTouchingPortal(exitPortal, player)) {
            exit(player);
            return true;
        }
        return false;
    }

    private boolean checkTouchingPortal(PortalStructure structure, Player player) {
        Location center = structure.getPortalCenterLocation();
        double distance = player.getLocation().distance(center) - 1; //-1 т.к игрок стоит на блоке
        return distance <= radius;
    }


}