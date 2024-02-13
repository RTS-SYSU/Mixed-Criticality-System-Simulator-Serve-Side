package com.example.serveside.service;

import java.util.*;

import com.example.serveside.response.*;
import com.example.serveside.service.mrsp.MrsPWorstCase;
import com.example.serveside.service.msrp.MSRPWorstCase;
import com.example.serveside.service.pwlp.PWLPWorstCase;
import com.example.serveside.service.dynamic.DynamicWorstCase;
import com.example.serveside.service.CommonUse.generatorTools.SimpleSystemGenerator;
import com.example.serveside.service.CommonUse.BasicPCB;
import com.example.serveside.service.CommonUse.BasicResource;
import com.example.serveside.request.ConfigurationInformation;
import com.example.serveside.service.mrsp.MrsPMixedCriticalSystem;
import com.example.serveside.service.msrp.MSRPMixedCriticalSystem;
import com.example.serveside.service.pwlp.PWLPMixedCriticalSystem;
import com.example.serveside.service.dynamic.DynamicMixedCriticalSystem;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * {@code FrontInterfaces} 主要起以下作用
 * <p>
 *     <ol>
 *         <li>使用前端传递的系统环境配置参数随机生成一组任务、资源、任务发布时间等信息，并模拟任务在不同资源共享协议下的执行情况，并将执行结果返回。</li>
 *         <li>保存模拟器生成的任务、资源、任务发布时间等信息，以便后续模拟运行任务在特定资源共享协议下的执行情况做准备。</li>
 *         <li>模拟指定任务在不同资源共享协议下的最差运行情况。</li>
 *     </ol>
 * </p>
 * */
public class FrontInterfaces
{
    /**
     * 是否是历史记录。
     * */
    public static Boolean isHistoryRecord = false;

    public static Boolean isTestAtScale = false;
    /**
     * 随机生成的任务的信息。
     * */
    public static ArrayList<BasicPCB> totalTasks;

    /**
     * 随机生成的资源的信息。
     * */
    public static ArrayList<BasicResource> totalResources;

    /**
     * 任务发布的时间点。
     * */
    public static TreeMap<Integer, ArrayList<Integer>> taskReleaseTimes;

    /**
     * MrsP 协议下某一任务的较差运行情况下的任务的时间点发布
     * */
    public static TreeMap<Integer, ArrayList<Integer>> MrsPWorstCaseTaskReleaseTimes;

    /**
     * MSRP 协议下某一个任务的较差运行情况下的任务的时间点发布
     * */
    public static TreeMap<Integer, ArrayList<Integer>> MSRPWorstCaseTaskReleaseTimes;
    /**
     * PWLP 协议下某一个任务的较差运行情况下的任务的时间点发布
     * */
    public static TreeMap<Integer, ArrayList<Integer>> PWLPWorstCaseTaskReleaseTimes;

    /**
     * 动态资源共享协议下某一个任务的最坏运行情况的任务的时间点发布
     */
    public static TreeMap<Integer, ArrayList<Integer>> DynamicWorstCaseTaskReleaseTimes;

    /**
     * 在动态资源共享协议下，任务访问局部资源时短暂提升的优先级 / 任务自旋等待全局资源时优先级短暂提升（任务持有全局资源时也提升到对应的优先级）
     */
    public static HashMap<Integer, ArrayList<Integer>> resourceRequiredPrioritiesArray;

    /**
     * {@code SchedulableResult}使用前端传递的系统环境配置参数随机生成一组任务、资源、任务发布时间等信息，并模拟任务在不同资源共享协议下的执行情况，并将执行结果返回。
     *
     * @param configurationInformation 系统环境配置参数
     */
    public static SchedulableInformation SchedulableResult(ConfigurationInformation configurationInformation)
    {
        isHistoryRecord = false;
        // 初始化一个模拟器并生成任务、资源以及任务使用资源的情况
        SimpleSystemGenerator systemGenerator = new SimpleSystemGenerator(configurationInformation);
        totalTasks = systemGenerator.generateTasks();
        // 按照 Task Id 从小到大进行排序
        totalTasks.sort((t1, t2) -> Integer.compare(t1.staticTaskId, t2.staticTaskId));
        totalResources = systemGenerator.generateResources();
        resourceRequiredPrioritiesArray = systemGenerator.generateResourceUsage(totalTasks, totalResources);
        taskReleaseTimes = systemGenerator.generateTaskReleaseTime(totalTasks);

        System.out.print("Simulation: \n");
        System.out.println("{");

// 假设tmpTotalTasks和tmpResources已经正确初始化
        System.out.println("  \"tasks\": [");
        int count = 0;
        for (int i = 0; i < totalTasks.size(); i++) {
            BasicPCB task = totalTasks.get(i);
            System.out.println("    {");
            System.out.printf("      \"id\": %d,\n", task.staticTaskId);
            System.out.printf("      \"WCCT_low\": %d,\n", task.WCCT_low);
            System.out.printf("      \"period\": %d,\n", task.period);
            System.out.printf("      \"deadline\": %d,\n", task.deadline);
            System.out.printf("      \"partition\": %d,\n", task.baseRunningCpuCore);
            System.out.printf("      \"priority\": %d,\n", task.basePriority);
            System.out.printf("      \"util\": %.2f,\n", task.utilization);
            System.out.print("      \"resource_required_index\": [");
            for (int k = 0; k < task.accessResourceIndex.size(); k++) {
                System.out.print(task.accessResourceIndex.get(k));
                if (k < task.accessResourceIndex.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print("],\n      \"access_resource_time\": [");
            for (int k = 0; k < task.resourceAccessTime.size(); k++) {
                System.out.print(task.resourceAccessTime.get(k));
                if (k < task.resourceAccessTime.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print("]\n");
            if (count < totalTasks.size() - 1) {
                System.out.println("    },");
            } else {
                System.out.println("    }");
            }
            ++count;

        }
        System.out.println("  ],");

        System.out.println("  \"resources\": [");
        for (int i = 0; i < totalResources.size(); i++) {
            BasicResource resource = totalResources.get(i);
            System.out.println("    {");
            System.out.printf("      \"id\": %d,\n", resource.id);
            System.out.printf("      \"csl\": %d,\n", resource.c_low);
            System.out.printf("      \"isGlobal\": %b,\n", resource.isGlobal);
            System.out.print("      \"partitions\": [");
            System.out.print("],\n      \"requested_tasks\": [");

            System.out.print("]\n    }");
            if (i < totalResources.size() - 1) {
                System.out.println(", ");
            }
        }
        System.out.println("\n  ]");

        System.out.println("}");


        // 初始化 MSRP 协议的配置
        MSRPMixedCriticalSystem.MSRPInitialize(totalTasks, totalResources, SimpleSystemGenerator.total_partitions, taskReleaseTimes, configurationInformation.getIsStartUpSwitch(), configurationInformation.getCriticalitySwitchTime());
        // 初始化 MrsP 协议的配置
        MrsPMixedCriticalSystem.MrsPInitialize(totalTasks, totalResources, SimpleSystemGenerator.total_partitions, taskReleaseTimes, configurationInformation.getIsStartUpSwitch(), configurationInformation.getCriticalitySwitchTime());
        // 初始化 PWLP 协议的设置
        PWLPMixedCriticalSystem.PWLPInitialize(totalTasks, totalResources, SimpleSystemGenerator.total_partitions, taskReleaseTimes, configurationInformation.getIsStartUpSwitch(), configurationInformation.getCriticalitySwitchTime());
        // 初始化动态资源共享协议的设置
        DynamicMixedCriticalSystem.DynamicInitialize(totalTasks, resourceRequiredPrioritiesArray, totalResources, SimpleSystemGenerator.total_partitions, taskReleaseTimes, configurationInformation.getIsStartUpSwitch(), configurationInformation.getCriticalitySwitchTime());

        // 运行 MSRP 协议
        MSRPMixedCriticalSystem.SystemExecute();
        // 运行 MrsP 协议
        MrsPMixedCriticalSystem.SystemExecute();
        // 运行 PWLP
        PWLPMixedCriticalSystem.SystemExecute();
        // 运行动态资源共享协议
        DynamicMixedCriticalSystem.SystemExecute();

        // task gantt chart information: 任务甘特图信息：
        List<TaskGanttInformation> taskGanttInformations = new ArrayList<>();
        for (com.example.serveside.response.TaskInformation releaseTaskInformation : MrsPMixedCriticalSystem.releaseTaskInformations)
        {
            com.example.serveside.response.GanttInformation ganttInformation = new com.example.serveside.response.GanttInformation(new ArrayList<>(), new ArrayList<>(), 30);

            int baseRunningCPUCore = -1;
            for (BasicPCB staticTask : totalTasks)
                if (releaseTaskInformation.getStaticPid() == staticTask.staticTaskId)
                {
                    baseRunningCPUCore = staticTask.baseRunningCpuCore;
                    break;
                }

            taskGanttInformations.add(new com.example.serveside.response.TaskGanttInformation(releaseTaskInformation.getStaticPid(), releaseTaskInformation.getDynamicPid(), baseRunningCPUCore, ganttInformation));
        }

        // cpu gantt chart information cpu 甘特图信息 :
        List<com.example.serveside.response.GanttInformation> cpuGanttInformations = new ArrayList<>();
        // 后面这个 TOTAL_CPU_CORE_NUM 可以用前端传递过来的信息进行更正
        for (int i = 0; i < MrsPMixedCriticalSystem.TOTAL_CPU_CORE_NUM; ++i)
        {
            cpuGanttInformations.add(new com.example.serveside.response.GanttInformation(new ArrayList<>(), new ArrayList<>(), 30));
        }

        // 生成一下 ResourceInformation
        ArrayList<ResourceInformation> resourceInformations = new ArrayList<>();
        List<List<Integer>> accessTasks = new ArrayList<>();
        for (int i = 0; i < totalResources.size(); ++i) {
            resourceInformations.add(new ResourceInformation(totalResources.get(i)));
            accessTasks.add(new ArrayList<>());
        }
        for (BasicPCB task : totalTasks) {
            for (int resourceId : task.accessResourceIndex) {
                if (!accessTasks.get(resourceId).contains(task.staticTaskId)) {
                    accessTasks.get(resourceId).add(task.staticTaskId);
                }
            }
        }
        for (int i = 0; i < resourceInformations.size(); ++i) {
            resourceInformations.get(i).setAccessTasks(accessTasks.get(i));
        }



       SchedulableInformation schedulableInformation = new SchedulableInformation(MSRPMixedCriticalSystem.isSchedulable,
                                                            MrsPMixedCriticalSystem.isSchedulable,
                                                            PWLPMixedCriticalSystem.isSchedulable,
                                                            DynamicMixedCriticalSystem.isSchedulable,
                                                            totalTasks, resourceRequiredPrioritiesArray, totalResources, resourceInformations,
                                                            taskGanttInformations, cpuGanttInformations);

        if (!isHistoryRecord && !isTestAtScale) {
            saveLogInfo(configurationInformation, schedulableInformation);
        }

        return schedulableInformation;
    }

    /**
     * {@code MrsPWorstCaseExecution} 生成指定任务{@code sufferTaskId} 在 MrsP 资源共享协议下的最差运行情况。
     *
     * @param sufferTaskId 指定任务的静态标识符
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * */
    public static WorstCaseInformation MrsPWorstCaseExecution(Integer sufferTaskId, Boolean isStartUpSwitch, Integer criticalitySwitchTime)
    {
        // 先调用 SimpleSystemGenerator 生成：在 staticTaskId 最坏情况下所有任务的发布时间
        MrsPWorstCaseTaskReleaseTimes = new MrsPWorstCase().MrsPGeneratedWorstCaseReleaseTime(totalTasks, totalResources, sufferTaskId, SimpleSystemGenerator.TOTAL_CPU_CORE_NUM);
        // 初始化一下协议的运行配置
        MrsPMixedCriticalSystem.MrsPInitialize(totalTasks, totalResources, SimpleSystemGenerator.total_partitions, MrsPWorstCaseTaskReleaseTimes, isStartUpSwitch, criticalitySwitchTime);
        // 运行协议
        MrsPMixedCriticalSystem.SystemExecute();
        // 返回调度信息
        return new WorstCaseInformation(MrsPMixedCriticalSystem.isSchedulable, MrsPMixedCriticalSystem.PackageTotalInformation());
    }

    /**
     * {@code MSRPWorstCaseExecution} 生成指定任务{@code sufferTaskId} 在 MSRP 资源共享协议下的最差运行情况。
     *
     * @param sufferTaskId 指定任务的静态标识符
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * */
    public static WorstCaseInformation MSRPWorstCaseExecution(Integer sufferTaskId, Boolean isStartUpSwitch, Integer criticalitySwitchTime) {
        // 先调用 SimpleSystemGenerator 生成：在 staticTaskId 最坏情况下所有任务的发布时间
        MSRPWorstCaseTaskReleaseTimes = new MSRPWorstCase().MSRPGeneratedWorstCaseReleaseTime(totalTasks, totalResources, sufferTaskId, SimpleSystemGenerator.TOTAL_CPU_CORE_NUM);
        // 初始化一下协议的运行配置
        MSRPMixedCriticalSystem.MSRPInitialize(totalTasks, totalResources, SimpleSystemGenerator.total_partitions, MSRPWorstCaseTaskReleaseTimes, isStartUpSwitch, criticalitySwitchTime);
        // 运行协议
        MSRPMixedCriticalSystem.SystemExecute();
        // 返回调度信息
        return new WorstCaseInformation(MSRPMixedCriticalSystem.isSchedulable, MSRPMixedCriticalSystem.PackageTotalInformation());
    }

    /**
     * {@code PWLPWorstCaseExecution} 生成指定任务{@code sufferTaskId} 在 PWLP 资源共享协议下的最差运行情况。
     *
     * @param sufferTaskId 指定任务的静态标识符
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * */
    public static WorstCaseInformation PWLPWorstCaseExecution(Integer sufferTaskId, Boolean isStartUpSwitch, Integer criticalitySwitchTime) {
        // 先调用 SimpleSystemGenerator 生成：在 staticTaskId 最坏情况下所有任务的发布时间
        PWLPWorstCaseTaskReleaseTimes = new PWLPWorstCase().PWLPGeneratedWorstCaseReleaseTime(totalTasks, totalResources, sufferTaskId, SimpleSystemGenerator.TOTAL_CPU_CORE_NUM);
        // 初始化一下协议的运行配置
        PWLPMixedCriticalSystem.PWLPInitialize(totalTasks, totalResources, SimpleSystemGenerator.total_partitions, PWLPWorstCaseTaskReleaseTimes, isStartUpSwitch, criticalitySwitchTime);
        // 运行协议
        PWLPMixedCriticalSystem.SystemExecute();
        // 返回调度信息
        return new WorstCaseInformation(PWLPMixedCriticalSystem.isSchedulable, PWLPMixedCriticalSystem.PackageTotalInformation());
    }

    /**
     * {@code DynamicWorstCaseExecution} 生成指定任务{@code sufferTaskId} 在动态资源共享协议下的最差运行情况。
     *
     * @param sufferTaskId 指定任务的静态标识符
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * */
    public static WorstCaseInformation DynamicWorstCaseExecution(Integer sufferTaskId, Boolean isStartUpSwitch, Integer criticalitySwitchTime) {
        // 先调用 SimpleSystemGenerator 生成：在 staticTaskId 最坏情况下所有任务的发布时间
        DynamicWorstCaseTaskReleaseTimes = new DynamicWorstCase().DynamicGeneratedWorstCaseReleaseTime(totalTasks, resourceRequiredPrioritiesArray, totalResources, sufferTaskId, SimpleSystemGenerator.TOTAL_CPU_CORE_NUM);
        // 初始化一下协议的运行配置
        DynamicMixedCriticalSystem.DynamicInitialize(totalTasks, resourceRequiredPrioritiesArray, totalResources, SimpleSystemGenerator.total_partitions, DynamicWorstCaseTaskReleaseTimes, isStartUpSwitch, criticalitySwitchTime);
        // 运行协议
        DynamicMixedCriticalSystem.SystemExecute();
        // 返回调度信息
        return new WorstCaseInformation(DynamicMixedCriticalSystem.isSchedulable, DynamicMixedCriticalSystem.PackageTotalInformation());
    }

    /**
     * {@code saveLogInfo} 保存本次模拟运行的结果，包含系统环境配置信息， 任务和资源的信息、任务运行情况
     * */
    public static void saveLogInfo(ConfigurationInformation configurationInformation, SchedulableInformation schedulableInformation) {
        // 创建logs文件夹
        // 创建一个 File 对象以便创建 logs 文件夹
        File directory = new File("logs");

        // 检查文件夹是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            boolean isDirectoryCreated = directory.mkdir();
            if (isDirectoryCreated) {
                System.out.print("The logs directory was successfully created!\n");
            }else {
                System.out.print("Failed to create log directory!\n");
                return ;
            }
        }

        Date d = new Date();
        DateFormat dataFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String filename = "logs/" + dataFormat.format(d)+".txt";

        try {
            // 写入本次模拟的数据
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(new LogInformation(configurationInformation, schedulableInformation, taskReleaseTimes,
                    MSRPMixedCriticalSystem.PackageTotalInformation(), MrsPMixedCriticalSystem.PackageTotalInformation(), PWLPMixedCriticalSystem.PackageTotalInformation(), DynamicMixedCriticalSystem.PackageTotalInformation()));

            Files.write(Paths.get(filename), Arrays.asList(jsonString), StandardCharsets.UTF_8);
            System.out.println("Logs write successfully!");
        } catch (IOException e) {
            System.out.println("Error：" + e.getMessage());
        }
    }

    public static SchedulableInformation ImportHistoryRecord(String filename) {
        LogInformation logInformation = null;
        isHistoryRecord = true;
        try {
            // 读取文件内容到字符串
            String content = new String(Files.readAllBytes(Paths.get("logs/"+filename)), StandardCharsets.UTF_8);

            // 创建Gson实例
            Gson gson = new Gson();

            // 将字符串反序列化为LogInformation实例
            logInformation = gson.fromJson(content, LogInformation.class);

            // 覆盖当前所使用的任务和资源信息
            totalTasks = new ArrayList<>();
            for (TaskInformation taskInformation : logInformation.taskInformations) {
                totalTasks.add(new BasicPCB(taskInformation));
            }

            totalResources = new ArrayList<>();
            for (ResourceInformation resourceInformation : logInformation.resourceInformations) {
                totalResources.add(new BasicResource(resourceInformation, totalTasks, logInformation.configurationInformation.getTotalCPUNum()));
            }

            taskReleaseTimes = logInformation.taskReleaseTimes;

            resourceRequiredPrioritiesArray = new HashMap<>();
            for (TaskInformation taskInformation : logInformation.taskInformations) {
                resourceRequiredPrioritiesArray.put(taskInformation.getStaticPid(), taskInformation.getResourceRequiredPriorities());
            }

            // 重新初始化一个模拟器覆盖之前的版本
            SimpleSystemGenerator systemGenerator = new SimpleSystemGenerator(logInformation.configurationInformation);
            // 输出或处理logInformation对象
            System.out.println("Logs read successfully!");

        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
        return new SchedulableInformation(logInformation);
    }
}
