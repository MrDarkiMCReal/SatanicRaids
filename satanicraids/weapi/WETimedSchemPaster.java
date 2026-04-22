package org.mrdarkimc.satanicraids.weapi;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.satanicraids.SatanicRaids;
import org.mrdarkimc.satanicraids.tasks.FaweUndoTask;
import org.mrdarkimc.satanicraids.utils.Disableable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
unused
 */
@Deprecated
public class WETimedSchemPaster implements WorldPaster, Disableable {

    protected final JavaPlugin plugin;
    protected final Clipboard clipboard;
    protected final int time;
    protected final BukkitCommandSender actor;
    private static final List<Consumer<Void>> sessions = new ArrayList<>();
    private static final List<BukkitTask> undoTasks = new ArrayList<>();

    public WETimedSchemPaster(Clipboard clipboard, int time) {
        this.plugin = SatanicRaids.getInstance();
        this.clipboard = clipboard;
        this.time = time;
        this.actor = new BukkitCommandSender(WorldEditPlugin.getInstance(), Bukkit.getConsoleSender()); //todo сделать кастомного актора.
        Disableable.register(this);
        //plugin actor
    }

    /**
     * Возможен косяк, что если
     * вставить схему с удалением через минуту а потом
     * вставить схему с удалением через 2 минуты,
     * то отменится последняя вставка, тоесть вторая (t.k актор один(консоль) а отменяет последнее изменение? или не)
     */
    @Override
    public void paste(Location location){
        pasteAsync(location);
    }
    @Override
    public CompletableFuture<Void> pasteAsync(Location location) {

        CompletableFuture<Void> pasteFuture = pasteSchematicAsync(actor, location);
//        pasteFuture.thenAccept(editSession -> {
//            if (editSession != null) {
//                Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                    undoEditSession(actor, editSession, location);
//                }, time * 20L);
//                //rememberForUndo(actor, editSession, location);
//            }
//        }).exceptionally(throwable -> {
//            plugin.getLogger().severe("Ошибка при вставке схематики: " + throwable.getMessage());
//            throwable.printStackTrace();
//            return null;
//        });
        return pasteFuture;
    }


    private void rememberForUndo(Actor actor, EditSession editSession, Location loc) {
        Consumer<Void> undoSessionConsumer = (s) -> undoEditSession(actor, editSession, loc);
        sessions.add(undoSessionConsumer);
        BukkitTask undoTask = new FaweUndoTask(undoSessionConsumer).runTaskLater(time * 20L);
        undoTasks.add(undoTask);
    }

    private CompletableFuture<Void> pasteSchematicAsync(Actor actor, Location location) {
        CompletableFuture.supplyAsync(() -> {
            try {
                com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());
                EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);

                BlockVector3 pasteLocation = BlockVector3.at(
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ()
                );

                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
                Operation operation = clipboardHolder
                        .createPaste(editSession)
                        .to(pasteLocation)
                        .ignoreAirBlocks(false)
                        .build();

                Operations.complete(operation);

                editSession.flushSession();

                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
                localSession.remember(editSession);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    undoEditSession(actor, editSession, location);
                }, time * 20L);
                plugin.getLogger().info("Схематика успешно вставлена, будет удалена через " + time + " секунд");
                return editSession;

            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при асинхронной вставке: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
        return null;
    }

    /**
     * Отмена изменений (удаление вставленной схематики)
     */
    private void undoEditSession(Actor actor, EditSession editSession, Location loc) {
        System.out.println("Удаляю схему...");
        CompletableFuture.runAsync(() -> {
            try {
                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
                localSession.setWorldOverride(BukkitAdapter.adapt(loc.getWorld()));
                EditSession undoSession = localSession.undo(editSession.getBlockBag(), actor);

                if (undoSession != null) {
                    undoSession.flushSession();
                    plugin.getLogger().info("Схематика успешно удалена");
                } else {
                    plugin.getLogger().warning("Не удалось отменить изменения");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при удалении схематики: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void disable() {
        System.out.println("Calling disable on class: " + getClass().getSimpleName());
        System.out.println("Отменяю таски для отмены");
        for (BukkitTask undoTask : undoTasks) { //todo серьезная утечка. я не удаляю из листа таски до того, как будет вызван disable
            if (!undoTask.isCancelled()){
                undoTask.cancel();
            }
        }

        undoTasks.clear();
        System.out.println("Применяю undo");
        for (Consumer<Void> session : sessions) {
            session.accept(null);
        }
        sessions.clear();
    }
//    public void pasteAndRemoveAlternative(Location location, Actor actor) {
//        CompletableFuture.runAsync(() -> {
//            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(
//                    BukkitAdapter.adapt(location.getWorld()), -1)) {
//
//                BlockVector3 pasteLocation = BlockVector3.at(
//                        location.getBlockX(),
//                        location.getBlockY(),
//                        location.getBlockZ()
//                );
//
//                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
//                Operation operation = clipboardHolder
//                        .createPaste(editSession)
//                        .to(pasteLocation)
//                        .ignoreAirBlocks(false)
//                        .build();
//
//                Operations.complete(operation);
//                editSession.flushSession();
//
//
//                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
//                localSession.remember(editSession);
//
//                Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                    undoEditSession(actor, editSession, location);
//                }, time * 20L);
//
//            } catch (Exception e) {
//                plugin.getLogger().severe("Ошибка: " + e.getMessage());
//                e.printStackTrace();
//            }
//        });
//    }
}