package scheduling;

import controllers.StandardProcessScheduler;
import engine.FlowEngine;
import exception.ProcessException;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FormatUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuartzSchedulingAgent extends AbstractSchedulingAgent {

    private final Logger logger = LoggerFactory.getLogger(QuartzSchedulingAgent.class);
    private final Map<Object, List<AtomicBoolean>> canceledTriggers = new HashMap<>();

    public QuartzSchedulingAgent(final FlowEngine flowEngine) {
        super(flowEngine);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public synchronized void doSchedule(final StandardProcessScheduler processScheduler, final LifecycleState scheduleState) {

        final String cronSchedule = processScheduler.getSchedulingPeriod();
        final CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(cronSchedule);
        } catch (final Exception pe) {
            throw new IllegalStateException("Cannot schedule " + " to run because its scheduling period is not valid");
        }

        final List<AtomicBoolean> triggers = new ArrayList<>();

        final AtomicBoolean canceled = new AtomicBoolean(false);

        final Date initialDate = cronExpression.getTimeAfter(new Date());
        final long initialDelay = initialDate.getTime() - System.currentTimeMillis();

        

        final Runnable command = new Runnable() {

            private Date nextSchedule = initialDate;

            @Override
            public void run() {
                if (canceled.get()) {
                    return;
                }

                try {
                    processScheduler.run();
                    System.out.println(":)))");
                } catch (final RuntimeException re) {
                    throw re;
                } catch (final Exception e) {
                    throw new ProcessException(e);
                }

                nextSchedule = getNextSchedule(nextSchedule, cronExpression);
                final long delay = getDelay(nextSchedule);

                flowEngine.schedule(processScheduler, delay, TimeUnit.SECONDS);
            }
        };

        flowEngine.schedule(command, initialDelay, TimeUnit.SECONDS);
        triggers.add(canceled);
    }

    @Override
    public synchronized void doUnschedule(final StandardProcessScheduler processScheduler, final LifecycleState scheduleState) {
        unschedule((Object) processScheduler, scheduleState);
    }

    private void unschedule(final Object scheduled, final LifecycleState scheduleState) {
        final List<AtomicBoolean> triggers = canceledTriggers.remove(scheduled);
        if (triggers == null) {
            throw new IllegalStateException("Cannot unschedule " + scheduled + " because it was not scheduled to run");
        }

        for (final AtomicBoolean trigger : triggers) {
            trigger.set(true);
        }

        scheduleState.setScheduled(false);
        logger.info("Stopped scheduling {} to run", scheduled);
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

    private static Date getNextSchedule(final Date currentSchedule, final CronExpression cronExpression) {
        // Since the clock has not a millisecond precision, we have to check that we
        // schedule the next time after the time this was supposed to run, otherwise
        // we might end up with running the same task twice
        final Date now = new Date();
        return cronExpression.getTimeAfter(now.after(currentSchedule) ? now : currentSchedule);
    }

    private static long getDelay(Date nextSchedule) {
        return Math.max(nextSchedule.getTime() - System.currentTimeMillis(), 0L);
    }
}