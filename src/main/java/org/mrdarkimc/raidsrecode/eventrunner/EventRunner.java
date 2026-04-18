package org.mrdarkimc.raidsrecode.eventrunner;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.TaskHelper;
import org.mrdarkimc.raidsrecode.events.RunnableEvent;

import java.util.LinkedList;
import java.util.List;

public class EventRunner {
    private int currentIndex = 0;
    private List<RunnableEvent> avaliableEvents = new LinkedList<>();
    private int runInterval = 60; //in minutes
    private RunnableEvent currentRunningEvent;
    private BukkitTask currentStartTask;
    private BukkitTask currentEndTask;
    private BukkitTask currentRunningEventsTask;
    public void addEvent(RunnableEvent event){
        avaliableEvents.add(event);
    }

    public void runEvents() {
        if (currentRunningEvent != null) {
            Bukkit.getLogger().warning("Невозможно запустить цепочку событий т.к какой-то эвент уже запущен");
            return;
        }
        final long intervalInTicks = runInterval * 60 * 20L;
        currentRunningEventsTask = new BukkitRunnable() {
            @Override
            public void run() {
                RunnableEvent next = next();
                runEvent(next);
            }
        }.runTaskTimer(SatanicRaids.getInstance(), intervalInTicks, intervalInTicks);
    }

    private void runEvent(RunnableEvent event) {
        RunTask runTask = new RunTask(event);
        EndTask endTask = new EndTask(event);
        currentStartTask = runTask.startTask();
        currentEndTask = endTask.startTask();
    }

    private RunnableEvent next() {
        int size = avaliableEvents.size();
        if (currentIndex == size) {
            currentIndex = 0;
        }
        RunnableEvent runnableEvent = avaliableEvents.get(currentIndex);
        currentRunningEvent = runnableEvent;
        currentIndex++;
        return runnableEvent;
    }

    private void forceRunByEvent(RunnableEvent event) {
//        TaskHelper.cancelTask(currentStartTask);
//        TaskHelper.cancelTask(currentEndTask);
//        TaskHelper.cancelTask(currentRunningEventsTask);
//       currentRunningEvent.stop();
//        new BukkitRunnable(){
//
//            @Override
//            public void run() {
//                runEvent(event);
//                runEvents();
//            }
//        }.runTaskLater(SatanicRaids.getInstance(),endsIn*20L);
    }



    private void forceRunByInxed(int index) {
        RunnableEvent runnableEvent = avaliableEvents.get(index);
        if (runnableEvent != null) {
            forceRunByEvent(runnableEvent);
        }
        Bukkit.getLogger().warning("Не найден эвент с индексом: " + index);
    }

    public void forceRun(RunnableEvent eventRunner) {
        int index = avaliableEvents.indexOf(eventRunner);
        if (index != -1) {
            forceRunByInxed(index);
        }
    }
}
