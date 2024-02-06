package sysu.rtsg;

import com.google.gson.Gson;
import sysu.rtsg.analysis.ResponseTimeAnalysisWithVariousPriorities;
import sysu.rtsg.analysis.ResponseTimeAnalysisWithVariousPriorities_B_S;
import sysu.rtsg.analysis.ResponseTimeAnalysisWithVariousPriorities_S_B;
import sysu.rtsg.analysis.ResponseTimeAnalysisWithVariousPriorities_S_B_Worst;
import sysu.rtsg.entity.Resource;
import sysu.rtsg.entity.SporadicTask;
import sysu.rtsg.generatorTools.AllocationGeneator;
import sysu.rtsg.generatorTools.PriorityGeneator;
import sysu.rtsg.generatorTools.SystemGenerator;
import sysu.rtsg.utils.GeneatorUtils.CS_LENGTH_RANGE;
import sysu.rtsg.utils.GeneatorUtils.RESOURCES_RANGE;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExperimentMain {

    public static int MAX_PERIOD = 1000;
    public static int MIN_PERIOD = 1;
    public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
    public static int TOTAL_PARTITIONS = 8;
    static int NUMBER_OF_TASKS_ON_EACH_PARTITION = 5;
    static final CS_LENGTH_RANGE range = CS_LENGTH_RANGE.RANDOM;
    static final double RSF = 0.4;
    static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 6;

    // 粗粒度调整随机的代数
    static int RANDOM_NUM = 500;

    // GA方法的population和generation
    static int POPULATION = 500;
    static int GENERATIONS = 10;

    // RL方法的Step
    static int RL_DQN_STEP = 1000;
    static int RL_AC_STEP = 1000;

    // 多线程计数类
    public static class Counter {
        int MSRP_No = 0, PWLP_No = 0, Static_No = 0, Static_FineGrained_Pure_No = 0, Static_FineGrained_Greedy_No = 0;
        int Random_CoarseGrained_No = 0, Random_FineGrained_No = 0;
        int GA_CoarseGrained_No = 0, GA_FineGrained_No = 0;
        int RL_DQN_CoarseGrained_No = 0, RL_DQN_FineGrained_No = 0;
        int RL_AC_CoarseGrained_No = 0, RL_AC_FineGrained_No = 0;
        public synchronized void incMSRP_No() {
            MSRP_No++;
        }

        public synchronized void incPWLP_No() {
            PWLP_No++;
        }

        public synchronized void incStatic_No() {
            Static_No++;
        }

        public synchronized void incStatic_FineGrained_Pure_No() {
            Static_FineGrained_Pure_No++;
        }

        public synchronized void incStatic_FineGrained_Greedy_No() {
            Static_FineGrained_Greedy_No++;
        }

        public synchronized void incRandom_CoarseGrained_No() {
            Random_CoarseGrained_No++;
        }

        public synchronized void incRandom_FineGrained_No() {
            Random_FineGrained_No++;
        }

        public synchronized void incGA_CoarseGrained_No() {
            GA_CoarseGrained_No++;
        }

        public synchronized void incGA_FineGrained_No() {
            GA_FineGrained_No++;
        }

        public synchronized void incRL_DQN_CoarseGrained_No() {
            RL_DQN_CoarseGrained_No++;
        }

        public synchronized void incRL_DQN_FineGrained_No() {
            RL_DQN_FineGrained_No++;
        }

        public synchronized void incRL_AC_CoarseGrained_No() {
            RL_AC_CoarseGrained_No++;
        }

        public synchronized void incRL_AC_FineGrained_No() {
            RL_AC_FineGrained_No++;
        }

        public synchronized void initResults() {
            MSRP_No = 0; PWLP_No = 0; Static_No = 0;
            Static_FineGrained_Pure_No = 0; Static_FineGrained_Greedy_No = 0;
            Random_CoarseGrained_No = 0; Random_FineGrained_No = 0;
            GA_CoarseGrained_No = 0; GA_FineGrained_No = 0;
            RL_DQN_CoarseGrained_No = 0; RL_DQN_FineGrained_No = 0;
            RL_AC_CoarseGrained_No = 0; RL_AC_FineGrained_No = 0;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // ExperimentIncreasingCriticalSectionLength(6);
        ExperimentMain test = new ExperimentMain();

        // 线程池建立
        ExecutorService executor = Executors.newFixedThreadPool(100);

        // 串行测试
//         Counter counter = new Counter();
//         counter.initResults();
//         test.ExperimentIncreasingCriticalSectionLengthExecutor(6,counter);

//        test.ExperimentIncreasingCriticalSectionLength(6);

//        final CountDownLatch cslendownLatch = new CountDownLatch(6);
//        for (int i = 1; i < 7; i++) { // 1 2 3 4 5 6
//            final int count = i;
//            new Thread(() -> {
//                Counter counter = new Counter();
//                counter.initResults();
//                test.parallelExperimentIncreasingCriticalSectionLengthExecutor(count, counter, executor);
//                cslendownLatch.countDown();
//            }).start();
//        }
//        cslendownLatch.await();

//                Counter counter = new Counter();
//                counter.initResults();
//        test.parallelExperimentIncreasingCriticalSectionLengthExecutor(6, counter, executor);

//        final CountDownLatch tasksdownLatch = new CountDownLatch(1);
//        for (int i = 7; i < 8; i++) { // 1 2 3 4 5 6 7 8 9
//            final int count = i;
//             new Thread(() -> {
//                 Counter counter = new Counter();
//                 counter.initResults();
//                 test.parallelExperimentIncreasingWorkloadExecutor(count, counter, executor);
//                 tasksdownLatch.countDown();
//             }).start();
//         }
//         tasksdownLatch.await();

//         final CountDownLatch accessdownLatch = new CountDownLatch(9);
//         for (int i = 1; i < 42; i = i + 5) { // 1 6 11 16 21 26 31 36 41
//             final int count = i;
//             new Thread(() -> {
//                 Counter counter = new Counter();
//                 counter.initResults();
//                 test.parallelExperimentIncreasingAccessExecutor(count, counter, executor);
//                 accessdownLatch.countDown();
//             }).start();
//         }
//         accessdownLatch.await();
//
        final CountDownLatch processordownLatch = new CountDownLatch(10);
        for (int i = 4; i < 17; i = i + 2) { // 4 6 8 10 12 14 16 - 18 20 22
            final int count = i;
            new Thread(() -> {
                Counter counter = new Counter();
                counter.initResults();
                test.parallelExperimentIncreasingPartitionsExecutor(count, counter, executor);
                processordownLatch.countDown();
            }).start();
        }
        processordownLatch.await();
//
//        final CountDownLatch rsfdownLatch = new CountDownLatch(5);
//        for (int i = 1; i < 6; i++) { // 1 2 3 4 5
//            final int count = i;
//            new Thread(() -> {
//                Counter counter = new Counter();
//                counter.initResults();
//                test.parallelExperimentIncreasingRsfExecutor(count, counter, executor);
//                rsfdownLatch.countDown();
//            }).start();
//        }
//        rsfdownLatch.await();

        executor.shutdown();
    }

//    public void parallelExperimentIncreasingCriticalSectionLengthExecutor(int cslen, Counter counter, ExecutorService executor) {
//
//        final CountDownLatch downLatch = new CountDownLatch(TOTAL_NUMBER_OF_SYSTEMS);
//
//        final CS_LENGTH_RANGE cs_range = switch (cslen) {
//            case 1 -> CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
//            case 2 -> CS_LENGTH_RANGE.SHORT_CS_LEN;
//            case 3 -> CS_LENGTH_RANGE.MEDIUM_CS_LEN;
//            case 4 -> CS_LENGTH_RANGE.LONG_CSLEN;
//            case 5 -> CS_LENGTH_RANGE.VERY_LONG_CSLEN;
//            case 6 -> CS_LENGTH_RANGE.RANDOM;
//            default -> null;
//        };
//
//
////        ArrayList<Double> blocking_improve = new ArrayList<>();
//
//        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
//            int finalI = i;
//            Thread worker = new Thread(() -> {
//                SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS,
//                        TOTAL_PARTITIONS * NUMBER_OF_TASKS_ON_EACH_PARTITION, RSF, cs_range, RESOURCES_RANGE.PARTITIONS,
//                        NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);
//                ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
//                ArrayList<Resource> resources = generator.generateResources();
//                generator.generateResourceUsage(tasksToAlloc, resources);
//                // 任务核心分配
//                ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
//                // 任务优先级分配
//                tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);
//
//                /* Lazy Mode */
//                boolean[] LazyModeResult = performLazyMode(tasks,resources,counter,finalI);
//                /* Lazy Mode 至此结束 */
//
//                // Lazy Mode未有可调度的解
//                if(!LazyModeResult[0] && !LazyModeResult[1]){
//
//                    /* Method 1 —— 随机200代，作为baseline */
//                    RandomMethod(tasks, resources, counter, finalI, RANDOM_NUM);
//
//                    /* Method 2 —— GA **/
////                    GAMethod(tasks, resources, counter, finalI, POPULATION, GENERATIONS,String.valueOf(finalI));
//
////                    /* Method 3 —— 强化学习-DQN **/
////                    WriteToFile(tasks, resources, cslen, finalI);
////                    RL_DQNMethod(tasks, resources, counter, cslen, finalI, RL_DQN_STEP);
////
////                    /* Method 4 —— 强化学习-Actor-Critic */
////                    RL_ACMethod(tasks, resources, counter, cslen, finalI, RL_AC_STEP);
//
//                    /* Method 5 —— 粗粒度调整启发式算法 */
//                    // TODO 接入
//
//                }
//
//                System.out.println(Thread.currentThread().getName() + " Finished");
//
//                String directoryPath = "aaa"; // 你的目录路径
//                String fileName = directoryPath + File.separator + "Thread_" + finalI + ".txt";
//
//                // 创建文件内容
//                String fileContent = "线程 " + finalI + " 执行完成";
//
//                // 将内容写入文件
//                try {
//                    Files.write(Paths.get(fileName), fileContent.getBytes(StandardCharsets.UTF_8));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                downLatch.countDown();
//            });
//            worker.setName("2 " + cslen + " " + i);
//            worker.start();
//        }
//        // Python脚本要修改
//        try {
//            downLatch.await();
//        } catch (InterruptedException ignored) {}
//        writeSystem("2 2 " + cslen, generateResults("CS_LEN = " + cslen + " Results Summary:\n",counter));
////        double avg_val = calculateAverage(blocking_improve);
////
////        double max_val = Collections.max(blocking_improve);
////
////        double min_val = Collections.min(blocking_improve);
////
////        System.out.println("MAX_IMPROVE_VAL: " + max_val);
////        System.out.println("MIN_IMPROVE_VAL: " + min_val);
////        System.out.println("AVG_IMPROVE_VAL: " + avg_val);
//    }

    public void parallelExperimentIncreasingWorkloadExecutor(int NoT, Counter counter, ExecutorService executor) {

        final CountDownLatch downLatch = new CountDownLatch(TOTAL_NUMBER_OF_SYSTEMS);

        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
            int finalI = i;
            Thread worker = new Thread(() -> {
                SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS, TOTAL_PARTITIONS * NoT, RSF, range,
                        RESOURCES_RANGE.PARTITIONS, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);
                ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
                ArrayList<Resource> resources = generator.generateResources();
                generator.generateResourceUsage(tasksToAlloc, resources);
                // 任务核心分配
                ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
                // 任务优先级分配
                tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

                /* Lazy Mode */
                boolean[] LazyModeResult = performLazyMode(tasks,resources,counter,finalI);
                /* Lazy Mode 至此结束 */

                // Lazy Mode未有可调度的解
                if(!LazyModeResult[0] && !LazyModeResult[1]){

                    /* Method 1 —— 随机200代，作为baseline */
                    RandomMethod(tasks, resources, counter, finalI, RANDOM_NUM);

//                    /* Method 2 —— GA **/
//                    GAMethod(tasks, resources, counter, finalI, POPULATION, GENERATIONS,String.valueOf(finalI));
//
//                    /* Method 3 —— 强化学习-DQN **/
//                    WriteToFile(tasks, resources, NoT, finalI);
//                    RL_DQNMethod(tasks, resources, counter, NoT, finalI, RL_DQN_STEP);
//
//                    /* Method 4 —— 强化学习-Actor-Critic */
//                    RL_ACMethod(tasks, resources, counter, NoT, finalI, RL_AC_STEP);

                    /* Method 5 —— 粗粒度调整启发式算法 */
                    // TODO 接入
                }
                System.out.println(Thread.currentThread().getName() + " Finished");
                downLatch.countDown();
            });
            worker.setName("1 " + NoT + " " + i);
            worker.start();
        }

        try {
            downLatch.await();
        } catch (InterruptedException ignored) {}

        writeSystem("1 2 " + NoT, generateResults("TASK = " + NoT + " Results Summary:\n",counter));
    }

    public void parallelExperimentIncreasingAccessExecutor(int NoA, Counter counter, ExecutorService executor) {
        final CountDownLatch downLatch = new CountDownLatch(TOTAL_NUMBER_OF_SYSTEMS);

        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
            int finalI = i;
            Thread worker = new Thread(() -> {
                SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS,
                        TOTAL_PARTITIONS * NUMBER_OF_TASKS_ON_EACH_PARTITION, RSF, range, RESOURCES_RANGE.PARTITIONS, NoA, false);
                ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
                ArrayList<Resource> resources = generator.generateResources();
                generator.generateResourceUsage(tasksToAlloc, resources);
                // 任务核心分配
                ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
                // 任务优先级分配
                tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

                /* Lazy Mode */
                boolean[] LazyModeResult = performLazyMode(tasks,resources,counter,finalI);
                /* Lazy Mode 至此结束 */

                // Lazy Mode未有可调度的解
                if(!LazyModeResult[0] && !LazyModeResult[1]){

                    /* Method 1 —— 随机200代，作为baseline */
                    RandomMethod(tasks, resources, counter, finalI, RANDOM_NUM);

//                    /* Method 2 —— GA **/
//                    GAMethod(tasks, resources, counter, finalI, POPULATION, GENERATIONS,String.valueOf(finalI));
//
//                    /* Method 3 —— 强化学习-DQN **/
//                    WriteToFile(tasks, resources, NoA, finalI);
//                    RL_DQNMethod(tasks, resources, counter, NoA, finalI, RL_DQN_STEP);
//
//                    /* Method 4 —— 强化学习-Actor-Critic */
//                    RL_ACMethod(tasks, resources, counter, NoA, finalI, RL_AC_STEP);

                    /* Method 5 —— 粗粒度调整启发式算法 */
                    // TODO 接入
                }
                System.out.println(Thread.currentThread().getName() + " Finished");
                downLatch.countDown();
            });
            worker.setName("3 " + NoA + " " + i);
            worker.start();
        }

        try {
            downLatch.await();
        } catch (InterruptedException ignored) {}

        writeSystem("3 2 " + NoA, generateResults("Access = " + NoA + " Results Summary:\n",counter));

    }

    public void parallelExperimentIncreasingPartitionsExecutor(int NoP, Counter counter, ExecutorService executor) {
        final CountDownLatch downLatch = new CountDownLatch(TOTAL_NUMBER_OF_SYSTEMS);

        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
            int finalI = i;
            Thread worker = new Thread(() -> {

                SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, NoP, NoP * NUMBER_OF_TASKS_ON_EACH_PARTITION, RSF, range,
                        RESOURCES_RANGE.PARTITIONS, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);
                ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
                ArrayList<Resource> resources = generator.generateResources();
                generator.generateResourceUsage(tasksToAlloc, resources);
                // 任务核心分配
                ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
                // 任务优先级分配
                tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

                /* Lazy Mode */
                boolean[] LazyModeResult = performLazyMode(tasks,resources,counter,finalI);
                /* Lazy Mode 至此结束 */

                // Lazy Mode未有可调度的解
                if(!LazyModeResult[0] && !LazyModeResult[1]){

                    /* Method 1 —— 随机200代，作为baseline */
                    RandomMethod(tasks, resources, counter, finalI, RANDOM_NUM);

//                    /* Method 2 —— GA **/
//                    GAMethod(tasks, resources, counter, finalI, POPULATION, GENERATIONS ,String.valueOf(finalI));
//
//                    /* Method 3 —— 强化学习-DQN **/
//                    WriteToFile(tasks, resources, NoP, finalI);
//                    RL_DQNMethod(tasks, resources, counter, NoP, finalI, RL_DQN_STEP);
//
//                    /* Method 4 —— 强化学习-Actor-Critic */
//                    RL_ACMethod(tasks, resources, counter, NoP, finalI, RL_AC_STEP);

                    /* Method 5 —— 粗粒度调整启发式算法 */
                    // TODO 接入
                }
                System.out.println(Thread.currentThread().getName() + " Finished");
                downLatch.countDown();
            });
            worker.setName("4 " + NoP + " " + i);
            worker.start();
        }

        try {
            downLatch.await();
        } catch (InterruptedException ignored) {}

        writeSystem("4 2 " + NoP, generateResults("Processor = " + NoP + " Results Summary:\n",counter));

    }

//    public void parallelExperimentIncreasingRsfExecutor(int resourceSharingFactor, Counter counter, ExecutorService executor) {
//
//        final CountDownLatch downLatch = new CountDownLatch(TOTAL_NUMBER_OF_SYSTEMS);
//
//        double rsf = switch (resourceSharingFactor) {
//            case 1 -> 0.2;
//            case 2 -> 0.3;
//            case 3 -> 0.4;
//            case 4 -> 0.5;
//            case 5 -> 0.6;
//            default -> 0;
//        };
//
//        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
//            int finalI = i;
//            Thread worker = new Thread(() -> {
//                SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS,
//                        TOTAL_PARTITIONS * NUMBER_OF_TASKS_ON_EACH_PARTITION, rsf, range, RESOURCES_RANGE.PARTITIONS, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE,
//                        false);
//                ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
//                ArrayList<Resource> resources = generator.generateResources();
//                generator.generateResourceUsage(tasksToAlloc, resources);
//                // 任务核心分配
//                ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
//                // 任务优先级分配
//                tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);
//
//                /* Lazy Mode */
//                boolean[] LazyModeResult = performLazyMode(tasks,resources,counter,finalI);
//                /* Lazy Mode 至此结束 */
//
//                // Lazy Mode未有可调度的解
//                if(!LazyModeResult[0] && !LazyModeResult[1]){
//
//                    /* Method 1 —— 随机200代，作为baseline */
//                    RandomMethod(tasks, resources, counter, finalI, RANDOM_NUM);
//
////                    /* Method 2 —— GA **/
////                    GAMethod(tasks, resources, counter, finalI, POPULATION, GENERATIONS, String.valueOf(finalI));
////
////                    /* Method 3 —— 强化学习-DQN **/
////                    WriteToFile(tasks, resources, resourceSharingFactor, finalI);
////                    RL_DQNMethod(tasks, resources, counter, resourceSharingFactor, finalI, RL_DQN_STEP);
////
////                    /* Method 4 —— 强化学习-Actor-Critic */
////                    RL_ACMethod(tasks, resources, counter, resourceSharingFactor, finalI, RL_AC_STEP);
//
//                    /* Method 5 —— 粗粒度调整启发式算法 */
//                    // TODO 接入
//                }
//                System.out.println(Thread.currentThread().getName() + " Finished");
//                downLatch.countDown();
//            });
//            worker.setName("5 " + resourceSharingFactor + " " + i);
//            worker.start();
//        }
//
//        try {
//            downLatch.await();
//        } catch (InterruptedException ignored) {}
//
//        writeSystem("5 2 " + resourceSharingFactor, generateResults("RSF = " + rsf + " Results Summary:\n",counter));
//
//    }

    private boolean[] performLazyMode2(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, Counter counter, int finalI, ArrayList<Double> blocking_improve){

        boolean Static_Flag = false;
        boolean Lazy_Flag = false;
        long[][] Ris;
        FineGrainedTuningMethod fineGrainedTuningMethod = new FineGrainedTuningMethod();
        ResponseTimeAnalysisWithVariousPriorities_S_B_Worst analysis = new ResponseTimeAnalysisWithVariousPriorities_S_B_Worst();

        long[] Static_Sum_Blocking = new long[5];
        boolean[] Static_Flag_List = new boolean[5];

        // 需要先统计PWLP的情况

        for (Resource resource : resources) {
            resource.protocol = 2;
        }
        updateResourceRequiredPri(tasks,resources);

        Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,false,true,true,true,false);

        ArrayList<Integer> UnScheId = new ArrayList<>();
        long base_sum = 0;
        if(!isSystemSchedulable(tasks,Ris)){
            for(int x = 0 ; x < tasks.size(); x++){
                for(int y = 0 ; y < tasks.get(x).size(); y++){
                    // 不可调度任务
                    if(Ris[x][y] > tasks.get(x).get(y).deadline){
                        UnScheId.add(tasks.get(x).get(y).id);
                    }
                    base_sum += (tasks.get(x).get(y).spin + tasks.get(x).get(y).local);
                }
            }
        }



        for(int protocol_index = 1; protocol_index <= 5; protocol_index++){
            for (Resource resource : resources) {
                resource.protocol = protocol_index;
            }
            updateResourceRequiredPri(tasks,resources);
            Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);

//            long sum = 0;
//            for(int x = 0 ; x < tasks.size(); x++){
//                for(int y = 0 ; y < tasks.get(x).size(); y++){
//                    if(y <= 1){
//                        SporadicTask task = tasks.get(x).get(y);
//                        sum += (task.spin + task.local);
//                    }
//                }
//            }
//            Static_Sum_Blocking[protocol_index - 1] = sum;

            if(isSystemSchedulable(tasks,Ris)){
                Static_Flag_List[protocol_index - 1] = true;
                Static_Flag = true;
                // 单独统计MSRP和PWLP的可调度性情况，作为baseline
                // PWLP这里先记录一下
                if(protocol_index == 1){
                    counter.incMSRP_No();
                }
                else if(protocol_index == 2){
                    counter.incPWLP_No();
                }
                if(UnScheId.size() > 0){
                    long sum = 0;
                    for(int x = 0 ; x < tasks.size(); x++){
                        for(int y = 0 ; y < tasks.get(x).size(); y++){
//                            if(UnScheId.contains(tasks.get(x).get(y).id)){
//                                SporadicTask task = tasks.get(x).get(y);
//                                sum += (task.spin + task.local);
//                            }
                            SporadicTask task = tasks.get(x).get(y);
                            sum += (task.spin + task.local);
                        }
                    }
                    blocking_improve.add((double)(base_sum - sum) / (double) base_sum);
                }
                break;
            }
        }

        if(!Static_Flag){

            // MSRP的时候无法调整
            for(int protocol_index = 2; protocol_index <= 5; protocol_index++){
                for (Resource resource : resources) {
                    resource.protocol = protocol_index;
                }
                updateResourceRequiredPri(tasks,resources);

                // 纯收益调整
                fineGrainedTuningMethod.FineGrained_PureProfit_Tuning(tasks,resources);

                // 纯收益调整结束后，判断可调度性情况
                Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                if(isSystemSchedulable(tasks,Ris)){
                    counter.incStatic_FineGrained_Pure_No();
                    Lazy_Flag = true;
                    System.out.println("Lazy Mode: After Pure Profit Tuning, System " + finalI + " is schedulable.");

                    if(UnScheId.size() > 0){
                        long sum = 0;
                        for(int x = 0 ; x < tasks.size(); x++){
                            for(int y = 0 ; y < tasks.get(x).size(); y++){
//                                if(UnScheId.contains(tasks.get(x).get(y).id)){
//                                    SporadicTask task = tasks.get(x).get(y);
//                                    sum += (task.spin + task.local);
//                                }
                                SporadicTask task = tasks.get(x).get(y);
                                sum += (task.spin + task.local);
                            }
                        }
                        blocking_improve.add((double)(base_sum - sum) / (double) base_sum);
                    }

                    break;
                }

                // 贪心调整
                fineGrainedTuningMethod.FineGrained_GreedyProfit_Tuning(tasks,resources);

                // 贪心调整结束后，判断可调度性情况
                Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                if(isSystemSchedulable(tasks,Ris)){
                    counter.incStatic_FineGrained_Greedy_No();
                    Lazy_Flag = true;
                    System.out.println("Lazy Mode: After Greedy Profit Tuning, System " + finalI + " is schedulable.");
                    if(UnScheId.size() > 0){
                        long sum = 0;
                        for(int x = 0 ; x < tasks.size(); x++){
                            for(int y = 0 ; y < tasks.get(x).size(); y++){
//                                if(UnScheId.contains(tasks.get(x).get(y).id)){
//                                    SporadicTask task = tasks.get(x).get(y);
//                                    sum += (task.spin + task.local);
//                                }
                                SporadicTask task = tasks.get(x).get(y);
                                sum += (task.spin + task.local);
                            }
                        }
                        blocking_improve.add((double)(base_sum - sum) / (double) base_sum);
                    }
                    break;
                }

//                if(protocol_index == 5){
//                    blocking_improve.add(0.0);
//                }

            }





        }
        else{

//            if(!Static_Flag_List[1]){
//                double max_imp = -1000000.0;
//                for(int w = 0 ; w < Static_Flag_List.length; w++){
//                    if(Static_Flag_List[w]){
//                        double imp = (double)(Static_Sum_Blocking[w] - Static_Sum_Blocking[1]) / (double)Static_Sum_Blocking[1];
//                        if(imp > max_imp){
//                            max_imp = imp;
//                        }
//                    }
//                }
//                blocking_improve.add(max_imp);
//            }
//            else{
//                blocking_improve.add(0.0);
//            }
            counter.incStatic_No();
            System.out.println("Lazy Mode: Coarse Grained Tuning, System " + finalI + " is schedulable.");
        }
        return new boolean[] {Static_Flag, Lazy_Flag};
    }

    private boolean[] performLazyMode(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, Counter counter, int finalI){

        boolean Static_Flag = false;
        boolean Lazy_Flag = false;
        long[][] Ris;
        FineGrainedTuningMethod fineGrainedTuningMethod = new FineGrainedTuningMethod();
        ResponseTimeAnalysisWithVariousPriorities_S_B_Worst analysis = new ResponseTimeAnalysisWithVariousPriorities_S_B_Worst();

//        long[] Static_Sum_Blocking = new long[5];
//        boolean[] Static_Flag_List = new boolean[5];

        for(int protocol_index = 1; protocol_index <= 5; protocol_index++){
            for (Resource resource : resources) {
                resource.protocol = protocol_index;
            }
            updateResourceRequiredPri(tasks,resources);
            Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);

//            long sum = 0;
//            for(int x = 0 ; x < tasks.size(); x++){
//                for(int y = 0 ; y < tasks.get(x).size(); y++){
//                    SporadicTask task = tasks.get(x).get(y);
//                    sum += (task.spin + task.local);
//                }
//            }
////            Static_Sum_Blocking[protocol_index] = sum;

            if(isSystemSchedulable(tasks,Ris)){
//                Static_Flag_List[protocol_index] = true;
                Static_Flag = true;
                // 单独统计MSRP和PWLP的可调度性情况，作为baseline
                // PWLP这里先记录一下
                if(protocol_index == 1){
                    counter.incMSRP_No();
                }
                else if(protocol_index == 2){
                    counter.incPWLP_No();
                }
            }

        }

        if(!Static_Flag){

            // MSRP的时候无法调整
            for(int protocol_index = 2; protocol_index <= 5; protocol_index++){
                for (Resource resource : resources) {
                    resource.protocol = protocol_index;
                }
                updateResourceRequiredPri(tasks,resources);

                // 纯收益调整
                fineGrainedTuningMethod.FineGrained_PureProfit_Tuning(tasks,resources);

                // 纯收益调整结束后，判断可调度性情况
                Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                if(isSystemSchedulable(tasks,Ris)){
                    counter.incStatic_FineGrained_Pure_No();
                    Lazy_Flag = true;
                    System.out.println("Lazy Mode: After Pure Profit Tuning, System " + finalI + " is schedulable.");
                    break;
                }

                // 贪心调整
                fineGrainedTuningMethod.FineGrained_GreedyProfit_Tuning(tasks,resources);

                // 贪心调整结束后，判断可调度性情况
                Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                if(isSystemSchedulable(tasks,Ris)){
                    counter.incStatic_FineGrained_Greedy_No();
                    Lazy_Flag = true;
                    System.out.println("Lazy Mode: After Greedy Profit Tuning, System " + finalI + " is schedulable.");
                    break;
                }
            }
        }
        else{
            counter.incStatic_No();
            System.out.println("Lazy Mode: Coarse Grained Tuning, System " + finalI + " is schedulable.");
        }
        return new boolean[] {Static_Flag, Lazy_Flag};
    }

    public void RandomMethod(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, Counter counter, int finalI, int RandomNum){

        long[][] Ris;
        FineGrainedTuningMethod fineGrainedTuningMethod = new FineGrainedTuningMethod();
        ResponseTimeAnalysisWithVariousPriorities_S_B_Worst analysis = new ResponseTimeAnalysisWithVariousPriorities_S_B_Worst();

        System.out.println("System " + finalI + " Random: Start");
        for(int num = 0 ; num < RandomNum; num++){
            // System.out.println("Random" + num);
            // 随机生成一个粗颗粒度解决方案
            Random rand = new Random();
            for (Resource resource : resources) {
                resource.protocol = 1 + rand.nextInt(5);
            }
            updateResourceRequiredPri(tasks,resources);

            Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
            if(isSystemSchedulable(tasks,Ris)){
                counter.incRandom_CoarseGrained_No();
                // Random_CoarseGrained_No++;
                System.out.println("Random: Coarse Grained Tuning, System " + finalI + " is schedulable.");
                break;
            }
            else{
                // 纯收益调整
                fineGrainedTuningMethod.FineGrained_PureProfit_Tuning(tasks,resources);
                // 纯收益调整结束后，判断可调度性情况
                Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                if(isSystemSchedulable(tasks,Ris)){
                    counter.incRandom_FineGrained_No();
                    // Random_FineGrained_No++;
                    System.out.println("Random: After Pure Profit Tuning, System " + finalI + " is schedulable.");
                    break;
                }

                // 贪心调整
                fineGrainedTuningMethod.FineGrained_GreedyProfit_Tuning(tasks,resources);

                // 贪心调整结束后，判断可调度性情况
                Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                if(isSystemSchedulable(tasks,Ris)){
                    // Random_FineGrained_No++;
                    counter.incRandom_FineGrained_No();
                    System.out.println("Random: After Greedy Profit Tuning, System " + finalI + " is schedulable.");
                    break;
                }
            }
        }
        System.out.println("System " + finalI + " Random: End");

    }

    private void GAMethod(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, Counter counter, int finalI, int POPULATION, int GENERATIONS, String name){

        System.out.println("System " + finalI + " GA: Start");
        // 目前，对于GA的每一代，都做了细粒度调整，进而判断可调度性。
        GASolver solver = new GASolver(tasks, resources, POPULATION, GENERATIONS, 2, 2, 0.8,
                0.01, 2, 5, true, "System" + name);
        int GA_result = solver.solve(true);
        if(GA_result == 1){
            System.out.println("GA: Coarse Grained Tuning, System " + finalI + " is schedulable.");
            counter.incGA_CoarseGrained_No();
            // GA_CoarseGrained_No++;
        }
        else if(GA_result == 2){
            System.out.println("GA: After Pure Profit Tuning, System " + finalI + " is schedulable.");
            counter.incGA_FineGrained_No();
            // GA_FineGrained_No++;
        }
        else if(GA_result == 3){
            System.out.println("GA: After Greedy Profit Tuning, System " + finalI + " is schedulable.");
            counter.incGA_FineGrained_No();
            // GA_FineGrained_No++;
        }
        System.out.println("System " + finalI + " GA: End");

    }

    private void WriteToFile(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, int condition, int finalI){

        Gson gson = new Gson();
        String tasksString = gson.toJson(tasks);
        String resourcesString = gson.toJson(resources);
        File file_resources = new File("scripts", "resources_coNum_" + condition + "_sysNum_"+ finalI + ".txt");
        File file_tasks = new File("scripts", "tasks_coNum_" + condition + "_sysNum_"+ finalI + ".txt");
        try {
            FileWriter writer = new FileWriter(file_resources);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(resourcesString);
            bufferedWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            FileWriter writer2 = new FileWriter(file_tasks);
            BufferedWriter bufferedWriter = new BufferedWriter(writer2);
            bufferedWriter.write(tasksString);
            bufferedWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void RL_DQNMethod(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, Counter counter, int condition, int finalI, int RL_DQN_STEP){

        System.out.println("System " + finalI + " RL_DQN: Start");
        try {
            String arg1 = String.valueOf(resources.size());
            String arg2 = "D:\\Project\\HighPerformanceResourceSharingProtocol\\HighPerformanceResourceSharingProtocol\\scripts";
            // 哪个条件下的第几个系统
            String arg3 = String.valueOf(condition);
            String arg4 = String.valueOf(finalI);
            String arg5 = String.valueOf(RL_DQN_STEP);

            ProcessBuilder builder = new ProcessBuilder("python", "D:\\Reinforcement-learning-with-PyTorch-master\\content\\5_Deep_Q_Network\\run_this.py", arg1, arg2, arg3, arg4, arg5);
            builder.redirectErrorStream(true); // 合并标准输出和错误输出
            Process process = builder.start();
            // 读取进程的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.equals("1")){
                    // RL_DQN_CoarseGrained_No++;
                    counter.incRL_DQN_CoarseGrained_No();
                    System.out.println("RL_DQN: Coarse Grained Tuning, System " + finalI + " is schedulable.");
                }
                else if(line.equals("2")){
                    // RL_DQN_FineGrained_No++;
                    counter.incRL_DQN_FineGrained_No();
                    System.out.println("RL_DQN: After Pure Profit Tuning, System " + finalI + " is schedulable.");
                }
                else if(line.equals("3")){
                    // RL_DQN_FineGrained_No++;
                    counter.incRL_DQN_FineGrained_No();
                    System.out.println("RL_DQN: After Greedy Profit Tuning, System " + finalI + " is schedulable.");

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("System " + finalI + " RL_DQN: End");

    }

    private void RL_ACMethod(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, Counter counter, int condition, int finalI, int RL_AC_STEP){

        System.out.println("System " + finalI + " RL_AC: Start");
        try {
            String arg1 = String.valueOf(resources.size());
            String arg2 = "D:\\Project\\HighPerformanceResourceSharingProtocol\\HighPerformanceResourceSharingProtocol\\scripts";
            String arg3 = String.valueOf(condition);
            String arg4 = String.valueOf(finalI);
            String arg5 = String.valueOf(RL_AC_STEP);

            ProcessBuilder builder = new ProcessBuilder("python", "D:\\Reinforcement-learning-with-PyTorch-master\\content\\8_Actor_Critic_Advantage\\AC_CartPole.py", arg1, arg2, arg3, arg4, arg5);
            builder.redirectErrorStream(true); // 合并标准输出和错误输出
            Process process = builder.start();
            // 读取进程的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // System.out.println(line); // 这里将Python的输出打印到Java的控制台
                if(line.equals("1")){
                    // RL_AC_CoarseGrained_No++;
                    counter.incRL_AC_CoarseGrained_No();
                    System.out.println("RL_AC: Coarse Grained Tuning, System " + finalI + " is schedulable.");
                }
                else if(line.equals("2")){
                    // RL_AC_FineGrained_No++;
                    counter.incRL_AC_FineGrained_No();
                    System.out.println("RL_AC: After Pure Profit Tuning, System " + finalI + " is schedulable.");
                }
                else if(line.equals("3")){
                    // RL_AC_FineGrained_No++;
                    counter.incRL_AC_FineGrained_No();
                    System.out.println("RL_AC: After Greedy Profit Tuning, System " + finalI + " is schedulable.");

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("System " + finalI + " RL_AC: End");

    }

    private String generateResults(String title,Counter counter){

        StringBuilder result = new StringBuilder();
        String separator = "========================================\n";
        result.append(separator);
        result.append(title);
        result.append("TOTAL_NUMBER_OF_SYSTEMS : " + TOTAL_NUMBER_OF_SYSTEMS + "\n");
        result.append(separator);
        result.append("MSRP: ").append((double) counter.MSRP_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("PWLP: ").append((double) counter.PWLP_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Static (OverAll): ").append((double) counter.Static_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Static (FineGrained Pure): ").append((double) counter.Static_FineGrained_Pure_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Static (FineGrained Greedy): ").append((double) counter.Static_FineGrained_Greedy_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Random (CoarseGrained): ").append((double) counter.Random_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Random (FineGrained): ").append((double) counter.Random_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("GA (CoarseGrained): ").append((double) counter.GA_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("GA (FineGrained): ").append((double) counter.GA_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL DQN (CoarseGrained): ").append((double) counter.RL_DQN_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL DQN (FineGrained): ").append((double) counter.RL_DQN_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL AC (CoarseGrained): ").append((double) counter.RL_AC_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL AC (FineGrained): ").append((double) counter.RL_AC_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append(separator);
        // Calculate the total width of the separator and align the content to the center
        int separatorWidth = separator.length() - 1;
        String[] lines = result.toString().split("\n");
        int maxLineLength = 0;
        for (String line : lines) {
            if (line.length() > maxLineLength) {
                maxLineLength = line.length();
            }
        }
        StringBuilder centeredResult = new StringBuilder();
        for (String line : lines) {
            int padding = (maxLineLength - line.length()) / 2;
            String centeredLine = String.format("%" + (padding + line.length()) + "s", line);
            centeredResult.append(centeredLine).append("\n");
        }
        centeredResult.append(separator);
        System.out.println(centeredResult);
        return centeredResult.toString();
    }

    public void parallelExperimentIncreasingCriticalSectionLength(int cslen, Counter counter) {
        final CountDownLatch downLatch = new CountDownLatch(TOTAL_NUMBER_OF_SYSTEMS);

        final CS_LENGTH_RANGE cs_range;
        switch (cslen) {
            case 1:
                cs_range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
                break;
            case 2:
                cs_range = CS_LENGTH_RANGE.SHORT_CS_LEN;
                break;
            case 3:
                cs_range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
                break;
            case 4:
                cs_range = CS_LENGTH_RANGE.LONG_CSLEN;
                break;
            case 5:
                cs_range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
                break;
            case 6:
                cs_range = CS_LENGTH_RANGE.RANDOM;
                break;
            default:
                cs_range = null;
                break;
        }

        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
            int finalI = i;
            int finalI1 = i;

            Thread worker = new Thread(() -> {
                System.out.println(finalI);
                long[][] Ris;
                FineGrainedTuningMethod fineGrainedTuningMethod = new FineGrainedTuningMethod();
                ResponseTimeAnalysisWithVariousPriorities_B_S analysis = new ResponseTimeAnalysisWithVariousPriorities_B_S();

                SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS,
                        TOTAL_PARTITIONS * NUMBER_OF_TASKS_ON_EACH_PARTITION, RSF, cs_range, RESOURCES_RANGE.PARTITIONS,
                        NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);
                ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
                ArrayList<Resource> resources = generator.generateResources();
                generator.generateResourceUsage(tasksToAlloc, resources);
                // 任务核心分配
                ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
                // 任务优先级分配
                tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

                /****** Lazy Mode ******/
                boolean Static_Flag = false;
                boolean Lazy_Flag = false;

                // 粗粒度
                for(int protocol_index = 1; protocol_index <= 5; protocol_index++){
                    for (Resource resource : resources) {
                        resource.protocol = protocol_index;
                    }
                    updateResourceRequiredPri(tasks,resources);
                    Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                    if(isSystemSchedulable(tasks,Ris)){
                        // Static_No++;
                        Static_Flag = true;
                        // 单独统计MSRP和PWLP的可调度性情况，作为baseline
                        if(protocol_index == 1){
                            counter.incMSRP_No();
                        }
                        else if(protocol_index == 2){
                            counter.incPWLP_No();
                        }
                    }
                }

                // 均不可调度，考虑细粒度调整
                if(!Static_Flag){
                    // MSRP的时候无法调整
                    for(int protocol_index = 2; protocol_index <= 5; protocol_index++){
                        for (Resource resource : resources) {
                            resource.protocol = protocol_index;
                        }
                        updateResourceRequiredPri(tasks,resources);

                        // 纯收益调整
                        fineGrainedTuningMethod.FineGrained_PureProfit_Tuning(tasks,resources);

                        // 纯收益调整结束后，判断可调度性情况
                        Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                        if(isSystemSchedulable(tasks,Ris)){
                            counter.incStatic_FineGrained_Pure_No();
                            Lazy_Flag = true;
                            System.out.println("Lazy Mode: After Pure Profit Tuning, System " + finalI + " is schedulable.");
                            break;
                        }

                        // 贪心调整
                        fineGrainedTuningMethod.FineGrained_GreedyProfit_Tuning(tasks,resources);

                        // 贪心调整结束后，判断可调度性情况
                        Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                        if(isSystemSchedulable(tasks,Ris)){
                            counter.incStatic_FineGrained_Greedy_No();
                            Lazy_Flag = true;
                            System.out.println("Lazy Mode: After Greedy Profit Tuning, System " + finalI + " is schedulable.");
                            break;
                        }
                    }
                }
                else{
                    counter.incStatic_No();
                    System.out.println("Lazy Mode: Coarse Grained Tuning, System " + finalI + " is schedulable.");
                }
                /****** Lazy Mode 至此结束******/
                if(!Static_Flag && !Lazy_Flag){

                    // 粗粒度调整
                    /** Method 1 —— 随机200代，作为baseline **/
                    System.out.println("System " + finalI1 + " Random: Start");
                    for(int num = 0 ; num < 200; num++){
                        // System.out.println("Random" + num);
                        // 随机生成一个粗颗粒度解决方案
                        Random rand = new Random();
                        for (Resource resource : resources) {
                            resource.protocol = 1 + rand.nextInt(5);
                        }
                        updateResourceRequiredPri(tasks,resources);

                        Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                        if(isSystemSchedulable(tasks,Ris)){
                            counter.incRandom_CoarseGrained_No();
                            // Random_CoarseGrained_No++;
                            System.out.println("Random: Coarse Grained Tuning, System " + finalI1 + " is schedulable.");
                            break;
                        }
                        else{
                            // 纯收益调整
                            fineGrainedTuningMethod.FineGrained_PureProfit_Tuning(tasks,resources);
                            // 纯收益调整结束后，判断可调度性情况
                            Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                            if(isSystemSchedulable(tasks,Ris)){
                                counter.incRandom_FineGrained_No();
                                // Random_FineGrained_No++;
                                System.out.println("Random: After Pure Profit Tuning, System " + finalI1 + " is schedulable.");
                                break;
                            }

                            // 贪心调整
                            fineGrainedTuningMethod.FineGrained_GreedyProfit_Tuning(tasks,resources);

                            // 贪心调整结束后，判断可调度性情况
                            Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
                            if(isSystemSchedulable(tasks,Ris)){
                                // Random_FineGrained_No++;
                                counter.incRandom_FineGrained_No();
                                System.out.println("Random: After Greedy Profit Tuning, System " + finalI1 + " is schedulable.");
                                break;
                            }
                        }
                    }
                    System.out.println("System " + finalI1 + " Random: End");


                    /** Method 2 —— GA **/
                    System.out.println("System " + finalI1 + " GA: Start");
                    int GENERATIONS = 100;
                    int POPULATION = 500;
                    // 目前，对于GA的每一代，都做了细粒度调整，进而判断可调度性。
                    GASolver solver = new GASolver(tasks, resources, POPULATION, GENERATIONS, 2, 2, 0.8,
                            0.01, 2, 5, true, "System");
                    int GA_result = solver.solve(true);
                    if(GA_result == 1){
                        System.out.println("GA: Coarse Grained Tuning, System " + finalI1 + " is schedulable.");
                        counter.incGA_CoarseGrained_No();
                        // GA_CoarseGrained_No++;
                    }
                    else if(GA_result == 2){
                        System.out.println("GA: After Pure Profit Tuning, System " + finalI1 + " is schedulable.");
                        counter.incGA_FineGrained_No();
                        // GA_FineGrained_No++;
                    }
                    else if(GA_result == 3){
                        System.out.println("GA: After Greedy Profit Tuning, System " + finalI1 + " is schedulable.");
                        counter.incGA_FineGrained_No();
                        // GA_FineGrained_No++;
                    }
                    System.out.println("System " + finalI1 + " GA: End");

                    /** Method 3 —— 强化学习-DQN **/
                    // 调用python脚本，同时还要将任务和资源对象保存成.txt，便于python脚本读取。

                    Gson gson = new Gson();
                    String tasksString = gson.toJson(tasks);
                    String resourcesString = gson.toJson(resources);
                    File file_resources = new File("scripts", "resources" + finalI1 + ".txt");
                    File file_tasks = new File("scripts", "tasks" + finalI1 + ".txt");
                    try {
                        FileWriter writer = new FileWriter(file_resources);
                        BufferedWriter bufferedWriter = new BufferedWriter(writer);
                        bufferedWriter.write(resourcesString);
                        bufferedWriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        FileWriter writer2 = new FileWriter(file_tasks);
                        BufferedWriter bufferedWriter = new BufferedWriter(writer2);
                        bufferedWriter.write(tasksString);
                        bufferedWriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("System " + finalI1 + " RL_DQN: Start");
                    try {
                        String arg1 = String.valueOf(resources.size());
                        String arg2 = "D:\\Project\\HighPerformanceResourceSharingProtocol\\HighPerformanceResourceSharingProtocol\\scripts";
                        String arg3 = String.valueOf(finalI1);
                        ProcessBuilder builder = new ProcessBuilder("python", "D:\\Reinforcement-learning-with-PyTorch-master\\content\\5_Deep_Q_Network\\run_this.py", arg1, arg2, arg3);
                        builder.redirectErrorStream(true); // 合并标准输出和错误输出
                        Process process = builder.start();
                        // 读取进程的输出
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if(line.equals("1")){
                                // RL_DQN_CoarseGrained_No++;
                                counter.incRL_DQN_CoarseGrained_No();
                                System.out.println("RL_DQN: Coarse Grained Tuning, System " + finalI1 + " is schedulable.");
                            }
                            else if(line.equals("2")){
                                // RL_DQN_FineGrained_No++;
                                counter.incRL_DQN_FineGrained_No();
                                System.out.println("RL_DQN: After Pure Profit Tuning, System " + finalI1 + " is schedulable.");
                            }
                            else if(line.equals("3")){
                                // RL_DQN_FineGrained_No++;
                                counter.incRL_DQN_FineGrained_No();
                                System.out.println("RL_DQN: After Greedy Profit Tuning, System " + finalI1 + " is schedulable.");

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("System " + finalI1 + " RL_DQN: End");

                    /** Method 4 —— 强化学习-Actor-Critic **/

                    System.out.println("System " + finalI1 + " RL_AC: Start");
                    try {
                        String arg1 = String.valueOf(resources.size());
                        String arg2 = "D:\\Project\\HighPerformanceResourceSharingProtocol\\HighPerformanceResourceSharingProtocol\\scripts";
                        String arg3 = String.valueOf(finalI1);
                        ProcessBuilder builder = new ProcessBuilder("python", "D:\\Reinforcement-learning-with-PyTorch-master\\content\\8_Actor_Critic_Advantage\\AC_CartPole.py", arg1, arg2, arg3);
                        builder.redirectErrorStream(true); // 合并标准输出和错误输出
                        Process process = builder.start();
                        // 读取进程的输出
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // System.out.println(line); // 这里将Python的输出打印到Java的控制台
                            if(line.equals("1")){
                                // RL_AC_CoarseGrained_No++;
                                counter.incRL_AC_CoarseGrained_No();
                                System.out.println("RL_AC: Coarse Grained Tuning, System " + finalI1 + " is schedulable.");
                            }
                            else if(line.equals("2")){
                                // RL_AC_FineGrained_No++;
                                counter.incRL_AC_FineGrained_No();
                                System.out.println("RL_AC: After Pure Profit Tuning, System " + finalI1 + " is schedulable.");
                            }
                            else if(line.equals("3")){
                                // RL_AC_FineGrained_No++;
                                counter.incRL_AC_FineGrained_No();
                                System.out.println("RL_AC: After Greedy Profit Tuning, System " + finalI1 + " is schedulable.");

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("System " + finalI1 + " RL_AC: End");


                    /** Method 5 —— 粗粒度调整启发式算法 **/
                    // TODO 接入
                    //    System.out.println("System " + i + " Heuristic: Start");
                    //    System.out.println("System " + i + " Heuristic: End");
                }

                System.out.println(Thread.currentThread().getName() + " Finished");
                downLatch.countDown();

            });
            worker.setName("2 " + cslen + " " + i);
            worker.start();
        }

        try {
            downLatch.await();
        } catch (InterruptedException ignored) {}

        StringBuilder result = new StringBuilder();
        String separator = "========================================\n";
        result.append(separator);
        result.append("CS_LEN = " + cslen + " Results Summary:\n");
        result.append("TOTAL_NUMBER_OF_SYSTEMS : " + TOTAL_NUMBER_OF_SYSTEMS + "\n");
        result.append(separator);
        result.append("MSRP: ").append((double) counter.MSRP_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("PWLP: ").append((double) counter.PWLP_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Static (OverAll): ").append((double) counter.Static_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Static (FineGrained Pure): ").append((double) counter.Static_FineGrained_Pure_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Static (FineGrained Greedy): ").append((double) counter.Static_FineGrained_Greedy_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Random (CoarseGrained): ").append((double) counter.Random_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("Random (FineGrained): ").append((double) counter.Random_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("GA (CoarseGrained): ").append((double) counter.GA_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("GA (FineGrained): ").append((double) counter.GA_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL DQN (CoarseGrained): ").append((double) counter.RL_DQN_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL DQN (FineGrained): ").append((double) counter.RL_DQN_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL AC (CoarseGrained): ").append((double) counter.RL_AC_CoarseGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append("RL AC (FineGrained): ").append((double) counter.RL_AC_FineGrained_No / TOTAL_NUMBER_OF_SYSTEMS).append("\n");
        result.append(separator);
        // Calculate the total width of the separator and align the content to the center
        int separatorWidth = separator.length() - 1;
        String[] lines = result.toString().split("\n");
        int maxLineLength = 0;
        for (String line : lines) {
            if (line.length() > maxLineLength) {
                maxLineLength = line.length();
            }
        }
        StringBuilder centeredResult = new StringBuilder();
        for (String line : lines) {
            int padding = (maxLineLength - line.length()) / 2;
            String centeredLine = String.format("%" + (padding + line.length()) + "s", line);
            centeredResult.append(centeredLine).append("\n");
        }
        centeredResult.append(separator);
        System.out.println(centeredResult);
        writeSystem("2 2 " + cslen, centeredResult.toString());
    }

    private static void updateResourceRequiredPri(ArrayList<ArrayList<SporadicTask>> generatedTaskSets, ArrayList<Resource> resources){
        for(int i = 0 ; i < generatedTaskSets.size(); i++){
            for(int j = 0 ; j < generatedTaskSets.get(i).size(); j++){
                SporadicTask task = generatedTaskSets.get(i).get(j);
                ArrayList<Integer> resourceIndexList = task.resource_required_index;
                task.resource_required_priority = new ArrayList<>();
                for (int k = 0 ; k < resourceIndexList.size(); k++){
                    Resource re = resources.get(resourceIndexList.get(k));
                    int max_i_pri = 0;
                    // 遍历第i号处理器上的所有任务，找到该处理器上所有任务的最高优先级
                    for(int w = 0 ; w < generatedTaskSets.get(i).size(); w++) {
                        if(generatedTaskSets.get(i).get(w).priority > max_i_pri){
                            max_i_pri = generatedTaskSets.get(i).get(w).priority;
                        }
                    }

                    int protocol = re.protocol;
                    boolean isGlobal = re.isGlobal;
                    int ceiling_pri = re.getCeilingForProcessor(generatedTaskSets,task.partition);

                    if(!isGlobal){
                        task.resource_required_priority.add(ceiling_pri);
                    } else{
                        switch (protocol) {
                            case 1:
                                task.resource_required_priority.add(max_i_pri + 1);
                                break;
                            case 2:
                                task.resource_required_priority.add(task.priority);
                                break;
                            case 3:
                                task.resource_required_priority.add((int) Math.ceil(task.priority + ((double)(ceiling_pri - task.priority) / 2.0)));
                                break;
                            case 4:
                            case 6:
                                task.resource_required_priority.add(ceiling_pri);
                                break;
                            case 5:
                            case 7:
                                task.resource_required_priority.add((int) Math.ceil(ceiling_pri + (max_i_pri - ceiling_pri) / 2.0));
                                break;
                            default:
                                System.err.print("NO RULES");
                                System.exit(-1);
                        }

                    }

                }
            }

        }
    }

    public static boolean isSystemSchedulable(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.get(i).size(); j++) {
                if (tasks.get(i).get(j).deadline < Ris[i][j]){
                    return false;
                }
            }
        }
        return true;
    }

    public static void ExperimentIncreasingCriticalSectionLength(int cslen) {
        final CS_LENGTH_RANGE cs_range;
        switch (cslen) {
            case 1:
                cs_range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
                break;
            case 2:
                cs_range = CS_LENGTH_RANGE.SHORT_CS_LEN;
                break;
            case 3:
                cs_range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
                break;
            case 4:
                cs_range = CS_LENGTH_RANGE.LONG_CSLEN;
                break;
            case 5:
                cs_range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
                break;
            case 6:
                cs_range = CS_LENGTH_RANGE.RANDOM;
                break;
            default:
                cs_range = null;
                break;
        }

        int MSRP_No = 0, PWLP_No = 0, Static_No = 0, Static_FineGrained_Pure_No = 0, Static_FineGrained_Greedy_No = 0;
        int Random_CoarseGrained_No = 0, Random_FineGrained_No = 0;
        int GA_CoarseGrained_No = 0, GA_FineGrained_No = 0;
        int RL_DQN_CoarseGrained_No = 0, RL_DQN_FineGrained_No = 0;
        int RL_AC_CoarseGrained_No = 0, RL_AC_FineGrained_No = 0;

        FineGrainedTuningMethod fineGrainedTuningMethod = new FineGrainedTuningMethod();
        // 时间调度
        ResponseTimeAnalysisWithVariousPriorities_B_S analysis = new ResponseTimeAnalysisWithVariousPriorities_B_S();
        ResponseTimeAnalysisWithVariousPriorities_S_B analysis_2 = new ResponseTimeAnalysisWithVariousPriorities_S_B();
        ResponseTimeAnalysisWithVariousPriorities analysis_3 = new ResponseTimeAnalysisWithVariousPriorities();
        ResponseTimeAnalysisWithVariousPriorities_S_B_Worst analysis_4 = new ResponseTimeAnalysisWithVariousPriorities_S_B_Worst();


        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

            SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS,
                    TOTAL_PARTITIONS * NUMBER_OF_TASKS_ON_EACH_PARTITION, RSF, cs_range, RESOURCES_RANGE.PARTITIONS,
                    NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);
            ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
            ArrayList<Resource> resources = generator.generateResources();
            generator.generateResourceUsage(tasksToAlloc, resources);
            // 任务核心分配
            ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
            // 任务优先级分配
            tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

            Random rand = new Random();
            for (Resource resource : resources) {
                resource.protocol = 1 + rand.nextInt(5);
            }

//            for (Resource resource : resources) {
//                resource.protocol = 2;
//            }
            updateResourceRequiredPri(tasks,resources);

            // long[][] Ris = analysis.getResponseTimeByDMPO(tasks, resources, 1, false, true, true, true, false);
            // long[][] Ris_2 = analysis_2.getResponseTimeByDMPO(tasks, resources, 1, false, true, true, true, false);
//            long[][] Ris_3 = analysis_3.getResponseTimeByDMPO(tasks, resources, 1, true, true, true, true, false);
//            long[][] Ris_4 = analysis_3.getResponseTimeByDMPO(tasks, resources, 1, false, true, true, true, false);

            long[][] Ris_5 = analysis_4.getResponseTimeByDMPO(tasks, resources, 1, false, true, true, true, false);

            if(isSystemSchedulable(tasks,Ris_5)){
                MSRP_No++;
            }
//            if(isSystemSchedulable(tasks,Ris_2)){
//                PWLP_No++;
//            }
//
//            if(isSystemSchedulable(tasks,Ris) && isSystemSchedulable(tasks,Ris_2)){
//                for(int a = 0 ; a < Ris.length; a++){
//                    for(int b = 0 ; b < Ris[a].length ; b++){
//                        if(Ris[a][b] < Ris_2[a][b]){
//                            System.out.println("P");
//                        }
//                    }
//                }
//            }


//
//            if(isSystemSchedulable(tasks,Ris_3)){
//                Static_No++;
//            }

//            if(!isSystemSchedulable(tasks,Ris) && isSystemSchedulable(tasks,Ris_2)){
//                System.out.println(22);
//            }
            System.out.println(i + "/ 1000");
        }
        String result = (double) MSRP_No / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) PWLP_No / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
                + (double) Static_No / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) Static_FineGrained_Pure_No / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
                + (double) Static_FineGrained_Greedy_No / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) Random_CoarseGrained_No / (double) TOTAL_NUMBER_OF_SYSTEMS + " " + (double) Random_FineGrained_No / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";
        // writeSystem("2 2 " + cslen, result);
        System.out.println(result);
    }

//    public void ExperimentIncreasingCriticalSectionLengthExecutor(int cslen, Counter counter) {
//
//        final CS_LENGTH_RANGE cs_range = switch (cslen) {
//            case 1 -> CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
//            case 2 -> CS_LENGTH_RANGE.SHORT_CS_LEN;
//            case 3 -> CS_LENGTH_RANGE.MEDIUM_CS_LEN;
//            case 4 -> CS_LENGTH_RANGE.LONG_CSLEN;
//            case 5 -> CS_LENGTH_RANGE.VERY_LONG_CSLEN;
//            case 6 -> CS_LENGTH_RANGE.RANDOM;
//            default -> null;
//        };
//
//        for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
//            System.out.println(i);
//            int finalI = i;
//            SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, true, TOTAL_PARTITIONS,
//                    TOTAL_PARTITIONS * NUMBER_OF_TASKS_ON_EACH_PARTITION, RSF, cs_range, RESOURCES_RANGE.PARTITIONS,
//                    NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE, false);
//            ArrayList<SporadicTask> tasksToAlloc = generator.generateTasks();
//            ArrayList<Resource> resources = generator.generateResources();
//            generator.generateResourceUsage(tasksToAlloc, resources);
//            // 任务核心分配
//            ArrayList<ArrayList<SporadicTask>> tasks = new AllocationGeneator().allocateTasks(tasksToAlloc, resources, generator.total_partitions, 0);
//            // 任务优先级分配
//            tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);
//
//            /* Lazy Mode */
//            boolean[] LazyModeResult = performLazyMode(tasks,resources,counter,finalI);
//            /* Lazy Mode 至此结束 */
//
//            // Lazy Mode未有可调度的解
//            if(!LazyModeResult[0] && !LazyModeResult[1]){
//
//                /* Method 1 —— 随机200代，作为baseline */
//                RandomMethod(tasks, resources, counter, finalI, RANDOM_NUM);
//
//                /* Method 2 —— GA **/
////                    GAMethod(tasks, resources, counter, finalI, POPULATION, GENERATIONS);
//
////                    /* Method 3 —— 强化学习-DQN **/
////                    WriteToFile(tasks, resources, cslen, finalI);
////                    RL_DQNMethod(tasks, resources, counter, cslen, finalI, RL_DQN_STEP);
////
////                    /* Method 4 —— 强化学习-Actor-Critic */
////                    RL_ACMethod(tasks, resources, counter, cslen, finalI, RL_AC_STEP);
//
//                /* Method 5 —— 粗粒度调整启发式算法 */
//                // TODO 接入
//
//            }
//                System.out.println(Thread.currentThread().getName() + " Finished");
//        }
//
//        writeSystem("2 2 " + cslen, generateResults("CS_LEN = " + cslen + " Results Summary:\n",counter));
//    }

    public static void writeSystem(String filename, String result) {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new FileWriter(new File("result/" + filename + ".txt"), false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writer.println(result);
        writer.close();
    }

    public static double calculateAverage(ArrayList<Double> list) {
        if (list == null || list.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        for (double num : list) {
            sum += num;
        }
        return sum / list.size();
    }




}


