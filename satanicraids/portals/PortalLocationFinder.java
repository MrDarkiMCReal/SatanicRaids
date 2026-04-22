package org.mrdarkimc.satanicraids.portals;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.mrdarkimc.satanicraids.finders.AbstracLocationFinder;

import java.util.Random;

/**
 * Находит случайные координаты для портала в радиусе 3000 блоков от центра мира (0,0,0)
 */
public class PortalLocationFinder extends AbstracLocationFinder {
    private static final int PORTAL_SPAWN_RADIUS = 3000;
    private final Random random = new Random();

    public PortalLocationFinder(World world) {
        super(world);
    }

    @Override
    public Location find() {
        int attempts = 0;
        int maxAttempts = 5;

        //while (attempts < maxAttempts) {
            //    // Генерируем случайные координаты в радиусе 3000 блоков
            //    int x = random.nextInt(PORTAL_SPAWN_RADIUS * 2) - PORTAL_SPAWN_RADIUS;
            //    int z = random.nextInt(PORTAL_SPAWN_RADIUS * 2) - PORTAL_SPAWN_RADIUS;
            //
            //    // Находим безопасную Y координату
            //    int y = getSafeY(x, z);
            //    if (y != -1) {
                //        Location location = new Location(world, x, y, z);
                //
                //        // Проверяем, что в радиусе 50 блоков нет игроков
                //        if (!hasNearPlayersInRangeOf(location, 25,5,25)) {
                    //                return location;
                    //        }
                //    }
            //
            //    attempts++;
            //}

        // Если не нашли подходящее место, возвращаем случайную локацию в радиусе
        int x = random.nextInt(PORTAL_SPAWN_RADIUS * 2) - PORTAL_SPAWN_RADIUS;
        int z = random.nextInt(PORTAL_SPAWN_RADIUS * 2) - PORTAL_SPAWN_RADIUS;
        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x, y, z);
    }
}




