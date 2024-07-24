package org.example.concurrent.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ex2b_ProducerConsumerLockWithTimeout {
    public static void main(String[] args) throws InterruptedException {

        List<Integer> buffer = new ArrayList<>();

        Lock lock = new ReentrantLock();
        Condition isEmpty = lock.newCondition();
        Condition isFull = lock.newCondition();

        class Consumer implements Callable<String> {
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {

                    try {
                        lock.lock();
                        while (isEmpty(buffer)) {
                            // isEmpty stays locked and await() without timeout is never fulfilled because we never reach .signalAll() in Producers
                            // Therefore wrap around timeout, which returns boolean if timedout
                            if (!isEmpty.await(100, TimeUnit.MILLISECONDS)) {
                                throw new TimeoutException("Consumer timed out");
                            }
                        }
                        buffer.remove(buffer.size() - 1);
                        isFull.signalAll();

                    } finally {
                        lock.unlock();
                    }
                }
                return "Consumed " + (count - 1);
            }
        }

        class Producer implements Callable<String> {
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {

                    try {
                        lock.lock();
                        // Simulate exception
                        int i = 0 / 0;

                        while (isFull(buffer)) {
                            isFull.await();
                        }
                        buffer.add(1);
                        // isEmpty will never get signaled because we leave try block after exception above
                        isEmpty.signalAll();

                    } finally {
                        lock.unlock();
                    }
                }
                return "Produced " + (count - 1);
            }
        }

        // Start Algorithm
        System.out.println("Producers and Consumers launched");

        List<Producer> producers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            producers.add(new Producer());
        }

        List<Consumer> consumers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            consumers.add(new Consumer());
        }

        List<Callable<String>> producersAndConsumers = new ArrayList<>();
        producersAndConsumers.addAll(producers);
        producersAndConsumers.addAll(consumers);

        ExecutorService executor = Executors.newFixedThreadPool(8);

        try {
            List<Future<String>> futures = executor.invokeAll(producersAndConsumers);

            futures.forEach(future -> {
                try {
                    System.out.println(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Execption: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            });

        } finally {
            executor.shutdown();
            System.out.println("Executor service shut down");
        }
    }

    public static boolean isEmpty(List<Integer> buffer) {
        return buffer.isEmpty();
    }

    public static boolean isFull(List<Integer> buffer) {
        return buffer.size() == 10;
    }

}
