package me.personal.task_schedule;

import java.util.*;

interface TaskScheduler {
    void addTask(Task task);

    void start();

    void stop();
}

// ========================
// PULL: workers pull tasks from a shared queue themselves
// ========================
class PullTaskScheduler implements TaskScheduler {
    private final Object lock = new Object();
    private final PriorityQueue<Task> taskQueue = new PriorityQueue<>();
    private final List<Thread> threads = new ArrayList<>();
    private final int numberOfWorker;
    private volatile boolean running = false;

    public PullTaskScheduler(int numberOfWorker) {
        this.numberOfWorker = numberOfWorker;
    }

    @Override
    public void addTask(Task task) {
        synchronized (lock) {
            taskQueue.add(task);
            lock.notifyAll();
        }
    }

    @Override
    public void start() {
        running = true;
        for (int i = 0; i < numberOfWorker; i++) {
            Thread thread = new Thread(() -> {
                while (running) {
                    Task task;
                    synchronized (lock) {
                        while (taskQueue.isEmpty() && running) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        task = taskQueue.poll();
                    }
                    if (task != null) {
                        process(task);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
    }

    @Override
    public void stop() {
        running = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void process(Task task) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        System.out.println(Thread.currentThread().getName() + " processed " + task.getName());
    }
}

// ========================
// PUSH: scheduler assigns tasks to specific workers
// ========================
class PushTaskScheduler implements TaskScheduler {
    private final Object lock = new Object();
    private final PriorityQueue<Task> taskQueue = new PriorityQueue<>();
    private final List<Worker> workerList = new ArrayList<>();
    private volatile boolean running = false;

    public PushTaskScheduler(int numberOfWorker) {
        for (int i = 0; i < numberOfWorker; i++) {
            workerList.add(new Worker("worker-" + i));
        }
    }

    @Override
    public void addTask(Task task) {
        synchronized (lock) {
            taskQueue.add(task);
            lock.notifyAll();
        }
    }

    @Override
    public void start() {
        running = true;
        // start all worker threads
        for (Worker worker : workerList) {
            worker.start();
        }
        // dispatcher thread decides which worker gets which task
        Thread dispatcherThread = new Thread(() -> {
            while (running) {
                Task task;
                synchronized (lock) {
                    while (taskQueue.isEmpty() && running) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    task = taskQueue.poll();
                }
                if (task != null) {
                    assignToWorker(task);
                }
            }
        });
        dispatcherThread.start();
    }

    private void assignToWorker(Task task) {
        while (running) {
            for (Worker worker : workerList) {
                if (worker.isAvailable()) {
                    worker.submit(task);
                    return;
                }
            }
            // no worker available, brief pause before retrying
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void stop() {
        running = false;
        synchronized (lock) {
            lock.notifyAll();
        }
        for (Worker worker : workerList) {
            worker.shutdown();
        }
    }
}

class Worker {
    private final String name;
    private final Object inbox = new Object();
    private final Queue<Task> taskQueue = new LinkedList<>();
    private volatile boolean running = false;
    private volatile boolean idle = true;

    public Worker(String name) {
        this.name = name;
    }

    public void start() {
        running = true;
        Thread thread = new Thread(() -> {
            while (running) {
                Task task;
                synchronized (inbox) {
                    while (taskQueue.isEmpty() && running) {
                        try {
                            idle = true;
                            inbox.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    task = taskQueue.poll();
                }
                if (task != null) {
                    idle = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    System.out.println(name + " processed " + task.getName());
                }
            }
        }, name);
        thread.start();
    }

    public void submit(Task task) {
        synchronized (inbox) {
            taskQueue.add(task);
            inbox.notifyAll();
        }
    }

    public boolean isAvailable() {
        return idle;
    }

    public void shutdown() {
        running = false;
        synchronized (inbox) {
            inbox.notifyAll();
        }
    }
}

public class GPTTaskScheduler {
    public static void main(String[] args) {
        int numberOfWorkingThread = 10;
        int numberOfTask = 1000;

        // Switch between strategies:
        TaskScheduler scheduler = new PullTaskScheduler(numberOfWorkingThread);
        // TaskScheduler scheduler = new PushTaskScheduler(numberOfWorkingThread);

        scheduler.start();

        for (int i = 1; i <= numberOfTask; i++) {
            scheduler.addTask(new Task("task " + i, 1));
        }
    }
}