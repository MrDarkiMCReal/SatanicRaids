package org.mrdarkimc.raidsrecode.api;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.SatanicLib.NotifyAPI.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.eventrunner.EndTask;

import java.util.*;
import java.util.function.Supplier;

public class SchedulerImpl implements EventScheduler {
    private final Queue<Supplier<RunnableEvent>> events;
    private final long between; // Интервал между событиями в секундах
    private BukkitRunnable scheduleTask;
    public RunnableEvent currentRunningEvent;

    public RunnableEvent getCurrentRunningEvent() {
        return currentRunningEvent;
    }

    public long schedulerStartTime;
    public long lastEventRan;
    //private EventRunner eventRunner = new EventRunner(); //на случай, если нужен forceStop
    private BukkitTask plannedEndTask;

    public SchedulerImpl(List<Supplier<RunnableEvent>> events, long delayBetweenEventsInSeconds) {
        this.events = new ArrayDeque<>(events);
        this.between = delayBetweenEventsInSeconds;
    }

    @Override
    public void startSchedule() {
        // Отменяем предыдущее расписание, если есть
        if (scheduleTask != null) {
            if (currentRunningEvent == null) {
                scheduleTask.cancel();
                new Message(null, "[SatanicEvents] Scheduler disabled", null).sendToPlayersWithPermission("satanic.helper");
            } else {
                new Message(null, "[SatanicEvents] Scheduler are not disabled", null).sendToPlayersWithPermission("satanic.helper");
                new Message(null, "[SatanicEvents] Disable started event first!", null).sendToPlayersWithPermission("satanic.helper");
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
        new Message(null, "[SatanicEvents] Scheduler started", null).sendToPlayersWithPermission("satanic.helper");
        new Message(null, "[SatanicEvents] Количество событий: " + events.size(), null).sendToPlayersWithPermission("satanic.helper");
        new Message(null, "[SatanicEvents] Эвент будет запущен через " + runFirstEventAfterSchedulerStart / 20 + " сек.", null).sendToPlayersWithPermission("satanic.helper");
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
            return;
        }
        if (currentRunningEvent.isEnded()) {
            currentRunningEvent = null;
            return;
        }
        currentRunningEvent.stop();
        currentRunningEvent = null;
    }

    //Вызывается каждый час.(каждый between)
    public void spawnNextEvent() {
        //todo добавить поддержку
        if (currentRunningEvent != null) {
            if (!currentRunningEvent.isEnded()) {
                new Message(null, "Event is already running. stop it manually", null).sendToPlayersWithPermission("satanic.helper");
                return;
            }
        }
        RunnableEvent runnableEvent = nextEvent();
        if (runnableEvent == null) {
            new Message(null, "[SatanicRaids] Событие не может запуститься т.к оно null", null).sendToPlayersWithPermission("satanic.helper");
            return;
        }
        int duration = runnableEvent.getDuration();
        if (duration >= between) {
            new Message(null, "Событие не может длиться дольше, чем будет запущено следующее событие", null).sendToPlayersWithPermission("satanic.helper");
            return;
        }
       currentRunningEvent = runnableEvent;
        runnableEvent.start();
        EndTask endTask = new EndTask(runnableEvent);
        endTask.afterEnd(() -> this.currentRunningEvent = null);
        plannedEndTask = endTask.startTask();
    }
    public RunnableEvent nextEvent() {
        //todo nullchecks etc
        if (events.isEmpty()) {
            stopSchedule();
            new Message(null, "Не найдено событий, планировщик остановлен", null).sendToPlayersWithPermission("satanic.helper");
            return null;
        }
        Supplier<RunnableEvent> poll = events.poll();
        events.add(poll);
        return poll.get();
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

        Supplier<RunnableEvent> nextSupplier = events.peek();
        if (nextSupplier == null) {
            KeyedMessage.of("scheduler-error-null").send(player);
            return;
        }

        String nextEventName = "Следующий эвент";

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
