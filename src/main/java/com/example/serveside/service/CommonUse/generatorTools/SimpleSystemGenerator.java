package com.example.serveside.service.CommonUse.generatorTools;
import com.example.serveside.service.CommonUse.BasicPCB;
import com.example.serveside.service.CommonUse.BasicResource;
import com.example.serveside.request.ConfigurationInformation;

import java.util.*;

/**
 * {@code SimpleSystemGenerator}用于随机生成一组新的任务、资源以及任务的发布时间。
 */
public class SimpleSystemGenerator {

    /**
     * 运行参数：处理器核心个数。
     */
    public static int TOTAL_CPU_CORE_NUM = 2;

    /**
     * 运行参数：访问资源的任务的比例。
     */
    public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;

    /**
     * 任务的最低优先级。
     */
    public static final int MIN_PRIORITY = 1;

    /**
     * 运行参数：一个处理器上平均分配的任务数。
     */
    public static int NUMBER_OF_TASK_IN_A_PARTITION = 3;

    /**
     *  系统中资源个数。
     */
    public enum RESOURCES_RANGE {
        /**
         * 系统资源个数是处理器核心个数的一半。
         */
        HALF_PARTITIONS,

        /**
         * 系统资源个数和处理器核心个数相同。
         */
        PARTITIONS,

        /**
         * 系统资源个数是处理器核心个数的两倍。
         */
        DOUBLE_PARTITIONS,
    }

    /**
     * 任务访问资源的时长。
     */
    public enum CS_LENGTH_RANGE {
            VERY_LONG_CSLEN, LONG_CSLEN, MEDIUM_CS_LEN, SHORT_CS_LEN, VERY_SHORT_CS_LEN, Random
    }

    /**
     * 运行参数：任务访问资源的时长。
     */
    public CS_LENGTH_RANGE cs_len_range;

    int csl = -1;

    /**
     * 运行参数：任务最大的最短发布时间间隔。
     */
    public int maxT;

    /**
     * 运行参数：任务最小的最短发布时间间隔。
     */
    public int minT;

    /**
     * 运行参数：任务在一次发布中访问同一资源的最多次数。
     */
    public int number_of_max_access;

    /**
     * 运行参数：系统中资源个数。
     */
    public RESOURCES_RANGE numberOfResource;

    /**
     * 运行参数：访问资源的任务的比例。
     */
    public double resourceSharingFactor;

    /**
     * 运行参数：系统中任务的个数。
     */
    public static int total_tasks;

    /**
     * 运行参数：处理器核心个数。
     */
    public static int total_partitions;

    /**
     * 运行参数：总利用率
     */
    public double totalUtil;

    /**
     * 根据前端传递的运行参数配置进行初始化。
     * @param requestInformation 前端传递的运行参数配置。
     */
    public SimpleSystemGenerator(ConfigurationInformation requestInformation) {
        TOTAL_CPU_CORE_NUM = requestInformation.getTotalCPUNum();
        NUMBER_OF_TASK_IN_A_PARTITION = requestInformation.getNumberOfTaskInAPartition();
        NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = requestInformation.getNumberOfMaxAccessToOneResource();
        total_partitions = TOTAL_CPU_CORE_NUM;
        total_tasks = TOTAL_CPU_CORE_NUM * NUMBER_OF_TASK_IN_A_PARTITION;
        
        this.minT = requestInformation.getMinPeriod();
        this.maxT = requestInformation.getMaxPeriod();
        this.resourceSharingFactor = requestInformation.getResourceSharingFactor();
        this.totalUtil = 0.1 * (double) TOTAL_CPU_CORE_NUM * NUMBER_OF_TASK_IN_A_PARTITION;
        this.number_of_max_access = NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE;

        switch (requestInformation.getResourceType()) {
            case "VERY LONG LENGTH":
                this.cs_len_range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
                break;
            case "LONG LENGTH":
                this.cs_len_range = CS_LENGTH_RANGE.LONG_CSLEN;
                break;
            case "MEDIUM LENGTH":
                this.cs_len_range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
                break;
            case "SHORT LENGTH":
                this.cs_len_range = CS_LENGTH_RANGE.SHORT_CS_LEN;
                break;
            case "VERY SHORT LENGTH":
                this.cs_len_range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
                break;
            case "RANDOM":
                this.cs_len_range = CS_LENGTH_RANGE.Random;
                break;
        }

        switch (requestInformation.getResourceNum()) {
            case "HALF PARTITIONS" :
                this.numberOfResource = RESOURCES_RANGE.HALF_PARTITIONS;
                break;
            case "PARTITIONS" :
                this.numberOfResource = RESOURCES_RANGE.PARTITIONS;
                break;
            case "DOUBLE PARTITIONS" :
                this.numberOfResource = RESOURCES_RANGE.DOUBLE_PARTITIONS;
                break;
        }
    }

    /**
     * 随机生成一组任务。
     */
    public ArrayList<BasicPCB> generateTasks() {
        ArrayList<BasicPCB> tasks = null;
        int times = 0;
        while (tasks == null) {
            tasks = generateT();
            if (tasks != null && WorstFitAllocation(tasks, total_partitions) == null)
                tasks = null;

            ++times;
        }

        // choose half the tasks, criticality is high
        Random ran = new Random();
        for (int i = 0; i < tasks.size() / 2; i++) {
            while (true) {
                int idx = ran.nextInt(tasks.size() - 1);
                if (tasks.get(idx).criticality == 0) {
                    tasks.get(idx).criticality = 1;
                    break;
                }
            }
        }


        return tasks;
    }

    /**
     *  使用Worst-Fit-Allocation方案为任务分配对应的处理器核心。
     * @param tasksToAllocate 系统中所有的任务。
     * @param partitions 处理器个数。
     */
    private ArrayList<ArrayList<BasicPCB>> WorstFitAllocation(ArrayList<BasicPCB> tasksToAllocate, int partitions) {
        // clear tasks' partitions
        for (BasicPCB procedureControlBlock : tasksToAllocate) {
            procedureControlBlock.baseRunningCpuCore = -1;
        }

        // Init allocated tasks array
        ArrayList<ArrayList<BasicPCB>> tasks = new ArrayList<>();
        for (int i = 0; i < partitions; i++) {
            ArrayList<BasicPCB> task = new ArrayList<>();
            tasks.add(task);
        }

        // init util array
        // the utilization of each cpu core
        ArrayList<Double> utilPerPartition = new ArrayList<>();
        for (int i = 0; i < partitions; i++) {
            utilPerPartition.add((double) 0);
        }

        // Worst-Fit-Allocation: allocate the task to the minimum utilization of the cpu core
        for (BasicPCB task : tasksToAllocate) {
            int target = -1;
            double minUtil = 2;
            // Find the minimum utilization of the cpu core
            for (int j = 0; j < partitions; j++) {
                if (minUtil > utilPerPartition.get(j)) {
                    minUtil = utilPerPartition.get(j);
                    target = j;
                }
            }

            if (target == -1) {
                System.err.println("WF error!");
                return null;
            }

            // have enough utilization to execute the task
            if ((double) 1 - minUtil >= task.utilization) {
                // allow the task to the cpu core
                task.baseRunningCpuCore = target;
                // update the utilization of the cpu core
                utilPerPartition.set(target, utilPerPartition.get(target) + task.utilization);
            } else
                return null;
        }

        // the CPU core record the task that is allocated to it.
        for (BasicPCB procedureControlBlock : tasksToAllocate) {
            int partition = procedureControlBlock.baseRunningCpuCore;
            tasks.get(partition).add(procedureControlBlock);
        }

        // sorted by the period from smallest to highest
        for (ArrayList<BasicPCB> task : tasks) {
            task.sort(Comparator.comparingDouble(p -> p.period));
        }

        return tasks;
    }

    /**
     * 随机生成任务的最短发布周期间隔。
     */
    private ArrayList<BasicPCB> generateT() {
        int task_id = 0;
        ArrayList<BasicPCB> tasks = new ArrayList<>(total_tasks);
        ArrayList<Integer> periods = new ArrayList<>(total_tasks);
        Random random = new Random();

        /* generates random periods */
        /* generates period for each task*/
        do {
            // using a log-uniform function
            double a1 = Math.log(minT);
            double a2 = Math.log(maxT + 1);
            double scaled = random.nextDouble() * (a2 - a1);
            double shifted = scaled + a1;
            double exp = Math.exp(shifted);

            int period = (int) exp;
            period = Math.max(minT, period);
            period = Math.min(maxT, period);

            if (!periods.contains(period))
                periods.add(period);

        } while (periods.size() < total_tasks);

        // sorted from smallest to largest
        periods.sort(Comparator.comparingDouble(p -> p));

        /* Using UUnifast-Discard Algorithm to generate utilization for each task*/
        /* generate utilization for each task and the sum of all utilization is less or equal to totalUtil */
        UUnifastDiscard unifastDiscard = new UUnifastDiscard(totalUtil, total_tasks, 1000);
        ArrayList<Double> utils;
        while (true) {
            utils = unifastDiscard.getUtils();

            double tt = 0;
            for (Double util : utils) {
                tt += util;
            }

            if (utils.size() == total_tasks && tt <= totalUtil)
                break;
        }

        /* generate sporadic tasks */
        for (int i = 0; i < utils.size(); i++) {
            long computation_time = (int) (periods.get(i) * utils.get(i));
            if (computation_time == 0) {
                return null;
            }
            BasicPCB t = new BasicPCB(-1, periods.get(i), utils.get(i), task_id);
            task_id++;
            tasks.add(t);
        }

        // sorted by utilization rate from smallest to largest
        tasks.sort((p1, p2) -> -Double.compare(p1.utilization, p2.utilization));
        return tasks;
    }

    /**
     * 随机生成资源。
     */
    public ArrayList<BasicResource> generateResources() {
        /* generate resources from partitions/2 to partitions*2 */
        Random ran = new Random();
        int number_of_resources = 0;

        // range: number of resource
        switch (numberOfResource) {
            case PARTITIONS:
                number_of_resources = total_partitions;
                break;
            case HALF_PARTITIONS:
                number_of_resources = total_partitions / 2;
                break;
            case DOUBLE_PARTITIONS:
                number_of_resources = total_partitions * 2;
                break;
            default:
                break;
        }

        ArrayList<BasicResource> resources = new ArrayList<>(number_of_resources);

        // generate the critical section length for every resource.
        for (int i = 0; i < number_of_resources; i++) {
            int cs_len = 0;
            if (csl == -1) {
                switch (cs_len_range) {
                    case VERY_LONG_CSLEN:
                        cs_len = ran.nextInt(300 - 200) + 201;
                        break;
                    case LONG_CSLEN:
                        cs_len = ran.nextInt(200 - 100) + 101;
                        break;
                    case MEDIUM_CS_LEN:
                        cs_len = ran.nextInt(100 - 50) + 51;
                        break;
                    case SHORT_CS_LEN:
                        cs_len = ran.nextInt(50 - 15) + 16;
                        break;
                    case VERY_SHORT_CS_LEN:
                        cs_len = ran.nextInt(4) + 1;
                        break;
                    case Random:
                        cs_len = ran.nextInt(300) + 1;
                    default:
                        break;
                }
            } else
                cs_len = csl;

            // c_high = c_low * [1~1.5]
            int cs_len_high = (int) ((1 + ran.nextFloat() / 2) * cs_len);

            BasicResource resource = new BasicResource(i + 1, cs_len, cs_len_high);
            resources.add(resource);
        }

        // sorted by the critical section length from largest to smallest
        resources.sort((r2, r1) -> Long.compare(r1.c_low, r2.c_low));

        // reset the index of the resource
        for (int i = 0; i < resources.size(); i++) {
            BasicResource res = resources.get(i);
            res.id = i;
        }

        return resources;
    }

    /**
     * 生成任务使用资源的情况。
     * @param tasks 系统中的任务。
     * @param resources 系统中的资源。
     */
    // resources: the resources with theirs corresponding critical section length
    public HashMap<Integer, ArrayList<Integer>> generateResourceUsage(ArrayList<BasicPCB> tasks, ArrayList<BasicResource> resources) {
        while (tasks == null)
            tasks = generateTasks();

        Random ran = new Random();

        // rsf:resource sharing factor
        // the number of tasks that will request resources.
        long number_of_resource_requested_tasks = Math.round(resourceSharingFactor * tasks.size());

        /* Generate resource usage and check reasonable whether */
        for (long l = 0; l < number_of_resource_requested_tasks; l++) {
            /* random select a task that doesn't require resource */
            int task_index = ran.nextInt(tasks.size());

            while (!tasks.get(task_index).accessResourceIndex.isEmpty()) {
                task_index = ran.nextInt(tasks.size());
            }

            // Find a task that doesn't require resource
            BasicPCB task = tasks.get(task_index);
            int startTime = 1;
            int endTime = task.totalNeededTime;

            // Initialize an array to record the times that a resource has been allocated to a task.
            int[] allocateTimes = new int[resources.size()];

            // First, generate the times that we try to allocate the resource in total.
            int tryToAllocateResourceTimes = (ran.nextInt(resources.size()) + 1) * 2;
            int resource_index;

            // get a resource to allocate.
            for (int j = 0; j < tryToAllocateResourceTimes; ++j) {
                if (number_of_max_access > 0) {
                    int whileLoopTimes = 0;
                    // get a resource to allocate.
                    do {
                        // random select a resource to access, satisfy: 1. allocateTimes[resource_index] <= max_access_time
                        resource_index = ran.nextInt(resources.size());
                        ++whileLoopTimes;
                    } while (allocateTimes[resource_index] >= number_of_max_access && whileLoopTimes <=  resources.size());

                    if (whileLoopTimes > resources.size())
                        break;

                    // check the remaining computation time is larger than critical section length
                    // recourse use c_low to compute accessResourceIndex no matter what criticality the task is,
                    if ((endTime - startTime) > resources.get(resource_index).c_low + 1) {
                        // we can allocate the resource.
                        int resourceAccessTime = ran.nextInt(endTime - startTime - resources.get(resource_index).c_low) + startTime;
                        task.accessResourceIndex.add(resource_index);
                        task.resourceAccessTime.add(resourceAccessTime);
                        // the time that next resource will use.
                        startTime = resourceAccessTime + resources.get(resource_index).c_low;
                        allocateTimes[resource_index] += 1;
                    }
                }
            }

        }

        /* Allocate the task to cpu core according to worst-fit-allocation */
        ArrayList<ArrayList<BasicPCB>> generatedTaskSets = WorstFitAllocation(tasks, total_partitions);

        if (generatedTaskSets == null) {
            System.err.print("Can't Allocate task to CPU!");
            System.exit(-1);
        }

        // set the task's running core, but in WortFitAllocation, this procedure has been done.
        for (int i = 0; i < generatedTaskSets.size(); i++) {
            for (int j = 0; j < generatedTaskSets.get(i).size(); j++) {
                generatedTaskSets.get(i).get(j).baseRunningCpuCore = i;
            }
        }

        // generate priority for every task
        // object passed by reference
        new PriorityGenerator().assignPrioritiesByDM(generatedTaskSets);

        // generate WCCT for every task
        for (ArrayList<BasicPCB> taskSet : generatedTaskSets) {
            for (BasicPCB oneTask : taskSet) {

                double Ui = oneTask.utilization;
                int Ti = oneTask.period;
                int sigma = 0;
                for (int k = 0; k < oneTask.accessResourceIndex.size(); ++k) {
                    BasicResource rk = resources.get(oneTask.accessResourceIndex.get(k));
                    sigma = sigma + rk.c_high;
                }

                int WCCT = (int) (Ui * Ti - sigma);
                if (WCCT < 0)
                    WCCT = 0;
                oneTask.WCCT_low = WCCT;
                oneTask.WCCT_high = (int) ((1 + ran.nextFloat() / 2) * WCCT);
            }
        }


        if (resources != null && !resources.isEmpty()) {
            // initialize the resource usage
            for (BasicResource res : resources) {
                res.isGlobal = false;
                res.ceiling.clear();
            }

            /* for each resource */
            /* resource 在每一个 CPU 核 上都对应的一个 priority. */
            for (BasicResource resource : resources) {
                /* for a given resource, traversing all the tasks and record the information */
                /* for each partition */
                resource.isGlobal = true;

                for (ArrayList<BasicPCB> generatedTaskSet : generatedTaskSets) {
                    int ceiling = 0;

                    /* for each task in the given partition */
                    for (BasicPCB task : generatedTaskSet) {
                        // set the request task and its cpu core
                        if (task.accessResourceIndex.contains(resource.id)) {
                            ceiling = Math.max(task.basePriority, ceiling);
                        }
                    }

                    // Priority Ceiling Protocol: get the resource ceiling that indicates the highest base priority among all the tasks
                    // that require this resource.
                    // record all the partition's highest ceiling
                    resource.ceiling.add(ceiling);
                    if (ceiling == 0)
                        resource.isGlobal = false;
                }
            }
        } else {
            System.err.print("ERROR at resource usage, taskset is NULL!");
            System.exit(-1);
        }

        return generateResourceRequiredPriorities(tasks, resources);
    }

    /**
     * 用于动态资源共享协议：随机生成每个任务自旋等待全局资源时的优先级提升
     */
    public HashMap<Integer, ArrayList<Integer>> generateResourceRequiredPriorities(ArrayList<BasicPCB> tasks, ArrayList<BasicResource> resources) {
        HashMap<Integer, ArrayList<Integer>> resourceRequiredPrioritiesArray = new HashMap<>();
        int maxPriority = MIN_PRIORITY + tasks.size();
        for (BasicPCB tmpTask : tasks) {
            ArrayList<Integer> resourceRequiredPriorities = new ArrayList<>();
            for (Integer resourceRequiredIndex : tmpTask.accessResourceIndex) {
                if (resources.get(resourceRequiredIndex).isGlobal) {
                    resourceRequiredPriorities.add(tmpTask.basePriority + (int)(Math.random() * (maxPriority - tmpTask.basePriority)));
                }else {
                    resourceRequiredPriorities.add(-1);
                }
            }
            resourceRequiredPrioritiesArray.put(tmpTask.staticTaskId, resourceRequiredPriorities);
        }
        return resourceRequiredPrioritiesArray;
    }


    /**
     * 随机生成任务的发布时间。
     * @param totalTasks 系统中的任务。
     */
    public TreeMap<Integer, ArrayList<Integer>> generateTaskReleaseTime(ArrayList<BasicPCB> totalTasks) {
        Random random = new Random();

        int longestDeadline = 0;
        int systemClock = 1;

        ArrayList<Boolean> isReleased = new ArrayList<>(totalTasks.size());
        ArrayList<Integer> timeSinceLastRelease = new ArrayList<>(totalTasks.size());
        TreeMap<Integer, ArrayList<Integer>> taskReleaseTimes = new TreeMap<>();

        for (BasicPCB task : totalTasks) {
            timeSinceLastRelease.add(task.period);
            isReleased.add(false);
        }

        do {
            ArrayList<Integer> releaseTasks = new ArrayList<>();

            for (int i = 0; i < totalTasks.size(); ++i) {
                // Even if the time elapsed since the last release is greater than the period, it still has a probability of being released.
                if (timeSinceLastRelease.get(i) >= totalTasks.get(i).period && random.nextDouble() < 0.2) {
                    // timeSinceLastRelease 对应任务项设置为0；重新开始记录时间
                    timeSinceLastRelease.set(i, 0);

                    // 如果该任务是第一次发布，需要更新 longestDeadline
                    if (!isReleased.get(i))
                        longestDeadline = Math.max(longestDeadline, totalTasks.get(i).period + systemClock);

                    isReleased.set(i, true);
                    releaseTasks.add(i);
                } else
                    timeSinceLastRelease.set(i, timeSinceLastRelease.get(i) + 1);
            }

            if (!releaseTasks.isEmpty())
                taskReleaseTimes.put(systemClock, releaseTasks);

            ++systemClock;
            // 只要有一个任务还没有完成发布 或者 当前系统时间超出了 每一个任务都完成一次的时间
        } while (isReleased.contains(false) || systemClock > longestDeadline);

        return taskReleaseTimes;
    }

}