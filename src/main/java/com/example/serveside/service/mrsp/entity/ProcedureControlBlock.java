package com.example.serveside.service.mrsp.entity;

import java.util.ArrayList;
import java.util.Stack;

public class ProcedureControlBlock {
    /* Resource */
    /* Show that the task is waiting for global resource. */
    public boolean spin;
    /* Determine that the task is accessing the global resource or not. */

    /* task 是否在帮助其他任务完成资源访问 */
    public boolean isHelp;

    public boolean isAccessGlobalResource;

    /* Determine that the task is accessing the local resource or not. */
    public boolean isAccessLocalResource;

    /* remain resource computation time; */
    public int remainResourceComputationTime;

    /* The current request source index(Relative to accessResourceIndex). */
    public int requestResourceTh;

    /* Record the time when the resource is accessed */
    public ArrayList<Integer> resourceAccessTime;

    /* Record the index that the task need to access. */
    public ArrayList<Integer> accessResourceIndex;

    /* Record the system criticality when the task get the resource. */
    public int systemCriticalityWhenAccessResource;

    /* Time that has elapsed since the release of the task, including the blocking, spin delay, interference from high priority task */
    public int elapsedTime;

    /* pure on the core time, including the resource access time */
    public int totalNeededTime;

    /* The time that has running on the cpu core. */
    public int executedTime;

    /* compute and spin time on the cpu core. */
    public int computeAndSpinTime;

    /* Task setup*/
    /* Deadline of the task */
    public int deadline;

    /* the release period */
    public int period;

    /* the utilization of the task */
    public double utilization;

    /* the priority of the task */
    public Stack<Integer> priorities;

    /* Base priority of the task */
    public int basePriority;

    /* The static id, represent a task, not running. */
    public int staticTaskId;

    /* The dynamic id, represent a task at running. */
    public int dynamicTaskId;

    /* 系统一开始分配所在的运行CPU */
    public int baseRunningCpuCore;

    /* 迁移之后所在的运行 CPU */
    public int immigrateRunningCpuCore;

    /* task‘s criticality, 0 is low and 1 is high */
    public int criticality;

    /* task‘s WCCT at HI/LO */
    public int WCCT_low;
    public int WCCT_high;

    /* Constructor function */
    public ProcedureControlBlock(int _priority, int _period, double _utilization, int _task_id)
    {
        priorities = new Stack<>();
        priorities.add(_priority);
        basePriority = _priority;

        period = _period;
        deadline = _period;
        utilization = _utilization;
        staticTaskId = _task_id;
        totalNeededTime = (int)(utilization * period);
        resourceAccessTime = new ArrayList<>();
        accessResourceIndex = new ArrayList<>();

        /* Initialize the procedure control block. */
        dynamicTaskId = -1;
        spin = false;
        elapsedTime = 0;
        executedTime = 0;
        computeAndSpinTime = 0;
        isAccessGlobalResource = false;
        isAccessLocalResource = false;
        remainResourceComputationTime = 0;
        requestResourceTh = 0;
        criticality = 0;
        WCCT_low = 0;
        WCCT_high = 0;
        systemCriticalityWhenAccessResource = 0;

        immigrateRunningCpuCore = -1;
        isHelp = false;
    }

    public ProcedureControlBlock(ProcedureControlBlock copy)
    {
        priorities = new Stack<>();
        priorities.addAll(copy.priorities);
        basePriority = copy.basePriority;

        period = copy.period;
        deadline = copy.period;
        utilization = copy.utilization;
        staticTaskId = copy.staticTaskId;
        totalNeededTime = (int)(utilization * period);
        resourceAccessTime = copy.resourceAccessTime;
        accessResourceIndex = copy.accessResourceIndex;
        baseRunningCpuCore = copy.baseRunningCpuCore;
        criticality = copy.criticality;
        WCCT_low = copy.WCCT_low;
        WCCT_high = copy.WCCT_high;

        /* Initialize the procedure control block. */
        dynamicTaskId = -1;
        spin = false;
        elapsedTime = 0;
        executedTime = 0;
        computeAndSpinTime = 0;
        isAccessGlobalResource = false;
        isAccessLocalResource = false;
        remainResourceComputationTime = 0;
        requestResourceTh = 0;
        systemCriticalityWhenAccessResource = 0;

        immigrateRunningCpuCore = -1;
        isHelp = false;
    }
}
