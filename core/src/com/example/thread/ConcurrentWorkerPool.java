package com.example.thread;/* Let you imagine that you are code reviewer for some task.
 Please review this code snippet.
 Notify about all drawbacks (bugs, stylistics, logical mistakes, etc...) in this code.
 As many as you can.
*/

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentWorkerPool {
    private final ConcurrentLinkedQueue<Item> queue = new ConcurrentLinkedQueue<>();
    private boolean running;


    private void threadFunc() {
        while (this.running) {
            Item task = this.queue.poll();
            if (task == null) {
                synchronized (this.queue) {
                    try {
                        this.queue.wait();
                    } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                    }
                }
            } else {
                task.processTask();
            }
        }
    }


    public ConcurrentWorkerPool(int countThreads) {
        this.running = true;
        for (int i = 0; i < countThreads; i++) {
            new Thread(this::threadFunc).start();
        }
    }

    //remove throws InterruptedException
    public void stop() {
        this.running = false;
    }

    //Camelcase for method name
    public AwaitableTask addTask(WorkerTask task) {
        Item item = new Item(task);
        synchronized (this.queue) {
            this.queue.add(item);
            this.queue.notify();
        }
        return item;
    }

    //Add curly braces for if
    static int reccursion(int val) {
        if (val == 1) {
            return 1;
        }
        return val + reccursion(val - 1);
    }

    //remove  throws InterruptedException
    public static void main(String[] args) {
        ConcurrentWorkerPool pool = new ConcurrentWorkerPool(2);
        List<AwaitableTask> tasks = new LinkedList<>();
        tasks.add(pool.addTask(() -> "Whatever..."));
        tasks.add(pool.addTask(() -> {
            throw new RuntimeException();
        }));
        tasks.add(pool.addTask(() -> reccursion(25)));
        tasks.stream()
                .map(task -> {
                    try {
                        return task.waitTask();
                    } catch (Throwable e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(System.out::println);
        pool.stop();
        System.exit(0);
    }
}