package org.mrdarkimc.raidsrecode.api;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.SatanicLib.NotifyAPI.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.eventrunner.EndTask;

import java.util.*;

public class SchedulerImpl implements EventScheduler {
    private final Queue<EventSupplier> events;
    private final long between; // Интервал между событиями в секундах
    private BukkitRunnable scheduleTask; //таска которая запускает события
    public RunnableEvent currentRunningEvent;

    public RunnableEvent getCurrentRunningEvent() {
        return currentRunningEvent;
    }

    public long schedulerStartTime;
    public long lastEventRan;
    //private EventRunner eventRunner = new EventRunner(); //на случай, если нужен forceStop
    private BukkitTask plannedEndTask;

    public SchedulerImpl(List<EventSupplier> events, long delayBetweenEventsInSeconds) {
        this.events = new ArrayDeque<>(events);
        this.between = delayBetweenEventsInSeconds;
    }

    @Override
    public void startSchedule() {
        // Отменяем предыдущее расписание, если есть
        if (scheduleTask != null) {
            if (currentRunningEvent == null) {
                scheduleTask.cancel(); //если событие запущено, то оно пусть работает, если нет. то отменяем планировщик и запускаем вновь
                KeyedMessage.of("scheduler-turned-off").toHelpers();
            } else {
                KeyedMessage.of("scheduler-not-disabled").toHelpers();
                return;
            }
        }
        long runFirstEventAfterSchedulerStart = between * 20;
        long delayBetweenInTicks = between * 20;
        scheduleTask = new BukkitRunnable() {
            @Override
            public void run() {
                lastEventRan = System.currentTimeMillis();
                spawnNextEvent();
            }
        };
        lastEventRan = System.currentTimeMillis();
        scheduleTask.runTaskTimer(SatanicRaids.getInstance(), runFirstEventAfterSchedulerStart, delayBetweenInTicks);

        KeyedMessage.of("scheduler-started")
                .withPlaceholders(Map.of(
                        "{amount}", String.valueOf(events.size()),
                        "{after}", runFirstEventAfterSchedulerStart / 20 + " сек."
                ))
                .toHelpers();

        schedulerStartTime = System.currentTimeMillis();
    }


    @Override
    public void stopSchedule() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }
    }

    @Override
    public void stopCurrentEvent() {
        if (currentRunningEvent == null) {
            KeyedMessage.of("scheduler-no-events-were-stopped").toHelpers();
            return;
        }
        if (currentRunningEvent.isEnded()) {
            KeyedMessage.of("scheduler-event-already-ended").toHelpers();
            currentRunningEvent = null;
            return;
        }

        currentRunningEvent.stop();
        currentRunningEvent = null;
        KeyedMessage.of("scheduler-event-stopped-success").toHelpers();
    }


    //Вызывается каждый час.(каждый between)
    public void spawnNextEvent() {
        //todo добавить поддержку
        if (currentRunningEvent != null) {
            if (!currentRunningEvent.isEnded()) {
                KeyedMessage.of("scheduler-event-already-running").toHelpers();
                return;
            }
        }

        EventSupplier supplier = nextEvent();
        RunnableEvent runnableEvent = supplier.get();
        if (runnableEvent == null) {
            KeyedMessage.of("scheduler-event-is-null").toHelpers();
            return;
        }
        int duration = runnableEvent.getDuration();
        if (duration >= between) {
            KeyedMessage.of("scheduler-event-duration-too-long").toHelpers();
            return;
        }

        currentRunningEvent = runnableEvent;
        runnableEvent.start();
        KeyedMessage.of("scheduler-next-event-started").withPlaceholders("{name}", supplier.getDisplayName()).toHelpers();
        EndTask endTask = new EndTask(runnableEvent);
        endTask.afterEnd(() -> this.currentRunningEvent = null);
        plannedEndTask = endTask.startTask();
    }

    public EventSupplier nextEvent() {
        //todo nullchecks etc
        if (events.isEmpty()) {
            stopSchedule(); //todo убрать вот это выше по уровню, т.к next event можно вызвать извне
            KeyedMessage.of("scheduler-no-events-stopped").toHelpers();
            return null;
        }
        EventSupplier poll = events.poll();
        events.add(poll);
        return poll;
    }

    public void sendNextEventInfo(Player player) {
        if (scheduleTask == null || scheduleTask.isCancelled()) {
            KeyedMessage.of("scheduler-disabled").send(player);
            return;
        }

        if (events.isEmpty()) {
            KeyedMessage.of("scheduler-empty").send(player);
            return;
        }

        EventSupplier nextSupplier = events.peek();
        if (nextSupplier == null) {
            KeyedMessage.of("scheduler-error-null").send(player);
            return;
        }

        String nextEventName = nextSupplier.getDisplayName() != null ? nextSupplier.getDisplayName() : "Следующий эвент";

        long currentTime = System.currentTimeMillis();
        long betweenInMillis = between * 1000;
        long timeSinceLastEvent = currentTime - lastEventRan;

        if (lastEventRan == 0) {
            timeSinceLastEvent = currentTime - schedulerStartTime;
        }

        long timeRemainingInSeconds = (betweenInMillis - timeSinceLastEvent) / 1000;
        if (timeRemainingInSeconds < 0) {
            timeRemainingInSeconds = 0;
        }

        Map<String, String> placeholders = Map.of(
                "{next_event}", nextEventName,
                "{time}", String.valueOf(timeRemainingInSeconds)
        );

        if (currentRunningEvent != null && !currentRunningEvent.isEnded()) {
            KeyedMessage.of("scheduler-info-running").withPlaceholders(placeholders).send(player);
        } else {
            KeyedMessage.of("scheduler-info-waiting").withPlaceholders(placeholders).send(player);
        }
    }


}
