package org.mrdarkimc.satanicraids.hooks;

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
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.satanicraids.SatanicRaids;

import java.util.concurrent.CompletableFuture;

public class SchematicPasteAndRemove {

    private final JavaPlugin plugin;
    private final Clipboard clipboard;
    private final int time;

    public SchematicPasteAndRemove(Clipboard clipboard, int time) {
        this.plugin = SatanicRaids.getInstance();
        this.clipboard = clipboard;
        this.time = time;
    }

    /**
     * Вставляет схематику и удаляет её через указанное время
     * @param actor Игрок, от имени которого выполняется операция
     * @param location Место для вставки
     */
    public void pasteAndRemove(Location location) {
        // Асинхронная вставка схематики
        BukkitCommandSender consoleActor = new BukkitCommandSender(WorldEditPlugin.getInstance(), Bukkit.getConsoleSender());
        CompletableFuture<EditSession> pasteFuture = pasteSchematicAsync(consoleActor, location);

        pasteFuture.thenAccept(editSession -> {
            if (editSession != null) {
                // Запланировать удаление через time секунд
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    undoEditSession(consoleActor, editSession,location);
                }, time * 20L); // Конвертируем секунды в тики (20 тиков = 1 секунда)
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().severe("Ошибка при вставке схематики: " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * Асинхронная вставка схематики
     */
    private CompletableFuture<EditSession> pasteSchematicAsync(Actor actor, Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Создаем EditSession с помощью WorldEdit
                com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());
                EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);

                // Конвертируем Location в BlockVector3
                BlockVector3 pasteLocation = BlockVector3.at(
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ()
                );

                // Создаем операцию вставки
                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
                Operation operation = clipboardHolder
                        .createPaste(editSession)
                        .to(pasteLocation)
                        .ignoreAirBlocks(false)
                        .build();

                // Выполняем операцию
                Operations.complete(operation);

                // Фиксируем изменения
                editSession.flushSession();

                // Сохраняем сессию для возможности отмены
                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
                localSession.remember(editSession);

                plugin.getLogger().info("Схематика успешно вставлена, будет удалена через " + time + " секунд");
                return editSession;

            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при асинхронной вставке: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Отмена изменений (удаление вставленной схематики)
     */
    private void undoEditSession(Actor actor, EditSession editSession, Location loc) {
        CompletableFuture.runAsync(() -> {
            try {
                // Получаем локальную сессию игрока
                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
                localSession.setWorldOverride(BukkitAdapter.adapt(loc.getWorld()));
                // Отменяем конкретный EditSession
                EditSession undoSession = localSession.undo(editSession.getBlockBag(),actor);

                if (undoSession != null) {
                    // Фиксируем отмену
                    undoSession.flushSession();
                    plugin.getLogger().info("Схематика успешно удалена");

                    // Уведомляем игрока
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getLogger().info("§aСхематика была удалена через " + time + " секунд");
                    });
                } else {
                    plugin.getLogger().warning("Не удалось отменить изменения");
                }

            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при удалении схематики: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Альтернативный метод с использованием try-with-resources
     */
    public void pasteAndRemoveAlternative(Location location, Actor actor) {
        CompletableFuture.runAsync(() -> {
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(
                    BukkitAdapter.adapt(location.getWorld()), -1)) {

                // Вставка схематики
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

                // Сохраняем для отмены

                LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
                localSession.remember(editSession);

                // Запланировать удаление
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    undoEditSession(actor, editSession, location);
                }, time * 20L);

            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}