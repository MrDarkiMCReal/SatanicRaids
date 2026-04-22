package org.mrdarkimc.satanicraids.raidevent;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.satanicraids.SatanicRaids;

public class RaidScheduler implements EventScheduler {
    private long between; // Интервал между событиями в тиках
    private long activeTime; // Длительность события в тиках
    private EventRunner runner;
    public boolean isRunning = false;
    private BukkitRunnable scheduleTask;
    private BukkitRunnable stopTask;
    private int announceTaskId = -1;

    public RaidScheduler(long between, long activeTime, EventRunner runner) {
        this.between = between;
        this.activeTime = activeTime;
        this.runner = runner;
    }

    @Override
    public boolean isAlreadyLaunched() {
        return !isRunning;
    }

    @Override
    public void announce(int timeToLaunch) {
        // Отменяем предыдущие объявления
        if (announceTaskId != -1) {
            Bukkit.getScheduler().cancelTask(announceTaskId);
        }

        // Объявление за 5 минут (6000 тиков = 5 минут)
        long fiveMinutes = 6000L;
        if (timeToLaunch >= fiveMinutes) {
            Bukkit.getScheduler().runTaskLater(SatanicRaids.getInstance(), () -> {
                new KeyedMessage(null, "messages.event-announce-5min", null).broadcast();
            }, timeToLaunch - fiveMinutes);
        }

        // Объявление за 3 минуты (3600 тиков = 3 минуты)
        long threeMinutes = 3600L;
        if (timeToLaunch >= threeMinutes) {
            Bukkit.getScheduler().runTaskLater(SatanicRaids.getInstance(), () -> {
                new KeyedMessage(null, "messages.event-announce-3min", null).broadcast();
            }, timeToLaunch - threeMinutes);
        }

        // Объявление сейчас
        Bukkit.getScheduler().runTaskLater(SatanicRaids.getInstance(), () -> {
            new KeyedMessage(null, "messages.event-announce-now", null).broadcast();
        }, timeToLaunch);
    }

    @Override
    public void startSchedule() {
        // Отменяем предыдущее расписание, если есть
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }

        // Отправляем объявление о начале
        if (scheduleTask != null) scheduleTask.cancel();

        // Планируем анонсы и подготовки
        //planPreparations();

        scheduleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isAlreadyLaunched()){
                    throw new RuntimeException("Already launched");
                    //return; //todo y not unreacheable?
                }
                isRunning = true;
                runner.startEvent();
                stopEventTask();
               // planPreparations();
            }
        };
        scheduleTask.runTaskTimer(SatanicRaids.getInstance(), between, between);
    }

    private void stopEventTask() {
        // Отменяем предыдущую задачу остановки, если есть
        if (stopTask != null) {
            stopTask.cancel();
        }

        stopTask = new BukkitRunnable() {
            @Override
            public void run() {
                isRunning = false;
                runner.stopEvent();
            }
        };
        stopTask.runTaskLater(SatanicRaids.getInstance(), activeTime);
    }

    public void stop() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }
        if (stopTask != null) {
            stopTask.cancel();
        }
        if (announceTaskId != -1) {
            Bukkit.getScheduler().cancelTask(announceTaskId);
        }
    }

    public void setBetween(long between) {
        this.between = between;
    }

    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    public void setRunner(EventRunner runner) {
        this.runner = runner;
    }
}
