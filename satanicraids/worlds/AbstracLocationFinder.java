package org.mrdarkimc.satanicraids.worlds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;


public abstract class AbstracLocationFinder implements LocationFinder {
    protected final World world;
    protected final int worldRadius = 500;
    public AbstracLocationFinder(World world) {
        this.world = world;
    }

    protected boolean hasNearPlayersInRangeOf(Location loc, int range) {
        return world.getNearbyEntities(loc, range, range, range).stream().anyMatch(e -> e instanceof Player);
    }
    protected final Random random = new Random();

    protected boolean isSafeLocation(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Material material = world.getBlockAt(x, y - 1, z).getType();
        return isSafeBlockMaterial(material);
    }
    protected boolean isSafeBlock(Block block) {
        Material material = block.getType();
        return isSafeBlockMaterial(material);
    }
    protected boolean isSafeBlockMaterial(Material material) {
        return material.isSolid() &&
                material != Material.LAVA &&
                material != Material.FIRE &&
                material != Material.CACTUS &&
                !material.toString().contains("MAGMA") &&
                !material.toString().contains("CAMPFIRE");
    }
    protected int getSafeY(int x, int z) {
        int highestY = world.getHighestBlockYAt(x, z);

        Material groundMaterial = world.getBlockAt(x, highestY - 1, z).getType();

        if (isSafeBlockMaterial(groundMaterial)) {
            return highestY + 1;
        }

        return -1;
    }

    protected Location findRandomSafeLocationInWorld() {

        int attempts = 0;
        int maxAttempts = 50;

        while (attempts < maxAttempts) {
            int x = random.nextInt(worldRadius) - worldRadius/2;
            int z = random.nextInt(worldRadius) - worldRadius/2;

            int y = getSafeY(x, z);
            if (y != -1) {
                Location location = new Location(world, x, y, z);

                if (isSafeLocation(location)) {
                    return location;
                }
            }

            attempts++;
        }

        return world.getSpawnLocation();
    }
//    public void findLocationAsync(Player player, int x, int z) {
//        CompletableFuture.supplyAsync(() -> {
//            return findLocation(x, z);
//        }).thenAccept(loc -> {
//            //Bukkit.getLogger().info("[SatanicEnhancements] Завершено. /backrtp ");
//            if (loc == null) {
//                teleportFailure(player);
//            } else {
//                new BukkitRunnable(){
//
//                    @Override
//                    public void run() {
//                        loc.setX(loc.getX()+0.5);
//                        loc.setZ(loc.getZ()+0.5);
//                        player.teleport(loc);
//
//                        var deathLocation = coords.getDeaths().get(player);
//                        double x1 = loc.getX();
//                        double z1 = loc.getZ();
//                        double x2 = deathLocation.getX();
//                        double z2 = deathLocation.getZ();
//
//                        int distance = (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2));
//                        coords.getDeaths().get(player).getZ();
//                        new KeyedMessage(player, "modules.death-coords.success", Map.of(
//                                "%x%", String.valueOf((int) loc.getX()),
//                                "%y%", String.valueOf((int) loc.getY()),
//                                "%z%", String.valueOf((int) loc.getZ()),
//                                "%coords-diff%",String.valueOf(distance)
//                        )).send();
//                        String cmd = SatanicEnhancements.getInstance().getConfig().getString("modules.death-coords.command").replace("%player_name%", player.getName());
//                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
//                        if (coords.getDeaths().containsKey(player)){
//                            var task = coords.getDeaths().remove(player);
//                            task.cancel();
//                        }
//                    }
//                }.runTask(SatanicEnhancements.getInstance());
//
//            }
//        });
//    }

    //    protected Location findLocation(int x, int z) {
//        int radius = 500;
//        for (int i = 0; i < 20; i++) { // 20 попыток для поиска
//            //Bukkit.getLogger().info("[SatanicEnhancements] локация №" + i);
//            int xf = ThreadLocalRandom.current().nextInt(x - radius, x + radius);
//            int zf = ThreadLocalRandom.current().nextInt(z - radius, z + radius);
//            int yf = Bukkit.getWorld("world").getHighestBlockAt(xf, zf).getY();
//
//            if (isOptimalPlace(xf, yf, zf)) {
//                return new Location(Bukkit.getWorld("world"), xf, yf+1, zf); // Возвращаем найденную локацию
//            }
//        }
//        return null;
//    }
//    public boolean isOptimalPlace(int x, int y, int z) {
//        var blocked = List.of(Material.AIR, Material.LAVA, Material.WATER);
//        Bukkit.getWorld("world").getBlockAt(x,y,z);
//        for (int dx = -1; dx <= 1; dx++) {
//            for (int dz = -1; dz <= 1; dz++) {
//                Block block = Bukkit.getWorld("world").getBlockAt(x + dx, y, z + dz);
//                //Bukkit.getLogger().info("Проверяю блок по коордам " + (x + dx) + (y) + (z + dz));
//                //Bukkit.getLogger().info("Материал " + block.getType().toString());
//                if (block == null || blocked.contains(block.getType())) {
//                    return false;  // Если блок не твердый или это пустой блок, локация не подходит
//                }
//            }
//        }
//
//        // Если все блоки в области твердые
//        return has3x3WithAirAbove(x,y,z);
//    }
    protected boolean has3x3WithAirAbove(Location loc) {
        int x, y, z;
        x = (int) loc.getX();
        y = (int) loc.getY();
        z = (int) loc.getZ();

        y = y + 1;
        world.getBlockAt(x, y, z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = world.getBlockAt(x + dx, y, z + dz);
                if (block == null || block.getType() != Material.AIR) {
                    return false;  // Если блок не твердый или это пустой блок, локация не подходит
                }
            }
        }
        return true;
    }
}
