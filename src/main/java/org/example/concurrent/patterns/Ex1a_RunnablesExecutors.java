package org.example.concurrent.patterns;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ex1a_RunnablesExecutors {

    public static void main(String[] args) {
        runnableExample();
        executorExample();

    }


    public static void runnableExample() {
        System.out.println("I am in thread " + Thread.currentThread().getName());
        System.out.println("Runnable: ");

        Runnable task = () -> System.out.println("Runnable: I am in thread " + Thread.currentThread().getName());

        for (int i = 0; i < 10; i++) {
            new Thread(task).start();
        }

    }

    public static void executorExample() {
        System.out.println("I am in thread " + Thread.currentThread().getName());
        System.out.println("Executor: ");

        Runnable task = () -> System.out.println("Executor: I am in thread " + Thread.currentThread().getName());

//        ExecutorService executor = Executors.newSingleThreadExecutor();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++) {
            executor.execute(task);
        }

        // Executors need to be shut down
        executor.shutdown();

    }


}
