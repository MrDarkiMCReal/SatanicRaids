package org.mrdarkimc.raidsrecode.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.raidsrecode.BossBarHandler;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.events.EventScheduler;
import org.mrdarkimc.raidsrecode.events.RaidScheduler;
import org.mrdarkimc.raidsrecode.events.RunnableEvent;

import java.util.*;

public class RaidsCommand implements CommandExecutor {
    private final String lootTitle = "Raid Loot Editor";

    public RaidsCommand(EventScheduler event) {
        this.scheduler = event;
    }

    final EventScheduler scheduler;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eИспользование: /" + label + " <start|stop|addRespawn|addChest|loot|paste>");
            requirePlayerOrOp(sender);
            return true;
        } else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "start":
                    return this.startStartEvent();
                case "startschedule":
                    return startSchedule();
                case "stopschedule":
                    return stopSchedule();
                case "stop":
                    return stopEvent();
                case "addrespawn":
                    return this.handleAddPoint(sender, "playerRespawns");
                case "addchest":
                    return this.handleAddPoint(sender, "chests");
                case "killbar":
                    return this.killbar();
                case "loot":
                    return this.handleLoot(sender);
//                    case "stop":
//                    return this.handleStop(sender);
//                case "addrespawn":
//                    return this.handleAddPoint(sender, "playerRespawns");
//                case "addchest":
//                    return this.handleAddPoint(sender, "chests");
//                case "loot":
//                    return this.handleLoot(sender);
//                case "paste":
//                    return this.handlePaste(sender);
//                case "paste2":
//                    return this.handlePaste2(sender);
//                case "undo":
//                    return this.forceUndo(sender);
                default:
                    sender.sendMessage("§cНеизвестная подкоманда.");
                    return true;
            }
        }
    }

    //    public boolean forceUndo(CommandSender sender){
//        SimpleSchemPaster.forceStopAll();
//        //for (Disableable disableable : Disableable.disableables) {
//        //    disableable.disable();
//        //}
//        return true;
//    }
//    WorldPaster paste = new SimpleSchemPaster(WGSchemLoader.clipboardMap.get("raidportal.schem"), 20);//1 min
//    SimpleSchemPaster worldPaster = new SimpleSchemPaster(WGSchemLoader.clipboardMap.get("worldraid.schem"), 15);
//    private boolean handlePaste2(CommandSender sender) {
//        Player player = (Player) sender;
//        worldPaster.pasteAsync((player.getLocation()));
//        return true;
//    }
    public boolean killbar(){
        NamespacedKey barKey = BossBarHandler.getInstance().barKey;
        KeyedBossBar bossBar = Bukkit.getBossBar(barKey);
        bossBar.removeAll();
        new Message(null,"killed all raids bossbars",null).sendToPlayersWithPermission("satanic.admin");
        return true;
    }
    private boolean handleAddPoint(CommandSender sender, String path) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cКоманда только для игрока.");
            return true;
        } else {
            Block target = player.getTargetBlockExact(10);
            if (target != null && target.getType() != Material.AIR) {
                Location l = target.getLocation();
                FileConfiguration cfg = SatanicRaids.getInstance().getConfig();
                List<Map<?, ?>> list = cfg.getMapList(path);
                List<Map<String, Object>> mutable = new ArrayList();
                Iterator var9 = list.iterator();

                while (var9.hasNext()) {
                    Map<?, ?> m = (Map) var9.next();
                    Map<String, Object> copy = new LinkedHashMap();
                    Iterator var12 = m.entrySet().iterator();

                    while (var12.hasNext()) {
                        Map.Entry<?, ?> e = (Map.Entry) var12.next();
                        copy.put(String.valueOf(e.getKey()), e.getValue());
                    }

                    mutable.add(copy);
                }

                Map<String, Object> entry = new LinkedHashMap();
                entry.put("x", l.getBlockX());
                entry.put("y", l.getBlockY());
                entry.put("z", l.getBlockZ());
                mutable.add(entry);
                cfg.set(path, mutable);
                SatanicRaids.getInstance().saveConfig();
                player.sendMessage("§aДобавлено в §e" + path + " §a: §f(" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")");
                return true;
            } else {
                player.sendMessage("§cПосмотрите на блок в пределах 10 блоков.");
                return true;
            }
        }
    }

    private boolean handleLoot(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cКоманда только для игрока.");
            return true;
        } else {
            Inventory inv = Bukkit.createInventory(new LootInventoryHolder(), 54, "Raid Loot Editor");
            player.openInventory(inv);
            player.sendMessage("§7Открылось меню редактирования лута. Перетащите предметы и закройте инвентарь.");
            return true;
        }
    }

    public boolean startSchedule() {
        ((RaidScheduler) scheduler).startSchedule();
        return false;
    }

    public boolean startStartEvent() {
        ((RaidScheduler) scheduler).spawnNextEvent();
        return true;
    }

    public boolean stopSchedule() {
        ((RaidScheduler) scheduler).stopSchedule();
        //runnableEvent.start();
        return false;
    }

    public boolean stopEvent() {
        ((RaidScheduler) scheduler).getCurrentRunningEvent().stop();
        return false;
    }

    public boolean callPrepare() {
        return true;
    }
//    private boolean handlePaste(CommandSender sender){
//
//
//        //Map<String, Clipboard> clipboardMap = WGSchemLoader.clipboardMap;
//        // WorldPaster paste = new WETimedSchemPaster(WGSchemLoader.clipboardMap.get("raidportal.schem"), 60);//1 min
//
//        Player player = (Player) sender;
//        worldPaster.paste(player.getLocation());
////WETimedSchemPaster schematicPasteAndRemove = new WETimedSchemPaster(value, 15);
//        //schematicPasteAndRemove.paste(((Player)sender).getLocation());
//
//        return true;
//    }
    //private boolean handlePaste2(CommandSender sender){
    //    WorldPaster paste = new FaweNoAsyncSchemPaster(WGSchemLoader.clipboardMap.get("raidportal.schem"),30);//1 min
    //    Player player = (Player) sender;
    //    paste.paste(player.getLocation());
//
    //    return true;
    //}
//    public boolean testPaste(CommandSender sender, String[] args){
//        if (!this.requirePlayerOrOp(sender)) {
//            return true;}
//        Player player = (Player) sender;
//        Location loc = player.getLocation();
//        if (args.length > 2){
//            loc = new Location(player.getWorld(),
//                    Integer.parseInt(args[1]),
//                    Integer.parseInt(args[2]),
//                    Integer.parseInt(args[3]));
//        }
//        Portal portalIn = SatanicRaids.getInstance().createPortal("portalIn");
//        ((RaidPortal)portalIn).setDuration(60);
//        Clipboard clipboard = WeSchemLoader.getClipboard("raidPortal");
//        WePaster paster = new WePasterImpl(clipboard, null);
//        paster.paste(loc);
//        new BukkitRunnable(){
//
//            @Override
//            public void run() {
//                paster.undo();
//            }
//        }.runTaskLater(SatanicRaids.getInstance(),20*20L);
//        return true;
//    }
//    private boolean handleStart(CommandSender sender) {
//        if (!this.requirePlayerOrOp(sender)) {
//            return true;}
//        event.start();
//        return true;
////        } else if (SatanicRaids.getInstance().getEventRunner() == null) {
////            sender.sendMessage("§cСобытийный раннер не инициализирован.");
////            return true;
////        } else {
////            SatanicRaids.getInstance().getEventRunner().startEvent();
////            sender.sendMessage("§aСобытие запущено вручную.");
////            return true;
////        }
//    }

    //    private boolean handleStop(CommandSender sender) {
//        if (!this.requirePlayerOrOp(sender)) {
//            return true;
//        } else if (SatanicRaids.getInstance().getEventRunner() == null) {
//            sender.sendMessage("§cСобытийный раннер не инициализирован.");
//            return true;
//        } else {
//            SatanicRaids.getInstance().getEventRunner().stopEvent();
//            sender.sendMessage("§eСобытие остановлено вручную.");
//            return true;
//        }
//    }
//
//    private boolean handleAddPoint(CommandSender sender, String path) {
//        if (!(sender instanceof Player player)) {
//            sender.sendMessage("§cКоманда только для игрока.");
//            return true;
//        } else {
//            Block target = player.getTargetBlockExact(10);
//            if (target != null && target.getType() != Material.AIR) {
//                Location l = target.getLocation();
//                FileConfiguration cfg = SatanicRaids.getInstance().getConfig();
//                List<Map<?, ?>> list = cfg.getMapList(path);
//                List<Map<String, Object>> mutable = new ArrayList();
//                Iterator var9 = list.iterator();
//
//                while(var9.hasNext()) {
//                    Map<?, ?> m = (Map)var9.next();
//                    Map<String, Object> copy = new LinkedHashMap();
//                    Iterator var12 = m.entrySet().iterator();
//
//                    while(var12.hasNext()) {
//                        Map.Entry<?, ?> e = (Map.Entry)var12.next();
//                        copy.put(String.valueOf(e.getKey()), e.getValue());
//                    }
//
//                    mutable.add(copy);
//                }
//
//                Map<String, Object> entry = new LinkedHashMap();
//                entry.put("x", l.getBlockX());
//                entry.put("y", l.getBlockY());
//                entry.put("z", l.getBlockZ());
//                mutable.add(entry);
//                cfg.set(path, mutable);
//                SatanicRaids.getInstance().saveConfig();
//                player.sendMessage("§aДобавлено в §e" + path + " §a: §f(" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")");
//                return true;
//            } else {
//                player.sendMessage("§cПосмотрите на блок в пределах 10 блоков.");
//                return true;
//            }
//        }
//    }
//
//    private boolean handleLoot(CommandSender sender) {
//        if (!(sender instanceof Player player)) {
//            sender.sendMessage("§cКоманда только для игрока.");
//            return true;
//        } else {
//            Inventory inv = Bukkit.createInventory(new LootInventoryHolder(), 54, "Raid Loot Editor");
//            player.openInventory(inv);
//            player.sendMessage("§7Открылось меню редактирования лута. Перетащите предметы и закройте инвентарь.");
//            return true;
//        }
//    }
//
    private boolean requirePlayerOrOp(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        } else if (!sender.isOp()) {
            sender.sendMessage("§cНедостаточно прав.");
            return false;
        } else {
            return true;
        }
    }
}

