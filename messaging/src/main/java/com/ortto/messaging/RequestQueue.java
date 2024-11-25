package com.ortto.messaging;

import android.util.Log;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RequestQueue {
    private final Queue<Supplier<CompletableFuture<Void>>> queue = new LinkedList<>();
    private boolean isProcessing = false;
    private final Object lock = new Object();
    private int jobCounter = 0;

    public void enqueue(Supplier<CompletableFuture<Void>> job) {
        synchronized (lock) {
            queue.offer(job);
            
            if (!isProcessing) {
                isProcessing = true;
                processNext();
            }
        }
    }

    private void processNext() {
        if (queue.isEmpty()) {
            synchronized (lock) {
                isProcessing = false;
            }
            return;
        }

        int currentJob = jobCounter;
        Supplier<CompletableFuture<Void>> job = queue.poll();
        
        try {
            CompletableFuture<Void> future = job.get();
            
            future.whenComplete((result, error) -> {
                synchronized (lock) {
                    if (error != null) {
                        Log.e("ortto@q", String.format("Job #%d: Failed with error: %s", currentJob, error.getMessage()));
                    }
                    processNext();
                }
            });
        } catch (Exception e) {
            synchronized (lock) {
                processNext();
            }
        }
    }

    public void clear() {
        synchronized (lock) {
            queue.clear();
            isProcessing = false;
        }
    }
} 