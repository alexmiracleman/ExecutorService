package org.alex.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;


public class MyFixedThreadPool {

    private volatile boolean isShuttingDown;

    private final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();


    public MyFixedThreadPool(int threadsCount) {
        for (int i = 0; i < threadsCount; i++) {
            new Thread(new TaskExecutor(), "my-pool-" + (i + 1)).start();
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
        synchronized (tasks) {
            isShuttingDown = true;
            tasks.notifyAll();
        }
    }

    public boolean isShutdown() {
        return isShuttingDown;
    }

    class TaskExecutor implements Runnable {

        @Override
        public void run() {

            while (!isShuttingDown || !tasks.isEmpty()) {
                if (tasks.isEmpty()) {
                    synchronized (tasks) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Thread interrupted ", e);
                        }
                    }
                }
                Runnable task = tasks.poll();
                if (task != null) {
                    task.run();
                }
            }
        }

    }

}
