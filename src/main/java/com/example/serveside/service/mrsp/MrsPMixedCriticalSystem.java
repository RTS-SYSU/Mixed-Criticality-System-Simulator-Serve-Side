package com.example.serveside.service.mrsp;

import com.example.serveside.response.EventTimePoint;
import com.example.serveside.service.CommonUse.BasicPCB;
import com.example.serveside.service.CommonUse.BasicResource;
import java.util.*;


/**
 * {@code MrsPixedCriticalSystem}用以模拟任务在Multiprocessor resource sharing Protocol(MrsP)资源共享协议下的执行并记录任务全周期执行情况。
 *
 * <p>
 * {@code MrsPRPMixedCriticalSystem} 主要由以下三个函数构成：
 * <ul>
 *   <li>{@link #MrsPInitialize(ArrayList, ArrayList, int, TreeMap, Boolean, Integer) MSRPInitialize}</li>
 *   <li>{@link #SystemExecute() SystemExecute}</li>
 *   <li>{@link #PackageTotalInformation() PackageTotalInformation}</li>
 * </ul>
 * 其余函数均用来辅助完成这三个函数的功能。
 * 函数{@code MrsPInitialize}初始化系统环境配置，例如任务信息、资源信息、任务发布时间等。
 * 函数{@code SystemExecute}模拟任务在MrsP资源共享协议下的执行。
 * 函数{@code PackageTotalInformation}打包整理任务在MrsP资源共享协议下的全周期执行情况并将相关信息传递给调用函数。
 * @author 陈炜琰
 * @author 李伊鹏
 * @author 许自泓
 * @author 介琛
 * */
public class MrsPMixedCriticalSystem {
    /**
     * 表示系统当前所处的关键级。
     * <p>
     * 该属性是一个整数，初始值为 0，用于表示系统当前所处的关键级。
     * 当 {@code criticality_indicator} 为 0 时，系统处于低关键级。
     * 当 {@code criticality_indicator} 为 1 时，系统处于高关键级。
     */
    public static int criticality_indicator = 0;

    /**
     * 表示系统的处理器个数
     * <p>
     * 该属性用于指定系统的处理器数量。在当前配置中，系统有{@code TOTAL_CPU_CORE_NUM}个处理器核心。
     * */
    public static int TOTAL_CPU_CORE_NUM = 2;

    /**
     * 表示系统的全局时钟。
     * <p>
     * 该属性是一个整数 初始值为0，用于存储系统当前的全局时钟值。
     * */
    public static int systemClock;

    /**
     * 表示系统已发布的任务数量。
     * <p>
     * 该属性是一个整数 初始值为0，用于跟踪系统已经发布的任务数量。当新任务被成功发布时，此计数将递增。
     * */
    public static int releaseTaskNum;

    /**
     * 存储系统中所有任务的信息。
     * <p>
     * 该属性是一个列表，用于存储系统中所有任务的信息。每个任务信息被封装为 {@code ProcedureControlBlock} 对象。
     * 每种任务对应列表中的一项。
     */
    public static ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> totalTasks;

    /**
     * 存储系统中所有资源的信息。
     * <p>
     * 该属性是一个列表，用于存储系统中所有资源的信息。每个资源信息被封装为 {@code Resource} 对象。
     */
    public static ArrayList<com.example.serveside.service.mrsp.Resource> totalResources;

    /**
     * 记录每种任务已经完成执行的次数。
     * <p>
     * 该属性是一个数组，用于记录系统中每种类型任务已经完成执行的次数。索引值与任务的静态标识符 {@code ProcedureControlBlock.basicPCB.staticTaskId} 相对应，对应的值表示该类型任务已经完成的次数。
     */
    public static int[] taskFinishTimes;

    /**
     * 记录每个处理器核心上正在执行的任务。
     * <p>
     * 该属性是一个列表，用于记录每个处理器核心当前正在执行的任务。
     * 每个任务被封装为 {@code ProcedureControlBlock} 对象。
     */
    public static ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> runningTaskPerCore;

    /**
     * 记录每个处理器核心正在等待执行的任务。
     * <p>
     * 该属性是一个列表的列表，用于记录每个处理器核心当前正在等待执行的任务。
     * 每个处理器核心对应一个子列表，子列表中的每个元素是一个被封装为 {@code ProcedureControlBlock} 对象的任务。
     * */
    public static ArrayList<ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock>> waitingTasksPerCore;

    /**
     * 记录任务在执行过程中触发的事件以及事件发生的时间点。
     * <p>
     * 该属性是一个列表的列表，用于记录系统中每个任务在执行过程中触发的事件及其发生的时间点。
     * 每个任务对应一个子列表，子列表中的每个元素是一个被封装为 {@code EventTimePoint} 对象的事件记录。
     * 可能的事件包括任务发布、请求资源、释放资源等。
     */
    public static ArrayList<ArrayList<com.example.serveside.response.EventTimePoint>> TaskEventTimePointsRecords;

    /**
     * 记录任务在执行过程中的状态。
     * <p>
     * 该属性是一个列表的列表，用于记录系统中每个任务在执行过程中的状态信息。
     * 每个任务对应一个子列表，子列表中的每个元素是一个被封装为 {@code EventInformation} 对象的状态记录。
     * 可能的状态包括到达阻塞、正常执行、使用资源等。
     * </p>
     */
    public static ArrayList<ArrayList<com.example.serveside.response.EventInformation>> TaskEventInformationRecords;

    /**
     * 记录每个处理器核心当前正在执行的任务的状态。
     * <p>
     * 该属性是一个列表的列表，用于记录系统中每个处理器核心当前正在执行的任务的状态信息。
     * 每个处理器核心对应一个子列表，子列表中的每个元素是一个被封装为 {@code EventInformation} 对象的状态记录。
     */
    public static ArrayList<ArrayList<com.example.serveside.response.EventInformation>> cpuEventInformationRecords;

    /**
     * 记录每个处理器核心当前正在执行的任务触发的事件以及事件发生的时间点。
     * <p>
     * 该属性是一个列表的列表，用于记录系统中每个处理器核心当前正在执行的任务触发的事件及其发生的时间点。
     * 每个处理器核心对应一个子列表，子列表中的每个元素是一个被封装为 {@code EventTimePoint} 对象的事件记录。
     */
    public static ArrayList<ArrayList<com.example.serveside.response.EventTimePoint>> cpuEventTimePointRecords;

    /* 任务进行切换时 那些被抢占的任务(正在访问资源) */
    public static ArrayList<ProcedureControlBlock> preemptedAccessResourceTasks;

    /**
     * 记录每个已发布的任务的信息。
     * <p>
     * 该属性是一个列表，用于记录系统中每个已发布的任务的信息。
     * 同一种类型的任务可能多次发布。
     * 每个已发布任务信息被封装为 {@code TaskInformation} 对象。
     * </p>
     */
    public static ArrayList<com.example.serveside.response.TaskInformation> releaseTaskInformations;

    /**
     * 表示前端甘特图的长度，即系统模拟运行的时长。
     * <p>
     * 该属性是一个整数，表示前端甘特图的长度，即系统模拟运行的时长。
     */
    public static Integer timeAxisLength;

    /**
     * 系统模拟运行过程中是否开启关键级切换。
     * <p>
     * 该属性是一个布尔值，用于表示系统模拟运行过程中是否开启关键级切换。
     * 当 {@code isStartUpSwitch} 为 true 时，表示系统开启关键级切换。
     * 当 {@code isStartUpSwitch} 为 false 时，表示系统关闭关键级切换。
     */
    public static Boolean isStartUpSwitch;

    /**
     * 系统在开启关键级切换时是否自动完成关键级切换。
     * <p>
     * 该属性是一个布尔值，用于表示系统在开启关键级切换时是否自动完成关键级切换。
     * 该属性在 {@code isStartUpSwitch} 为 true 时有效。
     * 当 {@code isAutomaticallySwitch} 为 true 时，表示系统自动完成关键级切换。
     * 当 {@code isAutomaticallySwitch} 为 false 时，表示系统在 {@code criticalitySwitchTime} 时间点完成关键级切换。
     */
    public static Boolean isAutomaticallySwitch;

    /**
     * 系统完成关键级切换的时间点。
     * <p>
     * 该属性是一个整数，用于表示系统完成关键级切换的时间点。
     * 当 {@code criticalitySwitchTime} 为 -1 时，表示系统没有发生关键级切换。
     * 当 {@code criticalitySwitchTime} 大于 0 时，表示系统在 {@code criticalitySwitchTime} 发生关键级切换。
     */
    public static Integer criticalitySwitchTime;

    /**
     * 表示系统中的任务在运行过程中能否在截止日期之前完成执行，即是否可调度。
     * 初始值为 true。
     * 当 {@code isSchedulable} 为 true 时，表示系统可调度，即所有任务都能够在截止时间之前完成执行。
     * 当 {@code isSchedulable} 为 false 时，表示系统不可调度，即存在任务的执行超过了对应的截止时间。
     */
    public static Boolean isSchedulable;

    /**
     * 存储每个时间点发布的任务的映射关系。
     * <p>
     * 使用 {@code TreeMap} 来确保任务按照时间点的升序存储。
     * 键表示发布时间点，值是包含在该时间点发布的所有任务组成的 {@code ArrayList<Integer>}。
     */
    public static TreeMap<Integer, ArrayList<Integer>> taskReleaseTimes;

    /**
     * 用于遍历 {@code taskReleaseTimes} 哈希表的迭代器。
     */
    public static Iterator<Map.Entry<Integer, ArrayList<Integer>>> iteratorTaskReleaseTimes;

    /**
     * 下一个释放任务的时间点及相应的任务列表。
     */
    public static Map.Entry<Integer, ArrayList<Integer>> itemTaskReleaseTimes = null;

    /**
     * 函数{@code MrsPInitialize()}初始化系统环境（例如任务信息、资源信息、任务发布时间等）、系统模拟运行过程使用到的属性（例如系统时钟、任务已经完成的次数、用于记录状态的列表等）。
     * @param _totalTasks          包含所有任务信息的 {@code ArrayList<BasicPCB>}。
     * @param _totalResources      包含所有资源信息的 {@code ArrayList<BasicResource>}。
     * @param _totalCpuCoreNum     总的处理器核心数量。
     * @param _taskReleaseTimes    任务发布时间的映射关系，使用 {@code TreeMap} 以确保按照时间点的升序存储。
     * @param _isStartUpSwitch     系统启动关键级切换开关。
     * @param _criticalitySwitchTime 系统关键级切换时间点。
     * */
    public static void MrsPInitialize(ArrayList<BasicPCB> _totalTasks, ArrayList<BasicResource> _totalResources, int _totalCpuCoreNum,
                                      TreeMap<Integer, ArrayList<Integer>> _taskReleaseTimes, Boolean _isStartUpSwitch, Integer _criticalitySwitchTime)
    {
        // 1. 初始化系统环境配置信息
        totalTasks = new ArrayList<>(_totalTasks.size());
        for (BasicPCB task : _totalTasks)
            totalTasks.add(new com.example.serveside.service.mrsp.ProcedureControlBlock(task));

        totalResources = new ArrayList<>(_totalResources.size());
        for (BasicResource resource : _totalResources)
            totalResources.add(new com.example.serveside.service.mrsp.Resource(resource));

        TOTAL_CPU_CORE_NUM = _totalCpuCoreNum;

        taskReleaseTimes = _taskReleaseTimes;

        iteratorTaskReleaseTimes = _taskReleaseTimes.entrySet().iterator();
        if (iteratorTaskReleaseTimes.hasNext())
            itemTaskReleaseTimes = iteratorTaskReleaseTimes.next();

        isStartUpSwitch = _isStartUpSwitch;
        isAutomaticallySwitch = (_criticalitySwitchTime == -1);
        criticalitySwitchTime = _criticalitySwitchTime;

        // 2. 初始化系统记录信息
        isSchedulable = true;

        // 时钟周期
        systemClock = 0;

        // 目前已经发布的任务个数
        releaseTaskNum = 0;

        // 初始化系统的关键级
        criticality_indicator = 0;

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

    }


    /**
     * 模拟器根据函数 {@code MrsPInitialize()} 初始化后的系统环境、任务信息、资源信息等来模拟任务的执行，并记录任务的执行情况。
     * 模拟器在函数 {@code SystemSuspend()} 返回为 true 时结束模拟，即所有类型的任务都执行完一次或者某个任务执行超过截止日期。
     * */
    public static void SystemExecute()
    {
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
        for (ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
        {
            // 1. 终止 waiting Task 的状态
            for (ProcedureControlBlock waitingTask : waitingTasks) {
                ArrayList<com.example.serveside.response.EventInformation> taskEventInformation = TaskEventInformationRecords.get(waitingTask.basicPCB.dynamicTaskId);
                if (!taskEventInformation.isEmpty())
                    taskEventInformation.get(taskEventInformation.size() - 1).setEndTime(systemClock);
            }
        }
    }

    /**
     * 函数{@code SystemSuspend()}用以表示模拟器是否继续模拟任务的执行。
     * @return {@code true} 当模拟器在模拟任务的执行过程中出现所有的任务都完成一次或者某个任务执行超过截止时间时，模拟器终止模拟。<strong>注意：</strong>返回值为 {@code true} 表示模拟器终止模拟。<br>
     *         {@code false} 当模拟器在模拟任务的执行过程中没有出现上述两种情况时，模拟器继续模拟。<strong>注意：</strong>返回值为 {@code false} 表示模拟器继续模拟。
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

    /**
     * 函数{@code ExecuteOneClock()}用以执行模拟器在一个时钟周期内需要完成的操作。
     * <p>
     * 该方法 {@code ExecuteOneClock()} 包含以下步骤：
     * <ol>
     *   <li>执行 {@link #ExecuteTasks()} 处理占用处理器核心的任务请求资源。</li>
     *   <li>执行 {@link #IncreaseElapsedTime()} 增加所有已发布但未执行完成的任务的各种类型时间。</li>
     *   <li>执行 {@link #ReleaseTasks()} 发布新的任务。</li>
     *   <li>执行 {@link #ChooseTaskToRun()} 对每个处理器进行重新调度，即把任务调度到对应处理器核心上执行。</li>
     * </ol>
     */
    public static void ExecuteOneClock()
    {
        ExecuteTasks();

        IncreaseElapsedTime();

        ReleaseTasks();

        ChooseTaskToRun();
    }

    /**
     * 函数{@code ExecuteTasks()}用以处理在当前系统时钟下占据处理器核心的任务请求资源的情况。
     * <p>
     * 如果任务请求的资源正在被其他任务持有，那么任务加入到请求资源的FIFO队列末尾中，任务的优先级暂时提升到相应的本地资源上限优先级，执行状态变成直接自旋（direct-spinning）。
     * 除此之外，其余在该处理器上等待执行的任务其状态由阻塞（blocked）变成间接自旋（indirect-spinning）。
     * 注意，在MrsP协议下，自旋等待全局资源或者持有资源（全局或局部）的任务可被抢占。因为请求资源会暂时提升任务的优先级，所以会给其他任务带来 arrival-blocking，在 {@code ChooseTaskToRun}时需要进行特殊处理。
     * </p>
     * <p>
     * 如果任务请求的资源空闲，那么任务成功持有资源并且在持有资源期间优先级就会短暂提升到该资源对应的资源上限优先级（resource ceiling priority），
     * 在释放资源后恢复到原先的优先级。
     * </p>
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
                    ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(runningTask.basicPCB.baseRunningCpuCore);
                    for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
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

    /**
     * 函数{@code ReleaseTasks}用以发布新任务。
     * */
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

    /**
     * 该方法 {@code IncreaseElapsedTime} 用于增加系统中所有已发布但尚未完成的任务的各种时间，处理占用资源的任务、执行完成的任务，以及启动关键级别切换。
     * 方法执行顺序如下：
     *
     * <ol>
     *     <li>
     *         增加未完成执行的任务的经过时间：
     *         <ul>
     *             <li>对于在处理器核心上执行的任务：
     *                 <ul>
     *                     <li>增加任务自发布以来的经过时间。</li>
     *                     <li>对于非自旋等待资源的任务，增加计算或资源使用时间。</li>
     *                 </ul>
     *             </li>
     *             <li>对于等待执行的任务，增加任务自发布以来的经过时间。（在MrsP协议下，等待执行的任务中可能存在正在提供帮助机制的任务，需要增加其计算时间）</li>
     *         </ul>
     *     </li>
     *     <li>
     *         处理释放资源的任务：
     *         <ul>
     *             <li>调用 {@code ReleaseResource} 函数释放资源并将释放的资源分配给FIFO请求队列头部的任务。</li>
     *         </ul>
     *     </li>
     *     <li>
     *         处理完成执行的任务：
     *         <ul>
     *             <li>标记处理器核心为空闲。</li>
     *             <li>增加任务完成次数。</li>
     *         </ul>
     *     </li>
     *     <li>
     *         执行关键级切换：
     *         <ul>
     *             <li>如果某个任务的执行时长超过 WCCT_low，触发关键级切换，将系统关键级提升为高关键级。</li>
     *         </ul>
     *     </li>
     *     <li>
     *         在高关键级模式下终止低关键级任务的执行：
     *         <ul>
     *             <li>取消低优先级任务的执行，增加其完成次数。</li>
     *              <li>如果低优先级任务当前持有资源，在延迟取消执行直到其释放资源。</li>
     *         </ul>
     *     </li>
     * </ol>
     */
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
        for (ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
        {
            for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
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

        // 3. 处理使用完资源的任务(需要加上条件判断：当前有任务在执行+任务有在访问资源)
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
            // 资源使用完，释放资源
            if (runningTask != null && (runningTask.basicPCB.isAccessLocalResource || runningTask.basicPCB.isAccessGlobalResource) && runningTask.basicPCB.remainResourceComputationTime == 0)
                ReleaseResource(runningTask);

        // 4. 处理在 baseRunningCpuCore 上完成计算的的任务
        for (ProcedureControlBlock runningTask : runningTaskPerCore)
            // 需要注意：此时 task 不能够访问资源（在高关键级下面，executedTime 可能会超过 totalNeededTime）
            if (runningTask != null && !runningTask.basicPCB.isAccessLocalResource && !runningTask.basicPCB.isAccessGlobalResource && runningTask.basicPCB.executedTime == runningTask.basicPCB.totalNeededTime)
            {
                runningTaskPerCore.set(runningTask.basicPCB.baseRunningCpuCore, null);
                ++taskFinishTimes[runningTask.basicPCB.staticTaskId];

                // 任务新增：事件时间点（完成任务）
                ChangeTaskState(runningTask, "completion");
                TaskEventTimePointsRecords.get(runningTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));

                // cpu 上的任务完成(状态发生变化)：normal-execution --> completion
                cpuEventTimePointRecords.get(runningTask.basicPCB.baseRunningCpuCore).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "completion", systemClock, -1));
                ChangeCpuTaskState(runningTask.basicPCB.baseRunningCpuCore, null, "");
            }

        // 5. 最后在完成关键级的切换
        if (isStartUpSwitch) {
            // 6. 判断 runningTaskPerCore 上的 task 和 waitingTask 上的 helpTask 的 computeAndSpin 有没有超过 WCCT_low
            if (criticality_indicator != 1 && isAutomaticallySwitch)
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
                    for (ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
                    {
                        for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
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
        }

        // 手动设置某一个时间点发生关键级切换
        if (isStartUpSwitch && !isAutomaticallySwitch && systemClock == criticalitySwitchTime) {
            criticality_indicator = 1;
        }

        // 终止低关键级任务的执行
        if (criticality_indicator == 1)
        {
            // 1. 处理在 cpu 上运行的低关键级任务
            for (int i = 0; i < runningTaskPerCore.size(); ++i)
            {
                com.example.serveside.service.mrsp.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

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

                // 如果任务在资源的等待队列当中，将其移除，同时还需要结束同一处理器上的部分任务的 indirect-spinning 或 arrival-blocking 状态
                if (runningTask.basicPCB.spin)
                {
                    Resource accquireResource = totalResources.get(runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));
                    accquireResource.waitingQueue.remove(runningTask);

                    // 终止该处理器上其他任务的indirect-spinning
                    for (com.example.serveside.service.mrsp.ProcedureControlBlock _waitingTask : waitingTasksPerCore.get(i))
                    {
                        ArrayList<com.example.serveside.response.EventInformation> _taskInformations = TaskEventInformationRecords.get(_waitingTask.basicPCB.dynamicTaskId);
                        if (!_taskInformations.isEmpty() && _taskInformations.get(_taskInformations.size() - 1).getState().equals("indirect-spinning"))
                        {
                            // 由 indirect-spinning-delay 变成 blocked
                            ChangeTaskState(_waitingTask, "blocked");
                        }
                    }

                    // 处理该处理器上其他任务的 arrival-blocking
                    for (com.example.serveside.service.mrsp.ProcedureControlBlock _waitingTask : waitingTasksPerCore.get(i))
                    {
                        ArrayList<com.example.serveside.response.EventInformation> _taskInformations = TaskEventInformationRecords.get(_waitingTask.basicPCB.dynamicTaskId);
                        if (!_taskInformations.isEmpty() && _taskInformations.get(_taskInformations.size() - 1).getState().equals("arrival-blocking"))
                        {
                            // 由 indirect-spinning-delay 变成 blocked
                            ChangeTaskState(_waitingTask, "blocked");
                        }
                    }
                }
            }

            // 2. 处理在 cpu 等待队列上的任务
            for (ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks : waitingTasksPerCore)
            {
                Iterator<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasksIterator = waitingTasks.iterator();
                while (waitingTasksIterator.hasNext())
                {
                    com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask = waitingTasksIterator.next();

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
                        // 不需要处理 indirect-spinning 或者 arrival-blocking，因为它已经被撤下来执行了
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

    /**
     * 该方法 {@code ReleaseResource} 用于释放 {@code runningTask} 所持有的资源，并将释放的资源分配给 FIFO 请求队列头部的任务。
     * <p>
     *     方法执行顺序如下：
     *      <ol>
     *          <li>
     *              结束同一处理器上等待任务到达的阻塞（arrival-blocking）状态。
     *              任务持有局部资源期间，优先级会短暂提升到资源上限优先级（resource ceiling priority）；
     *              任务持有全局资源期间不可被抢占。
     *              上述两种情况均可能导致高优先级任务遭受到达阻塞。
     *          </li>
     *          <li>
     *              {@code runningTask} 释放资源。
     *              修改 {@code runningTask} 实例中的属性，表明该任务不再持有资源。
     *          </li>
     *          <li>
     *              将刚释放的资源分配给 FIFO 请求队列头部的任务。
     *          </li>
     *      </ol>
     * </p>
     *
     * @param runningTask 释放资源的任务
     */
    public static void ReleaseResource(ProcedureControlBlock runningTask)
    {
        // 要释放的资源
        Resource releaseResource = totalResources.get(runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));

        // 获取help task
        com.example.serveside.service.mrsp.ProcedureControlBlock helpTask = null;

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
                if (helpTask.basicPCB.criticality == 1 || criticality_indicator == 0)
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
                    ++taskFinishTimes[helpTask.basicPCB.staticTaskId];

                    ChangeCpuTaskState(runningTask.immigrateRunningCpuCore, null, "");
                    runningTaskPerCore.set(runningTask.immigrateRunningCpuCore, null);

                    // 2. 任务本身发生变化：
                    ChangeTaskState(helpTask, "killed");
                    TaskEventTimePointsRecords.get(helpTask.basicPCB.dynamicTaskId).add(new com.example.serveside.response.EventTimePoint(runningTask.basicPCB.staticTaskId, runningTask.basicPCB.dynamicTaskId, "killed", systemClock, releaseResource.basicResource.id));

                    // 3. 将 helpTask 从 resource 的等待队列中去除
                    releaseResource.waitingQueue.remove(helpTask);

                    // helpTask 在 cpu 上执行, 需要终止其处理器上部分任务的 arrival-blocking 和 indirect-spinning
                    // 终止该处理器上其他任务的indirect-spinning
                    for (com.example.serveside.service.mrsp.ProcedureControlBlock _waitingTask : waitingTasksPerCore.get(helpTask.basicPCB.baseRunningCpuCore))
                    {
                        ArrayList<com.example.serveside.response.EventInformation> _taskInformations = TaskEventInformationRecords.get(_waitingTask.basicPCB.dynamicTaskId);
                        if (!_taskInformations.isEmpty() && _taskInformations.get(_taskInformations.size() - 1).getState().equals("indirect-spinning"))
                        {
                            // 由 indirect-spinning-delay 变成 blocked
                            ChangeTaskState(_waitingTask, "blocked");
                        }
                    }

                    // 处理该处理器上其他任务的 arrival-blocking
                    for (com.example.serveside.service.mrsp.ProcedureControlBlock _waitingTask : waitingTasksPerCore.get(helpTask.basicPCB.baseRunningCpuCore))
                    {
                        ArrayList<com.example.serveside.response.EventInformation> _taskInformations = TaskEventInformationRecords.get(_waitingTask.basicPCB.dynamicTaskId);
                        if (!_taskInformations.isEmpty() && _taskInformations.get(_taskInformations.size() - 1).getState().equals("arrival-blocking"))
                        {
                            // 由 indirect-spinning-delay 变成 blocked
                            ChangeTaskState(_waitingTask, "blocked");
                        }
                    }
                }
            }
        }

        // runningTask 在释放资源之前先结束该 baseRunningCPUCore 上某些的 arrival-blocking
        if (runningTask.immigrateRunningCpuCore == -1) {
            ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(runningTask.basicPCB.baseRunningCpuCore);
            for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
            {
                // 判断 Arrival Blocking 发生的情况
                if (waitingTask.basicPCB.executedTime == 0)
                {
                    // 1. runningTask 访问 local-resource 造成优先级反转
                    if (runningTask.basicPCB.basePriority < waitingTask.basicPCB.basePriority
                            && runningTask.basicPCB.isAccessLocalResource
                            && runningTask.basicPCB.priorities.peek() > waitingTask.basicPCB.priorities.peek()) {
                        ChangeTaskState(waitingTask, "blocked");
                    }
                    // 2. runningTask 访问 global-resource && runningTask 的优先级更低
                    else if (runningTask.basicPCB.basePriority < waitingTask.basicPCB.basePriority
                            && runningTask.basicPCB.isAccessGlobalResource) {
                        ChangeTaskState(waitingTask, "blocked");
                    }
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
            com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask = releaseResource.waitingQueue.get(0);
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
                for (com.example.serveside.service.mrsp.ProcedureControlBlock _waitingTask : waitingTasksPerCore.get(waitingTask.basicPCB.baseRunningCpuCore))
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

            // 如果是迁移到其他 cpu 上去执行，那么可以确定是 global-resource 并且 immigrateTask 上的任务还将继续造成 arrival-blocking
        }else
        {
            // 在原本的核心上执行，简单切换一下状态就行
            ChangeTaskState(runningTask, "");
            ChangeCpuTaskState(runningTask.basicPCB.baseRunningCpuCore, runningTask, "");
        }


    }

    /**
     * 该方法 {@code ChooseTaskToRun} 将任务分配到相应的处理器核心上执行。
     * <p>
     *     任务分配到处理器上执行的逻辑如下：
     *     <ol>
     *         <li>当处理器核心处于空闲状态时，它将开始执行与之对应的等待执行任务列表中优先级最高的任务。</li>
     *         <li>
     *             当处理器核心正在执行任务（任务单纯执行或者任务访问资源）并且等待执行的任务列表中存在优先级更高的任务时，更高优先级的任务抢占正在执行的任务，占据处理器核心，被抢占的任务重新放入等待执行的列表中。
     *             如果任务此时正在访问全局资源，可以尝试利用帮助机制（helping mechanism）迁移到其他处理器上执行。
     *         </li>
     *     </ol>
     * </p>
     * <p>
     *     除了将任务分配到处理器上执行，我们还需要考虑一个新发布的任务可能遭受到达阻塞（arrival-blocking）。
     * </p>
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
            waitingTasks.sort((task1, task2) -> -Integer.compare(task1.basicPCB.priorities.peek(), task2.basicPCB.priorities.peek()));

            ProcedureControlBlock waitingTask = waitingTasks.get(0);
            int waitingTaskIndex = 0;
            for (int j = 1; j < waitingTasks.size(); ++j) {
                if (waitingTasks.get(j).basicPCB.priorities.peek() == waitingTask.basicPCB.priorities.peek()) {
                    if (waitingTasks.get(j).basicPCB.elapsedTime > waitingTask.basicPCB.elapsedTime) {
                        waitingTask = waitingTasks.get(j);
                        waitingTaskIndex = j;
                    }
                }
            }

            // 1. CPU 空闲
            if (runningTask == null)
            {
                // waiting Task 放在对应的 CPU 核上运行
                runningTaskPerCore.set(i, waitingTask);

                // 任务状态发生变化: 恢复 waitingTask 以前的状态 / waitingTask 开始执行
                ChangeTaskState(waitingTask, "");

                // 需要将 waitingTask 从 waitingTasks 中移出来
                waitingTasks.remove(waitingTaskIndex);

                // cpu 甘特图发生变化: 运行的任务发生变化(state 根据任务自身的状态来决定)
                ChangeCpuTaskState(i, waitingTask, "");
                // cpu 甘特图加入 switch-task 标号
                cpuEventTimePointRecords.get(i).add(new EventTimePoint(waitingTask.basicPCB.staticTaskId, waitingTask.basicPCB.dynamicTaskId, "switch-task", systemClock, -1));

                // 将 waitingTask 从 preemptedAccessResourceTasks 中移除出来（如果有的话）
                preemptedAccessResourceTasks.remove(waitingTask);

                continue;
            }

            // 2. 抢占：waitingTask的优先级 > runningTask 的优先级
                // i. normal-execution
                // ii. access local resource || access global resource
            if (waitingTask.basicPCB.priorities.peek() > runningTask.basicPCB.priorities.peek())
            {
                // waitingTask 状态发生变化, waitingTask 放在 CPU 上运行
                ChangeTaskState(waitingTask, "");
                runningTaskPerCore.set(i, waitingTask);

                // 判断 runningTask 是否满足迁移条件, 即 runningTask 有没有访问 global resource.
                if (runningTask.basicPCB.isAccessGlobalResource)
                    preemptedAccessResourceTasks.add(runningTask);

                // runningTask 放入原生的 waitingTasks 中
                waitingTasksPerCore.get(runningTask.basicPCB.baseRunningCpuCore).add(runningTask);

                // 需要将 waitingTask 从 waitingTasks 以及 preemptedAccessResourceTasks(如果有的话)中移出来
                // 感觉其实并不需要 preemptedAccessResourceTasks
                waitingTasks.remove(waitingTaskIndex);
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
                    com.example.serveside.service.mrsp.ProcedureControlBlock helpTask = getHelpTask(waitingTasks, runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));
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
                    preemptedTask.basicPCB.priorities.push(helpTask.basicPCB.priorities.peek());

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
            ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            com.example.serveside.service.mrsp.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);

            if (runningTask == null)
                continue;

            // 根据 runningTask 的运行状态来给给出 waitingTasksPerCore 上的任务的状态
            // 任务正在 direct-spinning(该任务并没有发生任何迁移) || 任务迁移到该 cpu 上执行 ，对 waitingTasks 所带来的结果都是一样的
            if (runningTask.basicPCB.spin || runningTask.immigrateRunningCpuCore != -1)
            {
                // runningTask 能够执行，已经说明了它的优先级是最高的
                for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
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
                for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
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
            com.example.serveside.service.mrsp.ProcedureControlBlock runningTask = runningTaskPerCore.get(i);
            if (waitingTasksPerCore.get(i).isEmpty())
                continue;

            // Arrival Blocking 发生的条件
            // 1. waitingTask 刚刚到达，即还没有开始执行
            // 2. waitingTask 的基础优先级比 runningTask 的基础优先级更高
            // 3. runningTask 的运行时优先级(priorities.peek()) 比 waitingTask 的运行时优先级(priorities.peek()) 更高
            ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks = waitingTasksPerCore.get(i);
            for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
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
                        com.example.serveside.service.mrsp.ProcedureControlBlock helpTask = getHelpTask(waitingTasks, runningTask.basicPCB.accessResourceIndex.get(runningTask.basicPCB.requestResourceTh));

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

    /**
     * 该方法 {@code ChangeTaskState} 结束任务 {@code task} 当前所处的状态，并开启新的状态 {@code _state}。
     * @param task 进行任务状态切换的任务。
     * @param _state 任务{@code task} 的新状态。
     * */
    public static void ChangeTaskState(com.example.serveside.service.mrsp.ProcedureControlBlock task, String _state)
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

    /**
     * 该方法 {@code ChangeTaskState} 结束处理器 {@code runningCpuCore} 上执行的任务 {@code task} 当前所处的状态，并开启新的状态 {@code _state}。
     * @param runningCpuCore 进行任务状态切换的处理器核心。
     * @param task 进行任务状态切换的任务。
     * @param _state 任务{@code task} 的新状态。
     * */
    public static void ChangeCpuTaskState(int runningCpuCore, com.example.serveside.service.mrsp.ProcedureControlBlock task, String _state)
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

    /**
     * 该方法 {@code PackageTotalInformation} 通过整合 {@code cpuEventInformationRecords}、{@code cpuEventTimePointRecords}、
     * {@code TaskEventInformationRecords}、{@code TaskEventTimePointsRecords} 记录的信息，将其打包成 {@code TotalInformation} 对象，以便传递给前端。
     */
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

        return new com.example.serveside.response.ToTalInformation(cpuGanttInformations, taskGanttInformations, criticalitySwitchTime);
    }

    /**
     * 该方法 {@code CalculateTimeAxisLength} 根据模拟运行过程中收集到的信息计算出前端甘特图的长度。
     * */
    public static void CalculateTimeAxisLength()
    {
        timeAxisLength = 0;

        if (!TaskEventInformationRecords.isEmpty()) {
            for (ArrayList<com.example.serveside.response.EventInformation> TaskEventInformations : TaskEventInformationRecords)
            {
                if (!TaskEventInformations.isEmpty())
                    timeAxisLength = Math.max(timeAxisLength, TaskEventInformations.get(TaskEventInformations.size() - 1).getEndTime());
            }
        }
    }

    /**
     * 从资源的FIFO请求队列中查找提供帮助机制的任务。
     *
     * @param waitingTasks 提供帮助机制的任务所在的处理器核心对应的等待执行任务列表。
     * @param accessResourceId 访问资源的标识。
     * @return 提供帮助机制的任务，类型为 com.example.serveside.service.mrsp.ProcedureControlBlock。
     */
    public static com.example.serveside.service.mrsp.ProcedureControlBlock getHelpTask(ArrayList<com.example.serveside.service.mrsp.ProcedureControlBlock> waitingTasks, int accessResourceId)
    {
        com.example.serveside.service.mrsp.ProcedureControlBlock ret = null;

        for (com.example.serveside.service.mrsp.ProcedureControlBlock waitingTask : waitingTasks)
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