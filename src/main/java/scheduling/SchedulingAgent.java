package scheduling;

import controllers.StandardProcessScheduler;

import java.util.concurrent.TimeUnit;

public interface SchedulingAgent {
    void schedule(StandardProcessScheduler processScheduler, LifecycleState scheduleState);

    void unschedule(StandardProcessScheduler processScheduler, LifecycleState scheduleState);

    void setMaxThreadCount(int maxThreads);

    void incrementMaxThreadCount(int toAdd);

    void shutdown();
}
