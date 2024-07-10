package org.example.concurrent.patterns;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Ex4b_AtomicIntegersAfter {

    private static MyAtomicCounter counter = new MyAtomicCounter(0);

    public static void main(String[] args) {

        class Incrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    counter.myIncrementAndGet();
                }
            }
        }

        class Decrementer implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    counter.decrementAndGet();
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
            System.out.println("# incrementations = " + counter.getIncrements());


        } finally {
            executorService.shutdown();
        }

    }

    //  Custom class to count number of times AtomicInteger had operated
    private static class MyAtomicCounter extends AtomicInteger {

        private static Unsafe unsafe = null;

        static {
            Field unsafeField;
            try {
                // In Unsafe class there is the internal Unsafe instance called "theUnsafe"
                unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = (Unsafe) unsafeField.get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Internal counter for number of retries caused by concurrency
        private AtomicInteger countIncrement = new AtomicInteger(0);

        public MyAtomicCounter(int counter) {
            super(counter);
        }

        public int myIncrementAndGet() {

            long valueOffset = 0L;
            try {
                // valueOffset is the location in memory
                valueOffset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
            } catch (NoSuchFieldException | SecurityException exception) {
                exception.printStackTrace();
            }
            int v;
            do {
                v = unsafe.getIntVolatile(this, valueOffset);
                // internal AtomicInteger increments
                countIncrement.incrementAndGet();
                // For the object [this] and the heap memory location [valueOffset], check if it equals [v]. If so, replace with [v+1]
                // If another thread is already writing to this value, then the value at [valueOffset] will not equal v, so this loop repeats
            } while (!unsafe.compareAndSwapInt(this, valueOffset, v, v+1));
            /*
             *  From javadocs for Unsafe.class:
             *  o – Java heap object in which the variable resides, if any, else null
             *  offset – indication of where the variable resides in a Java heap object, if any, else a memory address locating the variable statically
             */
            return v;
        }

        public int getIncrements() {
            return this.countIncrement.get();
        }


    }


}
