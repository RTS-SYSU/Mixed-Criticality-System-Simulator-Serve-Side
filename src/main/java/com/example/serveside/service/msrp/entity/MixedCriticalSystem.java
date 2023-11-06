package com.example.serveside.service.msrp.entity;

import com.example.serveside.response.EventInformation;
import com.example.serveside.response.EventTimePoint;
import com.example.serveside.response.GanttInformation;
import com.example.serveside.response.TaskInformation;
import com.example.serveside.service.msrp.utils.ShowEventInformation;
import com.example.serveside.service.msrp.utils.ShowTaskStates;
import com.example.serveside.service.msrp.entity.SingleTaskState.TASK_STATE;
import com.example.serveside.service.CommonUse.BasicPCB;
import com.example.serveside.service.CommonUse.BasicResource;

import java.util.*;

public class MixedCriticalSystem {
    /* System Setup */

    /* the criticality of system, 0 is low, 1 is high */
    public static int criticality_indicator = 0;
    /* The cpu core num. */
    public static int TOTAL_CPU_CORE_NUM = 2;

    /* System Execute */
    /* system clock, to record the time. */
    public static int systemClock;

    /* The num of the released task. */
    public static int releaseTaskNum;

    /* generate total tasks that the system will execute. */
    public static ArrayList<ProcedureControlBlock>  totalTasks;

    /* generate total tasks that the system has. */
    public static ArrayList<Resource> totalResources;

    /* record the times of the task that has been finished. */
    public static int[] taskFinishTimes;

    /* create a ArrayList to keep the information about the running task. */
    public static ArrayList<ProcedureControlBlock> runningTaskPerCore;

    /* create a ArrayList to save the task that is looking for cpu core. */
    public static ArrayList<ArrayList<ProcedureControlBlock>> waitingTasksPerCore;

    /* create an int array list to show that the task can be release or not. */
    public static int[] timeSinceLastRelease;

    /* task witch can release resource */
    public static ArrayList<ProcedureControlBlock> ReleaseResourceTask;

    /* Save the CPUEventInformation. */
    public static ArrayList<ArrayList<com.example.serveside.service.msrp.entity.EventInformation>> eventRecords;

    /* Save the time point that the event occurs. */
    public static ArrayList<ArrayList<EventTimePoint>> CPUEventTimePointsRecords;

    /* Keep the running event. */
    public static ArrayList<com.example.serveside.service.msrp.entity.EventInformation> runningEvents;

    /* Save the IndicatorInformation. */
    public static ArrayList<com.example.serveside.service.msrp.entity.EventInformation> indicatorRecords;

    /* Store states for each task. */
    public static ArrayList<TaskStateInformation> taskStates;

    /* Save the time point that the event occurs. */
    public static ArrayList<ArrayList<EventTimePoint>> TaskEventTimePointsRecords;

    /* The time axis length*/
    public static Integer timeAxisLength;

    /* Record the release task's information and send it to client-side. */
    public static List<TaskInformation> releaseTaskInformations;

    /* 发生关键级切换的时间点 */
    public static Integer criticalitySwitchTime;

    /* 用以判断当前协议下的进程是否可以完成 */
    public static Boolean isSchedulable;

    /* 任务释放的时间 */
    public static LinkedHashMap<Integer, ArrayList<Integer>> taskReleaseTimes;

    /*
     * 遍历哈希表 taskReleaseTimes 的迭代器
     * */
    public static Iterator<Map.Entry<Integer, ArrayList<Integer>>> iteratorTaskReleaseTimes;

    /* 下一个任务释放的时间点 */
    public static Map.Entry<Integer, ArrayList<Integer>> itemTaskReleaseTimes = null;

    /* 构造函数：初始化系统中任务以及资源的基本信息 */
    public static void MSRPInitialize(ArrayList<BasicPCB> _totalTasks, ArrayList<BasicResource> _totalResources, int _totalCpuCoreNum, LinkedHashMap<Integer, ArrayList<Integer>> _taskReleaseTimes)
    {
        totalTasks = new ArrayList<>(_totalTasks.size());
        for (BasicPCB task : _totalTasks)
            totalTasks.add(new ProcedureControlBlock(task));

        totalResources = new ArrayList<>(_totalResources.size());
        for (BasicResource resource : _totalResources)
            totalResources.add(new Resource(resource));

        TOTAL_CPU_CORE_NUM = _totalCpuCoreNum;

        taskReleaseTimes = _taskReleaseTimes;

        iteratorTaskReleaseTimes = _taskReleaseTimes.entrySet().iterator();
        if (iteratorTaskReleaseTimes.hasNext())
            itemTaskReleaseTimes = iteratorTaskReleaseTimes.next();
    }


    /* Calculate the time axis length */
    public static void CalculateTimeAxisLength()
    {
        timeAxisLength = 0;
        for (ArrayList<com.example.serveside.service.msrp.entity.EventInformation> eventRecord : eventRecords)
            if (!eventRecord.isEmpty())
                timeAxisLength = Math.max(timeAxisLength, eventRecord.get(eventRecord.size() - 1).endTime);
    }

    public static com.example.serveside.response.ToTalInformation PackageTotalInformation()
    {
        // 计算 CPU 甘特图的长度: timeAxisLength
        CalculateTimeAxisLength();

        // 打包 CPU 甘特图的信息: cpuGanttInformations
        List<com.example.serveside.response.GanttInformation> ganttInformations = new ArrayList<>();
        for (int i = 0; i < eventRecords.size(); ++i)
        {
            ArrayList<com.example.serveside.service.msrp.entity.EventInformation> eventRecord = eventRecords.get(i);

            // record event information
            List<com.example.serveside.response.EventInformation> eventInformations = new ArrayList<>();
            for (com.example.serveside.service.msrp.entity.EventInformation eventInformation : eventRecord)
            {
                // the cpu is spare, skip off
                if (eventInformation.staticTaskId == -1 && eventInformation.dynamicTaskId == -1)
                    continue;
                eventInformations.add(new EventInformation(eventInformation));
            }

            ganttInformations.add(new com.example.serveside.response.GanttInformation(eventInformations, CPUEventTimePointsRecords.get(i), timeAxisLength));
        }


        // 打包任务甘特图的信息 : taskGanttInformations
        List<com.example.serveside.response.TaskGanttInformation> taskGanttInformations = new ArrayList<>();
        for (TaskStateInformation taskState : taskStates)
        {
            // 从 taskState 中获取 eventInformations
            List<com.example.serveside.response.EventInformation> eventInformations = new ArrayList<>();
            for (SingleTaskState singleTaskState : taskState.taskStates)  {
                eventInformations.add(new EventInformation(taskState.staticTaskId, taskState.dynamicTaskId, singleTaskState.getState(), singleTaskState.beginTime, singleTaskState.endTime));
            }

            // 从 TaskEventTimePointRecords 中通过 dynamicPid 获取 eventTimePoint : TaskEventTimePointsRecords.get(taskState.dynamicTaskId - 1)

            com.example.serveside.response.GanttInformation taskGanttInformation = new GanttInformation(eventInformations, TaskEventTimePointsRecords.get(taskState.dynamicTaskId), timeAxisLength);

            taskGanttInformations.add(new com.example.serveside.response.TaskGanttInformation(
                    taskState.staticTaskId, taskState.dynamicTaskId, taskState.runningCpuCore, taskGanttInformation));
        }

        return new com.example.serveside.response.ToTalInformation(ganttInformations, releaseTaskInformations, taskGanttInformations, criticalitySwitchTime);
    }

    public static void SystemExecute()
    {
        /* start to execute */

        isSchedulable = true;

        // initialize the clock
        systemClock = 0;

        // Initialize the releaseTaskNum:
        releaseTaskNum = 0;
        // Initialize the criticality_indicator: low criticality
        criticality_indicator = 0;
        // 系统关键级切换的时间点
        criticalitySwitchTime = -1;

        // record the times of the task that has been finished.
        taskFinishTimes = new int[totalTasks.size()];

        // create a ArrayList to keep the information about the running task.
        runningTaskPerCore = new ArrayList<>(TOTAL_CPU_CORE_NUM);
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            runningTaskPerCore.add(null);

        // create a ArrayList to save the task that is looking for cpu core.
        waitingTasksPerCore = new ArrayList<>(TOTAL_CPU_CORE_NUM);
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            waitingTasksPerCore.add(new ArrayList<>(TOTAL_CPU_CORE_NUM));

        // create an int array list to show that the task can be release or not.
        timeSinceLastRelease = new int[totalTasks.size()];
        for (int i = 0; i < totalTasks.size(); ++i)
            timeSinceLastRelease[i] = totalTasks.get(i).basicPCB.period;

        // create a ArrayList to save the task that is can release resource.
        ReleaseResourceTask = new ArrayList<>(TOTAL_CPU_CORE_NUM);
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            ReleaseResourceTask.add(null);

        /* Initialize the event record array. */
        eventRecords = new ArrayList<>();
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            eventRecords.add(new ArrayList<>());

        /* Initialize the running event array. */
        runningEvents = new ArrayList<>();
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            com.example.serveside.service.msrp.entity.EventInformation newEvent = new com.example.serveside.service.msrp.entity.EventInformation();
            newEvent.startTime = systemClock;
            runningEvents.add(newEvent);
        }

        /* Initialize the CPUEventTimePointsRecords. */
        CPUEventTimePointsRecords = new ArrayList<>();
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            CPUEventTimePointsRecords.add(new ArrayList<>());

        /* Initialize the task states array. */
        taskStates = new ArrayList<>();

        /* Initialize the TaskEventTimePointsRecords. */
        TaskEventTimePointsRecords = new ArrayList<>();

        /* Initialize the releaseInformationRecords */
        releaseTaskInformations = new ArrayList<>();

        /* Initialize the indicator records array. */
        indicatorRecords = new ArrayList<>();
        com.example.serveside.service.msrp.entity.EventInformation newEvent = new com.example.serveside.service.msrp.entity.EventInformation();
        newEvent.startTime = systemClock;
        indicatorRecords.add(newEvent);

        while (!systemSuspend(taskFinishTimes))
            /* The thing that the system will do in a clock. */
            ExecuteOneClock();

        // end all task when cpu close
        for (TaskStateInformation ts : taskStates)
            ts.endState(systemClock);

        ModifyIndicatorEvent(null, true);
        ShowEventInformation.showEventInShell(eventRecords);
        ShowEventInformation.showIndicatorInShell(indicatorRecords);

        // Print task states
//        ShowTaskStates.printTaskStates(taskStates);
    }

    /* When all tasks are finished, we want to jump from the loop and show the program scheduler. */
    public static boolean systemSuspend(int[] taskFinishTimes)
    {
        if (!isSchedulable)
            return true;
        // check all the task has been finished.
        for (int taskFinishTime : taskFinishTimes)
            if (taskFinishTime == 0)
                return false;

        // All tasks have been executed once. We need to end the running task's state.
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            if (runningTask == null)
                continue;
            ModifyRunningEvent(i, runningTask);
        }

        return true;
    }

    /* In a clock, the system need to finish the following things. */
    public static void ExecuteOneClock()
    {
        /* Execute the task that is in runningTaskPerCore. */
        /* Action: Request for resource. */

        ExecuteTasks();

        /* After execute one clock, we firstly Increase the elapsed time of the task in runningTaskPerCore and waitingTasks. */
        IncreaseElapsedTime();

        /* Release tasks */
        ReleaseTasks();

        /* Choose a task to running. */
        ChooseTaskToRun();
    }

    /* Execute the task in the runningTaskPerCore. */
    /* The thing you need to do is to check that the whether the task in the runningCore is request for resource or not.
     *  If the task requests for global resource, you need to set the related symbol(isAccessGlobalResource, RemainResourceComputationTime and so on) so that the task can't be preempted.
     *  What's more, you also need to consider the request resource is occupied by other task and put it into the resource waiting queue(set the spin, modify the waitingQueue and so on).
     *  If the task requests for local resource, you need to set the related symbol(isAccessLocalResource) and boost the priority of the task.
     *  When the task take up a spare resource, you need to set the RemainResourceComputationTime, isOccupied and so on.
     * Beyond that, you also need to boost the priority of the task.
     * */

    /* Now, we need to log CPUEventInformation when the task starts requests for resource.
     * Therefor, we need to save an older CPUEventInformation and create a new CPUEventInformation. */
    public static void ExecuteTasks()
    {
        for (int i = 0; i < runningTaskPerCore.size(); ++i)
        {
            // runningTask is null, or don't need resource, or is accessing resource: continue
            if (runningTaskPerCore.get(i) == null)
                continue;

            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            if(runningTask.basicPCB.accessResourceIndex.isEmpty())
                continue;

            // test: System.out.printf("I'm task %d，I want resource %d，remainTime is %d\n",
            //      runningTask.staticTaskId, runningTask.accessResourceIndex.get(0), runningTask.remainResourceComputationTime);
            if(runningTask.basicPCB.isAccessGlobalResource || runningTask.basicPCB.isAccessLocalResource)
                continue;

            // determine that whether we need to request a resource or not.
            if (runningTask.basicPCB.requestResourceTh >= runningTask.basicPCB.accessResourceIndex.size() ||
                    runningTask.basicPCB.resourceAccessTime.get(runningTask.basicPCB.requestResourceTh) > runningTask.basicPCB.executedTime ||
                    runningTask.basicPCB.spin)
                continue;

            // set and get requestResource
            int requestResourceIndex = runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh);
            Resource requestResource = totalResources.get(requestResourceIndex);

            // task state information
            TaskStateInformation runningTaskState = taskStates.get(runningTask.basicPCB.dynamicTaskId);

            // global resource
            if(requestResource.basicResource.isGlobal) {
                // global resource is occupied, spin and wait
                if(requestResource.basicResource.isOccupied) {
                    runningTask.basicPCB.spin = true;
                    requestResource.waitingQueue.add(runningTask);
                    // record task state and begin direct spin
                    runningTaskState.priority = Integer.MAX_VALUE;
                    runningTaskState.endState(systemClock);
                    runningTaskState.addState(TASK_STATE.DIRECT_SPIN, systemClock, requestResource.basicResource.id);

                    // 处理 indirect-spinning delay
                    for (ProcedureControlBlock waitingTask : waitingTasksPerCore.get(runningTask.basicPCB.baseRunningCpuCore))
                    {
                        if (waitingTask.basicPCB.basePriority < runningTask.basicPCB.basePriority)
                        {
                            taskStates.get(waitingTask.basicPCB.dynamicTaskId).endState(systemClock);
                            taskStates.get(waitingTask.basicPCB.dynamicTaskId).addState(TASK_STATE.INDIRECT_SPIN, systemClock);
                        }
                    }

                    // Event: Wait for a resource, record the event time point.
                    CPUEventTimePointsRecords.get(i).add(new EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "lock-attempt", systemClock, requestResourceIndex));
                    // Task Event: Wait for a resource, record the event time point.
                    TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "lock-attempt", systemClock, requestResourceIndex));

                }
                // unoccupied, get it
                else {
                    requestResource.basicResource.isOccupied = true;
                    runningTask.basicPCB.isAccessGlobalResource = true;
                    if(criticality_indicator == 0)
                        runningTask.basicPCB.remainResourceComputationTime = requestResource.basicResource.c_low;
                    else
                        runningTask.basicPCB.remainResourceComputationTime = requestResource.basicResource.c_high;

                    runningTask.basicPCB.systemCriticalityWhenAccessResource = criticality_indicator;

                    // record task state
                    runningTaskState.priority = Integer.MAX_VALUE;
                    runningTaskState.endState(systemClock);
                    runningTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, requestResource.basicResource.id);
                    runningTaskState.resourceId = requestResource.basicResource.id;
                    runningTaskState.remainResourceComputationTime = runningTask.basicPCB.remainResourceComputationTime;

                    // Event: Get a resource, record the event time point.
                    CPUEventTimePointsRecords.get(i).add(new EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "locked", systemClock, requestResourceIndex));
                    // Task Event: Get a global resource, record the event time point.
                    TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "locked", systemClock, requestResourceIndex));
                }
            }

            // local resource
            else {
                // task gets recourse and boost its priority
                requestResource.basicResource.isOccupied = true;
                runningTask.basicPCB.isAccessLocalResource = true;
                if(criticality_indicator == 0)
                    runningTask.basicPCB.remainResourceComputationTime = requestResource.basicResource.c_low;
                else
                    runningTask.basicPCB.remainResourceComputationTime = requestResource.basicResource.c_high;
                int currentCeiling = requestResource.basicResource.ceiling.get(i);
                runningTask.basicPCB.priorities.push(currentCeiling);
                // record task state
                runningTaskState.priority = currentCeiling;
                runningTaskState.endState(systemClock);
                runningTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, requestResource.basicResource.id);
                runningTaskState.resourceId = requestResource.basicResource.id;
                runningTaskState.remainResourceComputationTime = runningTask.basicPCB.remainResourceComputationTime;

                runningTask.basicPCB.systemCriticalityWhenAccessResource = criticality_indicator;

                // Event: Get a resource, record the event time point.
                CPUEventTimePointsRecords.get(i).add(new EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "locked", systemClock, requestResourceIndex));
                // Task Event: Get a local resource, record the event time point.
                TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "locked", systemClock, requestResourceIndex));
            }
            // record old event and create a new one
            ModifyRunningEvent(i, runningTask);

        }
    }

    /* Increase the elapsed time of the task in runningTaskPerCore and waitingTasks. */
    public static void IncreaseElapsedTime()
    {
        ++systemClock;

        /* Increase the elapsed time of the task in runningCore. */
        for (int i = 0; i < runningTaskPerCore.size(); ++i) {
            if (runningTaskPerCore.get(i) == null)
                continue;

            // if the cpu core is running a task.
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            // task state information
            TaskStateInformation runningTaskState = taskStates.get(runningTask.basicPCB.dynamicTaskId);

            ++runningTask.basicPCB.elapsedTime;

            // when task is not spin（computed and access resource）, executedTime++
            if (!runningTask.basicPCB.spin)
                ++runningTask.basicPCB.executedTime;

            // when task is not access, computeAndSpinTime++;
            // include compute and spin time, not include access resource time.
            if (!runningTask.basicPCB.isAccessLocalResource && !runningTask.basicPCB.isAccessGlobalResource) {
                ++runningTask.basicPCB.computeAndSpinTime;

                // if computeAndSpinTime > WCCT_low, upgrade criticality to high
                if (runningTask.basicPCB.computeAndSpinTime > runningTask.basicPCB.WCCT_low && criticality_indicator == 0) {
                    criticalitySwitchTime = systemClock;
                    criticality_indicator = 1;
                    ModifyIndicatorEvent(runningTask, false);
                }
            }

            // if the task is accessing a resource, reduce its needed resource computation time.
            if (runningTask.basicPCB.isAccessLocalResource || runningTask.basicPCB.isAccessGlobalResource) {

                --runningTask.basicPCB.remainResourceComputationTime;

                // Finish the use of the resource, set the resource free.
                if (runningTask.basicPCB.remainResourceComputationTime == 0) // the running task releases the access to the resource that occupied.
                {
                    runningTaskState.priority = runningTask.basicPCB.priorities.peek();
                    runningTaskState.endState(systemClock);
                    runningTaskState.addState(TASK_STATE.RUNNING, systemClock);

                    // add runningTask to array, Free the use of resource after loop
                    ReleaseResourceTask.set(i, runningTask);
                }
            }

            // 任务的 elapsedTime 超过 DeadLine 的, 这样子的话就没有必要进行调度了
            if (runningTask.basicPCB.elapsedTime > runningTask.basicPCB.deadline)
                isSchedulable = false;
        }

        // ReleaseResource after loop to avoid bug
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            if (ReleaseResourceTask.get(i) != null) {
                ReleaseResource(ReleaseResourceTask.get(i));
                // Finish the event and create a new event.
                ModifyRunningEvent(i, ReleaseResourceTask.get(i));
                ReleaseResourceTask.set(i, null);
            }
        }

        // shutdown the low priority task and complete task.
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            // The CPU is spare, skip off it.
            if (runningTask == null)
                continue;
            TaskStateInformation runningTaskState = taskStates.get(runningTask.basicPCB.dynamicTaskId);

            // If the task is finished, then we can remove it from the cpu core and choose other tasks to run.
            if (runningTask.basicPCB.executedTime >= runningTask.basicPCB.totalNeededTime)
            {
                // if task is accessing resource, wait until the resource accessing complete
                if (runningTask.basicPCB.isAccessGlobalResource || runningTask.basicPCB.isAccessLocalResource)
                    continue;
                // The task has been finished again.
                ++taskFinishTimes[runningTask.basicPCB.staticTaskId];
                // set the cpu core is spare.
                runningTaskPerCore.set(i, null);

                // end the running task
                ModifyRunningEvent(i, null);

                // end previous state
                runningTaskState.endState(systemClock);
                runningTaskState.live = false;

                // Complete the execution of a task.
                CPUEventTimePointsRecords.get(i).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));
                TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));
                continue;
            }

            // if low task without resource at high system, end it directly
            if (runningTask.basicPCB.criticality < criticality_indicator && !runningTask.basicPCB.isAccessGlobalResource && !runningTask.basicPCB.isAccessLocalResource) {
                ++taskFinishTimes[runningTask.basicPCB.staticTaskId];
                runningTaskPerCore.set(i, null);
                ModifyRunningEvent(i, null);
                runningTaskState.killTask(systemClock);

                // Killed a low critical task(Show in cpu gantt chart).
                CPUEventTimePointsRecords.get(i).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "killed", systemClock, -1));
                TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "killed", systemClock, -1));

            }
        }

        // Increase the waiting time that the blocking task.
        for (ArrayList<ProcedureControlBlock> procedureControlBlocks : waitingTasksPerCore) {
            for (ProcedureControlBlock waitingTask : procedureControlBlocks) {
                ++waitingTask.basicPCB.elapsedTime;

                // 任务的 elapsedTime 超过 DeadLine 的, 这样子的话就没有必要进行调度了
                if (waitingTask.basicPCB.elapsedTime > waitingTask.basicPCB.deadline)
                    isSchedulable = false;
            }
        }

    }

    /* The task finishes the use of the request resource. This function is used to release the resource and some more action.
     * 1. Release the resource.
     *
     * 2. Restore the task's original priority.
     *
     * 3. If there exists other tasks that is waiting for the resource, assign the resource to task according to FIFO principle.
     * What's more, set the allocated task's corresponding attributes and the resource's attributes.
     * */

    /* Now, we need to save an older CPUEventInformation because the task finishes the use of the resource.
     * In addition, we also need to save and create CPUEventInformation when the resource is waiting by other tasks. */
    public static void ReleaseResource(ProcedureControlBlock runningTask)
    {
        //get the id of resource which is need to be released
        int resource_id = runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh);
        Resource resource = totalResources.get(resource_id);

        // Release the resource
        CPUEventTimePointsRecords.get(runningTask.basicPCB.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "unlocked", systemClock, resource_id));
        TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "unlocked", systemClock, resource_id));

        //1 reset some parameter of resource
        resource.basicResource.isOccupied = false;          //now it is free

        //2 reset some parameter of running task
        if (!resource.basicResource.isGlobal)
            runningTask.basicPCB.priorities.pop();       //remove the resource from task's request list

        ++runningTask.basicPCB.requestResourceTh;    //now the task continue to request next resource.

        runningTask.basicPCB.isAccessGlobalResource = runningTask.basicPCB.isAccessLocalResource = false;

        //3 check if exists a task waiting for this resource
        if(!resource.waitingQueue.isEmpty()){
            //if exists,get the PCB of new task(n_task)(First In First Out)
            ProcedureControlBlock n_task = resource.waitingQueue.get(0);
            resource.waitingQueue.remove(0);

            // if indicator is HI, low task can't be chosen
            while (n_task.basicPCB.criticality < criticality_indicator) {
                // record: killed
                ++taskFinishTimes[n_task.basicPCB.staticTaskId];
                taskStates.get(n_task.basicPCB.dynamicTaskId).killTask(systemClock);
                TaskEventTimePointsRecords.get(n_task.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(n_task.basicPCB.staticTaskId, n_task.basicPCB.dynamicTaskId, "killed", systemClock, -1));
                if (resource.waitingQueue.isEmpty())
                    return;
                n_task = resource.waitingQueue.get(0);
                resource.waitingQueue.remove(0);
            }

            resource.basicResource.isOccupied = true;
            //Assign resources to this task

            if(criticality_indicator == 0)
                n_task.basicPCB.remainResourceComputationTime = resource.basicResource.c_low;
            else
                n_task.basicPCB.remainResourceComputationTime = resource.basicResource.c_high;

            // Modify the task's attributes.
            if(resource.basicResource.isGlobal) {
                n_task.basicPCB.isAccessGlobalResource=true;
                n_task.basicPCB.spin = false;

                // 处理 indirect spinning delay
                for (ProcedureControlBlock waitingTask : waitingTasksPerCore.get(n_task.basicPCB.baseRunningCpuCore))
                {
                    if (waitingTask.basicPCB.basePriority < n_task.basicPCB.basePriority)
                    {
                        taskStates.get(waitingTask.basicPCB.dynamicTaskId).endState(systemClock);
                    }
                }

            } else {
                n_task.basicPCB.isAccessLocalResource=true;
                n_task.basicPCB.priorities.push(resource.basicResource.ceiling.get(n_task.basicPCB.baseRunningCpuCore));
            }

            n_task.basicPCB.systemCriticalityWhenAccessResource = criticality_indicator;

            // End the new task's old event and create a new event.
            ModifyRunningEvent(n_task.basicPCB.baseRunningCpuCore, n_task);

            // Get a resource:
            CPUEventTimePointsRecords.get(n_task.basicPCB.baseRunningCpuCore).add(new EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "lock", systemClock, resource_id));

            // record task state
            TaskStateInformation waitingTaskState = taskStates.get(n_task.basicPCB.dynamicTaskId);
            waitingTaskState.priority = n_task.basicPCB.priorities.peek();
            if (n_task.basicPCB.isAccessGlobalResource)
                waitingTaskState.priority = Integer.MAX_VALUE;
            waitingTaskState.endState(systemClock);
            waitingTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, resource.basicResource.id);
            waitingTaskState.resourceId = resource.basicResource.id;
            waitingTaskState.remainResourceComputationTime = n_task.basicPCB.remainResourceComputationTime;
        }

        // Modify the execution time.
        if (runningTask.basicPCB.systemCriticalityWhenAccessResource == 1)
            runningTask.basicPCB.executedTime -= (resource.basicResource.c_high - resource.basicResource.c_low);
    }

    /* Function that can release task. */
    public static void ReleaseTasks()
    {
        // 1. 某一时间点有任务需要释放
        // 2. 当前的时间点 = 任务释放的时间点
        if (itemTaskReleaseTimes != null && itemTaskReleaseTimes.getKey() == systemClock)
        {
            for (Integer releaseTaskId : itemTaskReleaseTimes.getValue())
            {
                // if at HI, low task be seen finished
                if (totalTasks.get(releaseTaskId).basicPCB.criticality < criticality_indicator){
                    ++taskFinishTimes[totalTasks.get(releaseTaskId).basicPCB.staticTaskId];
                    continue;
                }

                // initialize the release task and set its pid.
                com.example.serveside.service.msrp.entity.ProcedureControlBlock releaseTask = new com.example.serveside.service.msrp.entity.ProcedureControlBlock(totalTasks.get(releaseTaskId));
                releaseTask.basicPCB.dynamicTaskId = releaseTaskNum++;
                waitingTasksPerCore.get(totalTasks.get(releaseTaskId).basicPCB.baseRunningCpuCore).add(releaseTask);

                // 记录一下发布的任务的基本信息
                releaseTaskInformations.add(new com.example.serveside.response.TaskInformation(releaseTask, systemClock));

                // 记录一下发布的任务的基本信息
                taskStates.add(new TaskStateInformation(releaseTask.basicPCB.staticTaskId, releaseTask.basicPCB.dynamicTaskId, releaseTask.basicPCB.priorities.peek(), releaseTask.basicPCB.baseRunningCpuCore));
                System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.basicPCB.staticTaskId, releaseTask.basicPCB.dynamicTaskId, systemClock);

                // 记录一下任务发布的时间点
                TaskEventTimePointsRecords.add(new ArrayList<>());
                TaskEventTimePointsRecords.get(releaseTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.basicPCB.staticTaskId, releaseTask.basicPCB.dynamicTaskId, "release", systemClock, -1));

            }

            if (iteratorTaskReleaseTimes.hasNext())
                itemTaskReleaseTimes = iteratorTaskReleaseTimes.next();
            else
                itemTaskReleaseTimes = null;
        }
    }

    /* Choose a task to run at the corresponding cpu core if the cpu core is spare.
     * Or waiting task preemptive the running task.
     * Two conditions that can preemptive the cpu core.
     * 1. the cpu core is spare.
     * 2. the local task's priority(including the boosted) is smaller than the task.
     * */

    /* In addition, we also need to create an evenInformation when cpu core is spare.
     * We also need to save an evenInformation and create an evenInformation when a high priority task preempted the low priority task. */
    public static void ChooseTaskToRun()
    {
        for (int i = 0; i < waitingTasksPerCore.size(); ++i)
        {
            ArrayList<ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);

            // If the cpu core don't have task is waiting, we pass it.
            if (waitingTasks.isEmpty())
                continue;

            // Firstly, sort the ArrayList by the priority from largest to smallest
            waitingTasks.sort((task1, task2) -> -Integer.compare(task1.basicPCB.priorities.peek(), task2.basicPCB.priorities.peek()));
            ProcedureControlBlock waitingTask = waitingTasks.get(0);

            // if indicator is HI, low task can't be chosen
            while (waitingTask.basicPCB.criticality < criticality_indicator) {
                // record :kill
                ++taskFinishTimes[waitingTask.basicPCB.staticTaskId];
                taskStates.get(waitingTask.basicPCB.dynamicTaskId).killTask(systemClock);
                TaskEventTimePointsRecords.get(waitingTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "killed", systemClock, -1));
                waitingTasks.remove(0);
                if (waitingTasks.isEmpty())
                    break;
                waitingTask = waitingTasks.get(0);
            }
            if (waitingTasks.isEmpty())
                continue;


            // 1. Condition: the cpu core is spare.
            if (runningTaskPerCore.get(i) == null)
            {
                // set the running task
                runningTaskPerCore.set(i, waitingTask);
                // remove the waitingTask from the waitingTasks
                waitingTasks.remove(0);
                runningTaskPerCore.get(i).basicPCB.baseRunningCpuCore = i;

                // modify the event information.
                ModifyRunningEvent(i, waitingTask);

                // add running state
                TaskStateInformation runningTaskState = taskStates.get(waitingTask.basicPCB.dynamicTaskId);
                // record task running cpu for record indirect spin
                runningTaskState.endState(systemClock);
                if (runningTaskState.remainResourceComputationTime > 0)
                    runningTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, runningTaskState.resourceId);
                else
                    runningTaskState.addState(TASK_STATE.RUNNING, systemClock);

                // Add switch-task event.
                CPUEventTimePointsRecords.get(i).add(new EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "switch-task", systemClock, -1));

                continue;
            }


            // 2. Condition: the local task's priority(including the boosted) is smaller than the task.
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            // If the task is waiting for a global resource, we can't stop it.
            if (runningTask.basicPCB.spin)
                continue;

            // running task just use cpu to compute, doesn't access the resource. What's more, the task's priority is lower than the waiting task.
            if (!runningTask.basicPCB.isAccessLocalResource && !runningTask.basicPCB.isAccessGlobalResource && runningTask.basicPCB.priorities.peek() < waitingTask.basicPCB.priorities.peek())
            {
                // put the running task into the waiting queue.
                waitingTasks.add(runningTask);
                // set the running task, choosing from waitingTasks.
                runningTaskPerCore.set(i, waitingTask);
                // remove it from waiting tasks.
                waitingTasks.remove(0);
                runningTaskPerCore.get(i).basicPCB.baseRunningCpuCore = i;

                // Modify the event information.
                ModifyRunningEvent(i, waitingTask);

                // end state for running task
                TaskStateInformation modifyTaskState = taskStates.get(runningTask.basicPCB.dynamicTaskId);
                modifyTaskState.endState(systemClock);
                modifyTaskState.addState(TASK_STATE.PREEMPTED, systemClock);
                // add running state for waitting task
                modifyTaskState = taskStates.get(waitingTask.basicPCB.dynamicTaskId);
                // record task running cpu for record indirect spin
                modifyTaskState.endState(systemClock);
                if (modifyTaskState.remainResourceComputationTime > 0)
                    modifyTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, modifyTaskState.resourceId);
                else
                    modifyTaskState.addState(TASK_STATE.RUNNING, systemClock);

                // Add switch-task event.
                CPUEventTimePointsRecords.get(i).add(new EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "switch-task", systemClock, -1));
            }

            // running task is access local resource and priority is lower.
            if (runningTask.basicPCB.isAccessLocalResource && runningTask.basicPCB.priorities.peek() < waitingTask.basicPCB.priorities.peek())
            {
                // put the running task into the waiting queue.
                waitingTasks.add(runningTask);
                // set the running task, choosing from waitingTasks.
                runningTaskPerCore.set(i, waitingTask);
                // remove it from waiting task.
                waitingTasks.remove(0);
                runningTaskPerCore.get(i).basicPCB.baseRunningCpuCore = i;

                // Modify the event information.
                ModifyRunningEvent(i, waitingTask);

                // end state for running task
                TaskStateInformation modifyTaskState = taskStates.get(runningTask.basicPCB.dynamicTaskId);
                modifyTaskState.endState(systemClock);
                modifyTaskState.addState(TASK_STATE.PREEMPTED, systemClock);
                // add running state for waitting task
                modifyTaskState = taskStates.get(waitingTask.basicPCB.dynamicTaskId);
                // record task running cpu for record indirect spin
                modifyTaskState.endState(systemClock);
                if (modifyTaskState.remainResourceComputationTime > 0)
                    modifyTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, modifyTaskState.resourceId);
                else
                    modifyTaskState.addState(TASK_STATE.RUNNING, systemClock);

                // Add switch-task event.
                CPUEventTimePointsRecords.get(i).add(new EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "switch-task", systemClock, -1));
            }
        }

        // 处理 arrival blocking 的状况
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            ArrayList<ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            for (ProcedureControlBlock waitingTask : waitingTasks)
            {
                // runningTask  的优先级比 waitingTask 大，并且 runningTask 还能够执行，说明：
                // 1. runningTask 正在 spinning
                // 2. runningTask 正在访问 global resource
                // 3. runningTask 访问 local resource，但是 runningTask 优先级被短暂提升
                // 除此之外，我们还需要确保 waitingTask 此前没有执行过
                if (runningTask != null && runningTask.basicPCB.basePriority < waitingTask.basicPCB.basePriority && waitingTask.basicPCB.executedTime == 0)
                {
                    taskStates.get(waitingTask.basicPCB.dynamicTaskId).addState(TASK_STATE.ARRIVAL_BLOCK, systemClock);
                }
            }
        }
    }

    /**
     * Save the old CPUEventInformation and create a new CPUEventInformation.
     * @param i the ith cpu core.
     */
    public static void ModifyRunningEvent(int i, ProcedureControlBlock runningTask)
    {
        // The new event.
        com.example.serveside.service.msrp.entity.EventInformation newEvent;

        // Finish an event and use an array to save it.
        com.example.serveside.service.msrp.entity.EventInformation runningEvent = runningEvents.get(i);
        runningEvent.endTime = systemClock;

        // special case: Finish the use of a resource and been preempted.
        if (runningEvent.startTime != runningEvent.endTime)
            eventRecords.get(i).add(runningEvent);

        if (runningTask == null)
        {
            // the cpu core is spare.
            newEvent = new com.example.serveside.service.msrp.entity.EventInformation();
            newEvent.startTime = systemClock;
            newEvent.systemCriticality = criticality_indicator;
        }else
            // create an event information to record this event that the task was released.
            newEvent = new com.example.serveside.service.msrp.entity.EventInformation(systemClock, runningTask, criticality_indicator);

        runningEvents.set(i, newEvent);
    }

    /**
     * Save the old indicatorInformation and create a new indicatorInformation.
     */
    public static void ModifyIndicatorEvent(ProcedureControlBlock runningTask, boolean end) {

        // finish last indicator event
        int idx = indicatorRecords.size() - 1;
        com.example.serveside.service.msrp.entity.EventInformation runningEvent = indicatorRecords.get(idx);
        runningEvent.endTime = systemClock;

        // if it's not last one, add new event
        if(!end){
            com.example.serveside.service.msrp.entity.EventInformation newEvent = new com.example.serveside.service.msrp.entity.EventInformation(systemClock, runningTask, criticality_indicator);
            indicatorRecords.add(newEvent);
        }
    }

}
