package com.example.serveside.service.msrp.entity;

import com.example.serveside.service.msrp.generatorTools.SimpleSystemGenerator;
import com.example.serveside.service.msrp.utils.ShowEventInformation;
import com.example.serveside.service.msrp.utils.ShowTaskStates;
import com.example.serveside.service.msrp.entity.SingleTaskState.TASK_STATE;

import java.util.ArrayList;
import java.util.Random;

public class MixedCriticalSystem {
    /* System Setup */

    /* the criticality of system, 0 is low, 1 is high */
    public static int criticality_indicator = 0;
    /* The cpu core num. */
    public static int TOTAL_CPU_CORE_NUM = 2;

    /* The minimum period for every task. */
    public static int MIN_PERIOD = 50;

    /* The maximum period for every task. */
    public static int MAX_PERIOD = 1000;

    /* The max number of the access resource time. */
    public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;

    /* A ratio of task to access resource. */
    public static double RESOURCE_SHARING_FACTOR = 1;

    /* The max priority in the system. */
    public static final int MAX_PRIORITY = 1000;

    /* define how long the critical section can be */
    public enum CS_LENGTH_RANGE {
        VERY_LONG_CSLEN, LONG_CSLEN, MEDIUM_CS_LEN, SHORT_CS_LEN, VERY_SHORT_CS_LEN, Random
    }

    /* Allocate a number of task to a partition. */
    public static int NUMBER_OF_TASK_IN_A_PARTITION = 2;

    /* define how many resources in the system */
    public enum RESOURCES_RANGE {
        /* partitions / 2 us */
        HALF_PARITIONS,

        /* partitions us */
        PARTITIONS,

        /* partitions * 2 us */
        DOUBLE_PARTITIONS,
    }

    /* System Execute */
    /* system clock, to record the time. */
    public static int systemClock;

    /* The num of the released task. */
    public static int releaseTaskNum;

    /* generate total tasks that the system will execute. */
    public static ArrayList<ProcedureControlBlock>  totalTasks;

    /* generate total tasks that the system has. */
    public static ArrayList<Resource> totalResources;

    /*  allocate task to the cpu core. */
    public static ArrayList<ArrayList<ProcedureControlBlock>> allocatedTasks;

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

    /* Save the EventInformation. */
    public static ArrayList<ArrayList<EventInformation>> eventRecords;

    /* Keep the running event. */
    public static ArrayList<EventInformation> runningEvents;

    /* Save the IndicatorInformation. */
    public static ArrayList<EventInformation> indicatorRecords;

    /* Store states for each task. */
    public static ArrayList<TaskStateInformation> taskStates;

    public static void main(String[] args)
    {
        SimpleSystemGenerator systemGenerator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_CPU_CORE_NUM,
                TOTAL_CPU_CORE_NUM * NUMBER_OF_TASK_IN_A_PARTITION, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
                RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

        // Generate the tasks that the system need to execute.
        totalTasks = systemGenerator.generateTasks();

        // Generate the resources that the system has.
        totalResources = systemGenerator.generateResources();

        // Generate the resource usage, i.e. task access the resource(time, times)
        allocatedTasks = systemGenerator.generateResourceUsage(totalTasks, totalResources);

        // Print the information about the task.
        for (int i = 0; i < allocatedTasks.size(); ++i)
        {
            System.out.printf("CPU %d:\n", i);
            System.out.print("\tTask Information:\n");

            for (ProcedureControlBlock task : allocatedTasks.get(i))
            {
                System.out.printf("\t\t\tTask %d\n", task.staticTaskId);
                System.out.printf("\t\t\t\tPriority: %d, Criticality: %d\n", task.priorities.peek(), task.criticality);
                System.out.printf("\t\t\t\tWCCT_low: %d, WCCT_high: %d\n", task.WCCT_low, task.WCCT_high);
                System.out.printf("\t\t\t\tUtilization: %.2f, Period: %d, CPU time: %d\n", task.utilization, task.period, task.totalNeededTime);
                for (int j = 0; j < task.accessResourceIndex.size(); ++j)
                {
                    System.out.printf("\t\t\t\tAccess Resource Id: %d, Access Time: %d\n", task.accessResourceIndex.get(j), task.resourceAccessTime.get(j));
                }
            }
        }

        // Print the information about the resource.
        for (Resource resource : totalResources) {
            System.out.printf("Resource Id:%d\n", resource.id);
            System.out.printf("\t\t\tc_low: %d, c_high: %d\n", resource.c_low, resource.c_high);
            System.out.print("\t\t\tResource Ceiling\n");
            for (int j = 0; j < resource.ceiling.size(); ++j) {
                System.out.printf("\t\t\t\tCPU %d: %d\n", j, resource.ceiling.get(j));
            }
        }

        /* Start running. */
        SystemExecute();

    }

    public static void SystemExecute()
    {
        /* start to execute */
        // initialize the clock
        systemClock = 0;

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
            timeSinceLastRelease[i] = totalTasks.get(i).period;

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
            EventInformation newEvent = new EventInformation();
            newEvent.startTime = systemClock;
            runningEvents.add(newEvent);
        }

        /* Initialize the task states array. */
        taskStates = new ArrayList<>();

        /* Initialize the indicator records array. */
        indicatorRecords = new ArrayList<>();
        EventInformation newEvent = new EventInformation();
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
        ShowTaskStates.printTaskStates(taskStates);
    }

    /* When all tasks are finished, we want to jump from the loop and show the program scheduler. */
    public static boolean systemSuspend(int[] taskFinishTimes)
    {
        // check all the task has been finished.
        for (int taskFinishTime : taskFinishTimes)
            if (taskFinishTime == 0)
                return false;

        return true;
    }

    /* In a clock, the system need to finish the following things. */
    public static void ExecuteOneClock()
    {
        /* Execute the task that is in runningTaskPerCore. */
        /* Action: Request for resource. */
        ExecuteTasks();

        /* Release tasks */
        ReleaseTasks();

        /* After execute one clock, we firstly Increase the elapsed time of the task in runningTaskPerCore and waitingTasks. */
        IncreaseElapsedTime();

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

    /* Now, we need to log EventInformation when the task starts requests for resource.
     * Therefor, we need to save an older EventInformation and create a new EventInformation. */
    public static void ExecuteTasks()
    {
        for (int i = 0; i < runningTaskPerCore.size(); ++i)
        {
            // runningTask is null, or don't need resource, or is accessing resource: continue
            if (runningTaskPerCore.get(i) == null)
                continue;

            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            if(runningTask.accessResourceIndex.isEmpty())
                continue;

            // test: System.out.printf("I'm task %d，I want resource %d，remainTime is %d\n",
            //      runningTask.staticTaskId, runningTask.accessResourceIndex.get(0), runningTask.remainResourceComputationTime);
            if(runningTask.isAccessGlobalResource || runningTask.isAccessLocalResource)
                continue;

            // determine that whether we need to request a resource or not.
            if (runningTask.requestResourceTh >= runningTask.accessResourceIndex.size() ||
                    runningTask.resourceAccessTime.get(runningTask.requestResourceTh) > runningTask.executedTime ||
                    runningTask.spin)
                continue;

            // set and get requestResource
            int requestResourceIndex = runningTask.accessResourceIndex.get(runningTask.requestResourceTh);
            Resource requestResource = totalResources.get(requestResourceIndex);

            // task state information
            TaskStateInformation runningTaskState = taskStates.get(runningTask.dynamicTaskId - 1);

            // global resource
            if(requestResource.isGlobal) {
                // global resource is occupied, spin and wait
                if(requestResource.isOccupied) {
                    runningTask.spin = true;
                    requestResource.waitingQueue.add(runningTask);
                    // record task state
                    runningTaskState.priority = Integer.MAX_VALUE;
                    runningTaskState.endState(systemClock);
                    runningTaskState.addState(TASK_STATE.DIRECT_SPIN, systemClock, requestResource.id);
                    // indirect spin
                    for (TaskStateInformation tsi : taskStates) {
                        if (tsi.live && tsi.priority > runningTaskState.priority && tsi.runningCpuCore == runningTaskState.runningCpuCore) {
                            tsi.endState(systemClock);
                            tsi.addState(TASK_STATE.INDIRECT_SPIN, systemClock, requestResource.id);
                        }
                    }
                }
                // unoccupied, get it
                else {
                    requestResource.isOccupied = true;
                    runningTask.isAccessGlobalResource = true;
                    if(criticality_indicator == 0)
                        runningTask.remainResourceComputationTime = requestResource.c_low;
                    else
                        runningTask.remainResourceComputationTime = requestResource.c_high;

                    runningTask.systemCriticalityWhenAccessResource = criticality_indicator;

                    // record task state
                    runningTaskState.priority = Integer.MAX_VALUE;
                    runningTaskState.endState(systemClock);
                    runningTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, requestResource.id);
                    runningTaskState.resourceId = requestResource.id;
                    runningTaskState.remainResourceComputationTime = runningTask.remainResourceComputationTime;
                }
            }

            // local resource
            else {
                // task gets recourse and boost its priority
                requestResource.isOccupied = true;
                runningTask.isAccessLocalResource = true;
                if(criticality_indicator == 0)
                    runningTask.remainResourceComputationTime = requestResource.c_low;
                else
                    runningTask.remainResourceComputationTime = requestResource.c_high;
                int currentCeiling = requestResource.ceiling.get(i);
                runningTask.priorities.push(currentCeiling);
                // record task state
                runningTaskState.priority = currentCeiling;
                runningTaskState.endState(systemClock);
                runningTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, requestResource.id);
                runningTaskState.resourceId = requestResource.id;
                runningTaskState.remainResourceComputationTime = runningTask.remainResourceComputationTime;

                runningTask.systemCriticalityWhenAccessResource = criticality_indicator;
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
            TaskStateInformation runningTaskState = taskStates.get(runningTask.dynamicTaskId - 1);

            ++runningTask.elapsedTime;

            // when task is not spin（computed and access resource）, executedTime++
            if (!runningTask.spin)
                ++runningTask.executedTime;

            // when task is not access, computeAndSpinTime++;
            // include compute and spin time, not include access resource time.
            if (!runningTask.isAccessLocalResource && !runningTask.isAccessGlobalResource) {
                ++runningTask.computeAndSpinTime;

                // if computeAndSpinTime > WCCT_low, upgrade criticality to high
                if (runningTask.computeAndSpinTime > runningTask.WCCT_low && criticality_indicator == 0) {
                    criticality_indicator = 1;
                    ModifyIndicatorEvent(runningTask, false);
                }

                // if low task without resource at high system, end it directly
                if (runningTask.criticality < criticality_indicator) {
                    ++taskFinishTimes[runningTask.staticTaskId];
                    runningTaskPerCore.set(i, null);
                    ModifyRunningEvent(i, null);
                    runningTaskState.killTask(systemClock);
                    continue;
                }
            }

            // if the task is accessing a resource, reduce its needed resource computation time.
            if (runningTask.isAccessLocalResource || runningTask.isAccessGlobalResource) {

                --runningTask.remainResourceComputationTime;

                // Finish the use of the resource, set the resource free.
                if (runningTask.remainResourceComputationTime == 0) // the running task releases the access to the resource that occupied.
                {
                    runningTaskState.priority = runningTask.priorities.peek();
                    runningTaskState.endState(systemClock);
                    runningTaskState.addState(TASK_STATE.RUNNING, systemClock);

                    // add runningTask to array, Free the use of resource after loop
                    ReleaseResourceTask.set(i, runningTask);

                    // if low task with resource at high system, end it after it released resource
                    if(runningTask.criticality < criticality_indicator){
                        ++taskFinishTimes[runningTask.staticTaskId];
                        runningTaskPerCore.set(i, null);
                        ModifyRunningEvent(i, null);
                        runningTaskState.killTask(systemClock);
                        continue;
                    }
                }
            }


            // If the task is finished, then we can remove it from the cpu core and choose other tasks to run.
            if (runningTask.executedTime >= runningTask.totalNeededTime)
            {
                // if task is accessing resource, wait until the resource accessing complete
                if ((runningTask.isAccessGlobalResource || runningTask.isAccessLocalResource) && runningTask.remainResourceComputationTime > 0)
                    continue;
                // The task has been finished again.
                ++taskFinishTimes[runningTask.staticTaskId];
                // set the cpu core is spare.
                runningTaskPerCore.set(i, null);

                // end the running task
                ModifyRunningEvent(i, null);

                // end previous state
                runningTaskState.endState(systemClock);
                runningTaskState.live = false;
            }
        }

        // ReleaseResource after loop to avoid bug
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i) {
            if (ReleaseResourceTask.get(i) != null) {
                ReleaseResource(ReleaseResourceTask.get(i));
                // Finish the event and create a new event.
                ModifyRunningEvent(i, ReleaseResourceTask.get(i));
                ReleaseResourceTask.set(i, null);
            }
        }

        // Increase the waiting time that the blocking task.
        for (ArrayList<ProcedureControlBlock> procedureControlBlocks : waitingTasksPerCore) {
            for (ProcedureControlBlock waitingTask : procedureControlBlocks) {
                ++waitingTask.elapsedTime;
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

    /* Now, we need to save an older EventInformation because the task finishes the use of the resource.
     * In addition, we also need to save and create EventInformation when the resource is waiting by other tasks. */
    public static void ReleaseResource(ProcedureControlBlock runningTask)
    {
        //get the id of resource which is need to be released
        int resource_id = runningTask.accessResourceIndex.get(runningTask.requestResourceTh);
        Resource resource = totalResources.get(resource_id);

        //1 reset some parameter of resource
        resource.isOccupied = false;          //now it is free

        //2 reset some parameter of running task
        if (!resource.isGlobal)
            runningTask.priorities.pop();       //remove the resource from task's request list

        ++runningTask.requestResourceTh;    //now the task continue to request next resource.

        runningTask.isAccessGlobalResource = runningTask.isAccessLocalResource = false;

        //3 check if exists a task waiting for this resource
        if(!resource.waitingQueue.isEmpty()){
            //if exists,get the PCB of new task(n_task)(First In First Out)
            ProcedureControlBlock n_task = resource.waitingQueue.poll();

            // if indicator is HI, low task can't be chosen
            while (n_task.criticality < criticality_indicator) {
                ++taskFinishTimes[n_task.staticTaskId];
                taskStates.get(n_task.dynamicTaskId - 1).killTask(systemClock);
                if (resource.waitingQueue.isEmpty())
                    return;
                n_task = resource.waitingQueue.poll();
            }

            resource.isOccupied = true;
            //Assign resources to this task

            if(criticality_indicator == 0)
                n_task.remainResourceComputationTime = resource.c_low;
            else
                n_task.remainResourceComputationTime = resource.c_high;

            // Modify the task's attributes.
            if(resource.isGlobal) {
                n_task.isAccessGlobalResource=true;
                n_task.spin = false;
            } else {
                n_task.isAccessLocalResource=true;
                n_task.priorities.push(resource.ceiling.get(n_task.runningCpuCore));
            }

            n_task.systemCriticalityWhenAccessResource = criticality_indicator;

            // End the new task's old event and create a new event.
            ModifyRunningEvent(n_task.runningCpuCore, n_task);

            // record task state
            TaskStateInformation waitingTaskState = taskStates.get(n_task.dynamicTaskId - 1);
            waitingTaskState.priority = n_task.priorities.peek();
            if (n_task.isAccessGlobalResource)
                waitingTaskState.priority = Integer.MAX_VALUE;
            waitingTaskState.endState(systemClock);
            waitingTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, resource.id);
            waitingTaskState.resourceId = resource.id;
            waitingTaskState.remainResourceComputationTime = n_task.remainResourceComputationTime;
        }

        // Modify the execution time.
        if (runningTask.systemCriticalityWhenAccessResource == 1)
            runningTask.executedTime -= (resource.c_high - resource.c_low);
    }

    /* Function that can release task. */
    public static void ReleaseTasks()
    {
        Random random = new Random();
        for (int i = 0; i < totalTasks.size(); ++i)
        {
            // Even if the time elapsed since the last release is greater than the period, it still has a probability of being released.
            if (timeSinceLastRelease[i] >= totalTasks.get(i).period && random.nextDouble() < 0.2)
            {
                // if at HI, low task be seen finished
                if (totalTasks.get(i).criticality < criticality_indicator){
                    ++taskFinishTimes[totalTasks.get(i).staticTaskId];
                }

                // reset
                timeSinceLastRelease[i] = 0;
                // initialize the release task and set its pid.
                ProcedureControlBlock releaseTask = new ProcedureControlBlock(totalTasks.get(i));
                releaseTask.dynamicTaskId = ++releaseTaskNum;
                waitingTasksPerCore.get(totalTasks.get(i).runningCpuCore).add(releaseTask);
                // store state information for the release task
                taskStates.add(new TaskStateInformation(releaseTask.staticTaskId, releaseTask.dynamicTaskId, releaseTask.priorities.peek(), releaseTask.runningCpuCore));
                // add arrival block when task is released
                // this state will be deleted if task running immediately
                taskStates.get(taskStates.size() - 1).addState(TASK_STATE.ARRIVAL_BLOCK, systemClock + 1);
                System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.staticTaskId, releaseTask.dynamicTaskId, systemClock);
            }else
                ++timeSinceLastRelease[i];
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
            waitingTasks.sort((task1, task2) -> -Integer.compare(task1.priorities.peek(), task2.priorities.peek()));
            ProcedureControlBlock waitingTask = waitingTasks.get(0);

            // if indicator is HI, low task can't be chosen
            while (waitingTask.criticality < criticality_indicator) {
                ++taskFinishTimes[waitingTask.staticTaskId];
                taskStates.get(waitingTask.dynamicTaskId - 1).killTask(systemClock);
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
                runningTaskPerCore.get(i).runningCpuCore = i;

                // modify the event information.
                ModifyRunningEvent(i, waitingTask);

                // add running state
                TaskStateInformation runningTaskState = taskStates.get(waitingTask.dynamicTaskId - 1);
                // record task running cpu for record indirect spin
                runningTaskState.endState(systemClock);
                if (runningTaskState.remainResourceComputationTime > 0)
                    runningTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, runningTaskState.resourceId);
                else
                    runningTaskState.addState(TASK_STATE.RUNNING, systemClock);

                continue;
            }


            // 2. Condition: the local task's priority(including the boosted) is smaller than the task.
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            // If the task is waiting for a global resource, we can't stop it.
            if (runningTask.spin)
                continue;

            // running task just use cpu to compute, doesn't access the resource. What's more, the task's priority is lower than the waiting task.
            if (!runningTask.isAccessLocalResource && !runningTask.isAccessGlobalResource && runningTask.priorities.peek() < waitingTask.priorities.peek())
            {
                // put the running task into the waiting queue.
                waitingTasks.add(runningTask);
                // set the running task, choosing from waitingTasks.
                runningTaskPerCore.set(i, waitingTask);
                // remove it from waiting tasks.
                waitingTasks.remove(0);
                runningTaskPerCore.get(i).runningCpuCore = i;

                // Modify the event information.
                ModifyRunningEvent(i, waitingTask);

                // end state for running task
                TaskStateInformation modifyTaskState = taskStates.get(runningTask.dynamicTaskId - 1);
                modifyTaskState.endState(systemClock);
                modifyTaskState.addState(TASK_STATE.PREEMPTED, systemClock);
                // add running state for waitting task
                modifyTaskState = taskStates.get(waitingTask.dynamicTaskId - 1);
                // record task running cpu for record indirect spin
                modifyTaskState.endState(systemClock);
                if (modifyTaskState.remainResourceComputationTime > 0)
                    modifyTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, modifyTaskState.resourceId);
                else
                    modifyTaskState.addState(TASK_STATE.RUNNING, systemClock);
            }

            // running task is access local resource and priority is lower.
            if (runningTask.isAccessLocalResource && runningTask.priorities.peek() < waitingTask.priorities.peek())
            {
                // put the running task into the waiting queue.
                waitingTasks.add(runningTask);
                // set the running task, choosing from waitingTasks.
                runningTaskPerCore.set(i, waitingTask);
                // remove it from waiting task.
                waitingTasks.remove(0);
                runningTaskPerCore.get(i).runningCpuCore = i;

                // Modify the event information.
                ModifyRunningEvent(i, waitingTask);

                // end state for running task
                TaskStateInformation modifyTaskState = taskStates.get(runningTask.dynamicTaskId - 1);
                modifyTaskState.endState(systemClock);
                modifyTaskState.addState(TASK_STATE.PREEMPTED, systemClock);
                // add running state for waitting task
                modifyTaskState = taskStates.get(waitingTask.dynamicTaskId - 1);
                // record task running cpu for record indirect spin
                modifyTaskState.endState(systemClock);
                if (modifyTaskState.remainResourceComputationTime > 0)
                    modifyTaskState.addState(TASK_STATE.RUNNING_WITH_LOCK, systemClock, modifyTaskState.resourceId);
                else
                    modifyTaskState.addState(TASK_STATE.RUNNING, systemClock);
            }
        }
    }

    /**
     * Save the old EventInformation and create a new EventInformation.
     * @param i the ith cpu core.
     */
    public static void ModifyRunningEvent(int i, ProcedureControlBlock runningTask)
    {
        // The new event.
        EventInformation newEvent;

        // Finish an event and use an array to save it.
        EventInformation runningEvent = runningEvents.get(i);
        runningEvent.endTime = systemClock;

        // special case: Finish the use of a resource and been preempted.
        if (runningEvent.startTime != runningEvent.endTime)
            eventRecords.get(i).add(runningEvent);

        if (runningTask == null)
        {
            // the cpu core is spare.
            newEvent = new EventInformation();
            newEvent.startTime = systemClock;
            newEvent.systemCriticality = criticality_indicator;
        }else
            // create an event information to record this event that the task was released.
            newEvent = new EventInformation(systemClock, runningTask, criticality_indicator);

        runningEvents.set(i, newEvent);
    }

    /**
     * Save the old indicatorInformation and create a new indicatorInformation.
     */
    public static void ModifyIndicatorEvent(ProcedureControlBlock runningTask, boolean end) {

        // finish last indicator event
        int idx = indicatorRecords.size() - 1;
        EventInformation runningEvent = indicatorRecords.get(idx);
        runningEvent.endTime = systemClock;

        // if it's not last one, add new event
        if(!end){
            EventInformation newEvent = new EventInformation(systemClock, runningTask, criticality_indicator);
            indicatorRecords.add(newEvent);
        }
    }

}
