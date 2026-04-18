package org.mrdarkimc.raidsrecode.portals;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.SatanicLib.worldedit.WeSchemLoader;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePaster;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePasterImpl;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.tasks.UpdateHoloTask;
import org.mrdarkimc.raidsrecode.SatanicRaids;

//Создает портал
@Deprecated
public class PortalStructure {
    private final JavaPlugin plugin;
    private final WePaster paster;
    private final Location portalSpawnLocation;
    private final int lifeTime;
    private final int hologramOffset = 10;
    private Offset offset;

    public PortalStructure(String portalClipboardName, MiniTextDisplay portalTemplate, Location portalSpawnLocation, int lifetime, Offset offset) {

        Clipboard clipboard = WeSchemLoader.getClipboard(portalClipboardName);
        this.plugin = SatanicRaids.getInstance();

        this.paster = new WePasterImpl(clipboard, null);
        this.portalSpawnLocation = portalSpawnLocation;
        this.lifeTime = lifetime;
        this.offset = offset;
    }

    public void spawn() {
        paster.paste(portalSpawnLocation);
        new BukkitRunnable() {
            @Override
            public void run() {
                paster.undo();
            }
        }.runTaskLater(plugin, lifeTime);
    }


    public Location getTeleportArea(){
        return portalSpawnLocation.clone().add(offset.toVector());
    }
}
