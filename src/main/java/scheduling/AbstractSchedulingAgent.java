package scheduling;

import controllers.StandardProcessScheduler;
import engine.FlowEngine;

abstract class AbstractSchedulingAgent implements SchedulingAgent {

    protected final FlowEngine flowEngine;

    protected AbstractSchedulingAgent(FlowEngine flowEngine) {
        this.flowEngine = flowEngine;
    }

    @Override
    public void schedule(StandardProcessScheduler processScheduler, LifecycleState scheduleState) {
        scheduleState.setScheduled(true);
        this.doSchedule(processScheduler, scheduleState);
    }

    @Override
    public void unschedule(StandardProcessScheduler processScheduler, LifecycleState scheduleState) {
        scheduleState.setScheduled(false);
        this.doUnschedule(processScheduler, scheduleState);
    }

    protected abstract void doSchedule(StandardProcessScheduler processScheduler, LifecycleState scheduleState);

    protected abstract void doUnschedule(StandardProcessScheduler processScheduler, LifecycleState scheduleState);
}