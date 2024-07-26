package org.example.concurrent.patterns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ex2c_HashMapReadWriteLockCache {

    static class CacheWithReadWriteLock {
        // Hashmaps are inherently not threadsafe
        private Map<Long, String> cache = new HashMap<>();
        // ReentrantReadWriteLock is a good default implementation
        private ReadWriteLock lock = new ReentrantReadWriteLock();
        private Lock readLock = lock.readLock();
        private Lock writeLock = lock.writeLock();

        public String get(Long key) {
            // Using RW Lock, all read operations are free and can be made in parallel
            // This is not the case using the synchronized Collection SynchronizedMap
            readLock.lock();
            try {
                return cache.get(key);
            } finally {
                readLock.unlock();
            }
        }

        public String put(Long key, String value) {
            // Write operation will actually lock modification access to single thread only
            writeLock.lock();
            try {
                return cache.put(key, value);
            } finally {
                writeLock.unlock();
            }
        }
    }

    public static void main(String[] args) {

        CacheWithReadWriteLock cache = new CacheWithReadWriteLock();

        class Producer implements Callable<String> {

            private Random rand = new Random();

            @Override
            public String call() throws Exception {
                while (true) {
                    long key = rand.nextInt(1000);
                    cache.put(key, Long.toString(key));
                    // The key might not be put into the map because there are 4 threads running
                    // note that with RW Lock, the following race condition doesn't happen any more
                    if (cache.get(key) == null) {
                        System.out.println("Key " + key + " has not been put in the map");
                    }
                }
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);
        System.out.println("Adding values to map");

        try {
            for (int i = 0; i < 4; i++) {
                executor.submit(new Producer());
            }
        } finally {
            executor.shutdown();
        }

    }

}
