package com.example.serveside.service.msrp.generatorTools;

import com.example.serveside.service.msrp.entity.MixedCriticalSystem.CS_LENGTH_RANGE;
import com.example.serveside.service.msrp.entity.MixedCriticalSystem.RESOURCES_RANGE;
import com.example.serveside.service.msrp.entity.ProcedureControlBlock;
import com.example.serveside.service.msrp.entity.Resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class SimpleSystemGenerator {

    public CS_LENGTH_RANGE cs_len_range;
    int csl = -1;

    public int maxT;
    public int minT;

    public int number_of_max_access;
    public RESOURCES_RANGE numberOfResource;
    public double resourceSharingFactor;

    public int total_tasks;
    public int total_partitions;
    public double totalUtil;

    // minT: MIN_PERIOD
    // maxT: MAX_PERIOD
    // total_partitions: the total num of the resources
    // rsf: resource sharing factor
    public SimpleSystemGenerator(int minT, int maxT, int total_partitions, int totalTasks, CS_LENGTH_RANGE cs_len_range,
                                 RESOURCES_RANGE numberOfResources, double rsf, int number_of_max_access) {
        this.minT = minT;
        this.maxT = maxT;
        this.totalUtil = 0.2 * (double) totalTasks;
        this.total_partitions = total_partitions;
        this.total_tasks = totalTasks;
        this.cs_len_range = cs_len_range;
        this.numberOfResource = numberOfResources;
        this.resourceSharingFactor = rsf;
        this.number_of_max_access = number_of_max_access;
    }

    /*
     * generate task sets for multiprocessor fully partitioned fixed-priority
     * system
     */
    public ArrayList<ProcedureControlBlock> generateTasks() {
        ArrayList<ProcedureControlBlock> tasks = null;
        while (tasks == null) {
            tasks = generateT();
            if (tasks != null && WorstFitAllocation(tasks, total_partitions) == null)
                tasks = null;
        }

        // choose half the tasks, criticality is high
        Random ran = new Random();
        for (int i = 0; i < tasks.size() / 2; i++) {
            while(true){
                int idx = ran.nextInt(tasks.size()-1);
                if(tasks.get(idx).criticality == 0){
                    tasks.get(idx).criticality = 1;
                    break;
                }
            }
        }

        return tasks;
    }

    /* Wort-Fit-Allocation: allocate tasks to cpu core */
    private ArrayList<ArrayList<ProcedureControlBlock>> WorstFitAllocation(ArrayList<ProcedureControlBlock> tasksToAllocate, int partitions) {
        // clear tasks' partitions
        for (ProcedureControlBlock procedureControlBlock : tasksToAllocate) {
            procedureControlBlock.runningCpuCore = -1;
        }

        // Init allocated tasks array
        ArrayList<ArrayList<ProcedureControlBlock>> tasks = new ArrayList<>();
        for (int i = 0; i < partitions; i++) {
            ArrayList<ProcedureControlBlock> task = new ArrayList<>();
            tasks.add(task);
        }

        // init util array
        // the utilization of each cpu core
        ArrayList<Double> utilPerPartition = new ArrayList<>();
        for (int i = 0; i < partitions; i++) {
            utilPerPartition.add((double) 0);
        }

        // Worst-Fit-Allocation: allocate the task to the minimum utilization of the cpu core
        for (ProcedureControlBlock task : tasksToAllocate) {
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
                task.runningCpuCore = target;
                // update the utilization of the cpu core
                utilPerPartition.set(target, utilPerPartition.get(target) + task.utilization);
            } else
                return null;
        }

        // the CPU core record the task that is allocated to it.
        for (ProcedureControlBlock procedureControlBlock : tasksToAllocate) {
            int partition = procedureControlBlock.runningCpuCore;
            tasks.get(partition).add(procedureControlBlock);
        }

        // sorted by the period from smallest to highest
        for (ArrayList<ProcedureControlBlock> task : tasks) {
            task.sort(Comparator.comparingDouble(p -> p.period));
        }

        return tasks;
    }

    private ArrayList<ProcedureControlBlock> generateT() {
        int task_id = 0;
        ArrayList<ProcedureControlBlock> tasks = new ArrayList<>(total_tasks);
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
            ProcedureControlBlock t = new ProcedureControlBlock(-1, periods.get(i), utils.get(i), task_id);
            task_id++;
            tasks.add(t);
        }

        // sorted by utilization rate from smallest to largest
        tasks.sort((p1, p2) -> -Double.compare(p1.utilization, p2.utilization));
        return tasks;
    }

    /*
     * Generate a set of resources.
     */
    public ArrayList<Resource> generateResources() {
        /* generate resources from partitions/2 to partitions*2 */
        Random ran = new Random();
        int number_of_resources = 0;

        // range: number of resource
        switch (numberOfResource) {
            case PARTITIONS:
                number_of_resources = total_partitions;
                break;
            case HALF_PARITIONS:
                number_of_resources = total_partitions / 2;
                break;
            case DOUBLE_PARTITIONS:
                number_of_resources = total_partitions * 2;
                break;
            default:
                break;
        }

        ArrayList<Resource> resources = new ArrayList<>(number_of_resources);

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
            int cs_len_high = (int) ( (1 + ran.nextFloat() / 2)  * cs_len);

            Resource resource = new Resource(i + 1, cs_len, cs_len_high);
            resources.add(resource);
        }

        // sorted by the critical section length from largest to smallest
        resources.sort((r2, r1) -> Long.compare(r1.c_low, r2.c_low));

        // reset the index of the resource
        for (int i = 0; i < resources.size(); i++) {
            Resource res = resources.get(i);
            res.id = i;
        }

        return resources;
    }

    // resources: the resources with theirs corresponding critical section length
    public ArrayList<ArrayList<ProcedureControlBlock>> generateResourceUsage(ArrayList<ProcedureControlBlock> tasks, ArrayList<Resource> resources) {
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
            ProcedureControlBlock task = tasks.get(task_index);
            int startTime = 1;
            int endTime = task.totalNeededTime;

            // Initialize an array to record the times that a resource has been allocated to a task.
            int[] allocateTimes = new int[resources.size()];

            // First, generate the times that we try to allocate the resource in total.
            int tryToAllocateResourceTimes = (ran.nextInt(resources.size()) + 1) * 2;
            int resource_index;

            // get a resource to allocate.
            for (int j = 0; j < tryToAllocateResourceTimes; ++j) {

                // get a resource to allocate.
                do {
                    // random select a resource to access, satisfy: 1. allocateTimes[resource_index] <= max_access_time
                    resource_index = ran.nextInt(resources.size());
                } while (allocateTimes[resource_index] >= number_of_max_access);


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

        /* Allocate the task to cpu core according to worst-fit-allocation */
        ArrayList<ArrayList<ProcedureControlBlock>> generatedTaskSets = WorstFitAllocation(tasks, total_partitions);

        if (generatedTaskSets == null)
        {
            System.err.print("Can't Allocate task to CPU!");
            System.exit(-1);
        }

        // set the task's running core, but in WortFitAllocation, this procedure has been done.
        for (int i = 0; i < generatedTaskSets.size(); i++) {
            for (int j = 0; j < generatedTaskSets.get(i).size(); j++) {
                generatedTaskSets.get(i).get(j).runningCpuCore = i;
            }
        }

        // generate priority for every task
        // object passed by reference
        new PriorityGenerator().assignPrioritiesByDM(generatedTaskSets);

        // generate WCCT for every task
        for (ArrayList<ProcedureControlBlock> taskSet : generatedTaskSets) {
            for (ProcedureControlBlock oneTask : taskSet) {

                double Ui = oneTask.utilization;
                int Ti = oneTask.period;
                int sigma = 0;
                for (int k = 0; k < oneTask.accessResourceIndex.size(); ++k) {
                    Resource rk = resources.get(oneTask.accessResourceIndex.get(k));
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
            for (Resource res : resources) {
                res.isGlobal = false;
                res.partitions.clear();
                res.requested_tasks.clear();
                res.ceiling.clear();
            }

            /* for each resource */
            for (Resource resource : resources) {
                /* for a given resource, traversing all the tasks and record the information */
                /* for each partition */
                for (ArrayList<ProcedureControlBlock> generatedTaskSet : generatedTaskSets)
                {
                    int ceiling = 0;

                    /* for each task in the given partition */
                    for (ProcedureControlBlock task : generatedTaskSet)
                    {
                        // set the request task and its cpu core
                        if (task.accessResourceIndex.contains(resource.id)) {
                            resource.requested_tasks.add(task);
                            ceiling = Math.max(task.priorities.peek(), ceiling);
                            if (!resource.partitions.contains(task.runningCpuCore)) {
                                resource.partitions.add(task.runningCpuCore);
                            }
                        }
                    }

                    // Priority Ceiling Protocol: get the resource ceiling that indicates the highest base priority among all the tasks
                    // that require this resource.
                    // record all the partition's highest ceiling
                    resource.ceiling.add(ceiling);
                }

                // more than one cpu core access this resource
                if (resource.partitions.size() > 1)
                    resource.isGlobal = true;
            }
        }
        else {
            System.err.print("ERROR at resource usage, taskset is NULL!");
            System.exit(-1);
        }

        return generatedTaskSets;
    }

}
