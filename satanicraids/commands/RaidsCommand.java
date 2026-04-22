//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.mrdarkimc.satanicraids.commands;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mrdarkimc.satanicraids.SatanicRaids;
import org.mrdarkimc.satanicraids.hooks.WGSchemLoader;
import org.mrdarkimc.satanicraids.weapi.SimpleSchemPaster;
import org.mrdarkimc.satanicraids.weapi.WorldPaster;

public class RaidsCommand implements CommandExecutor {
    private final String lootTitle = "Raid Loot Editor";

    public RaidsCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eИспользование: /" + label + " <start|stop|addRespawn|addChest|loot|paste>");
            return true;
        } else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "start":
                    return this.handleStart(sender);
                case "stop":
                    return this.handleStop(sender);

                case "paste":
                    return this.handlePaste(sender);
                case "paste2":
                    return this.handlePaste2(sender);
                case "undo":
                    return this.forceUndo(sender);
                default:
                    sender.sendMessage("§cНеизвестная подкоманда.");
                    return true;
            }
        }
    }

    public boolean forceUndo(CommandSender sender) {
        SimpleSchemPaster.forceStopAll();
        //for (Disableable disableable : Disableable.disableables) {
        //    disableable.disable();
        //}
        return true;
    }

    WorldPaster paste = new SimpleSchemPaster(WGSchemLoader.clipboardMap.get("raidportal.schem"), 20);//1 min
    SimpleSchemPaster worldPaster = new SimpleSchemPaster(WGSchemLoader.clipboardMap.get("worldraid.schem"), 15);

    private boolean handlePaste2(CommandSender sender) {
        Player player = (Player) sender;
        worldPaster.pasteAsync((player.getLocation()));
        return true;
    }

    private boolean handlePaste(CommandSender sender) {


        //Map<String, Clipboard> clipboardMap = WGSchemLoader.clipboardMap;
        // WorldPaster paste = new WETimedSchemPaster(WGSchemLoader.clipboardMap.get("raidportal.schem"), 60);//1 min

        Player player = (Player) sender;
        worldPaster.paste(player.getLocation());
//WETimedSchemPaster schematicPasteAndRemove = new WETimedSchemPaster(value, 15);
        //schematicPasteAndRemove.paste(((Player)sender).getLocation());

        return true;
    }
    //private boolean handlePaste2(CommandSender sender){
    //    WorldPaster paste = new FaweNoAsyncSchemPaster(WGSchemLoader.clipboardMap.get("raidportal.schem"),30);//1 min
    //    Player player = (Player) sender;
    //    paste.paste(player.getLocation());
//
    //    return true;
    //}

    private boolean handleStart(CommandSender sender) {
        if (!this.requirePlayerOrOp(sender)) {
            return true;
        } else if (SatanicRaids.getInstance().getEventRunner() == null) {
            sender.sendMessage("§cСобытийный раннер не инициализирован.");
            return true;
        } else {
            SatanicRaids.getInstance().getEventRunner().startEvent();
            sender.sendMessage("§aСобытие запущено вручную.");
            return true;
        }
    }

    private boolean handleStop(CommandSender sender) {
        if (!this.requirePlayerOrOp(sender)) {
            return true;
        } else if (SatanicRaids.getInstance().getEventRunner() == null) {
            sender.sendMessage("§cСобытийный раннер не инициализирован.");
            return true;
        } else {
            SatanicRaids.getInstance().getEventRunner().stopEvent();
            sender.sendMessage("§eСобытие остановлено вручную.");
            return true;
        }
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
