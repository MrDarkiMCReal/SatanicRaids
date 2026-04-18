package org.mrdarkimc.raidsrecode.finders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mrdarkimc.SatanicLib.messages.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class FromPointAsyncFinder implements LocationFinder{
    private final int x;
    private final int z;

    public FromPointAsyncFinder(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public FromPointAsyncFinder(int x, int z, Consumer<Void> onFailure, Consumer<Location> whenFind) {
        this.x = x;
        this.z = z;
        this.onFailure = onFailure;
        this.whenFind = whenFind;
    }

    private Consumer<Void> onFailure;
    private Consumer<Location> whenFind;

    @Override
    public Location find() {
        findLocationAsync();
        return null;
    }
    public void findLocationAsync() {
        CompletableFuture.supplyAsync(() -> {
            return findLocation(x, z);
        }).thenAccept(loc -> {
            //Bukkit.getLogger().info("[SatanicEnhancements] Завершено. /backrtp ");
            if (loc == null) {
                teleportFailure();
            } else {
                if (whenFind!=null) {
                    whenFind.accept(loc);
                }
            }
        });
    }
    void teleportFailure(){
        if (onFailure!=null){
            onFailure.accept(null);
        }
        //new Message(player,"не найдена лока",null).send();
        // new KeyedMessage(player,"modules.death-coords.not-found",null).send();

    }

    public Location findLocation(int x, int z) {
        int radius = 500;
        for (int i = 0; i < 20; i++) { // 20 попыток для поиска
            //Bukkit.getLogger().info("[SatanicEnhancements] локация №" + i);
            int xf = ThreadLocalRandom.current().nextInt(x - radius, x + radius);
            int zf = ThreadLocalRandom.current().nextInt(z - radius, z + radius);
            int yf = Bukkit.getWorld("world").getHighestBlockAt(xf, zf).getY();

            if (isOptimalPlace(xf, yf, zf)) {
                return new Location(Bukkit.getWorld("world"), xf, yf+1, zf); // Возвращаем найденную локацию
            }
        }
        return null;
    }
    public boolean isOptimalPlace(int x, int y, int z) {
        var blocked = List.of(Material.AIR, Material.LAVA, Material.WATER);
        Bukkit.getWorld("world").getBlockAt(x,y,z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = Bukkit.getWorld("world").getBlockAt(x + dx, y, z + dz);
                //Bukkit.getLogger().info("Проверяю блок по коордам " + (x + dx) + (y) + (z + dz));
                //Bukkit.getLogger().info("Материал " + block.getType().toString());
                if (block == null || blocked.contains(block.getType())) {
                    return false;  // Если блок не твердый или это пустой блок, локация не подходит
                }
            }
        }

        // Если все блоки в области твердые
        return has3x3WithAirAbove(x,y,z);
    }
    public boolean has3x3WithAirAbove(int x, int y, int z) {
        y=y+1;
        Bukkit.getWorld("world").getBlockAt(x,y,z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = Bukkit.getWorld("world").getBlockAt(x + dx, y, z + dz);
                Bukkit.getLogger().info("Проверяю блок по коордам " + (x + dx) + (y) + (z + dz));
                Bukkit.getLogger().info("Материал " + block.getType().toString());
                if (block == null || block.getType()!=Material.AIR) {
                    return false;  // Если блок не твердый или это пустой блок, локация не подходит
                }
            }
        }

        // Если все блоки в области твердые
        return true;
    }
}
