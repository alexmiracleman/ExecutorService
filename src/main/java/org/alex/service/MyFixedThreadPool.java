package org.alex.service;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


public class MyFixedThreadPool {

    private static volatile boolean isShuttingDown;

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

        public TaskExecutor(Queue<Runnable> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            synchronized (tasks) {
                while (true) {
                    if (!isShuttingDown && !tasks.isEmpty()) {
                        Runnable task = tasks.poll();
                        if (task != null) {
                            task.run();
                        }
                    }
                    if (!isShuttingDown && tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Runnable task = tasks.poll();
                        if(task != null) {
                        task.run();
                        }
                    }
                    if (isShuttingDown && !tasks.isEmpty()) {
                        Runnable task = tasks.poll();
                        if (task != null) {
                            task.run();
                        }
                    }
                    if (isShuttingDown && tasks.isEmpty()) {
                        tasks.notifyAll();
                        break;
                    }
                }
            }
        }
    }
}
