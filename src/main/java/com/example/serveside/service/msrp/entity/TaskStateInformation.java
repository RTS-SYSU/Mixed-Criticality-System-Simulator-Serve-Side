package com.example.serveside.service.msrp.entity;

import java.util.ArrayList;

/* This class Stores a single state in the lifecycle of the task */
class SingleTaskState {

    public enum TASK_STATE {
        PREEMPTED,
        ARRIVAL_BLOCK,
        DIRECT_SPIN,
        INDIRECT_SPIN,
        RUNNING,
        RUNNING_WITH_LOCK,
        KILLED,
    }

    public TASK_STATE state;

    public int beginTime;
    
    public int endTime;

    public int resourceId;

    public int priority;

    public SingleTaskState(TASK_STATE _state, int _beginTime, int _endTime, int _resourceId, int _priority) {
        state = _state;
        beginTime = _beginTime;
        endTime = _endTime;
        resourceId = _resourceId;
        priority = _priority;
    }

    public SingleTaskState(SingleTaskState copy) {
        state = copy.state;
        beginTime = copy.beginTime;
        endTime = copy.endTime;
        resourceId = copy.resourceId;
        priority = copy.priority;
    }

    public String getState() {
        switch (state) {
            case PREEMPTED:
                return "Preempted";

            case ARRIVAL_BLOCK:
                return "Arrival block";

            case DIRECT_SPIN:
                return "Direct spinning for resource " + resourceId;

            case INDIRECT_SPIN:
                return "Indirect spinning for resource " + resourceId;

            case RUNNING:
                return "Executing without lock";

            case RUNNING_WITH_LOCK:
                return "Executing with resource " + resourceId;

            case KILLED:
                return "Killed";

            default:
                return "";
        }
    }
}

public class TaskStateInformation {
    
    /* Stores all states of task in chronological order */
    public ArrayList<SingleTaskState> taskStates;

    public int staticTaskId;

    public int dynamicTaskId;

    public int runningCpuCore;

    public int priority;

    public int resourceId;

    public int remainResourceComputationTime;

    public boolean live;

    public TaskStateInformation(int _staticTaskId, int _dynamicTaskId, int _priority, int _runningCpuCore) {
        taskStates = new ArrayList<>();
        staticTaskId = _staticTaskId;
        dynamicTaskId = _dynamicTaskId;
        runningCpuCore = _runningCpuCore;
        priority = _priority;
        resourceId = -1;
        remainResourceComputationTime = 0;
        live = true;
    }

    public TaskStateInformation(TaskStateInformation copy) {
        taskStates = new ArrayList<>();
        staticTaskId = copy.staticTaskId;
        dynamicTaskId = copy.dynamicTaskId;
        runningCpuCore = copy.runningCpuCore;
        priority = copy.priority;
        resourceId = copy.resourceId;
        remainResourceComputationTime = copy.remainResourceComputationTime;
        for (SingleTaskState sts : copy.taskStates) {
            taskStates.add(new SingleTaskState(sts));
        }
    }

    /* Add a new state for task. */
    public void addState(SingleTaskState.TASK_STATE _state, int _beginTime) {
        taskStates.add(new SingleTaskState(_state, _beginTime, -1, -1, priority));
    }
    
    public void addState(SingleTaskState.TASK_STATE _state, int _beginTime, int _resourceId) {
        taskStates.add(new SingleTaskState(_state, _beginTime, -1, _resourceId, priority));
    }

    public void killTask(int _time) {
        if (!live)
            return;
        endState(_time);
        taskStates.add(new SingleTaskState(SingleTaskState.TASK_STATE.KILLED, _time, _time, -1, -1));
        live = false;
    }

    /* End previous state of task if existed. */
    public void endState(int _endTime) {
        // return if previous state does not exist.
        if (taskStates.isEmpty())
            return;
        SingleTaskState ts = taskStates.get(taskStates.size() - 1);
        // if previous state not end.
        if (ts.endTime == -1)
            ts.endTime = _endTime;
        // drop if previous state last zero second.
        if (ts.endTime == ts.beginTime && ts.state != SingleTaskState.TASK_STATE.KILLED)
            taskStates.remove(ts);
        // update remain resource computation time;
        if (ts.state == SingleTaskState.TASK_STATE.RUNNING_WITH_LOCK) {
            remainResourceComputationTime = remainResourceComputationTime - (ts.endTime - ts.beginTime);
            if (remainResourceComputationTime == 0)
                resourceId = -1;
        }
    }

    /* Print task state information. */
    public void printTaskStates() {
        if (taskStates.size() == 0 || taskStates.get(0).endTime == -1)
            return;
        System.out.printf("\tStatic task id: %d\n", staticTaskId);
        System.out.printf("\tDynamic task id: %d\n", dynamicTaskId);
        for (SingleTaskState sts : taskStates) {
            System.out.printf("\t\tState: %s\n", sts.getState());
            System.out.printf("\t\tTime Interval: [%d, %d)\n", sts.beginTime, sts.endTime);
            System.out.printf("\t\tPriority: %d\n\n", sts.priority);
        }
    }
}
