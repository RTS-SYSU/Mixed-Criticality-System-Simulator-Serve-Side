package com.example.serveside.service.mrsp.entity;

import com.example.serveside.response.EventTimePoint;
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
    public static ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> totalTasks;

    /* generate total tasks that the system has. */
    public static ArrayList<com.example.serveside.service.mrsp.entity.Resource> totalResources;

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
    public static void MrsPInitialize(ArrayList<BasicPCB> _totalTasks, ArrayList<BasicResource> _totalResources, int _totalCpuCoreNum, LinkedHashMap<Integer, ArrayList<Integer>> _taskReleaseTimes)
    {
        totalTasks = new ArrayList<>(_totalTasks.size());
        for (BasicPCB task : _totalTasks)
            totalTasks.add(new com.example.serveside.service.mrsp.entity.ProcedureControlBlock(task));

        totalResources = new ArrayList<>(_totalResources.size());
        for (BasicResource resource : _totalResources)
            totalResources.add(new com.example.serveside.service.mrsp.entity.Resource(resource));

        TOTAL_CPU_CORE_NUM = _totalCpuCoreNum;

        taskReleaseTimes = _taskReleaseTimes;

        iteratorTaskReleaseTimes = _taskReleaseTimes.entrySet().iterator();
        if (iteratorTaskReleaseTimes.hasNext())
            itemTaskReleaseTimes = iteratorTaskReleaseTimes.next();
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
        isSchedulable = true;

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
            timeSinceLastRelease[i] = totalTasks.get(i).basicPCB.period;

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
            ExecuteOneClock();

        // 终止所有当前所有运行在 cpu 上的任务的状态
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            // 1. 终止 cpu 上的状态
            ChangeCpuTaskState(i, null, "");
            // 2. 终止 running 的状态
            if (runningTaskPerCore.get(i) != null)
            {
                ArrayList<com.example.serveside.response.EventInformation> taskEventInformation = TaskEventInformationRecords.get(runningTaskPerCore.get(i).basicPCB.dynamicTaskId);
                taskEventInformation.get(taskEventInformation.size() - 1).setEndTime(systemClock);
            }
        }

        // 终止所有没有运行在 cpu 上的任务的状态
        for (ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
        {
            // 1. 终止 waiting Task 的状态
            for (ProcedureControlBlock waitingTask : waitingTasks) {
                ArrayList<com.example.serveside.response.EventInformation> taskEventInformation = TaskEventInformationRecords.get(waitingTask.basicPCB.dynamicTaskId);
                if (!taskEventInformation.isEmpty())
                    taskEventInformation.get(taskEventInformation.size() - 1).setEndTime(systemClock);
            }
        }
    }

    /*
    * SystemSuspend: 系统停止模拟的条件：所有类型的任务都执行过1次或者有任务超时了
    * */
    public static boolean SystemSuspend()
    {
        if (!isSchedulable)
            return true;

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
    public static void ExecuteOneClock()
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
            if (runningTask.basicPCB.resourceAccessTime.isEmpty() || runningTask.basicPCB.requestResourceTh >= runningTask.basicPCB.resourceAccessTime.size() || runningTask.basicPCB.spin || runningTask.basicPCB.isAccessGlobalResource || runningTask.basicPCB.isAccessLocalResource)
                continue;

            // 任务到申请资源的时间点了
            if (runningTask.basicPCB.executedTime == runningTask.basicPCB.resourceAccessTime.get(runningTask.basicPCB.requestResourceTh))
            {
                Resource accquireResource = totalResources.get(runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));

                // accquireResource 被占据(说明是全局资源)
                if (accquireResource.basicResource.isOccupied)
                {
                    // 设置 runningTask 的状态：spin, 优先级短暂提升
                    runningTask.basicPCB.spin = true;
                    runningTask.basicPCB.priorities.push(accquireResource.basicResource.ceiling.get(runningTask.basicPCB.baseRunningCpuCore));

                    // 任务加入到资源的等待队列当中
                    accquireResource.waitingQueue.add(runningTask);

                    // 任务新增状态：direct-spinning
                    ChangeTaskState(runningTask, "direct-spinning");

                    // 任务新增：事件时间点
                    TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "lock-attempt", systemClock, accquireResource.basicResource.id));

                    // cpu 任务状态发生变化：execution --> direct-spinning
                    ChangeCpuTaskState(runningTask.basicPCB.baseRunningCpuCore, runningTask, "direct-spinning");
                    // cpu 新增事件发生时间点：lock-attempt
                    cpuEventTimePointRecords.get(runningTask.basicPCB.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "lock-attempt", systemClock, accquireResource.basicResource.id));

                    // 处理 indirect spinning 的情况：高优先级任务抢占低优先级的任务，然后高优先级的任务申请访问资源时进行自旋
                    ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(runningTask.basicPCB.baseRunningCpuCore);
                    for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
                    {
                        if (waitingTask.basicPCB.basePriority < runningTask.basicPCB.basePriority)
                        {
                            ChangeTaskState(waitingTask, "indirect-spinning");
                        }
                    }

                }
                // accquireResource 没有被占据，空闲的
                else {
                    // accquireResource 访问的是全局资源还是局部资源（访问资源的类型）
                    runningTask.basicPCB.isAccessGlobalResource = accquireResource.basicResource.isGlobal;
                    runningTask.basicPCB.isAccessLocalResource = !runningTask.basicPCB.isAccessGlobalResource;

                    // 设置资源访问时间、访问资源时的系统关键级、优先级
                    runningTask.basicPCB.remainResourceComputationTime = criticality_indicator == 0 ? accquireResource.basicResource.c_low : accquireResource.basicResource.c_high;
                    runningTask.basicPCB.systemCriticalityWhenAccessResource = criticality_indicator;
                    runningTask.basicPCB.priorities.push(accquireResource.basicResource.ceiling.get(runningTask.basicPCB.baseRunningCpuCore));

                    // 设置资源现在被占据
                    accquireResource.basicResource.isOccupied = true;

                    // 任务新增状态：access-resource
                    ChangeTaskState(runningTask, "access-resource");

                    // 任务新增：事件时间点
                    TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "locked", systemClock, accquireResource.basicResource.id));

                    // cpu 上运行的任务状态发生变化：normal-execution --> access-resource
                    ChangeCpuTaskState(runningTask.basicPCB.baseRunningCpuCore, runningTask, "access-resource");
                    // cpu 新增事件发生时间点：locked
                    cpuEventTimePointRecords.get(runningTask.basicPCB.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "locked", systemClock, accquireResource.basicResource.id));

                }
            }
        }
    }

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
                ProcedureControlBlock releaseTask = new ProcedureControlBlock(totalTasks.get(releaseTaskId));
                releaseTask.basicPCB.dynamicTaskId = releaseTaskNum++;
                waitingTasksPerCore.get(totalTasks.get(releaseTaskId).basicPCB.baseRunningCpuCore).add(releaseTask);

                // 记录一下发布的任务的基本信息
                releaseTaskInformations.add(new com.example.serveside.response.TaskInformation(releaseTask, systemClock));

                System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.basicPCB.staticTaskId, releaseTask.basicPCB.dynamicTaskId, systemClock);

                // 记录一下任务发布的时间点
                TaskEventTimePointsRecords.add(new ArrayList<>());
                TaskEventTimePointsRecords.get(releaseTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.basicPCB.staticTaskId, releaseTask.basicPCB.dynamicTaskId, "release", systemClock, -1));

                // 创建一个新的 taskEventInformation 来保存任务的运行过程
                TaskEventInformationRecords.add(new ArrayList<>());
            }

            if (iteratorTaskReleaseTimes.hasNext())
                itemTaskReleaseTimes = iteratorTaskReleaseTimes.next();
            else
                itemTaskReleaseTimes = null;
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

            ++runningTask.basicPCB.elapsedTime;

            // 任务正在访问资源（资源使用还剩多少时间）
            if (runningTask.basicPCB.isAccessGlobalResource || runningTask.basicPCB.isAccessLocalResource)
                --runningTask.basicPCB.remainResourceComputationTime;

            // 增加 computeAndSpinTime（判断有没有超 WCCT_Low）
            // computeAndSpinTIme: pure computation + spin
            if (!runningTask.basicPCB.isAccessGlobalResource && !runningTask.basicPCB.isAccessLocalResource)
                ++runningTask.basicPCB.computeAndSpinTime;

            // 增加 executedTime（用于判断什么时候访问资源）
            if (!runningTask.basicPCB.spin)
                ++runningTask.basicPCB.executedTime;

            // 任务的 elapsedTime 超过 DeadLine 的, 这样子的话就没有必要进行调度了
            if (runningTask.basicPCB.elapsedTime > runningTask.basicPCB.deadline)
                isSchedulable = false;

        }

        // 2. 处理没有在 cpu 上执行的任务
        for (ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
        {
            for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
            {
                ++waitingTask.basicPCB.elapsedTime;

                // 判断 waitingTask 是否在帮助其他 task 运行
                if (waitingTask.isHelp)
                    ++waitingTask.basicPCB.computeAndSpinTime;

                // 任务的 elapsedTime 超过 DeadLine 的, 这样子的话就没有必要进行调度了
                if (waitingTask.basicPCB.elapsedTime > waitingTask.basicPCB.deadline)
                    isSchedulable = false;
            }
        }

        // 4. 处理使用完资源的任务(需要加上条件判断：当前有任务在执行+任务有在访问资源)
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
            // 资源使用完，释放资源
            if (runningTask != null && (runningTask.basicPCB.isAccessLocalResource || runningTask.basicPCB.isAccessGlobalResource) && runningTask.basicPCB.remainResourceComputationTime == 0)
                ReleaseResource(runningTask);

        // 5. 处理在 baseRunningCpuCore 上完成计算的的任务
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
            // 需要注意：此时 task 不能够访问资源（在高关键级下面，executedTime 可能会超过 totalNeededTime）
            if (runningTask != null && !runningTask.basicPCB.isAccessLocalResource && !runningTask.basicPCB.isAccessGlobalResource && runningTask.basicPCB.executedTime == runningTask.basicPCB.totalNeededTime)
            {
                ChangeTaskState(runningTask, "completion");
                runningTaskPerCore.set(runningTask.basicPCB.baseRunningCpuCore, null);
                ++taskFinishTimes[runningTask.basicPCB.staticTaskId];

                // 任务新增：事件时间点（完成任务）
                ChangeTaskState(runningTask, "completion");
                TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));

                // cpu 上的任务完成(状态发生变化)：normal-execution --> completion
                cpuEventTimePointRecords.get(runningTask.basicPCB.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));
                ChangeCpuTaskState(runningTask.basicPCB.baseRunningCpuCore, null, "");
            }

        // 6. 判断 runningTaskPerCore 上的 task 和 waitingTask 上的 helpTask 的 computeAndSpin 有没有超过 WCCT_low
        if (criticality_indicator != 1)
        {
            // 在 cpu 上运行的任务/running task
            for (ProcedureControlBlock runningTask : runningTaskPerCore)
            {
                if (runningTask != null && runningTask.basicPCB.computeAndSpinTime > runningTask.basicPCB.WCCT_low)
                {
                    criticalitySwitchTime = systemClock;
                    System.out.printf("Static Task Id : %d (Dynamic Task Id : %d) cause Criticality Switch at System Clock : %d\n", runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, systemClock);
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
                        if (waitingTask.isHelp && waitingTask.basicPCB.computeAndSpinTime > waitingTask.basicPCB.WCCT_low)
                        {
                            criticalitySwitchTime = systemClock;
                            System.out.printf("Static Task Id : %d (Dynamic Task Id : %d) cause Criticality Switch at System Clock : %d\n", waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, systemClock);
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
                if (runningTask.basicPCB.criticality >= criticality_indicator)
                    continue;

                // 2. 任务正在访问资源
                if (runningTask.basicPCB.isAccessGlobalResource || runningTask.basicPCB.isAccessLocalResource)
                    continue;

                // 不满足前面两个条件的进程可以被终止(killed)
                // 1. 低关键级的任务 && 2. (任务 normal-execution || 任务 spin)

                // cpu 核上运行的任务变为空
                runningTaskPerCore.set(i, null);

                // cpu 甘特图上终止 runningTask 的运行, 并显示 shut-down 符号
                ChangeCpuTaskState(i, null, "");
                cpuEventTimePointRecords.get(i).add(new EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "killed", systemClock, -1));

                // task 甘特图上终止运行，并显示 shut-down 符号
                ChangeTaskState(runningTask, "killed");
                TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "killed", systemClock, -1));

                // task 标记为已经执行完一次
                ++taskFinishTimes[runningTask.basicPCB.staticTaskId];

                // 如果任务在资源的等待队列当中，将其移除
                if (runningTask.basicPCB.spin)
                {
                    Resource accquireResource = totalResources.get(runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));
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
                    if (waitingTask.basicPCB.criticality < criticality_indicator && !waitingTask.basicPCB.isAccessLocalResource && !waitingTask.basicPCB.isAccessGlobalResource && !waitingTask.isHelp)
                    {
                        // 将 waitingTask 从等待列表中移除
                        waitingTasksIterator.remove();

                        // 任务甘特图上显示终止/shut-down
                        ChangeTaskState(waitingTask, "killed");
                        TaskEventTimePointsRecords.get(waitingTask.basicPCB.dynamicTaskId).add(new EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "killed", systemClock, -1));

                        // task 标记为已经执行完一次
                        ++taskFinishTimes[waitingTask.basicPCB.staticTaskId];

                        // 如果任务此时正在等待资源，将其从等待队列中移除
                        if (waitingTask.basicPCB.spin)
                        {
                            Resource accquireResource = totalResources.get(waitingTask.basicPCB.accessResourceIndex.get(waitingTask.basicPCB.requestResourceTh));
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
        Resource releaseResource = totalResources.get(runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));

        // 获取help task
        com.example.serveside.service.mrsp.entity.ProcedureControlBlock helpTask = null;

        //  runningTask 迁移到其他 cpu 核上运行
        if (runningTask.immigrateRunningCpuCore != -1)
        {
            // 弹出在其迁移 cpu 核上运行的优先级
            runningTask.basicPCB.priorities.pop();

            for (ProcedureControlBlock waitingTask : releaseResource.waitingQueue)
            {
                if (waitingTask.basicPCB.baseRunningCpuCore == runningTask.immigrateRunningCpuCore)
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

                // 如果 helpTask 是高关键级任务 || 系统目前处于低关键级状态，那么 helpTask 就可以重新放回 base cpu 上进行执行
                // 相反，helpTask 是低关键级任务 && 系统目前处于高关键级状态，那么 helpTask 就会被终止
                if (helpTask.basicPCB.criticality == 1 || criticality_indicator == 1)
                {
                    // 放入 base cpu 上进行执行
                    ChangeTaskState(helpTask, "");
                    ChangeCpuTaskState(runningTask.immigrateRunningCpuCore, helpTask, "");
                    runningTaskPerCore.set(runningTask.immigrateRunningCpuCore, helpTask);
                    cpuEventTimePointRecords.get(helpTask.basicPCB.baseRunningCpuCore).add(new EventTimePoint(helpTask.basicPCB.staticTaskId, helpTask.basicPCB.dynamicTaskId, "switch-task", systemClock, -1));
                }
                else {
                    // 终止 helpTask
                    // runningTask.immigrateRunningCpuCore 上没有任务在跑

                    // 1. cpu 上进行的处理
                        // 没有任务在运行
                        // cpu 上任务状态发生变化
                    ChangeCpuTaskState(runningTask.immigrateRunningCpuCore, null, "");
                    runningTaskPerCore.set(runningTask.immigrateRunningCpuCore, null);

                    // 2. 任务本身发生变化：
                    ChangeTaskState(helpTask, "killed");
                    TaskEventTimePointsRecords.get(helpTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "killed", systemClock, releaseResource.basicResource.id));

                    // 3. 将 helpTask 从 resource 的等待队列中去除
                    releaseResource.waitingQueue.remove(helpTask);
                }
            }
        }

        // runningTask 释放资源
        releaseResource.basicResource.isOccupied = false;
        // 1. 弹出 runningTask 的 boosted priority 并修改相关配置
        runningTask.basicPCB.priorities.pop();
        runningTask.basicPCB.isAccessLocalResource = runningTask.basicPCB.isAccessGlobalResource = false;
        ++runningTask.basicPCB.requestResourceTh;
        if (runningTask.basicPCB.systemCriticalityWhenAccessResource == 1)
            runningTask.basicPCB.executedTime -= (releaseResource.basicResource.c_high - releaseResource.basicResource.c_low);

        // runningTask 记录事件发生的时间点
        TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "unlocked", systemClock, releaseResource.basicResource.id));
        // cpu 上也需要记录释放资源的时间点
        cpuEventTimePointRecords.get(runningTask.immigrateRunningCpuCore == -1 ? runningTask.basicPCB.baseRunningCpuCore : runningTask.immigrateRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "unlocked", systemClock, releaseResource.basicResource.id));

        // 2.有任务在等待该资源
        if (!releaseResource.waitingQueue.isEmpty())
        {
            com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask = releaseResource.waitingQueue.get(0);
            releaseResource.waitingQueue.remove(0);
            // waitingTask 占据资源
            releaseResource.basicResource.isOccupied = true;
            waitingTask.basicPCB.remainResourceComputationTime = (criticality_indicator == 0) ? releaseResource.basicResource.c_low : releaseResource.basicResource.c_high;
            waitingTask.basicPCB.systemCriticalityWhenAccessResource = criticality_indicator;
            // waitingTask 访问资源的类型
            waitingTask.basicPCB.isAccessGlobalResource = releaseResource.basicResource.isGlobal;
            waitingTask.basicPCB.isAccessLocalResource = !waitingTask.basicPCB.isAccessGlobalResource;
            waitingTask.basicPCB.spin = false;

            // task state：access-resource
            ChangeTaskState(waitingTask, "access-resource");
            TaskEventTimePointsRecords.get(waitingTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "locked", systemClock, releaseResource.basicResource.id));

            // 判断 waiting Task 是否正在执行
            if (waitingTask == runningTaskPerCore.get(waitingTask.basicPCB.baseRunningCpuCore))
            {
                // cpu state:
                ChangeCpuTaskState(waitingTask.basicPCB.baseRunningCpuCore, waitingTask, "access-resource");
                cpuEventTimePointRecords.get(waitingTask.basicPCB.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "locked", systemClock, releaseResource.basicResource.id));

                // waitingTask 获取到资源之后，waitingTasks 中的阻塞任务状态就需要从 indirect-spinning --> blocked
                for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock _waitingTask : waitingTasksPerCore.get(waitingTask.basicPCB.baseRunningCpuCore))
                {
                    ArrayList<com.example.serveside.response.EventInformation> _taskInformations = TaskEventInformationRecords.get(_waitingTask.basicPCB.dynamicTaskId);
                    if (!_taskInformations.isEmpty() && _taskInformations.get(_taskInformations.size() - 1).getState().equals("indirect-spinning"))
                    {
                        // 由 indirect-spinning-delay 变成 blocked
                        ChangeTaskState(_waitingTask, "blocked");
                    }
                }

            }else
            {
                // 加入 preemptedAccessResourceTasks 当中去判断能不能启动 help mechanism
                preemptedAccessResourceTasks.add(waitingTask);
            }

        }

        // runningTask 是迁移到别人的核心上进行执行的话，需要迁移回去
        if (runningTask.immigrateRunningCpuCore != -1)
        {
            // runningTask 此时也完成了执行，即 executedTime = totalNeedTime，那就直接在 immigrate cpu core 上结束该任务
            if (runningTask.basicPCB.executedTime == runningTask.basicPCB.totalNeededTime)
            {
                // immigrate cpu core 上显示 completion 标记
                cpuEventTimePointRecords.get(runningTask.immigrateRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));

                // task state :1. end event; 2. symbol execution --> completion
                ChangeTaskState(runningTask, "completion");
                TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));
            }else {
                // runningTask 返回到 base running cpu core 上继续执行
                waitingTasksPerCore.get(runningTask.basicPCB.baseRunningCpuCore).add(runningTask);

                // task state: 1. help-access-resource --> blocked(immigrate to base cpu core)
                ChangeTaskState(runningTask, "blocked");
            }

            // runningTask 取消迁移
            runningTask.immigrateRunningCpuCore = -1;
        }else
        {
            // 在原本的核心上执行，简单切换一下状态就行
            ChangeTaskState(runningTask, "");
            ChangeCpuTaskState(runningTask.basicPCB.baseRunningCpuCore, runningTask, "");
        }


    }

    /*
    * 任务进行切换：
    *   1. 高优先级的任务抢占低优先级的任务
    * */
    public static void ChooseTaskToRun()
    {

        // 2. 高优先级的任务抢占低优先级的任务
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            ArrayList<ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            if (waitingTasks.isEmpty())
                continue;

            // 先对 waitingTasks 按 priorities.peek() 进行优先级从大到小进行排序
            waitingTasks.sort((task1, task2) -> -Integer.compare(task1.basicPCB.priorities.peek(), task2.basicPCB.priorities.peek()));

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
                cpuEventTimePointRecords.get(i).add(new EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "switch-task", systemClock, -1));

                // 将 waitingTask 从 preemptedAccessResourceTasks 中移除出来（如果有的话）
                preemptedAccessResourceTasks.remove(waitingTask);

                continue;
            }

            // 2. 抢占：waitingTask的优先级 > runningTask 的优先级
            if (waitingTask.basicPCB.priorities.peek() > runningTask.basicPCB.priorities.peek())
            {
                // waitingTask 状态发生变化, waitingTask 放在 CPU 上运行
                ChangeTaskState(waitingTask, "");
                runningTaskPerCore.set(i, waitingTask);

                // 判断 runningTask 是否满足迁移条件：
                if (runningTask.basicPCB.isAccessGlobalResource)
                    preemptedAccessResourceTasks.add(runningTask);

                // runningTask 放入原生的 waitingTasks 中
                waitingTasksPerCore.get(runningTask.basicPCB.baseRunningCpuCore).add(runningTask);

                // 需要将 waitingTask 从 waitingTasks 以及 preemptedAccessResourceTasks(如果有的话)中移出来
                // 感觉其实并不需要 preemptedAccessResourceTasks
                waitingTasks.remove(0);
                preemptedAccessResourceTasks.remove(waitingTask);

                // runningTask 的状态也需要发生变化：blocked
                ChangeTaskState(runningTask, "blocked");

                // cpu 甘特图发生变化 : 运行的任务发生变化(state 根据任务自身的状态来决定)
                ChangeCpuTaskState(i, waitingTask, "");

                // cpu 甘特图中标出 switch task
                cpuEventTimePointRecords.get(i).add(new EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "switch-task", systemClock, -1));

                // 如果 runningTask 是迁移到 immigrate running cpu core 上运行的话，需要修改 helpTask（提供帮助机制）的 isHelp 为 false
                if (runningTask.immigrateRunningCpuCore != -1)
                {
                    com.example.serveside.service.mrsp.entity.ProcedureControlBlock helpTask = getHelpTask(waitingTasks, runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));
                    helpTask.isHelp = false;
                }
            }
        }

        // 3. 访问全局资源的任务（被抢占）进行迁移
        for (Iterator<ProcedureControlBlock> iterator = preemptedAccessResourceTasks.iterator(); iterator.hasNext();)
        {
            ProcedureControlBlock preemptedTask = iterator.next();
            Resource accessResource = totalResources.get(preemptedTask.basicPCB.accessResourceIndex.get(preemptedTask.basicPCB.requestResourceTh));

            // 不可以迁移回去
            // 访问 waitingQueue 里面的 task 看看他们是否可以提供支持
            for (ProcedureControlBlock helpTask : accessResource.waitingQueue)
            {
                // 1. 自旋等待资源的任务可以提供帮助
                // 自旋等待资源任务在 cpu 核上自旋，可以提供帮助
                if (helpTask.basicPCB.dynamicTaskId == runningTaskPerCore.get(helpTask.basicPCB.baseRunningCpuCore).basicPCB.dynamicTaskId)
                {
                    // helpTask 切换任务状态 --> help-direct-spinning(helpTask 帮助 preemptedTask 执行)
                    // preemptedTask 切换任务状态 --> help-access-resource(preempted task 借助 helpTask 进行执行)
                    ChangeTaskState(helpTask, "help-direct-spinning");
                    helpTask.isHelp = true;

                    ChangeTaskState(preemptedTask, "help-access-resource");

                    // helpTask 放回对应 cpu 核上的 waitingTasksPerCore, preemptedTask 放在 helpTask 对应的 cpu 核上执行
                    waitingTasksPerCore.get(helpTask.basicPCB.baseRunningCpuCore).add(helpTask);

                    // preemptedTask 用比 helpTask 高一点的优先级在 helpTask 对应的 cpu 核上运行
                    // 在此之前，需要判断 preemptedTask 是否已经迁移过了
                    if (preemptedTask.immigrateRunningCpuCore != -1)
                        preemptedTask.basicPCB.priorities.pop();

                    // 任务迁移
                    preemptedTask.immigrateRunningCpuCore = helpTask.basicPCB.baseRunningCpuCore;
                    runningTaskPerCore.set(helpTask.basicPCB.baseRunningCpuCore, preemptedTask);
                    preemptedTask.basicPCB.priorities.push(helpTask.basicPCB.priorities.peek() + 1);

                    // immigrate cpu core 上运行的任务发生变化：help-task --> access-resource-task
                    // immigrate cpu core 上的甘特图显示 switch-task
                    ChangeCpuTaskState(preemptedTask.immigrateRunningCpuCore, preemptedTask, "help-access-resource");
                    cpuEventTimePointRecords.get(preemptedTask.immigrateRunningCpuCore).add(new EventTimePoint(preemptedTask.basicPCB.staticTaskId, preemptedTask.basicPCB.dynamicTaskId, "switch-task", systemClock, accessResource.basicResource.id));

                    // 将 preemptedATask 从队列当中删除
                    iterator.remove();
                    waitingTasksPerCore.get(preemptedTask.basicPCB.baseRunningCpuCore).remove(preemptedTask);

                    break;
                }
            }
        }

        // 4. 给 waitingTasksPerCore 上的任务更新当前的状态(blocked、indirect-spinning、help-access-resource)(根据当前 runningTask 的运行状态来决定)
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            com.example.serveside.service.mrsp.entity.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            if (runningTask == null)
                continue;

            // 根据 runningTask 的运行状态来给给出 waitingTasksPerCore 上的任务的状态
            // 任务正在 direct-spinning(该任务并没有发生任何迁移) || 任务迁移到该 cpu 上执行 ，对 waitingTasks 所带来的结果都是一样的
            if (runningTask.basicPCB.spin || runningTask.immigrateRunningCpuCore != -1)
            {
                // runningTask 能够执行，已经说明了它的优先级是最高的
                for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
                {
                    if (!waitingTask.isHelp)
                    {
                        ChangeTaskState(waitingTask, "indirect-spinning");
                    }
                }
            }
            // 剩下的情况就是 runningTask 正在 访问资源/正常执行, 该 waitingTasks 上的任务状态统一为 blocked
            else
            {
                for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
                {
                    if (!waitingTask.isHelp)
                    {
                        ChangeTaskState(waitingTask, "blocked");
                    }
                }
            }
        }

        //5.  处理 Arrival Blocking 的情况
        for (int i = 0; i < TOTAL_CPU_CORE_NUM; ++i)
        {
            com.example.serveside.service.mrsp.entity.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            if (waitingTasksPerCore.get(i).isEmpty())
                continue;

            // Arrival Blocking 发生的条件
            // 1. waitingTask 刚刚到达，即还没有开始执行
            // 2. waitingTask 的基础优先级比 runningTask 的基础优先级更高
            // 3. runningTask 的运行时优先级(priorities.peek()) 比 waitingTask 的运行时优先级(priorities.peek()) 更高
            ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            for (com.example.serveside.service.mrsp.entity.ProcedureControlBlock waitingTask : waitingTasks)
            {
                // 判断 Arrival Blocking 发生的情况
                if (waitingTask.basicPCB.executedTime == 0)
                {
                    // 1. runningTask 本身就是在该 cpu core 上进行运行的
                    if (runningTask.immigrateRunningCpuCore == -1 && waitingTask.basicPCB.basePriority > runningTask.basicPCB.basePriority)
                    {
                        ChangeTaskState(waitingTask, "arrival-blocking");
                    }

                    // 2. runningTask 是迁移到该 cpu 核上运行的
                    if (runningTask.immigrateRunningCpuCore != -1)
                    {
                        // 先找出提供帮助的 task
                        com.example.serveside.service.mrsp.entity.ProcedureControlBlock helpTask = getHelpTask(waitingTasks, runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));

                        // helpTask 的 优先级小于 waitingTask（因此helpTask本质上阻碍了waitingTask的执行）
                        if (helpTask.basicPCB.basePriority < waitingTask.basicPCB.basePriority)
                        {
                            ChangeTaskState(waitingTask, "arrival-blocking");
                        }
                    }
                }
            }
        }
    }

    /*
    * 切换任务状态：保存这一任务的上一个状态，然后开始下一个状态
    * */
    public static void ChangeTaskState(com.example.serveside.service.mrsp.entity.ProcedureControlBlock task, String _state)
    {
        ArrayList<com.example.serveside.response.EventInformation> taskInformations = TaskEventInformationRecords.get(task.basicPCB.dynamicTaskId);

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
                if (releaseTaskInformation.getStaticPid() == staticTask.basicPCB.staticTaskId)
                {
                    baseRunningCPUCore = staticTask.basicPCB.baseRunningCpuCore;
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
            if (waitingTask.basicPCB.spin && accessResourceId == waitingTask.basicPCB.accessResourceIndex.get(waitingTask.basicPCB.requestResourceTh))
            {
                ret = waitingTask;
                break;
            }
        }

        return ret;
    }
}