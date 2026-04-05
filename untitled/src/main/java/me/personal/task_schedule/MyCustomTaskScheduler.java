package me.personal.task_schedule;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

class CustomWorker implements Runnable {
    private final String name;
    private final CustomBLockingPriorityQueue<Task> queue;
    private final Random random = new Random();
    private final Phaser wg;

    public CustomWorker(String name, Phaser wg, CustomBLockingPriorityQueue<Task> sharedQueue) {
        this.queue = sharedQueue;
        this.name = name;
        this.wg = wg;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Task task = queue.poll();
                if (task == null) continue;
                Thread.sleep(random.nextInt(100, 500));
                wg.arriveAndDeregister();
                Logger.getAnonymousLogger().info(this + " has done " + task);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "Worker " + name;
    }
}

public class MyCustomTaskScheduler {
    private final static String CLASS_NAME = MyCustomTaskScheduler.class.getSimpleName();
    private final Phaser wg = new Phaser();
    private final CustomBLockingPriorityQueue<Task> queue = new CustomBLockingPriorityQueue<>();
    private final List<CustomWorker> workerList = new ArrayList<>();

    public MyCustomTaskScheduler(int numberOfWorker) {
        for (int i = 0; i < numberOfWorker; i++) {
            workerList.add(new CustomWorker("Worker " + i, wg, queue));
        }
    }

    public void start() {
        for (CustomWorker worker : workerList) {
            Thread t = new Thread(worker);
            t.setDaemon(true);
            t.start();
        }
    }

    public void submitTask(Task task) {
        wg.register();
        queue.add(task);
    }

    // gracefully stop
    public void stop() {
        wg.arriveAndAwaitAdvance();
        Logger.getAnonymousLogger().info(String.format("%s has been stop", CLASS_NAME));
    }

    public static void main(String[] args) {
        MyCustomTaskScheduler scheduler = new MyCustomTaskScheduler(10);
        scheduler.start();
        Random random = new Random();
        int numberOfTasks = 100;
        for (int i = 0; i < numberOfTasks; i++) {
            Task task = new Task(String.format("Task name %d", i), random.nextInt(1, 10));
            scheduler.submitTask(task);
        }

        scheduler.stop();
    }
}

@RequiredArgsConstructor
class CustomBLockingPriorityQueue<T> {
    private final PriorityQueue<T> queue = new PriorityQueue<>();
    private final AtomicInteger sz = new AtomicInteger(0);

    public T poll() {
        synchronized (queue) {
            if (!queue.isEmpty()) {
                sz.decrementAndGet();
                return queue.poll();
            }
            return null;
        }
    }

    public int getSize() {
        return sz.get();
    }

    public void add(T t) {
        synchronized (queue) {
            sz.incrementAndGet();
            queue.add(t);
        }
    }
}