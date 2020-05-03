import controllers.StandardProcessScheduler;
import engine.FlowEngine;
import processor.Mysql;
import processor.Processor;
import scheduling.LifecycleState;
import scheduling.QuartzSchedulingAgent;
import scheduling.TimerDrivenSchedulingAgent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SchedulingApp {
    public static void main(String[] args) {
        LifecycleState lifecycleState = new LifecycleState();
        AtomicLong schedulingNanos = new AtomicLong(1L);

        AtomicReference<String> schedulingPeriod = new AtomicReference<String>("0 42-43 21 * * ?");
        FlowEngine flowEngine = new FlowEngine(2, "Thread Scheduling");
        TimerDrivenSchedulingAgent timer = new TimerDrivenSchedulingAgent(flowEngine);
        QuartzSchedulingAgent quartz = new QuartzSchedulingAgent(flowEngine);
        Processor mysql = new Mysql("Mysql");
        Processor oracle = new Mysql("Oracle");
//        StandardProcessScheduler process = new StandardProcessScheduler(mysql, schedulingNanos);
//        StandardProcessScheduler processScheduler = new StandardProcessScheduler(oracle, schedulingNanos);
//
//        timer.doSchedule(process, lifecycleState);
//        timer.doSchedule(processScheduler, lifecycleState);
        StandardProcessScheduler process = new StandardProcessScheduler(mysql, schedulingPeriod);
        quartz.doSchedule(process, lifecycleState);
    }
}
