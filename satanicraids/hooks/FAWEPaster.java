//package org.mrdarkimc.satanicraids.hooks;
//import com.fastasyncworldedit.core.Fawe;
//import com.fastasyncworldedit.core.FaweAPI;
//import com.sk89q.worldedit.EditSession;
//import com.sk89q.worldedit.MaxChangedBlocksException;
//import com.sk89q.worldedit.WorldEdit;
//import com.sk89q.worldedit.WorldEditException;
//import com.sk89q.worldedit.bukkit.BukkitAdapter;
//import com.sk89q.worldedit.extent.clipboard.Clipboard;
//import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
//import com.sk89q.worldedit.function.operation.Operation;
//import com.sk89q.worldedit.function.operation.Operations;
//import com.sk89q.worldedit.math.BlockVector3;
//import com.sk89q.worldedit.regions.CuboidRegion;
//import com.sk89q.worldedit.session.ClipboardHolder;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.mrdarkimc.satanicraids.SatanicRaids;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//public class FAWEPaster {
//    private final List<PasteOperation> operations = new CopyOnWriteArrayList<>();
//    private final Clipboard clipboard;
//    private final int pasteActiveTime;
//
//    // Для отслеживания активных операций в FAWE
//    private final ConcurrentHashMap<UUID, PasteOperation> activeOperations = new ConcurrentHashMap<>();
//
//    public FAWEPaster(Clipboard clipboard, int pasteActiveTime) {
//        this.clipboard = clipboard;
//        this.pasteActiveTime = pasteActiveTime;
//    }
//
//    public void undoAll() {
//        Bukkit.getScheduler().runTask(SatanicRaids.getInstance(), () -> {
//            for (PasteOperation operation : new ArrayList<>(operations)) {
//                operation.undo();
//            }
//            operations.clear();
//            activeOperations.clear();
//        });
//    }
//
//    public void pasteAndRemove(Location loc) {
//        double x = loc.getX();
//        double y = loc.getY();
//        double z = loc.getZ();
//
//        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(loc.getWorld());
//
//        try {
//            // Создаем операцию вставки
//            PasteOperation pasteOp = new PasteOperation(adaptedWorld, clipboard,
//                    BlockVector3.at(x, y, z), loc);
//
//            // Выполняем вставку
//            boolean success = pasteOp.paste();
//
//            if (success) {
//                operations.add(pasteOp);
//                activeOperations.put(pasteOp.getOperationId(), pasteOp);
//
//                // Запланировать отмену
//                scheduleUndo(pasteOp);
//
//                // Создать защищенный регион
//                Regions.createTimedProtectedRegion(loc, pasteActiveTime, 6);
//            } else {
//                Bukkit.getLogger().warning("FAWE paste failed at location: " + loc);
//            }
//
//        } catch (Exception e) {
//            Bukkit.getLogger().warning("FAWE exception at location: " + loc);
//            e.printStackTrace();
//        }
//    }
//
//    private void scheduleUndo(PasteOperation operation) {
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                operation.undo();
//                operations.remove(operation);
//                activeOperations.remove(operation.getOperationId());
//            }
//        }.runTaskLater(SatanicRaids.getInstance(), pasteActiveTime * 20L);
//    }
//
//    // Внутренний класс для управления операциями
//    private static class PasteOperation {
//        private final UUID operationId;
//        private final com.sk89q.worldedit.world.World world;
//        private final Clipboard clipboard;
//        private final BlockVector3 location;
//        private final Location bukkitLocation;
//        private EditSession editSession;
//        private Operation operation;
//        private boolean completed = false;
//
//        public PasteOperation(com.sk89q.worldedit.world.World world,
//                              Clipboard clipboard,
//                              BlockVector3 location,
//                              Location bukkitLocation) {
//            this.operationId = UUID.randomUUID();
//            this.world = world;
//            this.clipboard = clipboard;
//            this.location = location;
//            this.bukkitLocation = bukkitLocation;
//        }
//
//        public boolean paste() {
//            try {
//                    return pasteWithFAWE();
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("Paste operation failed: " + e.getMessage());
//                return false;
//            }
//        }
//
//        private boolean pasteWithFAWE() {
//            try {
//                // Создаем EditSession через FAWE для лучшей производительности
//                editSession = FaweAPI.getEditSessionBuilder(world)
//                        .fastMode(true)
//                        .allowedRegionsEverywhere()
//                        .autoQueue(false)
//                        .build();
//
//                // Создаем операцию вставки
//                ClipboardHolder holder = new ClipboardHolder(clipboard);
//                operation = holder.createPaste(editSession)
//                        .to(location)
//                        .ignoreAirBlocks(false)
//                        .build();
//
//                // Выполняем операцию
//                Operations.complete(operation);
//                completed = true;
//
//                return true;
//            } catch (WorldEditException e) {
//                Bukkit.getLogger().warning("FAWE paste operation failed: " + e.getMessage());
//                cleanup();
//                return false;
//            }
//        }
//
//        private boolean pasteWithStandardWE() {
//            try {
//                // Стандартный способ для обычного WorldEdit
//                editSession = WorldEdit.getInstance().newEditSession(world);
//
//                ClipboardHolder holder = new ClipboardHolder(clipboard);
//                operation = holder.createPaste(editSession)
//                        .to(location)
//                        .ignoreAirBlocks(false)
//                        .build();
//
//                Operations.complete(operation);
//                completed = true;
//
//                return true;
//            } catch (WorldEditException e) {
//                Bukkit.getLogger().warning("Standard WE paste operation failed: " + e.getMessage());
//                cleanup();
//                return false;
//            }
//        }
//
//        public void undo() {
//            if (!completed) {
//                return;
//            }
//
//            try {
//                // Убеждаемся, что отмена выполняется в основном потоке
//                if (!Bukkit.isPrimaryThread()) {
//                    Bukkit.getScheduler().runTask(SatanicRaids.getInstance(), this::performUndo);
//                    return;
//                }
//                performUndo();
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("Undo operation failed: " + e.getMessage());
//            } finally {
//                cleanup();
//            }
//        }
//
//        private void performUndo() {
//            try {
//                    undoWithFAWE();
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("Undo execution failed: " + e.getMessage());
//            }
//        }
//
//        private void undoWithFAWE() {
//            try {
//                // Для FAWE сначала дожидаемся завершения всех операций
//                Fawe.instance().getQueueHandler().waitIdle();
//
//                if (editSession != null) {
//                    // Пытаемся использовать стандартную отмену
//                    Operation undoOperation = editSession.undo(editSession);
//                    if (undoOperation != null) {
//                        Operations.complete(undoOperation);
//                    } else {
//                        // Fallback: очищаем регион вручную
//                        clearRegionManually();
//                    }
//                }
//            } catch (Exception e) {
//                // Fallback на ручную очистку
//                clearRegionManually();
//            }
//        }
//
//        private void undoWithStandardWE() {
//            try {
//                if (editSession != null) {
//                    editSession.undo(editSession);
//                }
//            } catch (Exception e) {
//                clearRegionManually();
//            }
//        }
//
//        private void clearRegionManually() {
//            try {
//                // Создаем новый EditSession для очистки региона
//                try (EditSession clearSession = Fawe.isFaweEnabled() ?
//                        FaweAPI.getEditSessionBuilder(world).build() :
//                        WorldEdit.getInstance().newEditSession(world)) {
//
//                    // Определяем регион для очистки на основе размеров clipboard
//                    BlockVector3 min = location;
//                    BlockVector3 max = location.add(
//                            clipboard.getDimensions().getBlockX() - 1,
//                            clipboard.getDimensions().getBlockY() - 1,
//                            clipboard.getDimensions().getBlockZ() - 1
//                    );
//
//                    CuboidRegion region = new CuboidRegion(world, min, max);
//                    clearSession.setBlocks(region, BukkitAdapter.adapt(org.bukkit.Material.AIR.createBlockData()));
//
//                    if (Fawe.isFaweEnabled()) {
//                        Operations.completeBlindly(clearSession.commit());
//                    }
//                }
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("Manual region clear failed: " + e.getMessage());
//            }
//        }
//
//        private void cleanup() {
//            try {
//                if (editSession != null) {
//                    editSession.close();
//                    editSession = null;
//                }
//                operation = null;
//                completed = false;
//            } catch (Exception e) {
//                // Игнорируем ошибки при cleanup
//            }
//        }
//
//        public UUID getOperationId() {
//            return operationId;
//        }
//
//        public Location getBukkitLocation() {
//            return bukkitLocation;
//        }
//    }
//
//    // Дополнительные утилитные методы
//
//    public CompletableFuture<Boolean> pasteAsync(Location loc) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                pasteAndRemove(loc);
//                return true;
//            } catch (Exception e) {
//                return false;
//            }
//        });
//    }
//
//    public void cancelAll() {
//        undoAll();
//    }
//
//    public int getActiveOperationsCount() {
//        return operations.size();
//    }
//
//    public boolean hasActiveOperations() {
//        return !operations.isEmpty();
//    }
//}