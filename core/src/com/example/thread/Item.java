package com.example.thread;

public  class Item implements AwaitableTask {
        private final WorkerTask task;
        private Object result;
        private Throwable throwable;
        private boolean completed;

        public Item(WorkerTask task) {
            this.task = task;
        }

        synchronized void processTask() {
            try {
                this.result = this.task.process();
            } catch (Throwable e) {
                throwable = e;
            } finally {
                completed = true;
                this.notifyAll();
            }
        }

        @Override
        public Object waitTask() throws Throwable {
            synchronized (this) {
                if (!completed) {
                    this.wait();
                }
                if (throwable != null) {
                    throw throwable;
                }
                return this.result;
            }
        }
    }