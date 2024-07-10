package org.example.concurrent.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
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

                        }
                        buffer.remove(buffer.size() - 1);
                        // signal

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
                        }
                        buffer.remove(buffer.size() - 1);
                        // signal

                    } finally
                    // Need to add some code before this line, to guarantee that it will be unlocked. E.g. above methods can throw exception, leading to deadlock
                    {
                        lock.unlock();
                    }
                }
                return "Produced " + (count - 1);
            }
        }
    }

    public static boolean isEmpty(List<Integer> buffer) {
        return buffer.isEmpty();
    }
    public static boolean isFull(List<Integer> buffer) {
        return buffer.size() == 10;
    }

}
