//package org.mrdarkimc.satanicraids.listeners;
//
//import org.bukkit.Material;
//import org.bukkit.World;
//import org.bukkit.block.Block;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.event.block.Action;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.event.player.PlayerMoveEvent;
//import org.bukkit.event.player.PlayerTeleportEvent;
//import org.mrdarkimc.satanicraids.portals.PortalHandler;
//
//public class EventListener implements Listener {
//    private PortalHandler handler;
//
//    public EventListener(PortalHandler handler) {
//        this.handler = handler;
//    }
//
//    /**
//     * Обрабатывает телепортации и запрещает команды телепортации в эвентовый мир
//     */
//    @EventHandler(priority = EventPriority.HIGH)
//    public void onTeleport(PlayerTeleportEvent e) {
//        boolean equals = e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL);
//        if (!equals){
//            return;
//        }
//        Player player = e.getPlayer();
//        World from = e.getFrom().getWorld();
//        World to = e.getTo() != null ? e.getTo().getWorld() : null;
//
//        // Проверяем телепортацию из эвентового мира в обычный
//        boolean blockedTeleport = handler.isTeleportFromEventToNormalWorld(from, to, player);
//        if (blockedTeleport) {
//            e.setCancelled(true);
//            player.sendMessage("§cВы не можете использовать команды телепортации в эвентовом мире!");
//            return;
//        }
//
//        // Проверяем касание входного портала (в обычном мире)
//        if (handler.getActiveEntryPortal() != null) {
//            boolean eventTeleport = handler.isEventTeleport(player);
//            if (eventTeleport) {
//                System.out.println("[onTeleport] Добавляю игрока в мир");
//                handler.addPlayerToEventWorld(player);
//                e.setCancelled(true);
//            }
//        }
//
//        // Проверяем касание выходного портала (в эвентовом мире)
//        if (to != null && handler.getActiveExitPortal() != null &&
//            to.equals(handler.getActiveExitPortal().getWorld())) {
//            boolean exitTeleport = handler.isExitTeleport(player);
//            if (exitTeleport) {
//                handler.allowTeleport(player);
//                handler.removePlayerFromEventWorld(player);
//                handler.denyTeleport(player);
//            }
//        }
//    }
//
//    /**
//     * Обрабатывает касание портала через движение игрока
//     */
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerMove(PlayerMoveEvent e) {
//        Player player = e.getPlayer();
//        if (e.getTo() == null) {
//            return;
//        }
//
//        // Проверяем входной портал (энд-портал в обычном мире)
//        if (handler.getActiveEntryPortal() != null &&
//            e.getTo().getWorld().equals(handler.getActiveEntryPortal().getWorld())) {
//            Block block = e.getTo().getBlock();
//            if (block.getType() == Material.END_PORTAL || block.getType() == Material.END_PORTAL_FRAME) {
//                boolean eventTeleport = handler.isEventTeleport(player);
//                if (eventTeleport) {
//                    handler.addPlayerToEventWorld(player);
//                }
//            }
//        }
//
//        // Проверяем выходной портал (энд-портал в эвентовом мире)
//        if (handler.getActiveExitPortal() != null &&
//            e.getTo().getWorld().equals(handler.getActiveExitPortal().getWorld())) {
//            Block block = e.getTo().getBlock();
//            if (block.getType() == Material.END_PORTAL || block.getType() == Material.END_PORTAL_FRAME) {
//                boolean exitTeleport = handler.isExitTeleport(player);
//                if (exitTeleport) {
//                    handler.allowTeleport(player);
//                    handler.removePlayerFromEventWorld(player);
//                    handler.denyTeleport(player);
//                }
//            }
//        }
//    }
//
//    /**
//     * Обрабатывает взаимодействие с порталом
//     */
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerInteract(PlayerInteractEvent e) {
//        if (e.getAction() != Action.PHYSICAL) {
//            return;
//        }
//
//        Player player = e.getPlayer();
//        Block block = e.getClickedBlock();
//        if (block == null) {
//            return;
//        }
//
//        // Проверяем входной портал
//        if (handler.getActiveEntryPortal() != null &&
//            block.getWorld().equals(handler.getActiveEntryPortal().getWorld())) {
//            if (block.getType() == Material.END_PORTAL || block.getType() == Material.END_PORTAL_FRAME) {
//                boolean eventTeleport = handler.isEventTeleport(player);
//                if (eventTeleport) {
//                    handler.addPlayerToEventWorld(player);
//                }
//            }
//        }
//
//        // Проверяем выходной портал
//        if (handler.getActiveExitPortal() != null &&
//            block.getWorld().equals(handler.getActiveExitPortal().getWorld())) {
//            if (block.getType() == Material.END_PORTAL || block.getType() == Material.END_PORTAL_FRAME) {
//                boolean exitTeleport = handler.isExitTeleport(player);
//                if (exitTeleport) {
//                    handler.allowTeleport(player);
//                    handler.removePlayerFromEventWorld(player);
//                    handler.denyTeleport(player);
//                }
//            }
//        }
//    }
//}
