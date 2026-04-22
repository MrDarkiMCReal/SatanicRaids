package org.mrdarkimc.satanicraids.weapi;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.satanicraids.SatanicRaids;
import org.mrdarkimc.satanicraids.utils.Disableable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
@Deprecated
public class SimpleSchemPaster implements WorldPaster, Disableable {
    private SatanicRaids plugin;
    private Clipboard clipboard;
    private Actor actor;

    protected int time;

    //public SimpleSchemPaster(Clipboard clipboard) {
    //    this.plugin = SatanicRaids.getInstance();
    //    this.clipboard = clipboard;
    //    this.actor = new BukkitCommandSender(WorldEditPlugin.getInstance(), Bukkit.getConsoleSender());
    //}

    public SimpleSchemPaster(Clipboard clipboard, int time) {
        this.plugin = SatanicRaids.getInstance();
        this.clipboard = clipboard;
        this.actor = new BukkitCommandSender(WorldEditPlugin.getInstance(), Bukkit.getConsoleSender());
        this.time = time;
    }

    private static final Map<UUID, EditSession> editSessions = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> editSessionsTasks = new ConcurrentHashMap<>();


    public void pasteLocal(Location location, int time) {
        EditSession editSession = null;
        UUID uuid = UUID.randomUUID();
        BukkitTask removeTask = null;
        
        try {
            World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
            
            // Используем FAWE API если доступен, иначе стандартный WorldEdit

                editSession = WorldEdit.getInstance().newEditSession(adaptedWorld);


            BlockVector3 pasteLocation = BlockVector3.at(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            );

            Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession)
                    .to(pasteLocation)
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);

             editSession.close();
            
            // Создаем задачу для отмены
            removeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    undo(uuid);
                }
            }.runTaskLater(SatanicRaids.getInstance(), time * 20L);
            
            // Сохраняем открытый EditSession для последующего undo
            addToRemoveTask(uuid, editSession, removeTask);
        } catch (Exception e) {
            // В случае ошибки закрываем сессию и очищаем ресурсы
            if (editSession != null) {
                try {
                    editSession.close();
                } catch (Exception closeEx) {
                    Bukkit.getLogger().warning("[SimpleSchemPaster] Ошибка при закрытии EditSession: " + closeEx.getMessage());
                }
            }
            if (removeTask != null && !removeTask.isCancelled()) {
                removeTask.cancel();
            }
            Bukkit.getLogger().severe("[SimpleSchemPaster] Ошибка при вставке схемы: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void addToRemoveTask(UUID uuid, EditSession editSession, BukkitTask removeTask) {
        System.out.println(" ");
        System.out.println("==================================================");
        System.out.println("adding remove task. Current size: ");
        System.out.println("edit Session: " + editSessions.size());
        System.out.println("edit SessionTasks: " + editSessionsTasks.size());
        editSessions.put(uuid, editSession);
        editSessionsTasks.put(uuid, removeTask);
        System.out.println("Done. Updated Size: ");
        System.out.println("edit Session: " + editSessions.size());
        System.out.println("edit SessionTasks: " + editSessionsTasks.size());
        System.out.println("==================================================");
        System.out.println("==================================================");
    }

    public static void forceStopAll() {
        System.out.println("Forse updating Stopping task. ");
        System.out.println("EditSessionTasks size: " + editSessionsTasks.size());

        // Сначала отменяем все задачи
        for (BukkitTask task : editSessionsTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        // Затем делаем undo
        undoAll();

        // Очищаем мапы
        editSessions.clear();
        editSessionsTasks.clear();

        System.out.println("After clear. ");
        System.out.println("edit Session: " + editSessions.size());
        System.out.println("edit SessionTasks: " + editSessionsTasks.size());
    }

    private static void undoAll() {
        Set<Map.Entry<UUID, EditSession>> entries = new HashSet<>(editSessions.entrySet());
        for (Map.Entry<UUID, EditSession> entry : entries) {
            EditSession originalSession = entry.getValue();
            if (originalSession != null) {
                try {
                    World world = originalSession.getWorld();
                    EditSession undoSession = null;
                    // Используем FAWE API если доступен

                        undoSession = WorldEdit.getInstance().newEditSession(world);

                    
                    originalSession.undo(undoSession);
                    
                    // Закрываем обе сессии
                    originalSession.close();
                    if (undoSession != null) {
                        undoSession.close();
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[SimpleSchemPaster] Ошибка при undo: " + e.getMessage());
                    // Все равно пытаемся закрыть сессию
                    try {
                        originalSession.close();
                    } catch (Exception closeEx) {
                        // Игнорируем ошибки закрытия
                    }
                }
            }
        }
    }

    protected void undo(UUID uuid) {
        System.out.println("Undoning before. Size: " + editSessions.size());
        System.out.println("Undoning before. SizeTasks: " + editSessionsTasks.size());

        EditSession originalSession = editSessions.remove(uuid);
        BukkitTask task = editSessionsTasks.remove(uuid);
        
        if (originalSession != null) {
            try {
                // Создаем НОВЫЙ EditSession для отмены
                World world = originalSession.getWorld();
                EditSession undoSession = null;
                
                // Используем FAWE API если доступен

                    undoSession = WorldEdit.getInstance().newEditSession(world);


                // Отменяем операции из оригинальной сессии в новой сессии
                originalSession.undo(undoSession);

                // Закрываем обе сессии
                originalSession.close();
                if (undoSession != null) {
                    undoSession.close();
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("[SimpleSchemPaster] Ошибка при undo операции " + uuid + ": " + e.getMessage());
                // Все равно пытаемся закрыть сессию
                try {
                    originalSession.close();
                } catch (Exception closeEx) {
                    // Игнорируем ошибки закрытия
                }
            }
        }
        
        // Отменяем задачу если она еще не выполнена
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        System.out.println("Undoning after. Size: " + editSessions.size());
        System.out.println("Undoning after. SizeTasks: " + editSessionsTasks.size());
    }

    @Override
    public void paste(Location loc) {
        pasteLocal(loc, this.time);
    }

    @Override
    public CompletableFuture<Void> pasteAsync(Location loc) {
        // Выполняем в основном потоке, так как FAWE операции должны быть синхронизированы
        // Используем CompletableFuture.supplyAsync для асинхронного выполнения
        return CompletableFuture.supplyAsync(() -> {
            try {
                pasteLocal(loc, this.time);
                return null;
            } catch (Exception e) {
                Bukkit.getLogger().severe("[SimpleSchemPaster] Ошибка при асинхронной вставке: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void disable() {
        undoAll();
    }

//    public void PasteAsync(Location loc){
//        CompletableFuture.runAsync(() -> pasteLocal(loc, this.time));
//    }
}
