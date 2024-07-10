package org.example.concurrent.patterns;

import java.util.concurrent.*;

public class Ex1c_Exceptions {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<String> task = () -> {
            throw new IllegalStateException("I throw an exception in thread " + Thread.currentThread().getName());
        };

        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            for (int i = 0; i < 10; i++) {
                Future<String> future = executor.submit(task);
                System.out.println("I get: " + future.get());
            }
        } finally {
            executor.shutdown();
        }

    }

}
