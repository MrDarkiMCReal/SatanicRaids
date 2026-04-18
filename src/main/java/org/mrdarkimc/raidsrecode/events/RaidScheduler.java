package org.mrdarkimc.raidsrecode.events;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.TaskHelper;
import org.mrdarkimc.raidsrecode.eventrunner.EventRunner;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Supplier;

public class RaidScheduler implements EventScheduler {
    private final Queue<Supplier<RunnableEvent>> events;
    private final long between; // Интервал между событиями в секундах
    private BukkitRunnable scheduleTask;
    public RunnableEvent currentRunningEvent;

    public RunnableEvent getCurrentRunningEvent() {
        return currentRunningEvent;
    }
    public long startTime;

    public RaidScheduler(List<Supplier<RunnableEvent>> events, long delayBetweenEventsInSeconds) {
        this.events = new ArrayDeque<>(events);
        this.between = delayBetweenEventsInSeconds;
    }

    @Override
    public void startSchedule() {
        // Отменяем предыдущее расписание, если есть
        if (scheduleTask != null) {
            if (currentRunningEvent == null) {
                scheduleTask.cancel();
                new Message(null,"[Raid] Scheduler disabled",null).sendToPlayersWithPermission("satanic.helper");
            } else {
                new Message(null,"[Raid] Scheduler are not disabled",null).sendToPlayersWithPermission("satanic.helper");
                new Message(null,"[Raid] Disable started event first!",null).sendToPlayersWithPermission("satanic.helper");
                return;
            }
        }
        scheduleTask = new BukkitRunnable() {
            @Override
            public void run() {
//                if (!isAlreadyLaunched()){
//                    throw new RuntimeException("Already launched");
                //return; //todo y not unreacheable?
                //stopEventTask();
                // planPreparations();
                spawnNextEvent();
            }
        };
        scheduleTask.runTaskTimer(SatanicRaids.getInstance(), between * 20, between * 20);
        SatanicRaids.getInstance().getLogger().info("Scheduler started");
        startTime = System.currentTimeMillis();
    }
    public void sendMessageToNextEvent(){

    }

    @Override
    public void stopSchedule() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }
//        if (stopTask != null) {
//            stopTask.cancel();
//        }
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
            return;
        }
        int duration = runnableEvent.getDuration();
        if (duration >= between) {
            new Message(null, "Событие не может длиться дольше, чем будет запущено следующее событие", null).sendToPlayersWithPermission("satanic.helper");
            return;
        }
        runnableEvent.start();
        currentRunningEvent = runnableEvent;
    }

//    private void stopEventTask() {
//        // Отменяем предыдущую задачу остановки, если есть
//        if (stopTask != null) {
//            stopTask.cancel();
//        }
//
//        stopTask = new BukkitRunnable() {
//            @Override
//            public void run() {
//                isRunning = false;
//                runner.stopEvent();
//            }
//        };
//        stopTask.runTaskLater(SatanicRaids.getInstance(), activeTime);
//    }

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
}
