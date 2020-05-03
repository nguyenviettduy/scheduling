package scheduling;

import controllers.StandardProcessScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import engine.FlowEngine;
import util.FormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TimerDrivenSchedulingAgent extends AbstractSchedulingAgent {

    public TimerDrivenSchedulingAgent(final FlowEngine flowEngine) {
        super(flowEngine);
    }

    @Override
    public void shutdown() {
        flowEngine.shutdown();
    }

    @Override
    public void doSchedule(final StandardProcessScheduler processScheduler, final LifecycleState scheduleState) {
        final long schedulingNanos = processScheduler.getSchedulingPeriod(TimeUnit.SECONDS);

        final ScheduledFuture<?> future = flowEngine.scheduleWithFixedDelay(processScheduler, 0L, schedulingNanos, TimeUnit.SECONDS);
        final List<ScheduledFuture<?>> futures = new ArrayList<>(1);
        futures.add(future);
        scheduleState.setFutures(futures);
    }

    @Override
    public void doUnschedule(final StandardProcessScheduler connectable, final LifecycleState scheduleState) {
        for (final ScheduledFuture<?> future : scheduleState.getFutures()) {
            // stop scheduling to run but do not interrupt currently running tasks.
            future.cancel(false);
        }
    }

    @Override
    public void setMaxThreadCount(final int maxThreads) {
    }

    @Override
    public void incrementMaxThreadCount(int toAdd) {
        final int corePoolSize = flowEngine.getCorePoolSize();
        if (toAdd < 0 && corePoolSize + toAdd < 1) {
            throw new IllegalStateException("Cannot remove " + (-toAdd) + " threads from pool because there are only " + corePoolSize + " threads in the pool");
        }

        flowEngine.setCorePoolSize(corePoolSize + toAdd);
    }
}