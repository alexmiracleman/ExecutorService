package org.alex.service;


public class ExecutorServiceTest {

    static MyFixedThreadPool executorService = new MyFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            System.out.println("Thread name: " + Thread.currentThread().getName());
        };

        for (int i = 0; i < 10; i++) {
            executorService.execute(runnable);
        }
//        Thread.sleep(1000);

        System.out.println("Finished");

        executorService.shutdown();
    }
}
