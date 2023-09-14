package com.example.serveside.service.mrsp.entity;

import com.example.serveside.service.mrsp.generatorTools.SimpleSystemGenerator;

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

    /* 任务进行切换时 那些被抢占的任务(正在访问资源) */
    public static ArrayList<ProcedureControlBlock> preemptedAccessResourceTasks;

    /* The time axis length*/
    public static Integer timeAxisLength;

    public static void main(String[] args)
    {
        SimpleSystemGenerator systemGenerator = new SimpleSystemGenerator(MIN_PERIOD, MAX_PERIOD, TOTAL_CPU_CORE_NUM,
                TOTAL_CPU_CORE_NUM * NUMBER_OF_TASK_IN_A_PARTITION, CS_LENGTH_RANGE.SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS,
                RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

        // Generate the tasks that the system need to execute.
        totalTasks = systemGenerator.generateTasks();
//        totalTasks = systemGenerator.testGenerateTask();

        // Generate the resources that the system has.
        totalResources = systemGenerator.generateResources();
//        totalResources = systemGenerator.testGenerateResources();

        // Generate the resource usage, i.e. task access the resource(time, times)
        allocatedTasks = systemGenerator.generateResourceUsage(totalTasks, totalResources);
//        allocatedTasks = systemGenerator.testGenerateResourceUsage(totalTasks, totalResources);

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

        while (!SystemSuspend())
            ExecuteOnceClock();

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
    *   2. ReleaseTasks() :
    *       -模拟器在当前时刻需不需要释放任务
    *   3. IncreaseElapsedTime():
    *       -正在运行的任务运行时间+1（各种时间都需要加1）
    *   4. ChooseTaskToRun():
    *       -任务切换
    * */
    public static void ExecuteOnceClock()
    {
        ExecuteTasks();

        ReleaseTasks();

        IncreaseElapsedTime();

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

            // 任务到申请资源的时间点了
            if (runningTask.executedTime == runningTask.resourceAccessTime.get(runningTask.requestResourceTh))
            {
                Resource accquireResource = totalResources.get(runningTask.accessResourceIndex.get(runningTask.requestResourceTh));

                // accquireResource 被占据(说明是全局资源)
                if (accquireResource.isOccupied)
                {
                    // 设置 runningTask 的状态：spin, 优先级短暂提升
                    accquireResource.waitingQueue.add(runningTask);
                    runningTask.spin = true;
                    runningTask.priorities.push(accquireResource.ceiling.get(runningTask.baseRunningCpuCore));
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

                }
            }
        }
    }

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
                releaseTask.dynamicTaskId = releaseTaskNum++;
                waitingTasksPerCore.get(totalTasks.get(i).baseRunningCpuCore).add(releaseTask);

                System.out.printf("Release Task:\n\tStatic Task id:%d\n\tDynamic Task id:%d\n\tRelease Time:%d\n\n", releaseTask.staticTaskId, releaseTask.dynamicTaskId, systemClock);

                // 记录一下任务发布的时间点
                TaskEventTimePointsRecords.get(releaseTask.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(releaseTask.staticTaskId, releaseTask.dynamicTaskId, "release", systemClock, -1));

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

        // 3. 处理使用完资源的任务
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
        {
            // 资源使用完，释放资源
            if (runningTask.remainResourceComputationTime == 0)
            {

            }
        }
    }

    public static void ReleaseResource(ProcedureControlBlock runningTask)
    {
        // 要释放的资源
        Resource releaseResource = totalResources.get(runningTask.accessResourceIndex.get(runningTask.requestResourceTh));

        //  runningTask 迁移到其他 cpu 核上运行
        if (runningTask.immigrateRunningCpuCore != -1)
        {
            // 弹出在其迁移 cpu 核上运行的优先级
            runningTask.priorities.pop();

            // 获取help task
            com.example.serveside.service.mrsp.entity.ProcedureControlBlock helpTask = null;
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
                // 结束上一个状态：help-direct-spin
                ChangeTaskState(helpTask, "");

                // 设置 immigrateRunningCpuCore 上执行的任务为空：原因是：ReleaseTask中可能发布了一个优先级更高的任务，选择任务执行交到ChooseTaskToRun
                runningTaskPerCore.set(runningTask.immigrateRunningCpuCore, null);

                // runningTask 返回原cpu核上等待
                runningTask.immigrateRunningCpuCore = -1;
                waitingTasksPerCore.get(runningTask.baseRunningCpuCore).add(runningTask);
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

            // 

        }
    }

    /*
    * 任务进行切换：
    *   1. 高优先级的任务抢占低优先级的任务
    * */
    public static void ChooseTaskToRun()
    {
        // 初始化保存被抢占的任务（正在访问全局资源）
        preemptedAccessResourceTasks.clear();

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

                // 任务状态发生变化: 修改 waitingTask 对应的状态
                ChangeTaskState(waitingTask, "");

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
                else
                    waitingTasks.add(runningTask);
            }
        }

        // 2. 访问全局资源的任务（被抢占）进行迁移
        for (ProcedureControlBlock preemptedTask : preemptedAccessResourceTasks)
        {
            Resource accessResource = totalResources.get(preemptedTask.accessResourceIndex.get(preemptedTask.requestResourceTh));
            // 访问 waitingQueue 里面的 task 看看他们是否可以提供支持
            for (ProcedureControlBlock helpTask : accessResource.waitingQueue)
            {
                // 1. 自旋等待资源的任务可以提供帮助
                // 自旋等待资源任务在 cpu 核上
                if (helpTask.dynamicTaskId == runningTaskPerCore.get(helpTask.baseRunningCpuCore).dynamicTaskId)
                {
                    // 任务迁移
                    preemptedTask.immigrateRunningCpuCore = helpTask.baseRunningCpuCore;

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
                    preemptedTask.immigrateRunningCpuCore = helpTask.baseRunningCpuCore;
                    runningTaskPerCore.set(helpTask.baseRunningCpuCore, preemptedTask);
                    preemptedTask.priorities.push(helpTask.priorities.peek() + 1);

                    break;
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
        // 先终止上一个状态
        if (taskInformations.get(taskInformations.size() - 1).getStartTime() != systemClock)
            taskInformations.get(taskInformations.size() - 1).setEndTime(systemClock);
        else
            taskInformations.remove(taskInformations.size() - 1);

        // 开启一个新的状态
        taskInformations.add(new com.example.serveside.response.EventInformation(task, systemClock, _state));
    }
}