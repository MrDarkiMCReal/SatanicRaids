package org.mrdarkimc.raidsrecode.finders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import org.mrdarkimc.raidsrecode.SatanicRaids;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

//Ищет координаты и когда нашел -> onFound;
public class AsyncLocationFinder implements LocationFinder {
    private final World world;
    private final int radius;
    private final int maxAttempts;
    private final Location center;
    private final Predicate<Location> requirements;
    private Consumer<Location> onFound;
    private final Runnable onFailure;

    private AsyncLocationFinder(LocationFinderBuilder builder) {
        this.world = builder.world;
        this.radius = builder.radius;
        this.maxAttempts = builder.maxAttempts;
        this.center = builder.center;
        this.requirements = builder.requirements;
        this.onFound = builder.onFound;
        this.onFailure = builder.onFailure;
    }
    public void whenFound(Consumer<Location> whenFound){
        this.onFound = whenFound;
    }
    public static LocationFinderBuilder newBuilder() {
        return new LocationFinderBuilder();
    }

    @Override
    public Location find() {
        findLocationAsync();
        return null;
    }

    public void findLocationAsync() {
        attemptFind(0);
    }

    private void attemptFind(int attempt) {
        if (attempt >= maxAttempts) {
            if (onFailure != null) Bukkit.getScheduler().runTask(SatanicRaids.getInstance(), onFailure);
            return;
        }

        int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius);
        int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius);

        world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> {
            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x, y, z);

            if (requirements.test(loc)) {
                Bukkit.getScheduler().runTask(SatanicRaids.getInstance(), () -> {
                    Bukkit.getLogger().info("Location found " + loc.toString());
                    onFound.accept(loc.add(0.5, 1, 0.5));
                });
            } else {
                attemptFind(attempt + 1);
            }
        });
    }

    public static class LocationFinderBuilder {
        private World world;
        private int radius = 1000;
        private int maxAttempts = 50;
        private Location center;
        private Predicate<Location> requirements = loc -> true;
        private Consumer<Location> onFound;
        private Runnable onFailure;

        public LocationFinderBuilder() {
        }

        public LocationFinderBuilder radius(int radius) {
            this.radius = radius;
            return this;
        }

        public LocationFinderBuilder maxAttempts(int attempts) {
            this.maxAttempts = attempts;
            return this;
        }

        public LocationFinderBuilder center(Location center) {
            this.center = center;
            this.world = center.getWorld();
            return this;
        }

        public LocationFinderBuilder onFound(Consumer<Location> onFound) {
            this.onFound = onFound;
            return this;
        }

        public LocationFinderBuilder onFailure(Runnable onFailure) {
            this.onFailure = onFailure;
            return this;
        }


        public LocationFinderBuilder andRequire(Predicate<Location> predicate) {
            this.requirements = this.requirements.and(predicate);
            return this;
        }

        public LocationFinderBuilder requireSafeSurface() {
            return andRequire(loc -> {
                Material base = loc.getBlock().getType();
                return base.isSolid() && base != Material.LAVA && base != Material.WATER;
            });
        }

        public LocationFinderBuilder require3x3WithAirAbove() {
            return andRequire(loc -> {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        // Проверяем землю
                        if (!loc.clone().add(dx, 0, dz).getBlock().getType().isSolid()) return false;
                        // Проверяем воздух над землей (2 блока вверх)
                        if (!loc.clone().add(dx, 1, dz).getBlock().isEmpty()) return false;
                        if (!loc.clone().add(dx, 2, dz).getBlock().isEmpty()) return false;
                    }
                }
                return true;
            });
        }

        public LocationFinder build() {
            if (onFound == null || center == null) throw new IllegalStateException("Missing required stuff");
            return new AsyncLocationFinder(this);
        }
    }
}
