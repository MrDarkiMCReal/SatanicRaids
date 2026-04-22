//package org.mrdarkimc.satanicraids.weapi;
//
//import com.sk89q.worldedit.EditSession;
//import com.sk89q.worldedit.LocalSession;
//import com.sk89q.worldedit.WorldEdit;
//import com.sk89q.worldedit.bukkit.BukkitAdapter;
//import com.sk89q.worldedit.extension.platform.Actor;
//import com.sk89q.worldedit.extent.clipboard.Clipboard;
//import com.sk89q.worldedit.function.operation.Operation;
//import com.sk89q.worldedit.function.operation.Operations;
//import com.sk89q.worldedit.math.BlockVector3;
//import com.sk89q.worldedit.session.ClipboardHolder;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//
//import java.util.concurrent.CompletableFuture;
//
//public class FaweNoAsyncSchemPaster extends WETimedSchemPaster{
//    public FaweNoAsyncSchemPaster(Clipboard clipboard, int time) {
//        super(clipboard, time);
//    }
//
//    @Override
//    public CompletableFuture<Void> paste(Location location) {
//        pasteSchematicAsync(location);
//        return null;
//    }
//
//    private void pasteSchematicAsync(Location location) {
//            try {
//                com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());
//                EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
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
//
//                editSession.flushSession();
//
//                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
//                //localSession.remember(editSession);
//                Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                    undoEditSession(actor, editSession, location);
//                }, time * 20L);
//                //plugin.getLogger().info("Схематика успешно вставлена, будет удалена через " + time + " секунд");
//                //return null;
//
//            } catch (Exception e) {
//                //plugin.getLogger().severe("Ошибка при асинхронной вставке: " + e.getMessage());
//                e.printStackTrace();
//                //return null;
//            }
//        //return null;
//    }
//    private void undoEditSession(Actor actor, EditSession editSession, Location loc) {
//        System.out.println("Удаляю схему...");
//            try {
//                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
//                localSession.setWorldOverride(BukkitAdapter.adapt(loc.getWorld()));
//                EditSession undoSession = localSession.undo(editSession.getBlockBag(), actor);
//
//                if (undoSession != null) {
//                    undoSession.flushSession();
//                   // plugin.getLogger().info("Схематика успешно удалена");
//                } else {
//                    //plugin.getLogger().warning("Не удалось отменить изменения");
//                }
//            } catch (Exception e) {
//                //plugin.getLogger().severe("Ошибка при удалении схематики: " + e.getMessage());
//                e.printStackTrace();
//            }
//    }
//}
