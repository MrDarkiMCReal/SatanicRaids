package org.mrdarkimc.raidsrecode.events.airdropevent;

import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.raidsrecode.api.AbstractEvent;

public class BaseAirdropEvent extends AbstractEvent {
    protected BaseAirdropEvent(JavaPlugin plugin, int eventDuration) {
        super(plugin, eventDuration);
    }

    @Override
    public void start() {
        //найти локу
        //заспавнить схему
        //заспавнить сундук
        //создать боссбар
        //обьявление в чате
        //сделать таймер до открытия по клику
        //обьявление по активации
        //открыть сундук
        checkNotInitialized();
        setRunningStatus();
        eventTimer.startTask();

        taskRunner.runNext(this::prepareLocation, 0);
        taskRunner.runNext(this::spawnSchem, 2);
        taskRunner.runNext(this::spawnChest, 2);
        taskRunner.runNext(this::createClaimZone, 2);
        taskRunner.runNext(this::registerAirdropListener, 3);
        taskRunner.runNext(this::sendBossbars, 3);
        taskRunner.runNext(this::announceEventSpawned, 3);
        taskRunner.runNext(this::startHoloUpdatingTask, 3);

    }

    @Override
    protected void prepareLocation() {
        super.prepareLocation(l -> {
            int maxHeight = 230;
            double yCord = Math.min(maxHeight, l.getY() + 50);
            l.setY(yCord);
            eventLocation = l;
        });
    }

    @Override
    public void stop() {

    }

    private void createClaimZone() {

    }

    private void registerAirdropListener() {

    }

    private void spawnSchem() {

    }

    private void sendBossbars() {

    }

    private void announceEventSpawned() {

    }

    private void spawnChest() {

    }

    private void startHoloUpdatingTask() {

    }
}
