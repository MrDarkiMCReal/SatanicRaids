package org.mrdarkimc.raidsrecode;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.ConfigAPI.Config;
import org.mrdarkimc.SatanicLib.ConfigAPI.MessageLoader;
import org.mrdarkimc.SatanicLib.ConfigAPI.MessagesConfig;
import org.mrdarkimc.SatanicLib.Utils;
import org.mrdarkimc.SatanicLib.currency.PlayerPoints;
import org.mrdarkimc.SatanicLib.currency.Vault;
import org.mrdarkimc.SatanicLib.currency.interfaces.Currency;
import org.mrdarkimc.SatanicLib.worldedit.WeSchemLoader;
import org.mrdarkimc.raidsrecode.api.EventScheduler;
import org.mrdarkimc.raidsrecode.api.SchedulerImpl;
import org.mrdarkimc.raidsrecode.commands.RaidsCommand;
import org.mrdarkimc.raidsrecode.api.RunnableEvent;
import org.mrdarkimc.raidsrecode.listeners.BossbarListener;
import org.mrdarkimc.raidsrecode.listeners.LootSaveListener;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class SatanicRaids extends JavaPlugin {
    private static SatanicRaids instance;
    private Config mainConfig;
    private Config lootsConfig;

    public static SatanicRaids getInstance() {
        return instance;
    }

    private Config holograms;

    public Config getHologramConfig() {
        return holograms;
    }

    public Config getLootsConfig() {
        return lootsConfig;
    }

    private Currency hellic;
    private Currency dollar;
    private EventScheduler scheduler;
    private WeSchemLoader schemLoader;

    public Currency getHellic() {
        return hellic;
    }

    public Currency getDollar() {
        return dollar;
    }

    @Override
    public void onEnable() {
        instance = this;

        Utils.startUp("SatanicRaids");

        this.schemLoader = new WeSchemLoader(this);

        this.mainConfig = new Config(this, "config");
        this.holograms = new Config(this, "holograms");
        this.lootsConfig = new Config(this, "loots");

        MessageLoader messageLoader = new MessageLoader(this);
        messageLoader.loadAllLocales();

        this.dollar = new Vault();
        this.hellic = new PlayerPoints();

        this.schemLoader.loadSchematicsToCache();

        this.scheduler = createScheduler();
        this.scheduler.startSchedule();
        getCommand("raids").setExecutor(new RaidsCommand(scheduler));
    }

    private void reloadAllConfigs() {
        mainConfig.reloadConfig();
        holograms.reloadConfig();
        lootsConfig.reloadConfig();
    }

    private EventScheduler createScheduler() {
        final EventDeserializer eventDeserializer = new EventDeserializer(this, schemLoader, mainConfig, holograms);
        final List<Supplier<RunnableEvent>> events = eventDeserializer.allEvents();

        final BossbarListener bossbarListener = new BossbarListener();
        getServer().getPluginManager().registerEvents(bossbarListener, this);
        getServer().getPluginManager().registerEvents(new LootSaveListener(), this);

        ConfigurationSection eventGlobal = mainConfig.get().getConfigurationSection("event");
        long scheduleIntervalSec = eventGlobal != null ? eventGlobal.getLong("interval", 3600L) : 3600L;
        return new SchedulerImpl(events, scheduleIntervalSec);
    }

    public Config getMainConfig() {
        return mainConfig;
    }

}
