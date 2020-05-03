package controllers;

import processor.Processor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class StandardProcessScheduler implements Runnable{
    private Processor processor;
    private AtomicLong schedulingNanos;
    private AtomicReference<String> schedulingPeriod;

    public StandardProcessScheduler(Processor processor, AtomicLong schedulingNanos) {
        this.processor = processor;
        this.schedulingNanos = schedulingNanos;
    }
    public StandardProcessScheduler(Processor processor, AtomicReference<String> schedulingPeriod){
        this.processor = processor;
        this.schedulingPeriod = schedulingPeriod;
    }

    @Override
    public synchronized void run() {
         processor.config();
    }
    public long getSchedulingPeriod(final TimeUnit timeUnit) {
        return timeUnit.convert(schedulingNanos.get(), TimeUnit.SECONDS);
    }
    public String getSchedulingPeriod() {
        return schedulingPeriod.get();
    }
}
