package com.example.serveside.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.example.serveside.response.GanttInformation;
import com.example.serveside.service.CommonUse.generatorTools.SimpleSystemGenerator;
import com.example.serveside.service.CommonUse.BasicPCB;
import com.example.serveside.service.CommonUse.BasicResource;
import com.example.serveside.response.SchedulableInformation;
import com.example.serveside.response.TaskGanttInformation;
import com.example.serveside.service.mrsp.entity.ProcedureControlBlock;

public class FrontInterfaces
{
    /*
    * 生成任务的基本参数
    * */
    public static ArrayList<BasicPCB> totalTasks;

    /*
    * 生成资源的基本参数
    * */
    public static ArrayList<BasicResource> totalResources;

    /*
    * 任务发布的时间点
    * */
    public static LinkedHashMap<Integer, ArrayList<Integer>> taskReleaseTimes;

    /*
     * 接口1： 前端传递配置给后端（配置方面先不管）
     * 后端根据配置生成 tasks、resources以及任务如何使用资源，然后分别运行3种协议来看看是否可调度，并将是否可调度返回前端
     */
    public static SchedulableInformation SchedulableResult()
    {
        // 初始化一个模拟器并生成任务、资源以及任务使用资源的情况
        SimpleSystemGenerator systemGenerator = new SimpleSystemGenerator();
        totalTasks = systemGenerator.generateTasks();
        totalResources = systemGenerator.generateResources();
        systemGenerator.generateResourceUsage(totalTasks, totalResources);
        taskReleaseTimes = systemGenerator.generateTaskReleaseTime(totalTasks);
//        totalTasks = systemGenerator.testGenerateTask();
//        totalResources = systemGenerator.testGenerateResources();
//        systemGenerator.testGenerateResourceUsage(totalTasks, totalResources);
//        taskReleaseTimes = systemGenerator.testGenerateTaskReleaseTime(totalTasks);


        // 初始化 MSRP 协议的配置
        com.example.serveside.service.msrp.entity.MixedCriticalSystem.MSRPInitialize(totalTasks, totalResources, systemGenerator.total_partitions, taskReleaseTimes);
        // 初始化 MrsP 协议的配置
        com.example.serveside.service.mrsp.entity.MixedCriticalSystem.MrsPInitialize(totalTasks, totalResources, systemGenerator.total_partitions, taskReleaseTimes);
        // 运行 MSRP 协议
        com.example.serveside.service.msrp.entity.MixedCriticalSystem.SystemExecute();
        // 运行 MrsP 协议
        com.example.serveside.service.mrsp.entity.MixedCriticalSystem.SystemExecute();


        // 先给出 task gantt chart 上任务的基本信息
        // task gantt chart information: 任务甘特图信息：
        List<TaskGanttInformation> taskGanttInformations = new ArrayList<>();
        for (com.example.serveside.response.TaskInformation releaseTaskInformation : com.example.serveside.service.mrsp.entity.MixedCriticalSystem.releaseTaskInformations)
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

        // 再给出一共有几个 cpu 甘特图
        // cpu gantt chart information cpu 甘特图信息 :
        List<com.example.serveside.response.GanttInformation> cpuGanttInformations = new ArrayList<>();
        // 后面这个 TOTAL_CPU_CORE_NUM 可以用前端传递过来的信息进行更正
        for (int i = 0; i < com.example.serveside.service.mrsp.entity.MixedCriticalSystem.TOTAL_CPU_CORE_NUM; ++i)
        {
            cpuGanttInformations.add(new com.example.serveside.response.GanttInformation(new ArrayList<>(), new ArrayList<>(), 30));
        }

        return new SchedulableInformation(com.example.serveside.service.msrp.entity.MixedCriticalSystem.isSchedulable,
                com.example.serveside.service.mrsp.entity.MixedCriticalSystem.isSchedulable,
                com.example.serveside.service.mrsp.entity.MixedCriticalSystem.releaseTaskInformations,
                taskGanttInformations, cpuGanttInformations);
    }
}
