package org.example.concurrent.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ex2a_ProducerConsumerLock {
    public static void main(String[] args) throws InterruptedException {

        // Shared buffer, just a list of digits
        List<Integer> buffer = new ArrayList<>();

        // Standard implementation of Lock
        Lock lock = new ReentrantLock();
        Condition isEmpty = lock.newCondition();
        Condition isFull = lock.newCondition();

        class Consumer implements Callable<String> {
            // Implements single method Call that returns String
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {

                    try {
                        lock.lock();
                        // If buffer is empty, then wait
                        while (isEmpty(buffer)) {
                            // wait
                            // {isEmpty} Condition freezes this thread until this condition switches
                            // Unfreezes once buffer isn't empty anymore - see Producer buffer.add() step
                            isEmpty.await();
                        }
                        buffer.remove(buffer.size() - 1);
                        // signal
                        // Buffer is no longer full, signal Producer's {isFull} Condition to unfreeze that thread
                        isFull.signalAll();

                    } finally
                    // Need to add some code before this line, to guarantee that it will be unlocked. E.g. above methods can throw exception, leading to deadlock
                    {
                        lock.unlock();
                    }
                }
                return "Consumed " + (count - 1);
            }
        }

        class Producer implements Callable<String> {
            // Implements single method Call that returns String
            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++ < 50) {

                    try {
                        lock.lock();
                        // If buffer is full, then wait
                        while (isFull(buffer)) {
                            // wait
                            // Freeze the Producer. Unfrozen by Consumer once the buffer
                            isFull.await();
                        }
                        buffer.add(1);
                        // signal
                        // Need to unfreeze the {isEmpty} Condition for Consumer to continue
                        isEmpty.signalAll();

                    } finally
                    // Need to add some code before this line, to guarantee that it will be unlocked. E.g. above methods can throw exception, leading to deadlock
                    {
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
            // .invokeAll() takes list of Callables, invokes all, returns list of Futures from each Callable
            // Each Callabe was set up to either Produce or Consume 50 times into the same buffer, and finally return # of actions as a String log
            List<Future<String>> futures = executor.invokeAll(producersAndConsumers);

            futures.forEach(future -> {
                try {
                    System.out.println(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Execption: " + e.getMessage());
                    // Add extra line to interrupt this thread given an InterruptedException is thrown. Sonar Lint rule to prevent delaying Thread interruption
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
