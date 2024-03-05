package com.example.serveside.service.msrp;

import com.example.serveside.service.CommonUse.BasicPCB;
import com.example.serveside.service.CommonUse.BasicResource;
import com.example.serveside.service.CommonUse.BlockingRecord;
import com.example.serveside.service.CommonUse.generatorTools.SimpleSystemGenerator;

import java.util.*;

/**
 * {@code MSRPWorstCase} 用于计算系统任务的发布时间，使得指定任务在MrsP资源共享协议下达到最坏执行情况。
 * <p>
 *     {@code MSRPWorstCase} 使用贪心算法来最大化指定任务的调度和阻塞时延。一个任务在运行过程中受到的干扰有三种类型：到达阻塞（arrival-blocking）、高优先级任务的干扰以及访问资源时受到的阻塞。
 *     {@code MSRPWorstCase} 通过贪心算法分别最大化三种阻塞使得任务的响应时间最大，即任务达到最坏执行情况。
 * </p>
 * <p>
 *     <strong>注意:  为了使下面注释更具清晰度和详细性，记被查看最差运行情况的任务为sufferTask。</strong>
 * </p>
 */

public class MSRPWorstCase {
    /**
     * 系统任务信息。
     */
    public ArrayList<BasicPCB> totalTasks;

    /**
     * 系统资源信息。
     */
    public ArrayList<BasicResource> totalResources;

    /**
     * 将任务按照处理器核心进行分组。
     */
    public ArrayList<ArrayList<BasicPCB>> allocatedTaskSets;

    /**
     * 存储资源与相应访问任务的关联关系。
     * <p>
     *     这个数据结构是一个三维 ArrayList，其中每个维度的含义如下：
     *     <ol>
     *         <li>第一维度：资源索引，表示系统中的不同资源。</li>
     *         <li>第二维度：处理器核心。</li>
     *         <li>第三维度：在处理器核心上访问资源的任务。</li>
     *     </ol>
     * </p>
     */
    public ArrayList<ArrayList<ArrayList<BasicPCB>>> accessResourceTasks;

    /**
     * 记录系统中每个任务的每次发布对sufferTask所在处理器核心执行的任务的阻塞时长。
     * <p>
     *     使用任务的静态标识符作为键。<br>
     *     值是一个整数 ArrayList，表示任务的每次发布对sufferTask所在处理器核心上执行的任务的阻塞时长。
     * </p>
     */
    public LinkedHashMap<Integer, ArrayList<Integer>> recordBlockingTimesHashMap;

    /**
     * 记录系统中每个任务的发布时间。
     * <p>
     *     使用任务的静态标识符作为键。<br>
     *     值是一个整数 ArrayList，表示任务的每次的发布时间。
     * </p>
     */
    public LinkedHashMap<Integer, ArrayList<Integer>> recordTaskReleaseTimesHashMap;

    /**
     * 记录任务（查看其最坏运行情况）从发布开始所受到的阻塞与干扰。
     */
    public ArrayList<BlockingRecord> BlockingRecordArray;

    /**
     * 处理器核心个数。
     */
    public Integer total_partitions;

    /**
     * 在 MSRP 资源共享协议下，{@code MSRPGeneratedWorstCaseReleaseTime} 使用贪心算法为系统中的任务生成发布时间，使得 sufferTask 达到最差执行情况。
     * <p>
     *     {@code MSRPGeneratedWorstCaseReleaseTime} 按以下顺序计算系统中任务的发布时间，使 sufferTask 受到最大化的调度和阻塞时延。
     *     <ol>
     *         <li>考虑 sufferTask 在发布时受到到达阻塞，设置提供到达阻塞的低优先级任务等相关任务的发布时间。</li>
     *         <li>考虑高优先级任务的干扰以及高优先级任务访问资源时受到的阻塞时延，设置高优先级任务的发布时间等相关任务的发布时间。</li>
     *         <li>考虑高 sufferTask 访问全局资源时受到的阻塞时延，设置其他处理器核心上访问相同资源的任务的发布时间。</li>
     *     </ol>
     * </p>
     * @param totalTasks 系统任务的信息。
     * @param totalResources 系统资源的信息。
     * @param sufferTaskId 被查看最差执行情况的任务的静态标识符。
     * @param total_partitions 处理器核心数。
     */
    public TreeMap<Integer, ArrayList<Integer>> MSRPGeneratedWorstCaseReleaseTime(ArrayList<BasicPCB> totalTasks, ArrayList<BasicResource> totalResources, Integer sufferTaskId, Integer total_partitions) {
        // 用来记录每一个时间点下有哪些任务会被释放
        TreeMap<Integer, ArrayList<Integer>> recordPoints = new TreeMap<>();
        BasicPCB sufferTask = totalTasks.get(sufferTaskId);
        // 用来记录每一个任务每次发布时所对应的时间
        this.recordTaskReleaseTimesHashMap = new LinkedHashMap<>(totalTasks.size());
        // 用来记录每一个任务每次发布时理论上对 sufferTask 造成多少的 Blocking-Time
        this.recordBlockingTimesHashMap = new LinkedHashMap<>(totalTasks.size());
        // 记录在制造 sufferTask 最坏运行情况的过程中，每一种阻塞的具体情况
        this.BlockingRecordArray = new ArrayList<>();

        this.totalTasks = totalTasks;
        this.totalResources = totalResources;
        this.total_partitions = total_partitions;

        // allocatedTaskSets : 每一个 cpu 上有哪些任务在执行
        this.allocatedTaskSets = new ArrayList<>();
        for (int i = 0; i < total_partitions; ++i) {
            allocatedTaskSets.add(new ArrayList<>());
        }
        for (BasicPCB task : totalTasks) {
            allocatedTaskSets.get(task.baseRunningCpuCore).add(task);
            recordTaskReleaseTimesHashMap.put(task.staticTaskId, new ArrayList<>());
            recordBlockingTimesHashMap.put(task.staticTaskId, new ArrayList<>());
        }

        // accessResourceTasks : 每一个 cpu 上有哪些资源会访问该任务
        this.accessResourceTasks = new ArrayList<>();
        for (int i = 0; i < totalResources.size(); ++i) {
            accessResourceTasks.add(new ArrayList<>());
            for (int j = 0; j < total_partitions; ++j) {
                accessResourceTasks.get(i).add(new ArrayList<>());
            }
        }
        for (BasicPCB task : totalTasks) {
            for (int accessResourceId : task.accessResourceIndex) {
                if (!accessResourceTasks.get(accessResourceId).get(task.baseRunningCpuCore).contains(task))
                    accessResourceTasks.get(accessResourceId).get(task.baseRunningCpuCore).add(task);
            }
        }

        // 1. 考虑 arrival-blocking
        int baseReleaseTime = MSRPConsiderArrivalBlocking(sufferTaskId);

        // 2. 考虑 high-priority-task-interference
        baseReleaseTime = MSRPConsiderHighPriorityTasksInterference(baseReleaseTime, sufferTaskId);

        // 3. 考虑 sufferTask 开始运行时访问 global-resource 时遭受的 spin-delay
        baseReleaseTime = MSRPConsiderSufferTask(baseReleaseTime, sufferTaskId);

        // 没有影响的任务统一放到最后再发布
        for (Map.Entry<Integer, ArrayList<Integer>> entry : recordTaskReleaseTimesHashMap.entrySet()) {
            ArrayList<Integer> taskReleaseTimesRecord = entry.getValue();
            if (taskReleaseTimesRecord.isEmpty()) {
                taskReleaseTimesRecord.add(baseReleaseTime);
            }
        }

        AdjustTotalReleaseTime();

        // 现在的话依次遍历每一个 recordTaskReleaseTimesHashMap 以此来记录在哪些时间段发布哪些任务
        for (Map.Entry<Integer, ArrayList<Integer>> entry : recordTaskReleaseTimesHashMap.entrySet()) {
            ArrayList<Integer> taskReleaseTimes = entry.getValue();
            for (Integer taskReleaseTime : taskReleaseTimes) {
                if (recordPoints.containsKey(taskReleaseTime)) {
                    recordPoints.get(taskReleaseTime).add(entry.getKey());
                } else {
                    ArrayList<Integer> addList = new ArrayList<>();
                    addList.add(entry.getKey());
                    recordPoints.put(taskReleaseTime, addList);
                }
            }
        }

        // 需要按 key 值大小对 recordPoints 进行排序 :
        return recordPoints;
    }

    /**
     * 在 MSRP 协议下，最大化 sufferTask 访问全局资源时最大化自旋等待时间。
     * @param baseReleaseTime sufferTask 开始使用 cpu 的时间点。
     * @param sufferTaskId 被查看最差运行情况的任务的静态标识符。
     */
    public int MSRPConsiderSufferTask(int baseReleaseTime, int sufferTaskId) {
        // 这部分的想法跟 MrsPConsiderHighPriorityTasksInterference 中高优先级任务访问 global-resource 一样，尽量在访问 global-resource 时产生更多的 spin-delay=
        BasicPCB sufferTask = totalTasks.get(sufferTaskId);

        int sufferTaskStartTime = baseReleaseTime;
        int sufferTaskEndTime = baseReleaseTime;
        boolean sufferTaskIsFirstBlockingRecord = true;

        if (!sufferTask.accessResourceIndex.isEmpty()) {
            for (int i = 0; i < sufferTask.accessResourceIndex.size(); ++i) {
                // 上一次访问资源到这一次访问资源之间的空隙
                if (i > 0) {
                    sufferTaskEndTime = sufferTaskStartTime + sufferTask.resourceAccessTime.get(i) - sufferTask.resourceAccessTime.get(i-1) - totalResources.get(sufferTask.accessResourceIndex.get(i-1)).c_low;
                }else {
                    sufferTaskEndTime = sufferTaskStartTime + sufferTask.resourceAccessTime.get(i);
                }

                BlockingRecordArray.add(new BlockingRecord(-1, -1, sufferTask, recordTaskReleaseTimesHashMap.get(sufferTask.staticTaskId).size() - 1,
                        null, null, null,
                        BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, sufferTaskStartTime, sufferTaskEndTime, sufferTaskIsFirstBlockingRecord));
                sufferTaskIsFirstBlockingRecord = false;

                // 看看能不能进行合并
                mergeLastTwoBlockingRecord();
                sufferTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;

                // 这个时候看看能不能再次发布高优先级任务
                moreHigherPriorityTaskReleaseAgain(sufferTaskStartTime, sufferTask, sufferTask);
                sufferTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;

                BasicResource accessResource = totalResources.get(sufferTask.accessResourceIndex.get(i));
                if (accessResource.isGlobal) {
                    BlockingRecord lastBlockingRecord = MSRPConsiderAccessGlobalResourceSpinDelay(baseReleaseTime, sufferTask.staticTaskId, i, sufferTaskId, BlockingRecord.BlockingTypes.highPriorityTaskAccessGlobalResource,
                            sufferTaskIsFirstBlockingRecord);

                    sufferTaskStartTime = lastBlockingRecord.endTime;
                } else {
                    // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
                    BlockingRecordArray.add(new BlockingRecord(-1, -1, sufferTask, recordTaskReleaseTimesHashMap.get(sufferTask.staticTaskId).size() - 1,
                            null, null, null,
                            BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, sufferTaskStartTime, sufferTaskStartTime + accessResource.c_low, sufferTaskIsFirstBlockingRecord));

                    // 看看能不能进行合并
                    mergeLastTwoBlockingRecord();
                    sufferTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
                }

                // 这个时候看看能不能再次发布高优先级任务
                moreHigherPriorityTaskReleaseAgain(sufferTaskStartTime, sufferTask, sufferTask);
                sufferTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
            }

            // 到结束还有一段 block
            // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
            int leftTime = sufferTask.totalNeededTime - sufferTask.resourceAccessTime.get(sufferTask.resourceAccessTime.size()-1) - totalResources.get(sufferTask.accessResourceIndex.get(sufferTask.accessResourceIndex.size()-1)).c_low;
            BlockingRecordArray.add(new BlockingRecord(-1, -1, sufferTask,
                    recordTaskReleaseTimesHashMap.get(sufferTask.staticTaskId).size()-1,
                    null, null, null,
                    BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, sufferTaskStartTime, sufferTaskStartTime + leftTime , sufferTaskIsFirstBlockingRecord));
            mergeLastTwoBlockingRecord();
            sufferTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
        }else {
            sufferTaskStartTime += sufferTask.totalNeededTime;
        }
        return sufferTaskStartTime;
    }

    /**
     * 在 MSRP 协议下考虑高优先级任务给 sufferTask 带来的干扰。
     * <p>
     *     调整高优先级任务的发布时间使 sufferTask 受到所有高优先级任务的干扰（即所有高优先级任务在 sufferTask 发布之后，完成执行之前释放）。同时，最大化高优先级任务在访问全局资源时受到的自旋等待时间。
     * </p>
     */
    public int MSRPConsiderHighPriorityTasksInterference(int baseReleaseTime, int sufferTaskId) {
        // 1. 首先看看前面 sufferTask 有没有被释放过，如果没有被释放过，那么我们就需要在 baseReleaseTime 的时候释放
        if (recordTaskReleaseTimesHashMap.get(sufferTaskId).isEmpty()) {
            recordTaskReleaseTimesHashMap.get(sufferTaskId).add(baseReleaseTime);
        }

        // 如果 sufferTask 所在 cpu 上有高优先级的任务可以阻塞，那么 releaseTime 就会由上面的 if 语句进行设置
        // 先对 allocatedTaskSets 按优先级从高到低进行排序
        BasicPCB sufferTask = totalTasks.get(sufferTaskId);
        allocatedTaskSets.get(sufferTask.baseRunningCpuCore).sort(Comparator.comparingInt(t -> -t.basePriority));

        // 2.  依次遍历每一个更高优先级的任务
        for (int j = 0; j < allocatedTaskSets.get(sufferTask.baseRunningCpuCore).size(); ++j) {
            BasicPCB highPriorityTask = allocatedTaskSets.get(sufferTask.baseRunningCpuCore).get(j);

            // 更高优先级的任务
            if (highPriorityTask.basePriority > sufferTask.basePriority) {
                // 第一步 : 先设置 task 的 releaseTime
                recordTaskReleaseTimesHashMap.get(highPriorityTask.staticTaskId).add(baseReleaseTime);

                int highPriorityTaskStartTime = baseReleaseTime;
                int highPriorityTaskEndTime = baseReleaseTime;
                // isFirstBlockingRecord
                boolean highPriorityTaskIsFirstBlockingRecord = true;

                // 第二步 : 依次访问 task 的 global-resource
                if (!highPriorityTask.accessResourceIndex.isEmpty()) {
                    for (int i = 0; i < highPriorityTask.accessResourceIndex.size(); ++i) {
                        if (i > 0) {
                            // 上一次访问资源到这一次访问资源之间的纯使用 cpu 的时间
                            highPriorityTaskEndTime = highPriorityTaskStartTime + highPriorityTask.resourceAccessTime.get(i) - highPriorityTask.resourceAccessTime.get(i-1) - totalResources.get(highPriorityTask.accessResourceIndex.get(i-1)).c_low;
                        }else {
                            // 第一次访问资源之前纯使用 cpu 的时间
                            highPriorityTaskEndTime = highPriorityTaskStartTime + highPriorityTask.resourceAccessTime.get(i);
                        }

                        // 添加 high-priority-task 的 normal-execution
                        BlockingRecordArray.add(new BlockingRecord(-1, -1, highPriorityTask,
                                recordTaskReleaseTimesHashMap.get(highPriorityTask.staticTaskId).size() - 1,
                                null, null, null,
                                BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, highPriorityTaskStartTime, highPriorityTaskEndTime, highPriorityTaskIsFirstBlockingRecord));
                        highPriorityTaskIsFirstBlockingRecord = false;

                        // 看看能不能对 block 进行合并
                        mergeLastTwoBlockingRecord();
                        highPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;

                        // 在访问资源前看看能不能再次发布高优先级任务
                        moreHigherPriorityTaskReleaseAgain(highPriorityTaskStartTime, highPriorityTask, sufferTask);
                        highPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;

                        // 访问资源
                        BasicResource accessResource = totalResources.get(highPriorityTask.accessResourceIndex.get(i));
                        if (accessResource.isGlobal) {
                            BlockingRecord lastBlockingRecord = MSRPConsiderAccessGlobalResourceSpinDelay(baseReleaseTime, highPriorityTask.staticTaskId, i, sufferTaskId, BlockingRecord.BlockingTypes.highPriorityTaskAccessGlobalResource, highPriorityTaskIsFirstBlockingRecord);

                            highPriorityTaskStartTime = lastBlockingRecord.endTime;
                        } else {
                            // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
                            BlockingRecordArray.add(new BlockingRecord(-1, -1, highPriorityTask,
                                    recordTaskReleaseTimesHashMap.get(highPriorityTask.staticTaskId).size() - 1,
                                    null, null, null,
                                    BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, highPriorityTaskStartTime, highPriorityTaskStartTime + accessResource.c_low, highPriorityTaskIsFirstBlockingRecord));

                            // 看看能不能进行合并
                            mergeLastTwoBlockingRecord();
                            highPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
                        }

                        // 访问完资源之后再看看能不能再次发布高优先级任务
                        moreHigherPriorityTaskReleaseAgain(highPriorityTaskStartTime, highPriorityTask, sufferTask);
                        highPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;

                    }

                    // 到结束还有一段 block
                    // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
                    int leftTime = highPriorityTask.totalNeededTime - highPriorityTask.resourceAccessTime.get(highPriorityTask.resourceAccessTime.size()-1) - totalResources.get(highPriorityTask.accessResourceIndex.get(highPriorityTask.accessResourceIndex.size()-1)).c_low;
                    BlockingRecordArray.add(new BlockingRecord(-1, -1, highPriorityTask,
                            recordTaskReleaseTimesHashMap.get(highPriorityTask.staticTaskId).size()-1,
                            null, null, null,
                            BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, highPriorityTaskStartTime, highPriorityTaskStartTime + leftTime , highPriorityTaskIsFirstBlockingRecord));
                    mergeLastTwoBlockingRecord();
                    highPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
                }
                else {
                    // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
                    BlockingRecordArray.add(new BlockingRecord(-1, -1, highPriorityTask,
                            recordTaskReleaseTimesHashMap.get(highPriorityTask.staticTaskId).size()-1,
                            null, null, null,
                            BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, highPriorityTaskStartTime, highPriorityTaskStartTime + highPriorityTask.totalNeededTime, highPriorityTaskIsFirstBlockingRecord));
                    highPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
                }

                // 这个时候看看能不能再次发布高优先级任务
                moreHigherPriorityTaskReleaseAgain(highPriorityTaskStartTime, highPriorityTask, sufferTask);
                baseReleaseTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
            }
        }

        // 这里返回的 baseReleaseTime 是理论上 sufferTask 开始占据 CPU 的时间
        return baseReleaseTime;
    }

    /**
     * 考虑高优先级任务多次释放。
     */
    public void moreHigherPriorityTaskReleaseAgain(int highPriorityTaskStartTime, BasicPCB lowPriorityTask, BasicPCB sufferTask) {
        int moreHigherPriorityTaskStartTime = highPriorityTaskStartTime;
        int moreHigherPriorityTaskEndTime = highPriorityTaskStartTime;

        // 看看有没有高优先级的任务可以再次进行发布
        for (int k = 0; k < allocatedTaskSets.get(sufferTask.baseRunningCpuCore).size(); ++k) {
            boolean moreHigherPriorityTaskIsFirstBlockingRecord = true;
            BasicPCB moreHigherPriorityTask = allocatedTaskSets.get(sufferTask.baseRunningCpuCore).get(k);
            ArrayList<Integer> moreHigherPriorityTaskReleaseTimeArray = recordTaskReleaseTimesHashMap.get(moreHigherPriorityTask.staticTaskId);

            // 优先级更高并且达到发布周期
            if (moreHigherPriorityTask.basePriority > lowPriorityTask.basePriority && moreHigherPriorityTaskReleaseTimeArray.get(moreHigherPriorityTaskReleaseTimeArray.size() - 1) + moreHigherPriorityTask.period <= moreHigherPriorityTaskStartTime) {
                // 第一步：设置 moreHigherPriorityTask 的释放时间
                recordTaskReleaseTimesHashMap.get(moreHigherPriorityTask.staticTaskId).add(moreHigherPriorityTaskStartTime);

                if (!moreHigherPriorityTask.accessResourceIndex.isEmpty()) {
                    // 第二步：依次访问 more-higher-priority-task 的 global-resource
                    for (int l = 0; l < moreHigherPriorityTask.accessResourceIndex.size(); ++l) {
                        // 上一次访问资源到这一次访问资源之间的空隙
                        if (l > 0) {
                            moreHigherPriorityTaskEndTime = moreHigherPriorityTaskStartTime + moreHigherPriorityTask.resourceAccessTime.get(l) - moreHigherPriorityTask.resourceAccessTime.get(l-1) - totalResources.get(moreHigherPriorityTask.accessResourceIndex.get(l-1)).c_low;
                        }else {
                            moreHigherPriorityTaskEndTime = moreHigherPriorityTaskStartTime + moreHigherPriorityTask.resourceAccessTime.get(l);
                        }

                        BlockingRecordArray.add(new BlockingRecord(-1, -1, moreHigherPriorityTask,
                                recordTaskReleaseTimesHashMap.get(moreHigherPriorityTask.staticTaskId).size()-1,
                                null, null, null,
                                BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, moreHigherPriorityTaskStartTime, moreHigherPriorityTaskEndTime, moreHigherPriorityTaskIsFirstBlockingRecord));
                        moreHigherPriorityTaskIsFirstBlockingRecord = false;

                        // 看看能不能跟前面的 blockingRecord 进行合并
                        mergeLastTwoBlockingRecord();
                        moreHigherPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;


                        BasicResource moreHigherTaskAccessResource = totalResources.get(moreHigherPriorityTask.accessResourceIndex.get(l));
                        if (moreHigherTaskAccessResource.isGlobal) {
                            BlockingRecord lastBlockingRecord = MSRPConsiderAccessGlobalResourceSpinDelay(moreHigherPriorityTaskStartTime, moreHigherPriorityTask.staticTaskId, l, sufferTask.staticTaskId,
                                    BlockingRecord.BlockingTypes.highPriorityTaskAccessGlobalResource, moreHigherPriorityTaskIsFirstBlockingRecord);

                            moreHigherPriorityTaskStartTime = lastBlockingRecord.endTime;
                        } else {
                            // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
                            BlockingRecordArray.add(new BlockingRecord(-1, -1, moreHigherPriorityTask,
                                    recordTaskReleaseTimesHashMap.get(moreHigherPriorityTask.staticTaskId).size()-1,
                                    null, null, null,
                                    BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, moreHigherPriorityTaskStartTime, moreHigherPriorityTaskStartTime + moreHigherTaskAccessResource.c_low, moreHigherPriorityTaskIsFirstBlockingRecord));

                            // 看看能不能进行合并
                            mergeLastTwoBlockingRecord();
                            moreHigherPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
                        }

                        // 看看能不能跟之前的 blockingRecord 进行合并
                        mergeLastTwoBlockingRecord();
                    }

                    // 到结束还有一段 block
                    // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
                    int leftTime = moreHigherPriorityTask.totalNeededTime - moreHigherPriorityTask.resourceAccessTime.get(moreHigherPriorityTask.resourceAccessTime.size()-1) - totalResources.get(moreHigherPriorityTask.accessResourceIndex.get(moreHigherPriorityTask.accessResourceIndex.size()-1)).c_low;
                    BlockingRecordArray.add(new BlockingRecord(-1, -1, moreHigherPriorityTask,
                            recordTaskReleaseTimesHashMap.get(moreHigherPriorityTask.staticTaskId).size()-1,
                            null, null, null,
                            BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, highPriorityTaskStartTime, highPriorityTaskStartTime + leftTime, moreHigherPriorityTaskIsFirstBlockingRecord));
                    mergeLastTwoBlockingRecord();
                    highPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;

                }
                else {
                    // 增加一个类型为 highPriorityTaskNormalExecution 的 blockingRecord
                    BlockingRecordArray.add(new BlockingRecord(-1, -1, moreHigherPriorityTask,
                            recordTaskReleaseTimesHashMap.get(moreHigherPriorityTask.staticTaskId).size()-1,
                            null, null, null,
                            BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution, moreHigherPriorityTaskStartTime, moreHigherPriorityTaskStartTime + moreHigherPriorityTask.totalNeededTime, moreHigherPriorityTaskIsFirstBlockingRecord));
                }

                // 更新下一个 moreHigherPriorityTask 的 releaseTime
                moreHigherPriorityTaskStartTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
            }
        }
    }

    /**
     * 在 MSRP 协议下，{@code MSRPConsiderAccessGlobalResourceSpinDelay} 用于最大化高优先级任务或 {@code sufferTask} 访问某一全局资源时受到的自旋等待时间。
     * <p>
     *     该方法用于处理 {@code accessGlobalResourceTask} 访问 {@code accessGlobalResource} 的情况，其中：
     *     - {@code baseReleaseTime} 表示访问资源的时间点。
     *     - {@code accessGlobalResourceTaskId} 是访问任务的静态标识符。W
     * </p>
     * <p>
     *     在方法内部执行以下步骤：
     *     1. 找出其他处理器上在 {@code baseReleaseTime} 可以访问 {@code accessGlobalResource} 的任务，放入 {@code suitableTasks} 列表。
     *     2. 向 {@code @BlockingRecordArray} 中添加新的 {@code BlockingRecord}。
     *     3. 设置 {@code suitableTasks} 中任务的发布时间。
     * </p>
     * @param baseReleaseTime 访问资源的时间点。
     * @param accessGlobalResourceTaskId 访问任务的静态标识符。
     * @param accessResourceIndex 访问的第 {@code accessResourceIndex} 个资源。
     * @param sufferTaskId 被查看最差执行情况的任务的静态标识符。
     * @param blockingTypes 表示 {@code BlockingRecord} 的类型。
     * @param isFirstBlockingRecord 标识是否是第一个由 {@code accessGlobalResourceTask} 引起的 {@code BlockingRecord}。
     */
    public BlockingRecord MSRPConsiderAccessGlobalResourceSpinDelay(int baseReleaseTime, int accessGlobalResourceTaskId, int accessResourceIndex, int sufferTaskId,
                                                                    BlockingRecord.BlockingTypes blockingTypes,
                                                                    boolean isFirstBlockingRecord) {
        BasicPCB accessGlobalResourceTask = totalTasks.get(accessGlobalResourceTaskId);
        BasicResource accessGlobalResource = totalResources.get(accessGlobalResourceTask.accessResourceIndex.get(accessResourceIndex));
        // 记录一下 global-resource 可以被哪些 cpu 上的任务访问
        ArrayList<BasicPCB> suitableTasks = new ArrayList<>(SimpleSystemGenerator.total_partitions);
        // suitableTask 中第几次任务的发布可以被替换掉
        ArrayList<Integer> suitableTasksCoverIndex = new ArrayList<>(SimpleSystemGenerator.total_partitions);

        // 1. 看看其他 cpu 上有没有合适的任务可以访问 global-resource / accessGlobalResource
        for (int j = 0; j < accessResourceTasks.get(accessGlobalResource.id).size(); ++j) {
            // 第 j 个 cpu 上没有访问 accessGlobalResource 的 tasks || j 是 sufferTask 对应 cpu 上的任务
            ArrayList<BasicPCB> suitableTasksInJthCPU = accessResourceTasks.get(accessGlobalResource.id).get(j);
            if (suitableTasksInJthCPU.isEmpty() || j == accessGlobalResourceTask.baseRunningCpuCore)
                continue;

            BasicPCB chooseTask = null;
            int maxBlockingTimeAdd = 0;
            int startIndex = -1;
            // 1. 首先观察：是否有存在某一个任务不会产生冲突或者如果有冲突，能不能提高对 sufferTask 总的 BlockingTime
            for (BasicPCB suitableTask : suitableTasksInJthCPU) {
                ArrayList<Integer> suitableTaskBlockingTimes = recordBlockingTimesHashMap.get(suitableTask.staticTaskId);
                ArrayList<Integer> suitableTaskReleaseTimes = recordTaskReleaseTimesHashMap.get(suitableTask.staticTaskId);
                int suitableTaskReleaseTime = baseReleaseTime - suitableTask.resourceAccessTime.get(suitableTask.accessResourceIndex.indexOf(accessGlobalResource.id));
                // 任务没有被发布过，非常 nice || // 任务发布过，那我们再看看跟它的上一次 releaseTime 是否会产生冲突
                if (suitableTaskBlockingTimes.isEmpty() || suitableTaskReleaseTimes.get(suitableTaskReleaseTimes.size() - 1) + suitableTask.period <= suitableTaskReleaseTime) {
                    chooseTask = suitableTask;
                    maxBlockingTimeAdd = accessGlobalResource.c_low;
                    startIndex = suitableTaskBlockingTimes.size();
                    break;
                }
                // 其余情况就是会产生冲突
                // 在冲突的情况下，我们试试看能不能替换某一个发布时间点来提升总的 blocking-time
                else {
                    for (int k = 0; k < suitableTaskBlockingTimes.size(); ++k) {
                        if (accessGlobalResource.c_low - suitableTaskBlockingTimes.get(k) > 0 && accessGlobalResource.c_low - suitableTaskBlockingTimes.get(k) >= maxBlockingTimeAdd) {
                            chooseTask = suitableTask;
                            maxBlockingTimeAdd = accessGlobalResource.c_low - suitableTaskBlockingTimes.get(k);
                            startIndex = k;
                        }
                    }
                }
            }
            if (chooseTask != null) {
                suitableTasks.add(chooseTask);
                suitableTasksCoverIndex.add(startIndex);
            }
        }

        // 2. 对 suitableTasks 中的任务进行迁移
        for (int i = 0; i < suitableTasks.size(); ++i) {
            ArrayList<Integer> chooseTaskBlockingTimes = recordBlockingTimesHashMap.get(suitableTasks.get(i).staticTaskId);

            // 有冲突，调整一下
            if (suitableTasksCoverIndex.get(i) != chooseTaskBlockingTimes.size()) {
                // 先调整一下发生冲突的任务的释放次序
                MSRPAdjustBaseReleaseTimeAndCurrentTaskTotalSpinDelay(suitableTasks.get(i).staticTaskId, suitableTasksCoverIndex.get(i), sufferTaskId);
            }
        }

        int startTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;

        // 3. suitableTasks 中的任务都没有问题了，可以进行释放了
        // 现在来计算一下 highPriorityTask 所在的 cpu 是不是最后一个访问 global-resource 的，以此来修改 tmpTaskAccessGlobalResourceTime
        if (!suitableTasks.isEmpty()) {
            ArrayList<Integer> suitableTasksReleaseIndex = new ArrayList<>();
            ArrayList<Integer> suitableTasksBlockingTimes = new ArrayList<>();

            for (BasicPCB suitableTask : suitableTasks) {
                int releaseTime = startTime - suitableTask.resourceAccessTime.get(suitableTask.accessResourceIndex.indexOf(accessGlobalResource.id));

                // suitableTask 的阻塞时间以及发布次数
                suitableTasksBlockingTimes.add(accessGlobalResource.c_low);
                suitableTasksReleaseIndex.add(recordBlockingTimesHashMap.get(suitableTask.staticTaskId).size());

                // HashMap 记录其带给 blockingTask 的阻塞时间以及其对应的发布时间
                recordTaskReleaseTimesHashMap.get(suitableTask.staticTaskId).add(releaseTime);
                recordBlockingTimesHashMap.get(suitableTask.staticTaskId).add(accessGlobalResource.c_low);
            }

            // 记录一下 BlockingRecord
            BlockingRecordArray.add(new BlockingRecord(accessResourceIndex, accessGlobalResource.id, accessGlobalResourceTask,
                    recordTaskReleaseTimesHashMap.get(accessGlobalResourceTaskId).size()-1,
                    suitableTasks, suitableTasksBlockingTimes, suitableTasksReleaseIndex, blockingTypes,
                    startTime, startTime + accessGlobalResource.c_low * (suitableTasks.size()+1), isFirstBlockingRecord));

            // 调整一下新加入的 BlockingRecord
            AdjustAccessGlobalResourceBlockingRecord(BlockingRecordArray.get(BlockingRecordArray.size()-1), accessGlobalResource, startTime);

        } else {
            // suitableTasks 为空，说明没有任务可以帮助，可以将其 blockingType 视为 highPriorityTaskNormalExecution
            // 看看上一个 BlockingRecord 是不是也是 highPriorityTaskNormalExecution, 如果也是，那么修改一下 endTime 即可
            BlockingRecord.BlockingTypes _blockingType;
            if (blockingTypes == BlockingRecord.BlockingTypes.highPriorityTaskAccessGlobalResource) {
                _blockingType = BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution;
            }else {
                _blockingType = BlockingRecord.BlockingTypes.sufferTaskNormalExecution;
            }

            // 添加
            BlockingRecordArray.add(new BlockingRecord(-1, -1, accessGlobalResourceTask,
                    recordTaskReleaseTimesHashMap.get(accessGlobalResourceTaskId).size()-1,
                    null, null, null,
                    _blockingType,
                    startTime, startTime + accessGlobalResource.c_low, isFirstBlockingRecord));

            // 合并一下
            mergeLastTwoBlockingRecord();
        }

        isFirstBlockingRecord = false;
        // 返回最后一个 blockingRecord
        return BlockingRecordArray.get(BlockingRecordArray.size() - 1);
    }

    /**
     * {@code MSRPAdjustBaseReleaseTimeAndCurrentTaskTotalSpinDelay} 用于去除 {@code immigrateTask} 的第 {@code coverReleaseIndex} 次发布，并重新计算 {@code BlockingRecordArray} 中每个 {@code BlockingRecord} 的起止时间。
     */
    public BlockingRecord MSRPAdjustBaseReleaseTimeAndCurrentTaskTotalSpinDelay(int immigrateTaskId, int coverReleaseIndex, int sufferTaskId) {
        // 开始设置阻塞任务的发布时间（设置为）
        int baseReleaseTime = 1;
        // 任务的发布时间
        for (BasicPCB tmpTask : totalTasks) {
            for (int j = tmpTask.accessResourceIndex.size() - 1; j >= 0; --j) {
                if (totalResources.get(tmpTask.accessResourceIndex.get(j)).isGlobal) {
                    baseReleaseTime = Math.max(baseReleaseTime, tmpTask.resourceAccessTime.get(j));
                    break;
                }
            }
        }
        baseReleaseTime += 2;

        // immigrateTask 目前释放的次数
        int releaseNumber = -1;
        int i = 0;
        int startTime = baseReleaseTime;

        BasicPCB immigrateTask = totalTasks.get(immigrateTaskId);
        BlockingRecord blockingRecord = null;

        // 先找到 immigrateTaskId 的 coverReleaseIndex 是在第几个 BlockingRecord 里面
        for (; i < BlockingRecordArray.size(); ++i) {
            blockingRecord = BlockingRecordArray.get(i);

            // 0.0 看看本次 blockingRecord 中是否有 immigrateTask
            if (blockingRecord.blockingResourceTasks != null && blockingRecord.blockingResourceTasks.contains(totalTasks.get(immigrateTaskId))) {
                ++releaseNumber;
            }

            if (releaseNumber == coverReleaseIndex) {

                // 1. 不管怎么样，都首先把 immigrateTask 从 recordBlockingTaskHashMap 和 recordTaskReleaseTimeHashMap 当中删除
                recordBlockingTimesHashMap.get(immigrateTaskId).remove(coverReleaseIndex);
                recordTaskReleaseTimesHashMap.get(immigrateTaskId).remove(coverReleaseIndex);

                // case 1 : high-priority-task-access-global-resource
                // case 3 : suffer-task-access-global-resource
                if (blockingRecord.blockingTypes == BlockingRecord.BlockingTypes.highPriorityTaskAccessGlobalResource || blockingRecord.blockingTypes == BlockingRecord.BlockingTypes.sufferTaskAccessGlobalResource) {
                    BlockingRecord.BlockingTypes _blockingRecordType;

                    if (blockingRecord.blockingTypes == BlockingRecord.BlockingTypes.highPriorityTaskAccessGlobalResource)
                        _blockingRecordType = BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution;
                    else
                        _blockingRecordType = BlockingRecord.BlockingTypes.sufferTaskNormalExecution;

                    // 只有一个 cpu 上的任务可以造成阻塞，那么删除之后 blockingTask 访问该资源就畅通无阻, blockingType 就变成 normal-execution
                    if (blockingRecord.blockingResourceTasks.size() == 1) {
                        boolean isMerged = false;
                        // 将其 blockingType 修改成 normal-execution
                        blockingRecord.blockingTypes = _blockingRecordType;
                        blockingRecord.endTime -= blockingRecord.blockingTimes.get(0);

                        // 看看能不能跟左边进行合并
                        if (i - 1 >= 0) {
                            BlockingRecord leftBlockingRecord = BlockingRecordArray.get(i - 1);

                            // 满足条件，看看可以进行合并
                            if (leftBlockingRecord.blockingTask == blockingRecord.blockingTask && leftBlockingRecord.blockingTypes == _blockingRecordType) {
                                leftBlockingRecord.endTime += blockingRecord.endTime - blockingRecord.startTime;

                                BlockingRecordArray.remove(i);
                                --i;
                                isMerged = true;
                            }
                        }

                        blockingRecord = BlockingRecordArray.get(i);

                        // 看看能不能跟右边进行合并
                        if (i + 1 < BlockingRecordArray.size()) {
                            // 看看能不能跟右边的进行合并
                            BlockingRecord rightBlockingRecord = BlockingRecordArray.get(i + 1);

                            // 满足条件，可以进行合并
                            if (rightBlockingRecord.blockingTask == blockingRecord.blockingTask && rightBlockingRecord.blockingTypes == _blockingRecordType) {
                                blockingRecord.endTime += rightBlockingRecord.endTime - rightBlockingRecord.startTime;

                                BlockingRecordArray.remove(i + 1);
                                isMerged = true;
                            }
                        }

                        // 如果被合并了，就不需要进行多余的操作，毕竟将其删除了
                        // 如果没有，那么就修改它的 blockingTimes blockingResourceTasks blockingTask blockingResourceTasksReleaseIndex
                        if (!isMerged) {
                            blockingRecord.blockingTimes = null;
                            blockingRecord.blockingResourceTasks = null;
                            blockingRecord.blockingResourceTasksReleaseIndex = null;
                        }
                    } else {
                        // blockingTypes == high-priority-task-access-global-resource && blockingResourceTasks.size() > 1
                        // 这种情况下就不需要去考虑合并的事
                        // 这里的话还是需要进行修改的
                        int immigrateTaskAccessIndex = blockingRecord.blockingResourceTasks.indexOf(immigrateTask);

                        // 2. 将其从 blockingRecord 当中删除
                        blockingRecord.blockingResourceTasks.remove(immigrateTaskAccessIndex);
                        blockingRecord.blockingTimes.remove(immigrateTaskAccessIndex);
                        blockingRecord.blockingResourceTasksReleaseIndex.remove(immigrateTaskAccessIndex);

                    }
                }
                // case 2 : arrival-blocking-global-resource
                else if (blockingRecord.blockingTypes == BlockingRecord.BlockingTypes.arrivalBlockingGlobalResource) {
                    int immigrateTaskAccessIndex = blockingRecord.blockingResourceTasks.indexOf(immigrateTask);
                    // 将 immigrateTask 从 blockingRecord 的属性当中删除
                    blockingRecord.blockingResourceTasks.remove(immigrateTaskAccessIndex);
                    blockingRecord.blockingTimes.remove(immigrateTaskAccessIndex);
                    blockingRecord.blockingResourceTasksReleaseIndex.remove(immigrateTaskAccessIndex);

                    // 重新计算一遍 Arrival-Global-Resource-Blocking-Record
                    AdjustArrivalGlobalResourceBlockingRecord(blockingRecord, totalResources.get(blockingRecord.blockingResourceId), baseReleaseTime, sufferTaskId);
                }
                break;
            }
        }

        // 目前 i 所在的 blockingRecord 是访问过的，下一个是没有访问过的
        startTime = BlockingRecordArray.get(i).endTime;
        ++i;

        // 之后 blockingRecord 的类型：
        // 1. high-priority-task-normal-execution
        // 2. high-priority-task-access-global-resource
        // 3. suffer-task-normal-execution
        // 4. suffer-task-access-global-resource

        // 接下来处理那些还没有处理过的 blockingRecord
        // 开始更新之后每一个 blockingRecord 中任务的发布时间
        for (; i < BlockingRecordArray.size(); ++i) {
            blockingRecord = BlockingRecordArray.get(i);
            BasicPCB blockingTask = blockingRecord.blockingTask;
            // 先判断一下需不需要更改 blockingTask 的 releaseTime
            if (blockingRecord.isFirstBlockingRecord && blockingTask.staticTaskId != sufferTaskId) {
                recordTaskReleaseTimesHashMap.get(blockingTask.staticTaskId).set(blockingRecord.blockingTaskIthRelease, startTime);
            }

            if (blockingRecord.blockingResourceTasks != null) {
                // 先将 immigrateTask 中第几次释放往前移动一下
                if (blockingRecord.blockingResourceTasks.contains(immigrateTask)) {
                    int immigrateTaskIndex = blockingRecord.blockingResourceTasks.indexOf(immigrateTask);
                    blockingRecord.blockingResourceTasksReleaseIndex.set(immigrateTaskIndex, blockingRecord.blockingResourceTasksReleaseIndex.get(immigrateTaskIndex)-1);
                }

                AdjustAccessGlobalResourceBlockingRecord(blockingRecord, totalResources.get(blockingRecord.blockingResourceId), startTime);
                startTime = BlockingRecordArray.get(i).endTime;
            }
            else {
                // 正常修改一下 startTime 和 endTime 就可以了
                blockingRecord.endTime = startTime + blockingRecord.endTime - blockingRecord.startTime;
                blockingRecord.startTime = startTime;
                startTime = blockingRecord.endTime;
            }
        }

        return BlockingRecordArray.get(BlockingRecordArray.size()-1);
    }

    /**
     * {@code AdjustAccessGlobalResourceBlockingRecord}调整 access-global-resource 类型的 BlockingRecord 中相关任务的发布时间和阻塞时长。
     * <p>
     *     {@code blockingRecord} 中用于增加任务访问全局资源时遭受的自旋等待时间的任务数量发生变动，对{@code blockingRecord.blockingTask}的阻塞时长发生变化，修改相关任务的发布事件以及{@code BlockingRecord} 的相关属性。
     * </p>
     * @param blockingRecord 需要调整的阻塞类型。
     * @param accessGlobalResource 访问的资源。
     * @param startTime 任务访问 accessGlobalResource 的时间。
     */
    public void AdjustAccessGlobalResourceBlockingRecord(BlockingRecord blockingRecord, BasicResource accessGlobalResource, int startTime) {
        ArrayList<BasicPCB> suitableTasks = blockingRecord.blockingResourceTasks;
        ArrayList<Integer> suitableTasksReleaseIndex = blockingRecord.blockingResourceTasksReleaseIndex;
        ArrayList<Integer> suitableTasksBlockingTimes = blockingRecord.blockingTimes;
        BasicPCB blockingTask = blockingRecord.blockingTask;

        int releaseTime;
        int endTime = startTime;

        // blockingTask 最后一个释放
        if (suitableTasks.get(suitableTasks.size() - 1).baseRunningCpuCore < blockingTask.baseRunningCpuCore) {
            for (int i = 0; i < suitableTasks.size(); ++i) {
                BasicPCB suitableTask = suitableTasks.get(i);

                // 设置发布时间
                releaseTime = startTime - suitableTask.resourceAccessTime.get(suitableTask.accessResourceIndex.indexOf(accessGlobalResource.id));
                ArrayList<Integer> suitableTaskReleaseTimesHashMap = recordTaskReleaseTimesHashMap.get(suitableTask.staticTaskId);
                ArrayList<Integer> suitableTaskBlockingTimesHashMap = recordBlockingTimesHashMap.get(suitableTask.staticTaskId);

                // HashMap : 设置并记录其带给 blockingTask 的阻塞时间以及其对应的发布时间
                suitableTaskReleaseTimesHashMap.set(suitableTasksReleaseIndex.get(i), releaseTime);
                suitableTaskBlockingTimesHashMap.set(suitableTasksReleaseIndex.get(i), accessGlobalResource.c_low);

                // BlockingRecord : 设置并记录其带给 blockingTask 的阻塞时间
                suitableTasksBlockingTimes.set(i, accessGlobalResource.c_low);
            }
            endTime += accessGlobalResource.c_low * (suitableTasks.size() + 1);
        }
        // blockingTask 不是最后一个释放的
        else {
            // 所有 suitableTasks 上的任务的 releaseTime 都需要往前移动 1 个 system-clock
            for (int i = 0; i < suitableTasks.size(); ++i) {
                BasicPCB suitableTask = suitableTasks.get(i);

                // 设置发布时间
                releaseTime = startTime - suitableTask.resourceAccessTime.get(suitableTask.accessResourceIndex.indexOf(accessGlobalResource.id)) - 1;
                ArrayList<Integer> suitableTaskReleaseTimesHashMap = recordTaskReleaseTimesHashMap.get(suitableTask.staticTaskId);
                ArrayList<Integer> suitableTaskBlockingTimesHashMap = recordBlockingTimesHashMap.get(suitableTask.staticTaskId);

                // HashMap : 设置并记录其带给 blockingTask 的阻塞时间以及其对应的发布时间
                suitableTaskReleaseTimesHashMap.set(suitableTasksReleaseIndex.get(i), releaseTime);
                if (i != 0) {
                    suitableTaskBlockingTimesHashMap.set(suitableTasksReleaseIndex.get(i), accessGlobalResource.c_low);
                    // BlockingRecord : 设置并记录其带给 blockingTask 的阻塞时间
                    suitableTasksBlockingTimes.set(i, accessGlobalResource.c_low);
                } else {
                    suitableTaskBlockingTimesHashMap.set(suitableTasksReleaseIndex.get(i), accessGlobalResource.c_low - 1);
                    // BlockingRecord : 设置并记录其带给 blockingTask 的阻塞时间
                    suitableTasksBlockingTimes.set(i, accessGlobalResource.c_low - 1);
                }
            }
            endTime += accessGlobalResource.c_low * (suitableTasks.size() + 1) - 1;

        }

        blockingRecord.startTime = startTime;
        blockingRecord.endTime = endTime;
    }

    /**
     * 尝试合并两个 blockingRecord
     */
    public void mergeLastTwoBlockingRecord() {
        if (BlockingRecordArray.size() < 2)
            return;

        // 倒数第一个 blockingRecord
        BlockingRecord lastBlockingRecord = BlockingRecordArray.get(BlockingRecordArray.size() - 1);
        // 倒数第二个 blockingRecord
        BlockingRecord lastLastBlockingRecord = BlockingRecordArray.get(BlockingRecordArray.size() - 2);

        if (lastBlockingRecord.blockingTask == lastLastBlockingRecord.blockingTask
                && lastBlockingRecord.blockingTypes == BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution
                && lastLastBlockingRecord.blockingTypes == BlockingRecord.BlockingTypes.highPriorityTaskNormalExecution) {
            lastLastBlockingRecord.endTime = lastBlockingRecord.endTime;
            BlockingRecordArray.remove(lastBlockingRecord);
        }

        if (lastBlockingRecord.blockingTask == lastLastBlockingRecord.blockingTask
                && lastBlockingRecord.blockingTypes == BlockingRecord.BlockingTypes.sufferTaskNormalExecution
                && lastLastBlockingRecord.blockingTypes == BlockingRecord.BlockingTypes.sufferTaskNormalExecution) {
            lastLastBlockingRecord.endTime = lastBlockingRecord.endTime;
            BlockingRecordArray.remove(lastBlockingRecord);
        }
    }

    /**
     * 在 MSRP 协议下考虑低优先级任务给 sufferTask 带来的 Arrival-Blocking。
     * <p>
     *     在 MSRP 协议下，低优先级任务持有局部资源、自旋等待或持有全局资源均可以造成 sufferTask 遭受到达阻塞。
     *     从上述两种情况中找到提供最长到达阻塞时长的任务并设计其发布时间。
     * </p>
     */
    public int MSRPConsiderArrivalBlocking(int sufferTaskId) {
        // 1. 先处理低优先级任务的 Blocking
        BasicPCB sufferTask = totalTasks.get(sufferTaskId);
        int maxArrivalBlockingTime = 0;
        BasicPCB maxArrivalBlockingTask = null;
        int maxArrivalBlockingResourceId = -1;
        for (BasicPCB task : allocatedTaskSets.get(sufferTask.baseRunningCpuCore)) {
            // 低优先级任务 && 低优先级任务有在访问资源
            if (task.basePriority < sufferTask.basePriority && !task.accessResourceIndex.isEmpty()) {
                // 看看每个访问资源能够提供的最大阻塞时间
                for (int arrivalBlockingResourceId : task.accessResourceIndex) {
                    BasicResource arrivalBlockingResource = totalResources.get(arrivalBlockingResourceId);
                    // 1. 全局资源
                    if (arrivalBlockingResource.isGlobal) {
                        // 该全局资源会被哪些 cpu 所使用以此来计算访问该全局资源可能导致的 Arrival-Blocking-Time
                        ArrayList<ArrayList<BasicPCB>> accessResourceSets = accessResourceTasks.get(arrivalBlockingResourceId);
                        int accessCpuNum = 0;
                        // 看看哪些 cpu 上的任务会访问 arrivalBlockingResource
                        for (int i = 0; i < accessResourceSets.size(); ++i) {
                            if (i != sufferTask.baseRunningCpuCore && !accessResourceSets.get(i).isEmpty()) {
                                ++accessCpuNum;
                            }
                        }

                        // 看看这是不是一个能带来更长阻塞时间的 global-resource
                        if (maxArrivalBlockingTime <= accessCpuNum * arrivalBlockingResource.c_low - 1) {
                            maxArrivalBlockingTime = accessCpuNum * arrivalBlockingResource.c_low - 1;
                            maxArrivalBlockingTask = task;
                            maxArrivalBlockingResourceId = arrivalBlockingResourceId;
                        }
                    }
                    // 2. 访问局部资源 && 访问的资源所对应的天花板更高
                    else if (arrivalBlockingResource.ceiling.get(task.baseRunningCpuCore) >= sufferTask.basePriority) {
                        // 该 local-resource 能够造成优先级反转 --> 判断有没有达成更长的阻塞时间
                        if (maxArrivalBlockingTime <= arrivalBlockingResource.c_low - 1) {
                            maxArrivalBlockingTime = arrivalBlockingResource.c_low - 1;
                            maxArrivalBlockingTask = task;
                            maxArrivalBlockingResourceId = arrivalBlockingResourceId;
                        }
                    }
                }
            }
        }

        // 2. 开始设置阻塞任务的发布时间（设置为）
        int baseReleaseTime = 1;
        // 任务的发布时间
        int releaseTime = 0;
        for (BasicPCB tmpTask : totalTasks) {
            for (int j = tmpTask.accessResourceIndex.size() - 1; j >= 0; --j) {
                if (totalResources.get(tmpTask.accessResourceIndex.get(j)).isGlobal) {
                    baseReleaseTime = Math.max(baseReleaseTime, tmpTask.resourceAccessTime.get(j));
                    break;
                }
            }
        }
        baseReleaseTime += 2;

        // 经过上面的 for 循环之后，我们就知道当前 cpu 中哪个任务中的哪个资源可以造成最大的 arrival-blocking-time
        // 我们需要先判断 : 造成最大 arrival-blocking-time 的资源是 global-resource 还是 local-resource
        if (maxArrivalBlockingTask != null) {
            BasicResource maxArrivalBlockingResource = totalResources.get(maxArrivalBlockingResourceId);

            // 造成最大 arrival-blocking 的是一个全局资源
            if (totalResources.get(maxArrivalBlockingResourceId).isGlobal) {
                // 对其他 cpu 上访问 maxArrivalBlockingResource 的任务进行记录
                // 1 .挑选出每一个 cpu 上最适合访问 blocking-resource 的任务
                ArrayList<BasicPCB> suitableTasks = new ArrayList<>(SimpleSystemGenerator.total_partitions);
                // 2. 记录每一个任务参与本次阻塞时是第几次发布
                ArrayList<Integer> suitableTasksReleaseIndex = new ArrayList<>(SimpleSystemGenerator.total_partitions);
                // 3. suitableTasks 中每一个任务对 sufferTask 的阻塞时长
                ArrayList<Integer> suitableTasksBlockingTime = new ArrayList<>(SimpleSystemGenerator.total_partitions);

                // 开始挑选每一个 cpu 上的任务
                ArrayList<ArrayList<BasicPCB>> accessBlockingResourceTasks = accessResourceTasks.get(maxArrivalBlockingResourceId);
                for (int i = 0; i < accessBlockingResourceTasks.size(); ++i) {
                    // 挑选的条件 : 与 sufferTask 之间的关联度最小
                    if (!accessBlockingResourceTasks.get(i).isEmpty() && i != sufferTask.baseRunningCpuCore) {
                        BasicPCB chooseTask = null;
                        int minRelatedSufferTaskSum = SimpleSystemGenerator.NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE;
                        int minUnrelatedSufferTaskSum = SimpleSystemGenerator.NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE;
                        for (BasicPCB providedBlockingTimeTask : accessBlockingResourceTasks.get(i)) {
                            int accessBlockingResourceIndex = providedBlockingTimeTask.accessResourceIndex.indexOf(maxArrivalBlockingResourceId);
                            int nowRelatedSufferTaskSum = 0;
                            int nowUnrelatedSufferTaskSum = 0;
                            for (int j = accessBlockingResourceIndex + 1; j < providedBlockingTimeTask.accessResourceIndex.size(); ++j) {
                                if (sufferTask.accessResourceIndex.contains(providedBlockingTimeTask.accessResourceIndex.get(j))) {
                                    ++nowRelatedSufferTaskSum;
                                } else
                                    ++nowUnrelatedSufferTaskSum;
                            }
                            if (minRelatedSufferTaskSum > nowRelatedSufferTaskSum) {
                                minRelatedSufferTaskSum = nowRelatedSufferTaskSum;
                                minUnrelatedSufferTaskSum = nowUnrelatedSufferTaskSum;
                                chooseTask = providedBlockingTimeTask;
                            } else if (minRelatedSufferTaskSum == nowRelatedSufferTaskSum && minUnrelatedSufferTaskSum > nowUnrelatedSufferTaskSum) {
                                minUnrelatedSufferTaskSum = nowUnrelatedSufferTaskSum;
                                chooseTask = providedBlockingTimeTask;
                            }
                        }
                        // 按照 cpu 顺序加入到 suitableTasks 当中
                        suitableTasks.add(chooseTask);
                    }
                }

                // 以下为 suitableTasks 以及 maxArrivalBlockingTask 的记录都是随意的，AdjustArrivalGlobalResourceBlockingRecord 会进行处理
                // 为每一个在 suitableTask 中的任务添加 releaseTime 以及 blockingTime
                for (BasicPCB tmpTask : suitableTasks) {
                    // 1. 发布时间
                    releaseTime = baseReleaseTime - tmpTask.resourceAccessTime.get(tmpTask.accessResourceIndex.indexOf(maxArrivalBlockingResourceId));

                    // 2. blockingTime && releaseIndex
                    suitableTasksBlockingTime.add(totalResources.get(maxArrivalBlockingResourceId).c_low);
                    suitableTasksReleaseIndex.add(recordTaskReleaseTimesHashMap.get(tmpTask.staticTaskId).size());

                    // 3. releaseTime && releaseIndex
                    recordTaskReleaseTimesHashMap.get(tmpTask.staticTaskId).add(releaseTime);
                    recordBlockingTimesHashMap.get(tmpTask.staticTaskId).add(maxArrivalBlockingResource.c_low);
                }

                // 设置 maxArrivalBlockingTask 和 sufferTask 的 release
                recordTaskReleaseTimesHashMap.get(maxArrivalBlockingTask.staticTaskId).add(baseReleaseTime - maxArrivalBlockingTask.resourceAccessTime.get(maxArrivalBlockingTask.accessResourceIndex.indexOf(maxArrivalBlockingResourceId)));
                // sufferTask 先随机释放，AdjustArrivalGlobalResourceBlockingRecord 函数会进行修改
                recordTaskReleaseTimesHashMap.get(sufferTaskId).add(releaseTime);

                // BlockingRecordArray 记录一下本次阻塞的类型
                BlockingRecordArray.add(new BlockingRecord(maxArrivalBlockingTask.accessResourceIndex.indexOf(maxArrivalBlockingResourceId),
                        maxArrivalBlockingResourceId, maxArrivalBlockingTask, 0,
                        suitableTasks, suitableTasksBlockingTime, suitableTasksReleaseIndex, BlockingRecord.BlockingTypes.arrivalBlockingGlobalResource,
                        0, 0, true));

                AdjustArrivalGlobalResourceBlockingRecord(BlockingRecordArray.get(0), maxArrivalBlockingResource, baseReleaseTime, sufferTaskId);

                baseReleaseTime = BlockingRecordArray.get(BlockingRecordArray.size()-1).endTime;
            }
            else {
                // maxArrivalBlockingTask 访问 local-resource 造成优先级反转

                // 1. maxArrivalBlockingTask 的 releaseTime
                releaseTime = baseReleaseTime - maxArrivalBlockingTask.resourceAccessTime.get(maxArrivalBlockingTask.accessResourceIndex.indexOf(maxArrivalBlockingResourceId));
                recordTaskReleaseTimesHashMap.get(maxArrivalBlockingTask.staticTaskId).add(releaseTime);


                // 2. sufferTask 的 releaseTime
                releaseTime = baseReleaseTime + 1;
                recordTaskReleaseTimesHashMap.get(sufferTaskId).add(releaseTime);

                // 这里还是在 BlockingRecordArray 中记录一下，后面在计算 baseReleaseTime 会方便一点
                BlockingRecordArray.add(new BlockingRecord(maxArrivalBlockingTask.accessResourceIndex.indexOf(maxArrivalBlockingResourceId),
                        maxArrivalBlockingResourceId, maxArrivalBlockingTask, 0,
                        null, null, null,
                        BlockingRecord.BlockingTypes.arrivalBlockingLocalResource,
                        baseReleaseTime, baseReleaseTime + totalResources.get(maxArrivalBlockingResourceId).c_low,
                        true));

                baseReleaseTime = baseReleaseTime + totalResources.get(maxArrivalBlockingResourceId).c_low;
            }
        }

        // 此时返回的 baseReleaseTime 有两种可能
            // 1. arrival-blocking 结束的时间
            // 2. 没有任务可以提供 arrival-blocking
        // high-priority-task 应该在 baseReleaseTime 开始释放
        return baseReleaseTime;
    }

    /**
     * {@code AdjustArrivalGlobalResourceBlockingRecord}调整 arrival-global-resource-blocking 类型的 BlockingRecord 中相关任务的发布时间和阻塞时长。
     * <p>
     *     {@code blockingRecord} 中用于延长低优先级任务访问全局资源的自旋等待时间的任务数量发生变动，{@code blockingRecord}的阻塞时长发生变化，修改相关任务的发布事件以及{@code BlockingRecord} 的相关属性。
     * </p>
     * @param blockingRecord 需要调整的阻塞类型。
     * @param accessGlobalResource 访问的资源。
     * @param startTime 低优先级任务访问 accessGlobalResource 的时间。
     * @param sufferTaskId 被查看最差执行情况的任务的静态标识符。
     */
    public void AdjustArrivalGlobalResourceBlockingRecord(BlockingRecord blockingRecord, BasicResource accessGlobalResource, int startTime, int sufferTaskId) {
        ArrayList<BasicPCB> suitableTasks = blockingRecord.blockingResourceTasks;
        ArrayList<Integer> suitableTasksReleaseIndex = blockingRecord.blockingResourceTasksReleaseIndex;
        ArrayList<Integer> suitableTasksBlockingTimes = blockingRecord.blockingTimes;
        BasicPCB blockingTask = blockingRecord.blockingTask;

        int releaseTime;
        int endTime = startTime;

        if (!suitableTasks.isEmpty()) {
            // blockingTask 最后一个释放
            if (suitableTasks.get(suitableTasks.size() - 1).baseRunningCpuCore < blockingTask.baseRunningCpuCore) {
                for (int i = 0; i < suitableTasks.size(); ++i) {
                    BasicPCB suitableTask = suitableTasks.get(i);

                    // 1. 设置发布时间
                    releaseTime = startTime - suitableTask.resourceAccessTime.get(suitableTask.accessResourceIndex.indexOf(accessGlobalResource.id));

                    // 2. HashMap : 设置并记录其带给 blockingTask 的阻塞时间以及其对应的发布时间
                    ArrayList<Integer> suitableTaskReleaseTimesHashMap = recordTaskReleaseTimesHashMap.get(suitableTask.staticTaskId);
                    ArrayList<Integer> suitableTaskBlockingTimesHashMap = recordBlockingTimesHashMap.get(suitableTask.staticTaskId);
                    suitableTaskReleaseTimesHashMap.set(suitableTasksReleaseIndex.get(i), releaseTime);
                    suitableTaskBlockingTimesHashMap.set(suitableTasksReleaseIndex.get(i), accessGlobalResource.c_low);

                    // 3. BlockingRecord : 设置并记录其带给 blockingTask 的阻塞时间
                    suitableTasksBlockingTimes.set(i, accessGlobalResource.c_low);
                }
                endTime += accessGlobalResource.c_low * (suitableTasks.size() + 1);
            }
            else {
                // blockingTask 不是最后一个释放的，所有 suitableTasks 上的任务的 releaseTime 都需要往前移动 1 个 system-clock
                for (int i = 0; i < suitableTasks.size(); ++i) {
                    BasicPCB suitableTask = suitableTasks.get(i);

                    // 1. 发布时间
                    releaseTime = startTime - suitableTask.resourceAccessTime.get(suitableTask.accessResourceIndex.indexOf(accessGlobalResource.id)) - 1;

                    // 2. 设置 blockingTime 以及 releaseTime (HashMap 以及 BlockingRecord 中都需要更新)
                    ArrayList<Integer> suitableTaskReleaseTimesHashMap = recordTaskReleaseTimesHashMap.get(suitableTask.staticTaskId);
                    ArrayList<Integer> suitableTaskBlockingTimesHashMap = recordBlockingTimesHashMap.get(suitableTask.staticTaskId);
                    // HashMap : 设置并记录其带给 blockingTask 的阻塞时间以及其对应的发布时间
                    suitableTaskReleaseTimesHashMap.set(suitableTasksReleaseIndex.get(i), releaseTime);
                    if (i != 0) {
                        suitableTaskBlockingTimesHashMap.set(suitableTasksReleaseIndex.get(i), accessGlobalResource.c_low);
                        // BlockingRecord : 设置并记录其带给 blockingTask 的阻塞时间
                        suitableTasksBlockingTimes.set(i, accessGlobalResource.c_low);
                    } else {
                        suitableTaskBlockingTimesHashMap.set(suitableTasksReleaseIndex.get(i), accessGlobalResource.c_low - 1);
                        // BlockingRecord : 设置并记录其带给 blockingTask 的阻塞时间
                        suitableTasksBlockingTimes.set(i, accessGlobalResource.c_low - 1);
                    }
                }
                endTime += accessGlobalResource.c_low * (suitableTasks.size() + 1) - 1;

            }
            // 设置 blockingTask 的 releaseTime
            releaseTime = startTime - blockingTask.resourceAccessTime.get(blockingRecord.ithResource);
            recordTaskReleaseTimesHashMap.get(blockingTask.staticTaskId).set(blockingRecord.blockingTaskIthRelease, releaseTime);

            // 设置 sufferTask 的施放时间
            releaseTime = startTime + 1;
            recordTaskReleaseTimesHashMap.get(sufferTaskId).set(0, releaseTime);

            // 设置 blockingRecord 的属性
            blockingRecord.startTime = startTime;
            blockingRecord.endTime = endTime;
        }
        // 访问资源时没有遇到阻塞, 简单设置 blockingTask 和 sufferTask 即可
        else {
            // 设置 blockingTask 的 releaseTime
            releaseTime = startTime - blockingTask.resourceAccessTime.get(blockingRecord.ithResource);
            recordTaskReleaseTimesHashMap.get(blockingTask.staticTaskId).set(blockingRecord.blockingTaskIthRelease, releaseTime);

            // 设置 sufferTask 的施放时间
            releaseTime = startTime + 1;
            recordTaskReleaseTimesHashMap.get(sufferTaskId).set(0, releaseTime);

            // 设置 blockingRecord 的属性
            blockingRecord.startTime = startTime;
            blockingRecord.endTime = startTime + accessGlobalResource.c_low;
            blockingRecord.blockingResourceTasks = null;
            blockingRecord.blockingResourceTasksReleaseIndex = null;
            blockingRecord.blockingTimes = null;
        }

    }

    /**
     * 提前所有任务的发布时间，避免出现时间段冗余
     */
    public void AdjustTotalReleaseTime() {
        // 1. 找到最早发布的任务的发布时间
        int minimalReleaseTime = Integer.MAX_VALUE;
        for (Map.Entry<Integer, ArrayList<Integer>> entry : recordTaskReleaseTimesHashMap.entrySet()) {
            ArrayList<Integer> taskReleaseTimes = entry.getValue();
            for (Integer taskReleaseTime : taskReleaseTimes) {
                minimalReleaseTime = Math.min(minimalReleaseTime, taskReleaseTimes.get(0));
            }
        }

        --minimalReleaseTime;

        // 2. 调整任务发布时间
        for (Map.Entry<Integer, ArrayList<Integer>> entry : recordTaskReleaseTimesHashMap.entrySet()) {
            ArrayList<Integer> taskReleaseTimes = entry.getValue();
            for (int i = 0; i < taskReleaseTimes.size(); ++i) {
                taskReleaseTimes.set(i, taskReleaseTimes.get(i)-minimalReleaseTime);
            }
        }
    }
}
