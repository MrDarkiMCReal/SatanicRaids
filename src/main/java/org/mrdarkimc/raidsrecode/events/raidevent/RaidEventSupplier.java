package org.mrdarkimc.raidsrecode.events.raidevent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.raidsrecode.EventDeserializer;
import org.mrdarkimc.raidsrecode.api.EventSupplier;
import org.mrdarkimc.raidsrecode.api.RunnableEvent;
import org.mrdarkimc.raidsrecode.portals.Portal;

import java.util.List;

public class RaidEventSupplier extends EventSupplier {

    private JavaPlugin plugin;
    private EventDeserializer.PasteData pasteData;
    private List<Location> spawnPoints;
    private Portal raidIn;
    private Portal raidOut;
    private int duration;

    public RaidEventSupplier() {}

    @Override
    public EventSupplier fromConfig(FileConfiguration config, EventDeserializer deserializer) {
        ConfigurationSection eventSec = config.getConfigurationSection("event");
        if (eventSec == null) throw new IllegalArgumentException("Секция 'event' не найдена в конфиге рейд-эвента");

        RaidEventSupplier supplier = new RaidEventSupplier();
        supplier.plugin = deserializer.getPlugin();
        supplier.setDisplayName(eventSec.getString("display-name", "Рейдовый мир"));
        supplier.setType(eventSec.getString("type","EventTypeNull"));
        supplier.duration = eventSec.getInt("duration", 3600);
        supplier.pasteData = deserializer.getPasteData(eventSec);
        supplier.raidIn = deserializer.getPortal("raidInPortal");
        supplier.raidOut = deserializer.getPortal("raidOutPortal");

        String worldName = eventSec.getString("paste.pasteLocation.world", "raidWorld");
        World world = Bukkit.getWorld(worldName);
        supplier.spawnPoints = deserializer.loadSafeLocations(world);

        return supplier;
    }

    @Override
    public RunnableEvent get() {
        return new RaidEvent(plugin, pasteData, spawnPoints, raidIn, raidOut, duration);
    }
}
