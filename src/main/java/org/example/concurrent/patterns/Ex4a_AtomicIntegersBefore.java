package org.example.concurrent.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Ex4a_AtomicIntegersBefore {

    private static int counter = 0;

    public static void main(String[] args) {

        class Incrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    counter++;
                }
            }
        }

        class Decrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    counter--;
                }
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();

        try {
//            Create 4 threads each, which increment or decrement 1000 times
            for (int i = 0; i < 4; i++) {
                futures.add(executorService.submit(new Incrementer()));
            }

            for (int i = 0; i < 4; i++) {
                futures.add(executorService.submit(new Decrementer()));
            }

//            Execute all 8 Runnables concurrently
            futures.forEach(
                    future -> {
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            System.out.println(e.getMessage());
                        }
                    }
            );

//            Concurrency problem on counter
            System.out.println("counter = " + counter);


        } finally {
            executorService.shutdown();
        }

    }

}
