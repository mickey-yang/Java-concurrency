package org.example.concurrent.patterns;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Ex2d_Semaphore {

    // Semaphore simple example
    // Semaphores are like locks, but allow multiple threads to access same object
    public static void main(String[] args) throws InterruptedException {

        // Semaphore built in normal way is not fair: threads are accepted randomly in block of code
        Semaphore semaphore = new Semaphore(5);
        try {
            // acquire() is only allowed if there is a permit available
            semaphore.acquire();
            // followed by guarded block of code
        } finally {
            semaphore.release();
        }

        Semaphore fairSemaphore = new Semaphore(5, true);
        try {
            // can acquire() multiple permits
            semaphore.acquire(2);
            // guarded code here
        } finally {
            semaphore.release(2);
        }

        Semaphore noInterruptSemaphore = new Semaphore(5);
        try {
            // by default, if a waiting thread is interrupted it will throw an InterruptedException
            // Uninterruptibility means the thread that is waiting for this semaphore cannot be interrupted,
            // and can only be freed with release() method.
            // Once wait is over and permit is acquired, manifest the effect of any Interrupted Exception, meaning it would throw Exception right away
            semaphore.acquireUninterruptibly();
        } finally {
            semaphore.release();
        }

        Semaphore immediateSemaphore = new Semaphore(5);
        try {
            // tryAcquire() will not wait, and will return false if permit not available
            if(semaphore.tryAcquire(1, TimeUnit.SECONDS)){
                // guarded code
            } else {
                // cannot enter guarded code
            }
        } finally {
            semaphore.release();
        }


    }

}
