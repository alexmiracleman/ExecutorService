package org.alex.service;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyFixedThreadPool {

    private volatile boolean isShuttingDown;

    private final Queue<Runnable> tasks = new LinkedBlockingQueue<>();

    public MyFixedThreadPool(int threadsCount) {
        for (int i = 0; i < threadsCount; i++) {
            new Thread(new TaskExecutor(tasks), "my-pool-" + (i + 1)).start();
        }
    }

    public void execute(Runnable task) {
        if (isShuttingDown) {
            throw new RejectedExecutionException("Shutting down");
        }
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
    }

    public void shutdown() {
        isShuttingDown = true;
    }


    public boolean isShutdown() {
        return isShuttingDown;
    }


    static class TaskExecutor implements Runnable {

        private final Queue<Runnable> tasks;

        private final AtomicBoolean active = new AtomicBoolean(true);

        public TaskExecutor(Queue<Runnable> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            synchronized (tasks) {
                while (active.get()) {
                    if (tasks.isEmpty()) {
                        try {
                            tasks.wait(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Runnable task = tasks.poll();
                    if (task != null) {
                        task.run();
                    }
                    if (task == null) {
                        active.set(false);
                    }
                }
            }
        }
    }
}
