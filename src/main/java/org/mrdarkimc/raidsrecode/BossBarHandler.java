package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mrdarkimc.SatanicLib.ConfigAPI.Config;
import org.mrdarkimc.SatanicLib.Utils;
import org.mrdarkimc.raidsrecode.listeners.BossbarListener;


//кринжуха но надо сделать быстро
public class BossBarHandler {
    public static NamespacedKey barKey = new NamespacedKey(SatanicRaids.getInstance(), "raidworld_bar");;
    private BossBar masterBossbar;
    private static BossBarHandler instance;
    private EventTimer timer;


    public BossBarHandler(EventTimer timer) {
        instance = this;
        this.timer = timer;
    }

    public static BossBarHandler getInstance() {
        return instance;
    }

    public BossBar createBossbar(Location loc) {
        Config mainConfig = SatanicRaids.getInstance().getMainConfig();
        FileConfiguration config = mainConfig.get();
        String bossbarLine = config.getString("bossbar");
        bossbarLine = Utils.hexAndPAPI(bossbarLine, null);
        bossbarLine = bossbarLine.replace("{x}", String.valueOf((int) loc.getX()));
        bossbarLine = bossbarLine.replace("{y}", String.valueOf((int) loc.getY()));
        bossbarLine = bossbarLine.replace("{z}", String.valueOf((int) loc.getZ()));
        return Bukkit.createBossBar(bossbarLine, BarColor.YELLOW, BarStyle.SOLID);
    }
    public void updateText(Location loc){
        Config mainConfig = SatanicRaids.getInstance().getMainConfig();
        FileConfiguration config = mainConfig.get();
        String bossbarLine = config.getString("bossbar");
        bossbarLine = Utils.hexAndPAPI(bossbarLine, null);
        bossbarLine = bossbarLine.replace("{x}", String.valueOf((int) loc.getX()));
        bossbarLine = bossbarLine.replace("{y}", String.valueOf((int) loc.getY()));
        bossbarLine = bossbarLine.replace("{z}", String.valueOf((int) loc.getZ()));
        bossbarLine = bossbarLine.replace("%time%",timer.getFormattedTime());
        masterBossbar.setTitle(bossbarLine);
    }

    public void setEnabled(Location loc) {
        this.masterBossbar = createBossbar(loc);
        BossbarListener.getInstance().isEventRunning = true;
    }

    public void setDisabled() {
        Bukkit.removeBossBar(barKey);
        BossbarListener.getInstance().isEventRunning = false;
    }

    public void addEventBossbar(Player player) {
        if (masterBossbar != null) {
            masterBossbar.addPlayer(player);
        }
    }

    public void removeEventBossbar(Player player) {
        if (masterBossbar != null) {
            masterBossbar.removePlayer(player);
        }
    }

    public void addToAllPlayers() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            masterBossbar.addPlayer(onlinePlayer);
        }
    }

    public void removeFromAllPlayers() {
        masterBossbar.removeAll();
    }
}
