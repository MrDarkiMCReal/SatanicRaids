package org.mrdarkimc.raidsrecode.eventrunner;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.raidsrecode.TaskHelper;
import org.mrdarkimc.raidsrecode.api.RunnableEvent;

public class EventRunner {
    //    private int currentIndex = 0;
//    private int runInterval = 60; //in minutes
    private RunnableEvent event;
    //private BukkitTask startTask;
    private BukkitTask plannedEndTask;
//    private BukkitTask currentRunningEventsTask;

    public EventRunner() {
    }

    public void setEvent(RunnableEvent event) {
        this.event = event;
    }

//    public void runEvents() {
//        if (currentRunningEvent != null) {
//            Bukkit.getLogger().warning("Невозможно запустить цепочку событий т.к какой-то эвент уже запущен");
//            return;
//        }
//        final long intervalInTicks = runInterval * 60 * 20L;
//        currentRunningEventsTask = new BukkitRunnable() {
//            @Override
//            public void run() {
//                RunnableEvent next = next();
//                runEvent(next);
//            }
//        }.runTaskTimer(SatanicRaids.getInstance(), intervalInTicks, intervalInTicks);
//    }

    public void runEvent() {
        RunTask runTask = new RunTask(event);
        runTask.startTask();
        //event.start();
        EndTask endTask = new EndTask(event);
        plannedEndTask = endTask.startTask();
    }

//    private RunnableEvent next() {
//        int size = avaliableEvents.size();
//        if (currentIndex == size) {
//            currentIndex = 0;
//        }
//        RunnableEvent runnableEvent = avaliableEvents.get(currentIndex);
//        currentRunningEvent = runnableEvent;
//        currentIndex++;
//        return runnableEvent;
//    }

    private void forceRunByEvent(RunnableEvent event) {
        Bukkit.getLogger().warning("not implemented yet");
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

    public void forceStop() {
        TaskHelper.cancelTask(plannedEndTask);
        event.stop();
    }

//    private void forceRunByInxed(int index) {
//        RunnableEvent runnableEvent = avaliableEvents.get(index);
//        if (runnableEvent != null) {
//            forceRunByEvent(runnableEvent);
//        }
//        Bukkit.getLogger().warning("Не найден эвент с индексом: " + index);
//    }

//    public void forceRun(RunnableEvent eventRunner) {
//        int index = avaliableEvents.indexOf(eventRunner);
//        if (index != -1) {
//            forceRunByInxed(index);
//        }
//    }
}
