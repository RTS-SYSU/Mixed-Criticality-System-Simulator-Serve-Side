package com.example.serveside.service.mrsp.entity;

import com.example.serveside.response.EventTimePoint;
import com.example.serveside.response.ToTalInformation;
import com.example.serveside.service.mrsp.generatorTools.SimpleSystemGenerator;

import javax.lang.model.type.ArrayType;
import java.lang.reflect.Array;
import java.util.*;

public class MixedCriticalSystem {
    /* System Setup */

    /* the criticality of system, 0 is low, 1 is high */
    public static int criticality_indicator = 0;
    /* The cpu core num. */
    public static int TOTAL_CPU_CORE_NUM = 2;

    /* The minimum period for every task. */
    public static int MIN_PERIOD = 20;

    /* The maximum period for every task. */
    public static int MAX_PERIOD = 500;

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
    public static ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> totalTasks;

    /* generate total tasks that the system has. */
    public static ArrayList<com.example.serveside.service.mrsp.entity.Resource> totalResources;

    /*  allocate task to the cpu core. */
    public static ArrayList<ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock>> allocatedTasks;

    /* record the times of the task that has been finished. */
    public static int[] taskFinishTimes;

    /* create a ArrayList to keep the information about the running task. */
    public static ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> runningTaskPerCore;

    /* create a ArrayList to save the task that is looking for cpu core. */
    public static ArrayList<ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock>> waitingTasksPerCore;

    /* create an int array list to show that the task can be release or not. */
    public static int[] timeSinceLastRelease;

    /* Save the time point that the event occurs. */
    public static ArrayList<ArrayList<com.example.serveside.response.EventTimePoint>> TaskEventTimePointsRecords;

    /* 记录任务在运行过程中的状态 */
    public static ArrayList<ArrayList<com.example.serveside.response.EventInformation>> TaskEventInformationRecords;

    /* 记录 cpu 在运行过程中的状态 */
    public static ArrayList<ArrayList<com.example.serveside.response.EventInformation>> cpuEventInformationRecords;

    /* 记录 cpu 在运行过程中事件发生的时间点 */
    public static ArrayList<ArrayList<com.example.serveside.response.EventTimePoint>> cpuEventTimePointRecords;

    /* 任务进行切换时 那些被抢占的任务(正在访问资源) */
    public static ArrayList<ProcedureControlBlock> preemptedAccessResourceTasks;

    /* 已发布的任务的信息 */
    public static ArrayList<com.example.serveside.response.TaskInformation> releaseTaskInformations;

    /* The time axis length*/
    public static Integer timeAxisLength;

    /* 发生关键级切换的时间点 */
    public static Integer criticalitySwitchTime;


    public static void main(String[] args)
    {
        SimpleSystemGenerator systemGenerator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_CPU_CORE_NUM,
                TOTAL_CPU_CORE_NUM * NUMBER_OF_TASK_IN_A_PARTITION, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
                RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

        // Generate the tasks that the system need to execute.
//        totalTasks = systemGenerator.generateTasks();
        totalTasks = systemGenerator.testGenerateTask();

        // Generate the resources that the system has.
//        totalResources = systemGenerator.generateResources();
        totalResources = systemGenerator.testGenerateResources();

        // Generate the resource usage, i.e. task access the resource(time, times)
//        allocatedTasks = systemGenerator.generateResourceUsage(totalTasks, totalResources);
        allocatedTasks = systemGenerator.testGenerateResourceUsage(totalTasks, totalResources);

        // Print the information about the task.
        for (int i = 0; i < allocatedTasks.size(); ++i) {
            System.out.printf("CPU %d:\n", i);
            System.out.print("\tTask Information:\n");

            for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock task : allocatedTasks.get(i)) {
                System.out.printf("\t\t\tTask %d\n", task.staticTaskId);
                System.out.printf("\t\t\t\tPriority: %d, Criticality: %d\n", task.priorities.peek(), task.criticality);
                System.out.printf("\t\t\t\tWCCT_low: %d, WCCT_high: %d\n", task.WCCT_low, task.WCCT_high);
                System.out.printf("\t\t\t\tUtilization: %.2f, Period: %d, CPU time: %d\n", task.utilization, task.period, task.totalNeededTime);
                for (int j = 0; j < task.accessResourceIndex.size(); ++j) {
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

        // 开始运行模拟器进行模拟
        SystemExecute();
    }

    /*
    * 开始运行模拟器进行模拟:
    *   1. 初始化工作
    *   2. 每个周期都运行
    * */
    public static void SystemExecute()
    {
        /*
        * 初始化工作
        * */
        // 时钟周期
        systemClock = 0;

        // 目前已经发布的任务个数
        releaseTaskNum = 0;

        // 初始化系统的关键级
        criticality_indicator = 0;

        // 初始化发生关键级切换的时间点
        criticalitySwitchTime = -1;

        // 每一种任务执行完的次数
        taskFinishTimes = new int[totalTasks.size()];

        // CPU 上正在执行哪些任务
        runningTaskPerCore = new ArrayList<>(TOTAL_CPU_CORE_NUM);
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            runningTaskPerCore.add(null);

        // 每个 CPU 上正在等待的任务
        waitingTasksPerCore = new ArrayList<>();
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            waitingTasksPerCore.add(new ArrayList<>());

        // 每种任务距离上一次发布已经过去的时间
        timeSinceLastRelease = new int[totalTasks.size()];
        for (int i = 0; i < totalTasks.size(); ++i)
            timeSinceLastRelease[i] = totalTasks.get(i).period;

        // 记录每一个任务在运行过程中发生的一些事件点
        TaskEventTimePointsRecords = new ArrayList<>();

        // 记录每一个任务的运行过程
        TaskEventInformationRecords = new ArrayList<>();

        // 记录每一个发布的任务的信息
        releaseTaskInformations = new ArrayList<>();

        // 在这一个时钟周期内被抢占的任务（任务正在使用资源）
        preemptedAccessResourceTasks = new ArrayList<>();

        // 记录 cpu 在运行过程中的状态
        cpuEventInformationRecords = new ArrayList<>();
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            cpuEventInformationRecords.add(new ArrayList<>());

        // 记录 cpu 在运行过程发生的事件的时间点
        cpuEventTimePointRecords = new ArrayList<>();
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
            cpuEventTimePointRecords.add(new ArrayList<>());

        while (!SystemSuspend())
            ExecuteOnceClock();

        // 终止所有当前所有运行在 cpu 上的任务的状态
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            // 1. 终止 cpu 上的状态
            ChangeCpuTaskState(i, null, "");
            // 2. 终止 running 的状态
            if (runningTaskPerCore.get(i) != null)
            {
                ArrayList<com.example.serveside.response.EventInformation> taskEventInformation = TaskEventInformationRecords.get(runningTaskPerCore.get(i).dynamicTaskId);
                taskEventInformation.get(taskEventInformation.size() - 1).setEndTime(systemClock);
            }
        }

        // 终止所有没有运行在 cpu 上的任务的状态
        for (ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
        {
            // 1. 终止 waiting Task 的状态
            for (ProcedureControlBlock waitingTask : waitingTasks) {
                ArrayList<com.example.serveside.response.EventInformation> taskEventInformation = TaskEventInformationRecords.get(waitingTask.dynamicTaskId);
                if (!taskEventInformation.isEmpty())
                    taskEventInformation.get(taskEventInformation.size() - 1).setEndTime(systemClock);
            }
        }
    }

    /*
    * SystemSuspend: 系统暂停的条件：所有类型的任务都执行过1次
    * */
    public static boolean SystemSuspend()
    {
        for (int finishTimes : taskFinishTimes)
            if (finishTimes == 0)
                return false;

        return true;
    }

    /*
    * ExecuteOneClock: 模拟器在一个时钟周期内需要完成的事
    *   1. ExecuteTasks() :
    *       -判断任务在当前时刻要不要访问资源
    *   2. IncreaseElapsedTime():
    *       -正在运行的任务运行时间+1（各种时间都需要加1）
    *   3. ReleaseTasks() :
    *       -模拟器在当前时刻需不需要释放任务
    *   4. ChooseTaskToRun():
    *       -任务切换
    * */
    public static void ExecuteOnceClock()
    {
//        System.out.printf("System Clock: %d \n", systemClock);
//        if (systemClock == 16 || systemClock == 35 || systemClock == 49 || systemClock == 57)
//            System.out.print("Hello World");
        ExecuteTasks();

        IncreaseElapsedTime();

        ReleaseTasks();

        ChooseTaskToRun();
    }

    /*
    * 执行任务：主要任务是判断任务在这个 executeTime 下需不需要访问资源
    * */
    public static void ExecuteTasks()
    {
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
        {
            if (runningTask == null)
                continue;

            // 1. 任务不需要请求资源
            // 2. 任务已经请求完了所有的资源
            // 3. 任务正在等待资源(direct spinning)
            // 4. 任务已经获取到资源了
            if (runningTask.resourceAccessTime.isEmpty() || runningTask.requestResourceTh >= runningTask.resourceAccessTime.size() || runningTask.spin || runningTask.isAccessGlobalResource || runningTask.isAccessLocalResource)
                continue;

            // 任务到申请资源的时间点了
            if (runningTask.executedTime == runningTask.resourceAccessTime.get(runningTask.requestResourceTh))
            {
                Resource accquireResource = totalResources.get(runningTask.accessResourceIndex.get(runningTask.requestResourceTh));

                // accquireResource 被占据(说明是全局资源)
                if (accquireResource.isOccupied)
                {
                    // 设置 runningTask 的状态：spin, 优先级短暂提升
                    runningTask.spin = true;
                    runningTask.priorities.push(accquireResource.ceiling.get(runningTask.baseRunningCpuCore));

                    // 任务加入到资源的等待队列当中
                    accquireResource.waitingQueue.add(runningTask);

                    // 任务新增状态：direct-spinning
                    ChangeTaskState(runningTask, "direct-spinning");

                    // 任务新增：事件时间点
                    TaskEventTimePointsRecords.get(runningTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "lock-attempt", systemClock, accquireResource.id));

                    // cpu 任务状态发生变化：execution --> direct-spinning
                    ChangeCpuTaskState(runningTask.baseRunningCpuCore, runningTask, "direct-spinning");
                    // cpu 新增事件发生时间点：lock-attempt
                    cpuEventTimePointRecords.get(runningTask.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "lock-attempt", systemClock, accquireResource.id));

                    // 处理 indirect spinning 的情况：高优先级任务抢占低优先级的任务，然后高优先级的任务申请访问资源时进行自旋
                    ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(runningTask.baseRunningCpuCore);
                    for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
                    {
                        if (waitingTask.basePriority < runningTask.basePriority && waitingTask.priorities.peek() < runningTask.priorities.peek())
                        {
                            ChangeTaskState(waitingTask, "indirect-spinning");
                        }
                    }

                }
                // accquireResource 没有被占据，空闲的
                else {
                    // accquireResource 访问的是全局资源还是局部资源（访问资源的类型）
                    runningTask.isAccessGlobalResource = accquireResource.isGlobal;
                    runningTask.isAccessLocalResource = !runningTask.isAccessGlobalResource;;

                    // 设置资源访问时间、访问资源时的系统关键级、优先级
                    runningTask.remainResourceComputationTime = criticality_indicator == 0 ? accquireResource.c_low : accquireResource.c_high;
                    runningTask.systemCriticalityWhenAccessResource = criticality_indicator;
                    runningTask.priorities.push(accquireResource.ceiling.get(runningTask.baseRunningCpuCore));

                    // 设置资源现在被占据
                    accquireResource.isOccupied = true;

                    // 任务新增状态：access-resource
                    ChangeTaskState(runningTask, "access-resource");

                    // 任务新增：事件时间点
                    TaskEventTimePointsRecords.get(runningTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "locked", systemClock, accquireResource.id));

                    // cpu 上运行的任务状态发生变化：normal-execution --> access-resource
                    ChangeCpuTaskState(runningTask.baseRunningCpuCore, runningTask, "access-resource");
                    // cpu 新增事件发生时间点：locked
                    cpuEventTimePointRecords.get(runningTask.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "locked", systemClock, accquireResource.id));

                }
            }
        }
    }

    public static void ReleaseTasks()
    {
        Random random = new Random();

        int releaseTaskStaticId;

        if (systemClock == 1)
        {
            releaseTaskStaticId = 0;
            // reset
            timeSinceLastRelease[releaseTaskStaticId] = 0;
            // initialize the release task and set its pid.
            ProcedureControlBlock releaseTask = new ProcedureControlBlock(totalTasks.get(releaseTaskStaticId));
            releaseTask.dynamicTaskId = releaseTaskNum++;
            waitingTasksPerCore.get(totalTasks.get(releaseTaskStaticId).baseRunningCpuCore).add(releaseTask);

            // 记录一下发布的任务的基本信息
            releaseTaskInformations.add(new com.example.serveside.response.TaskInformation(releaseTask, systemClock));

            System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.staticTaskId, releaseTask.dynamicTaskId, systemClock);

            // 记录一下任务发布的时间点
            TaskEventTimePointsRecords.add(new ArrayList<>());
            TaskEventTimePointsRecords.get(releaseTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.staticTaskId, releaseTask.dynamicTaskId, "release", systemClock, -1));

            // 创建一个新的 taskEventInformation 来保存任务的运行过程
            TaskEventInformationRecords.add(new ArrayList<>());

            releaseTaskStaticId = 1;
            // reset
            timeSinceLastRelease[releaseTaskStaticId] = 0;
            // initialize the release task and set its pid.
            releaseTask = new ProcedureControlBlock(totalTasks.get(releaseTaskStaticId));
            releaseTask.dynamicTaskId = releaseTaskNum++;
            waitingTasksPerCore.get(totalTasks.get(releaseTaskStaticId).baseRunningCpuCore).add(releaseTask);

            // 记录一下发布的任务的基本信息
            releaseTaskInformations.add(new com.example.serveside.response.TaskInformation(releaseTask, systemClock));

            System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.staticTaskId, releaseTask.dynamicTaskId, systemClock);

            // 记录一下任务发布的时间点
            TaskEventTimePointsRecords.add(new ArrayList<>());
            TaskEventTimePointsRecords.get(releaseTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.staticTaskId, releaseTask.dynamicTaskId, "release", systemClock, -1));

            // 创建一个新的 taskEventInformation 来保存任务的运行过程
            TaskEventInformationRecords.add(new ArrayList<>());

            ++timeSinceLastRelease[2];
            ++timeSinceLastRelease[3];

            return ;
        }

        if (systemClock == 3)
        {
            releaseTaskStaticId = 2;
            // reset
            timeSinceLastRelease[releaseTaskStaticId] = 0;
            // initialize the release task and set its pid.
            ProcedureControlBlock releaseTask = new ProcedureControlBlock(totalTasks.get(releaseTaskStaticId));
            releaseTask.dynamicTaskId = releaseTaskNum++;
            waitingTasksPerCore.get(totalTasks.get(releaseTaskStaticId).baseRunningCpuCore).add(releaseTask);

            // 记录一下发布的任务的基本信息
            releaseTaskInformations.add(new com.example.serveside.response.TaskInformation(releaseTask, systemClock));

            System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.staticTaskId, releaseTask.dynamicTaskId, systemClock);

            // 记录一下任务发布的时间点
            TaskEventTimePointsRecords.add(new ArrayList<>());
            TaskEventTimePointsRecords.get(releaseTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.staticTaskId, releaseTask.dynamicTaskId, "release", systemClock, -1));

            // 创建一个新的 taskEventInformation 来保存任务的运行过程
            TaskEventInformationRecords.add(new ArrayList<>());

            ++timeSinceLastRelease[0];
            ++timeSinceLastRelease[1];
            ++timeSinceLastRelease[3];

            return ;
        }

        if (systemClock == 4)
        {
            releaseTaskStaticId = 3;
            // reset
            timeSinceLastRelease[releaseTaskStaticId] = 0;
            // initialize the release task and set its pid.
            ProcedureControlBlock releaseTask = new ProcedureControlBlock(totalTasks.get(releaseTaskStaticId));
            releaseTask.dynamicTaskId = releaseTaskNum++;
            waitingTasksPerCore.get(totalTasks.get(releaseTaskStaticId).baseRunningCpuCore).add(releaseTask);

            // 记录一下发布的任务的基本信息
            releaseTaskInformations.add(new com.example.serveside.response.TaskInformation(releaseTask, systemClock));

            System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.staticTaskId, releaseTask.dynamicTaskId, systemClock);

            // 记录一下任务发布的时间点
            TaskEventTimePointsRecords.add(new ArrayList<>());
            TaskEventTimePointsRecords.get(releaseTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.staticTaskId, releaseTask.dynamicTaskId, "release", systemClock, -1));

            // 创建一个新的 taskEventInformation 来保存任务的运行过程
            TaskEventInformationRecords.add(new ArrayList<>());

            ++timeSinceLastRelease[0];
            ++timeSinceLastRelease[1];
            ++timeSinceLastRelease[2    ];

            return ;
        }

        if (systemClock < 14)
            return ;

        for (int i = 0; i < totalTasks.size(); ++i)
        {
            // Even if the time elapsed since the last release is greater than the period, it still has a probability of being released.
            if (timeSinceLastRelease[i] >= totalTasks.get(i).period && random.nextDouble() < 0.2)
            {
                // if at HI, low task be seen finished
                if (totalTasks.get(i).criticality < criticality_indicator){
                    ++taskFinishTimes[totalTasks.get(i).staticTaskId];
                    continue;
                }

                // reset
                timeSinceLastRelease[i] = 0;
                // initialize the release task and set its pid.
                ProcedureControlBlock releaseTask = new ProcedureControlBlock(totalTasks.get(i));
                releaseTask.dynamicTaskId = releaseTaskNum++;
                waitingTasksPerCore.get(totalTasks.get(i).baseRunningCpuCore).add(releaseTask);

                // 记录一下发布的任务的基本信息
                releaseTaskInformations.add(new com.example.serveside.response.TaskInformation(releaseTask, systemClock));

                System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.staticTaskId, releaseTask.dynamicTaskId, systemClock);

                // 记录一下任务发布的时间点
                TaskEventTimePointsRecords.add(new ArrayList<>());
                TaskEventTimePointsRecords.get(releaseTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.staticTaskId, releaseTask.dynamicTaskId, "release", systemClock, -1));

                // 创建一个新的 taskEventInformation 来保存任务的运行过程
                TaskEventInformationRecords.add(new ArrayList<>());

            }else
                ++timeSinceLastRelease[i];
        }
    }

    public static void IncreaseElapsedTime()
    {
        ++systemClock;

        // 1. 处理运行在 cpu 上的任务
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
        {
            if (runningTask == null)
                continue;

            ++runningTask.elapsedTime;

            // 任务正在访问资源（资源使用还剩多少时间）
            if (runningTask.isAccessGlobalResource || runningTask.isAccessLocalResource)
                --runningTask.remainResourceComputationTime;

            // 增加 computeAndSpinTime（判断有没有超 WCCT_Low）
            // computeAndSpinTIme: pure computation + spin
            if (!runningTask.isAccessGlobalResource && !runningTask.isAccessLocalResource)
                ++runningTask.computeAndSpinTime;

            // 增加 executedTime（用于判断什么时候访问资源）
            if (!runningTask.spin)
                ++runningTask.executedTime;

        }

        // 2. 处理没有在 cpu 上执行的任务
        for (ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
        {
            for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
            {
                ++waitingTask.elapsedTime;

                // 判断 waitingTask 是否在帮助其他 task 运行
                if (waitingTask.isHelp)
                    ++waitingTask.computeAndSpinTime;
            }
        }

        // 4. 处理使用完资源的任务(需要加上条件判断：当前有任务在执行+任务有在访问资源)
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
            // 资源使用完，释放资源
            if (runningTask != null && (runningTask.isAccessLocalResource || runningTask.isAccessGlobalResource) && runningTask.remainResourceComputationTime == 0)
                ReleaseResource(runningTask);

        // 5. 处理在 baseRunningCpuCore 上完成计算的的任务
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
            // 需要注意：此时 task 不能够访问资源（在高关键级下面，executedTime 可能会超过 totalNeededTime）
            if (runningTask != null && !runningTask.isAccessLocalResource && !runningTask.isAccessGlobalResource && runningTask.executedTime == runningTask.totalNeededTime)
            {
                ChangeTaskState(runningTask, "completion");
                runningTaskPerCore.set(runningTask.baseRunningCpuCore, null);
                ++taskFinishTimes[runningTask.staticTaskId];

                // 任务新增：事件时间点（完成任务）
                ChangeTaskState(runningTask, "completion");
                TaskEventTimePointsRecords.get(runningTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "completion", systemClock, -1));

                // cpu 上的任务完成(状态发生变化)：normal-execution --> completion
                cpuEventTimePointRecords.get(runningTask.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "completion", systemClock, -1));
                ChangeCpuTaskState(runningTask.baseRunningCpuCore, null, "");
            }

        // 6. 判断 runningTaskPerCore 上的 task 和 waitingTask 上的 helpTask 的 computeAndSpin 有没有超过 WCCT_low
        if (criticality_indicator != 1)
        {
            // 在 cpu 上运行的任务/running task
            for (ProcedureControlBlock runningTask : runningTaskPerCore)
            {
                if (runningTask != null && runningTask.computeAndSpinTime > runningTask.WCCT_low)
                {
                    criticalitySwitchTime = systemClock;
                    System.out.printf("Static Task Id : %d (Dynamic Task Id : %d) cause Criticality Switch at System Clock : %d\n", runningTask.staticTaskId, runningTask.dynamicTaskId, systemClock);
                    criticality_indicator = 1;
                    break;
                }
            }

            // 在 waitingTaskPerCore 上的 helpTask
            if (criticality_indicator != 1)
            {
                for (ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
                {
                    for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
                    {
                        if (waitingTask.isHelp && waitingTask.computeAndSpinTime > waitingTask.WCCT_low)
                        {
                            criticalitySwitchTime = systemClock;
                            System.out.printf("Static Task Id : %d (Dynamic Task Id : %d) cause Criticality Switch at System Clock : %d\n", waitingTask.staticTaskId, waitingTask.dynamicTaskId, systemClock);
                            criticality_indicator = 1;
                            break;
                        }
                    }
                }
            }
        }

        // 终止低关键级任务的执行
        if (criticality_indicator == 1)
        {
            // 1. 处理在 cpu 上运行的低关键级任务
            for (int i = 0; i < runningTaskPerCore.size(); ++i)
            {
                com.example.serveside.service.mrsp.entity.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

                if (runningTask == null)
                    continue;

                // 高关键级下不需要被终止的任务
                // 1. 高关键级任务
                if (runningTask.criticality >= criticality_indicator)
                    continue;

                // 2. 任务正在访问资源
                if (runningTask.isAccessGlobalResource || runningTask.isAccessLocalResource)
                    continue;

                // 不满足前面两个条件的进程可以被终止(killed)
                // 1. 低关键级的任务 && 2. (任务 normal-execution || 任务 spin)

                // cpu 核上运行的任务变为空
                runningTaskPerCore.set(i, null);

                // cpu 甘特图上终止 runningTask 的运行, 并显示 shut-down 符号
                ChangeCpuTaskState(i, null, "");
                cpuEventTimePointRecords.get(i).add(new EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "killed", systemClock, -1));

                // task 甘特图上终止运行，并显示 shut-down 符号
                ChangeTaskState(runningTask, "killed");
                TaskEventTimePointsRecords.get(runningTask.dynamicTaskId).add(new EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "killed", systemClock, -1));

                // task 标记为已经执行完一次
                ++taskFinishTimes[runningTask.staticTaskId];

                // 如果任务在资源的等待队列当中，将其移除
                if (runningTask.spin)
                {
                    Resource accquireResource = totalResources.get(runningTask.accessResourceIndex.get(runningTask.requestResourceTh));
                    accquireResource.waitingQueue.remove(runningTask);
                }
            }

            // 2. 处理在 cpu 等待队列上的任务
            for (ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
            {
                Iterator<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasksIterator = waitingTasks.iterator();
                while (waitingTasksIterator.hasNext())
                {
                    com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask = waitingTasksIterator.next();

                    // 终止任务需要满足的条件
                    // 1. 低关键级任务
                    // 2. 任务没有访问资源
                    // 3. 任务没有启动帮助机制（help-mechanism）
                    if (waitingTask.criticality < criticality_indicator && !waitingTask.isAccessLocalResource && !waitingTask.isAccessGlobalResource && !waitingTask.isHelp)
                    {
                        // 将 waitingTask 从等待列表中移除
                        waitingTasksIterator.remove();

                        // 任务甘特图上显示终止/shut-down
                        ChangeTaskState(waitingTask, "killed");
                        TaskEventTimePointsRecords.get(waitingTask.dynamicTaskId).add(new EventTimePoint(waitingTask.staticTaskId, waitingTask.dynamicTaskId, "killed", systemClock, -1));

                        // task 标记为已经执行完一次
                        ++taskFinishTimes[waitingTask.staticTaskId];

                        // 如果任务此时正在等待资源，将其从等待队列中移除
                        if (waitingTask.spin)
                        {
                            Resource accquireResource = totalResources.get(waitingTask.accessResourceIndex.get(waitingTask.requestResourceTh));
                            accquireResource.waitingQueue.remove(waitingTask);
                        }
                    }
                }
            }
        }
    }

    public static void ReleaseResource(ProcedureControlBlock runningTask)
    {
        // 要释放的资源
        Resource releaseResource = totalResources.get(runningTask.accessResourceIndex.get(runningTask.requestResourceTh));

        // 获取help task
        com.example.serveside.service.mrsp.entity.ProcedureControlBlock helpTask = null;

        //  runningTask 迁移到其他 cpu 核上运行
        if (runningTask.immigrateRunningCpuCore != -1)
        {
            // 弹出在其迁移 cpu 核上运行的优先级
            runningTask.priorities.pop();

            for (ProcedureControlBlock waitingTask : releaseResource.waitingQueue)
            {
                if (waitingTask.baseRunningCpuCore == runningTask.immigrateRunningCpuCore)
                {
                    helpTask = waitingTask;
                    break;
                }
            }

            // 修改 helpTask 的状态
            if (helpTask != null)
            {
                helpTask.isHelp = false;
                // 结束 help-task 的上一个状态：help-direct-spin
                ChangeTaskState(helpTask, "");

                // 设置 immigrateRunningCpuCore 上执行的任务为 helpTask
                // helpTask 回归 immigrateRunningCpuCore.
                // immigrateRunningCpuCore 甘特图上显示 switch-task 标记
                waitingTasksPerCore.get(runningTask.immigrateRunningCpuCore).remove(helpTask);
                runningTaskPerCore.set(runningTask.immigrateRunningCpuCore, helpTask);
                ChangeTaskState(helpTask, "");
                ChangeCpuTaskState(runningTask.immigrateRunningCpuCore, helpTask, "");
                cpuEventTimePointRecords.get(helpTask.baseRunningCpuCore).add(new EventTimePoint(helpTask.staticTaskId, helpTask.dynamicTaskId, "switch-task", systemClock, -1));
            }
        }

        // runningTask 释放资源
        releaseResource.isOccupied = false;
        // 1. 弹出 runningTask 的 boosted priority 并修改相关配置
        runningTask.priorities.pop();
        runningTask.isAccessLocalResource = runningTask.isAccessGlobalResource = false;
        ++runningTask.requestResourceTh;
        if (runningTask.systemCriticalityWhenAccessResource == 1)
            runningTask.executedTime -= (releaseResource.c_high - releaseResource.c_low);

        // runningTask 记录事件发生的时间点
        TaskEventTimePointsRecords.get(runningTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "unlocked", systemClock, releaseResource.id));
        // cpu 上也需要记录释放资源的时间点
        cpuEventTimePointRecords.get(runningTask.immigrateRunningCpuCore == -1 ? runningTask.baseRunningCpuCore : runningTask.immigrateRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "unlocked", systemClock, releaseResource.id));

        // 2.有任务在等待该资源
        if (!releaseResource.waitingQueue.isEmpty())
        {
            com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask = releaseResource.waitingQueue.get(0);
            releaseResource.waitingQueue.remove(0);
            // waitingTask 占据资源
            releaseResource.isOccupied = true;
            waitingTask.remainResourceComputationTime = (criticality_indicator == 0) ? releaseResource.c_low : releaseResource.c_high;
            waitingTask.systemCriticalityWhenAccessResource = criticality_indicator;
            // waitingTask 访问资源的类型
            waitingTask.isAccessGlobalResource = releaseResource.isGlobal;
            waitingTask.isAccessLocalResource = !waitingTask.isAccessGlobalResource;
            waitingTask.spin = false;

            // task state：access-resource
            ChangeTaskState(waitingTask, "access-resource");
            TaskEventTimePointsRecords.get(waitingTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(waitingTask.staticTaskId, waitingTask.dynamicTaskId, "locked", systemClock, releaseResource.id));

            // 判断 waiting Task 是否正在执行
            if (waitingTask == runningTaskPerCore.get(waitingTask.baseRunningCpuCore))
            {
                // cpu state:
                ChangeCpuTaskState(waitingTask.baseRunningCpuCore, waitingTask, "access-resource");
                cpuEventTimePointRecords.get(waitingTask.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(waitingTask.staticTaskId, waitingTask.dynamicTaskId, "locked", systemClock, releaseResource.id));
            }else
            {
                // 加入 preemptedAccessResourceTasks 当中去判断能不能启动 help mechanism
                preemptedAccessResourceTasks.add(waitingTask);
            }

            // waitingTask 获取到资源之后
            for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock _waitingTask : waitingTasksPerCore.get(waitingTask.baseRunningCpuCore))
            {
                ArrayList<com.example.serveside.response.EventInformation> _taskInformations = TaskEventInformationRecords.get(_waitingTask.dynamicTaskId);
                if (!_taskInformations.isEmpty() && _taskInformations.get(_taskInformations.size() - 1).getState().equals("indirect-spinning"))
                {
                    // 由 indirect-spinning-delay 变成 blocked
                    ChangeTaskState(_waitingTask, "blocked");
                }
            }


        }

        // runningTask 是迁移到别人的核心上进行执行的话，需要迁移回去
        if (runningTask.immigrateRunningCpuCore != -1)
        {
            // runningTask 此时也完成了执行，即 executedTime = totalNeedTime，那就直接在 immigrate cpu core 上结束该任务
            if (runningTask.executedTime == runningTask.totalNeededTime)
            {
                // immigrate cpu core 上显示 completion 标记
                cpuEventTimePointRecords.get(runningTask.immigrateRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "completion", systemClock, -1));

                // task state :1. end event; 2. symbol execution --> completion
                ChangeTaskState(runningTask, "completion");
                TaskEventTimePointsRecords.get(runningTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.staticTaskId, runningTask.dynamicTaskId, "completion", systemClock, -1));
            }else {
                // runningTask 返回到 base running cpu core 上继续执行
                waitingTasksPerCore.get(runningTask.baseRunningCpuCore).add(runningTask);

                // task state: 1. help-access-resource --> blocked(immigrate to base cpu core)
                ChangeTaskState(runningTask, "blocked");
            }

            // runningTask 取消迁移
            runningTask.immigrateRunningCpuCore = -1;
        }else
        {
            // 在原本的核心上执行，简单切换一下状态就行
            ChangeTaskState(runningTask, "");
            ChangeCpuTaskState(runningTask.baseRunningCpuCore, runningTask, "");
        }


    }

    /*
    * 任务进行切换：
    *   1. 高优先级的任务抢占低优先级的任务
    * */
    public static void ChooseTaskToRun()
    {

        // 1. 高优先级的任务抢占低优先级的任务
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            ArrayList<ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            if (waitingTasks.isEmpty())
                continue;

            // 先对 waitingTasks 按 priorities.peek() 进行优先级从大到小进行排序
            waitingTasks.sort((task1, task2) -> -Integer.compare(task1.priorities.peek(), task2.priorities.peek()));

            ProcedureControlBlock waitingTask = waitingTasks.get(0);

            // 1. CPU 空闲
            if (runningTask == null)
            {
                // waiting Task 放在对应的 CPU 核上运行
                runningTaskPerCore.set(i, waitingTask);

                // 任务状态发生变化: 恢复 waitingTask 以前的状态 / waitingTask 开始执行
                ChangeTaskState(waitingTask, "");

                // 需要将 waitingTask 从 waitingTasks 中移出来
                waitingTasks.remove(0);

                // cpu 甘特图发生变化: 运行的任务发生变化(state 根据任务自身的状态来决定)
                ChangeCpuTaskState(i, waitingTask, "");
                // cpu 甘特图加入 switch-task 标号
                cpuEventTimePointRecords.get(i).add(new EventTimePoint(waitingTask.staticTaskId, waitingTask.dynamicTaskId, "switch-task", systemClock, -1));

                // 将 waitingTask 从 preemptedAccessResourceTasks 中移除出来（如果有的话）
                preemptedAccessResourceTasks.remove(waitingTask);

                continue;
            }

            // 2. 抢占：waitingTask的优先级 > runningTask 的优先级
            if (waitingTask.priorities.peek() > runningTask.priorities.peek())
            {
                // waitingTask 状态发生变化, waitingTask 放在 CPU 上运行
                ChangeTaskState(waitingTask, "");
                runningTaskPerCore.set(i, waitingTask);

                // 判断 runningTask 是否满足迁移条件：
                if (runningTask.isAccessGlobalResource)
                    preemptedAccessResourceTasks.add(runningTask);

                // runningTask 放入原生的 waitingTasks 中
                waitingTasksPerCore.get(runningTask.baseRunningCpuCore).add(runningTask);

                // 需要将 waitingTask 从 waitingTasks 以及 preempttedAccessResourceTasks(如果有的话)中移出来
                waitingTasks.remove(0);
                preemptedAccessResourceTasks.remove(waitingTask);

                // runningTask 的状态也需要发生变化：blocked
                ChangeTaskState(runningTask, "blocked");

                // cpu 甘特图发生变化 : 运行的任务发生变化(state 根据任务自身的状态来决定)
                ChangeCpuTaskState(i, waitingTask, "");

                // cpu 甘特图中标出 switch task
                cpuEventTimePointRecords.get(i).add(new EventTimePoint(waitingTask.staticTaskId, waitingTask.dynamicTaskId, "switch-task", systemClock, -1));

                // 处理 indirect-spinning 的情况
                // low-priority/runningTask  --> 关闭 indirect-spinning
                if (runningTask.spin)
                {
                    // waitingTask 优先级最高，所以就省去了判断 waitingTask.basePriority > _waitingTask.basePriority
                    for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock _waitingTask : waitingTasks)
                    {
                        ArrayList<com.example.serveside.response.EventInformation> taskInformations = TaskEventInformationRecords.get(_waitingTask.dynamicTaskId);
                        if (!taskInformations.isEmpty() && taskInformations.get(taskInformations.size() - 1).getState().equals("indirect-spinning"))
                        {
                            ChangeTaskState(_waitingTask, "blocked");
                        }
                    }
                }

                // 如果 runningTask 是迁移到 immigrate running cpu core 上运行的话，需要修改 helpTask（提供帮助机制）的 isHelp 为 false
                // 除此之外，helpTask 的状态也需要发生变化: direct-spinning --> blocked.
                // 除此之外，在 waitingTasks 中的任务状态也需要发生变化 : indirect spinning -- > blocked /  arrival blocking -- > blocked.
                if (runningTask.immigrateRunningCpuCore != -1)
                {
                    com.example.serveside.service.mrsp.entity.ProcedureControlBlock helpTask = null;
                    for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock _waitingTask : waitingTasks)
                    {
                        if (_waitingTask.isHelp)
                        {
                            _waitingTask.isHelp = false;
                            break;
                        }
                    }
                    runningTask.immigrateRunningCpuCore = -1;

                    // waitingTasks 中的任务都需要变成 blocked
                    for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock _waitingTask : waitingTasks)
                        ChangeTaskState(_waitingTask, "blocked");


                }
            }
        }

        // 2. 访问全局资源的任务（被抢占）进行迁移
        for (Iterator<ProcedureControlBlock> iterator = preemptedAccessResourceTasks.iterator(); iterator.hasNext();)
        {
            ProcedureControlBlock preemptedTask = iterator.next();
            Resource accessResource = totalResources.get(preemptedTask.accessResourceIndex.get(preemptedTask.requestResourceTh));

            // 当 access-resource-task 在 help-cpu-core 上再次被抢占，可能有机会迁移回去
            if (runningTaskPerCore.get(preemptedTask.baseRunningCpuCore) == null || accessResource.ceiling.get(preemptedTask.baseRunningCpuCore) > runningTaskPerCore.get(preemptedTask.baseRunningCpuCore).priorities.peek())
            {
                // access-resource-task 可以迁移回原来的 cpu core
                // 1. 弹出其在 immigrate cpu core 上的优先级
                // 2. 现在的优先级就应该是：resource.get(preemptedTask.baseRunningCpuCore.priorities.peek())
                // 弹出的条件是 access-resource-task 已经发生过迁移，现在要回来
                if (preemptedTask.immigrateRunningCpuCore != -1)
                {
                    preemptedTask.priorities.pop();
                }

                // 2. access-resource-task 抢占 base-cpu-core 上的任务
                ProcedureControlBlock baseCpuCoreTask = runningTaskPerCore.get(preemptedTask.baseRunningCpuCore);
                if (baseCpuCoreTask != null)
                {
                    // baseCpuCoreTask 新增状态：blocked, baseCpuCoreTask 被放入 waitingTasks 当中
                    ChangeTaskState(baseCpuCoreTask, "blocked");
                    waitingTasksPerCore.get(preemptedTask.baseRunningCpuCore).add(baseCpuCoreTask);
                }

                // access-resource-task 占据 base-running-cpu-core
                ChangeTaskState(preemptedTask, "");
                runningTaskPerCore.set(preemptedTask.baseRunningCpuCore, preemptedTask);
                preemptedTask.immigrateRunningCpuCore = -1;

                // base cpu core 上运行的任务发生变化: access-resource-task
                // base cpu core 甘特图上显示 switch-task
                ChangeCpuTaskState(preemptedTask.baseRunningCpuCore, preemptedTask, "");
                cpuEventTimePointRecords.get(preemptedTask.baseRunningCpuCore).add(new EventTimePoint(preemptedTask.staticTaskId, preemptedTask.dynamicTaskId, "switch-task", systemClock, accessResource.id));
                // 将 preemptedATask 从队列(preemptedTask 以及 waitingTasksPerCore 队列)当中删除
                iterator.remove();
                waitingTasksPerCore.get(preemptedTask.baseRunningCpuCore).remove(preemptedTask);

                continue;
            }

            // 不可以迁移回去
            // 访问 waitingQueue 里面的 task 看看他们是否可以提供支持
            for (ProcedureControlBlock helpTask : accessResource.waitingQueue)
            {
                // 1. 自旋等待资源的任务可以提供帮助
                // 自旋等待资源任务在 cpu 核上自旋，可以提供帮助
                if (helpTask.dynamicTaskId == runningTaskPerCore.get(helpTask.baseRunningCpuCore).dynamicTaskId)
                {
                    // helpTask 切换任务状态 --> help-direct-spinning(helpTask 帮助 preemptedTask 执行)
                    // preemptedTask 切换任务状态 --> help-access-resource(preempted task 借助 helpTask 进行执行)
                    ChangeTaskState(helpTask, "help-direct-spinning");
                    helpTask.isHelp = true;

                    ChangeTaskState(preemptedTask, "help-access-resource");

                    // helpTask 放回对应 cpu 核上的 waitingTasksPerCore, preemptedTask 放在 helpTask 对应的 cpu 核上执行
                    waitingTasksPerCore.get(helpTask.baseRunningCpuCore).add(helpTask);

                    // preemptedTask 用比 helpTask 高一点的优先级在 helpTask 对应的 cpu 核上运行
                    // 在此之前，需要判断 preemptedTask 是否已经迁移过了
                    if (preemptedTask.immigrateRunningCpuCore != -1)
                        preemptedTask.priorities.pop();

                    // 任务迁移
                    preemptedTask.immigrateRunningCpuCore = helpTask.baseRunningCpuCore;
                    runningTaskPerCore.set(helpTask.baseRunningCpuCore, preemptedTask);
                    preemptedTask.priorities.push(helpTask.priorities.peek() + 1);

                    // immigrate cpu core 上运行的任务发生变化：help-task --> access-resource-task
                    // immigrate cpu core 上的甘特图显示 switch-task
                    ChangeCpuTaskState(preemptedTask.immigrateRunningCpuCore, preemptedTask, "help-access-resource");
                    cpuEventTimePointRecords.get(preemptedTask.immigrateRunningCpuCore).add(new EventTimePoint(preemptedTask.staticTaskId, preemptedTask.dynamicTaskId, "switch-task", systemClock, accessResource.id));

                    // 将 preemptedATask 从队列当中删除
                    iterator.remove();
                    waitingTasksPerCore.get(preemptedTask.baseRunningCpuCore).remove(preemptedTask);

                    break;
                }
            }
        }

        // 处理开启 indirect-spinning 的情况（任务又重新获得 cpu 的执行权）
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            com.example.serveside.service.mrsp.entity.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            if (runningTask == null)
                continue;

            // 1. 普通的任务自旋
            // 2. help-mechanism: 启动帮助机制
            if (runningTask.spin || runningTask.immigrateRunningCpuCore != -1)
            {
                // 开启 indirect-spinning 状态
                for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasksPerCore.get(i))
                {
                    // 1. 如果是提供帮助机制的 task，则不需要由 help-access-resource 状态变成 indirect-spinning
                    if (!waitingTask.isHelp && waitingTask.priorities.peek() < runningTask.priorities.peek())
                    {
                        ChangeTaskState(waitingTask, "indirect-spinning");
                    }
                }
            }
        }

        // 处理 Arrival Blocking 的情况
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            if (waitingTasksPerCore.get(i).isEmpty())
                continue;

            com.example.serveside.service.mrsp.entity.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            // Arrival Blocking 发生的条件
            // 1. waitingTask 刚刚到达，即还没有开始执行
            // 2. waitingTask 的基础优先级比 runningTask 的基础优先级更高
            // 3. runningTask 的运行时优先级(priorities.peek()) 比 waitingTask 的运行时优先级(priorities.peek()) 更高
            ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
            {
                // 判断 Arrival Blocking 发生的情况
                if (waitingTask.executedTime == 0)
                {
                    // 1. runningTask 本身就是在该 cpu core 上进行运行的
                    if (runningTask.immigrateRunningCpuCore == -1 && waitingTask.basePriority > runningTask.basePriority && waitingTask.priorities.peek() < runningTask.priorities.peek())
                    {
                        ChangeTaskState(waitingTask, "arrival-blocking");
                    }

                    // 2. runningTask 是迁移到该 cpu 核上运行的
                    if (runningTask.immigrateRunningCpuCore != -1)
                    {
                        // 先找出提供帮助的 task
                        com.example.serveside.service.mrsp.entity.ProcedureControlBlock helpTask = null;
                        for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock _waitingTask : waitingTasks)
                        {
                            if (_waitingTask.isHelp)
                            {
                                helpTask = _waitingTask;
                                break;
                            }
                        }

                        // helpTask 的 优先级小于 waitingTask（因此helpTask本质上阻碍了waitingTask的执行）
                        if (helpTask.basePriority < waitingTask.basePriority)
                        {
                            ChangeTaskState(waitingTask, "arrival-blocking");
                        }
                    }
                }
            }
        }

        // 撤销 Arrival Blocking 的状态
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            if (waitingTasksPerCore.get(i).isEmpty())
                continue;

            com.example.serveside.service.mrsp.entity.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);

            // 撤销 Arrival Blocking 的时机 : 更高优先级的任务在执行
            for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
            {
                ArrayList<com.example.serveside.response.EventInformation> taskInformations = TaskEventInformationRecords.get(waitingTask.dynamicTaskId);

                if (!taskInformations.isEmpty() && taskInformations.get(taskInformations.size() - 1).getState().equals("arrival-blocking") && waitingTask.basePriority < runningTask.basePriority)
                {
                    ChangeTaskState(waitingTask, "blocked");
                }
            }
        }

    }

    /*
    * 切换任务状态：保存这一任务的上一个状态，然后开始下一个状态
    * */
    public static void ChangeTaskState(com.example.serveside.service.mrsp.entity.ProcedureControlBlock task, String _state)
    {
        ArrayList<com.example.serveside.response.EventInformation> taskInformations = TaskEventInformationRecords.get(task.dynamicTaskId);

        // 先终止上一个状态(如果有的话)
        if (!taskInformations.isEmpty())
        {
            if (taskInformations.get(taskInformations.size() - 1).getStartTime() != systemClock)
                taskInformations.get(taskInformations.size() - 1).setEndTime(systemClock);
            else
                taskInformations.remove(taskInformations.size() - 1);
        }

        // 新状态和旧状态状态一致（_arrival-blocking），那么就保持不变
        if (!taskInformations.isEmpty() && taskInformations.get(taskInformations.size() - 1).getState().equals(_state))
            return ;

        // 如果既不是 completion，也不是 shutdown，那么就开启一个新的状态
        if (!_state.equals("completion") && !_state.equals("killed"))
            taskInformations.add(new com.example.serveside.response.EventInformation(task, systemClock, _state));
    }

    /*
    * 切换 cpu 上任务的运行状态
    * */
    public static void ChangeCpuTaskState(int runningCpuCore, com.example.serveside.service.mrsp.entity.ProcedureControlBlock task, String _state)
    {
        ArrayList<com.example.serveside.response.EventInformation> cpuEventInformationRecord = cpuEventInformationRecords.get(runningCpuCore);
        // 终止上一个状态(如果不为空的话)
        if (!cpuEventInformationRecord.isEmpty())
        {
            if (cpuEventInformationRecord.get(cpuEventInformationRecord.size() - 1).getStartTime() != systemClock)
                cpuEventInformationRecord.get(cpuEventInformationRecord.size() - 1).setEndTime(systemClock);
            else
                cpuEventInformationRecord.remove(cpuEventInformationRecord.size() - 1);
        }

        // 开启一个新的状态
        cpuEventInformationRecord.add(new com.example.serveside.response.EventInformation(task, systemClock, _state));
    }

    /*
    * 打包 MrsP 协议的运行结果，然后返回给前端
    * */
    public static com.example.serveside.response.ToTalInformation PackageTotalInformation()
    {
        // 计算时间轴长度
        CalculateTimeAxisLength();

        // cpu gantt chart information cpu 甘特图信息 :
        List<com.example.serveside.response.GanttInformation> cpuGanttInformations = new ArrayList<>();
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            cpuGanttInformations.add(new com.example.serveside.response.GanttInformation(cpuEventInformationRecords.get(i), cpuEventTimePointRecords.get(i), timeAxisLength));
        }

        // task gantt chart information: 任务甘特图信息：
        List<com.example.serveside.response.TaskGanttInformation> taskGanttInformations = new ArrayList<>();
        for (com.example.serveside.response.TaskInformation releaseTaskInformation : releaseTaskInformations)
        {
            com.example.serveside.response.GanttInformation ganttInformation = new com.example.serveside.response.GanttInformation(TaskEventInformationRecords.get(releaseTaskInformation.getDynamicPid()), TaskEventTimePointsRecords.get(releaseTaskInformation.getDynamicPid()), timeAxisLength);

            int baseRunningCPUCore = -1;
            for (ProcedureControlBlock staticTask : totalTasks)
                if (releaseTaskInformation.getStaticPid() == staticTask.staticTaskId)
                {
                    baseRunningCPUCore = staticTask.baseRunningCpuCore;
                    break;
                }

            taskGanttInformations.add(new com.example.serveside.response.TaskGanttInformation(releaseTaskInformation.getStaticPid(), releaseTaskInformation.getDynamicPid(), baseRunningCPUCore, ganttInformation));
        }

        return new com.example.serveside.response.ToTalInformation(cpuGanttInformations, releaseTaskInformations, taskGanttInformations, criticalitySwitchTime);
    }

    /*
    * 计算 timeAxisLength
    * */
    public static void CalculateTimeAxisLength()
    {
        timeAxisLength = 0;

        for (ArrayList<com.example.serveside.response.EventInformation> TaskEventInformations : TaskEventInformationRecords)
        {
            if (!TaskEventInformations.isEmpty())
                timeAxisLength = Math.max(timeAxisLength, TaskEventInformations.get(TaskEventInformations.size() - 1).getEndTime());
        }
    }

    /*
    * 获取帮助 runningTask 运行的 helpTask(启动帮助机制)
    * */
    public static com.example.serveside.service.mrsp.entity.ProcedureControlBlock getHelpTask(ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks, int accessResourceId)
    {
        com.example.serveside.service.mrsp.entity.ProcedureControlBlock ret = null;

        for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
        {
            // 条件1 : waitingTask 正处于 spin 状态
            // 条件2 : waitingTask 和 runningTask 访问的是同一个资源
            if (waitingTask.spin && accessResourceId == waitingTask.accessResourceIndex.get(waitingTask.requestResourceTh))
            {
                ret = waitingTask;
                break;
            }
        }

        return ret;
    }
}