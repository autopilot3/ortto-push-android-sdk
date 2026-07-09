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
            Ortto.log().info("RequestQueue: Job enqueued. Queue size: " + queue.size());
            
            if (!isProcessing) {
                isProcessing = true;
                Ortto.log().info("RequestQueue: Starting processing queue.");
                processNext();
            }
        }
    }

    private void processNext() {
        if (queue.isEmpty()) {
            synchronized (lock) {
                isProcessing = false;
                Ortto.log().info("RequestQueue: Queue is empty. Stopping processing.");
            }
            return;
        }

        int currentJob = jobCounter++;
        Supplier<CompletableFuture<Void>> job = queue.poll();
        Ortto.log().info("RequestQueue: Starting job #" + currentJob + ". Remaining queue size: " + queue.size());
        
        try {
            CompletableFuture<Void> future = job.get();
            
            future.whenComplete((result, error) -> {
                synchronized (lock) {
                    if (error != null) {
                        Ortto.log().warning(String.format("RequestQueue: Job #%d failed with error: %s", currentJob, error.getMessage()));
                    } else {
                        Ortto.log().info("RequestQueue: Job #" + currentJob + " completed successfully.");
                    }
                    processNext();
                }
            });
        } catch (Exception e) {
            synchronized (lock) {
                Ortto.log().warning(String.format("RequestQueue: Exception in job #%d: %s", currentJob, e.getMessage()));
                processNext();
            }
        }
    }

    public void clear() {
        synchronized (lock) {
            queue.clear();
            isProcessing = false;
            Ortto.log().info("RequestQueue: Queue cleared.");
        }
    }
} 