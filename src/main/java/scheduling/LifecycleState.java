package scheduling;

import exception.TerminatedTaskException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LifecycleState {

    private final AtomicInteger activeThreadCount = new AtomicInteger(0);
    private final AtomicBoolean scheduled = new AtomicBoolean(false);
    private final Set<ScheduledFuture<?>> futures = new HashSet<>();
    private final AtomicBoolean mustCallOnStoppedMethods = new AtomicBoolean(false);
    private volatile long lastStopTime = -1;
    private volatile boolean terminated = false;

    public synchronized int incrementActiveThreadCount() {
        if (terminated) {
            throw new TerminatedTaskException();
        }
        return activeThreadCount.incrementAndGet();
    }

    public synchronized int decrementActiveThreadCount() {
        if (terminated) {
            return activeThreadCount.get();
        }
        return activeThreadCount.decrementAndGet();
    }

    public int getActiveThreadCount() {
        return activeThreadCount.get();
    }

    public boolean isScheduled() {
        return scheduled.get();
    }

    void setScheduled(final boolean scheduled) {
        this.scheduled.set(scheduled);
        mustCallOnStoppedMethods.set(true);

        if (!scheduled) {
            lastStopTime = System.currentTimeMillis();
        }
    }

    public long getLastStopTime() {
        return lastStopTime;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("activeThreads:").append(activeThreadCount.get()).append("; ")
                .append("scheduled:").append(scheduled.get()).append("; ").toString();
    }

    /**
     * Maintains an AtomicBoolean so that the first thread to call this method after a Processor is no longer
     * scheduled to run will receive a <code>true</code> and MUST call the methods annotated with
     * {@link OnStopped @OnStopped}
     *
     * @return <code>true</code> if the caller is required to call Processor methods annotated with @OnStopped
     */
    public boolean mustCallOnStoppedMethods() {
        return mustCallOnStoppedMethods.getAndSet(false);
    }

    /**
     * Establishes the list of relevant futures for this processor. Replaces any previously held futures.
     *
     * @param newFutures futures
     */
    public synchronized void setFutures(final Collection<ScheduledFuture<?>> newFutures) {
        futures.clear();
        futures.addAll(newFutures);
    }

    public synchronized void replaceFuture(final ScheduledFuture<?> oldFuture, final ScheduledFuture<?> newFuture) {
        futures.remove(oldFuture);
        futures.add(newFuture);
    }

    public synchronized Set<ScheduledFuture<?>> getFutures() {
        return Collections.unmodifiableSet(futures);
    }

    public synchronized void terminate() {
        this.terminated = true;
        activeThreadCount.set(0);
    }

    public void clearTerminationFlag() {
        this.terminated = false;
    }

    public boolean isTerminated() {
        return this.terminated;
    }
}
