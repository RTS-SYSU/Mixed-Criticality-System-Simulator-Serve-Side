package com.example.serveside.controller;

import com.example.serveside.request.ConfigurationInformation;

import com.example.serveside.response.ToTalInformation;
import com.example.serveside.response.WorstCaseInformation;
import com.example.serveside.service.CommonUse.generatorTools.SimpleSystemGenerator;
import com.example.serveside.service.mrsp.MrsPMixedCriticalSystem;
import com.example.serveside.service.msrp.MSRPMixedCriticalSystem;
import com.example.serveside.service.pwlp.PWLPMixedCriticalSystem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.serveside.response.SchedulableInformation;


@Controller
@CrossOrigin
@RequestMapping(path = "/api")
public class ProtocolController {

    /**
     * {@code MSRP} 接受前端传递的关键级切换配置参数，模拟任务在 MSRP 资源共享协议下的执行，以JSON格式将任务的运行情况传递至前端。
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * @return 任务在MSRP资源共享协议下的运行情况
     * */
    @ResponseBody // @ResponseBody注解将 java 对象转化为 json 返回给前端
    @PostMapping(value = "/msrp")
    public ToTalInformation MSRP(@RequestParam(value = "isStartUpSwitch") Boolean isStartUpSwitch, @RequestParam(value = "criticalitySwitchTime") Integer criticalitySwitchTime)
    {
        // 初始化 MSRP 协议的配置
        MSRPMixedCriticalSystem.MSRPInitialize(com.example.serveside.service.FrontInterfaces.totalTasks, com.example.serveside.service.FrontInterfaces.totalResources, SimpleSystemGenerator.total_partitions, com.example.serveside.service.FrontInterfaces.taskReleaseTimes, isStartUpSwitch, criticalitySwitchTime);
        // MSRP 协议进行运行
        MSRPMixedCriticalSystem.SystemExecute();
        return MSRPMixedCriticalSystem.PackageTotalInformation();
    }

    /**
     * {@code MrsP} 接受前端传递的关键级切换配置参数，模拟任务在 MrsP 资源共享协议下的执行，并将任务的执行情况打包成 JSON 格式传递给前端。
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * */
    @ResponseBody
    @PostMapping(value = "/mrsp")
    public ToTalInformation MrsP(@RequestParam(value = "isStartUpSwitch") Boolean isStartUpSwitch, @RequestParam(value = "criticalitySwitchTime") Integer criticalitySwitchTime)
    {
        // 初始化 MrsP 协议的配置
        MrsPMixedCriticalSystem.MrsPInitialize(com.example.serveside.service.FrontInterfaces.totalTasks, com.example.serveside.service.FrontInterfaces.totalResources, SimpleSystemGenerator.total_partitions, com.example.serveside.service.FrontInterfaces.taskReleaseTimes, isStartUpSwitch, criticalitySwitchTime);
        // MrsP 协议进行运行
        MrsPMixedCriticalSystem.SystemExecute();

        return MrsPMixedCriticalSystem.PackageTotalInformation();
    }

    /**
     * {@code PWLP} 接受前端传递的关键级切换配置参数，模拟任务在 PWLP 资源共享协议下的执行，并将任务的执行情况打包成 JSON 格式传递给前端。
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * */
    @ResponseBody
    @PostMapping(value = "/pwlp")
    public ToTalInformation PWLP(@RequestParam(value = "isStartUpSwitch") Boolean isStartUpSwitch, @RequestParam(value = "criticalitySwitchTime") Integer criticalitySwitchTime)
    {
        // 初始化 PWLP 协议的配置
        PWLPMixedCriticalSystem.PWLPInitialize(com.example.serveside.service.FrontInterfaces.totalTasks, com.example.serveside.service.FrontInterfaces.totalResources, SimpleSystemGenerator.total_partitions, com.example.serveside.service.FrontInterfaces.taskReleaseTimes, isStartUpSwitch, criticalitySwitchTime);
        // PWLP 协议进行运行
        PWLPMixedCriticalSystem.SystemExecute();

        return PWLPMixedCriticalSystem.PackageTotalInformation();
    }

    /**
     * {@code IsSchedulable} 接收前端传递的系统环境配置参数，完成系统环境初始化。随后随机生成一组新任务和资源，并模拟任务在不同资源共享协议下的执行、
     * 最终以 JSON 格式将任务在不同资源共享协议下的可调度性以及任务和资源的相关信息传递至前端。
     *
     * @param requestInformation 系统环境配置参数
     * @return 任务在不同资源共享协议下的可调度性以及任务和资源的相关信息
     */
    @ResponseBody
    @PostMapping(value = "/isSchedulable")
    public SchedulableInformation IsSchedulable(@RequestBody ConfigurationInformation requestInformation)
    {
        return com.example.serveside.service.FrontInterfaces.SchedulableResult(requestInformation);
    }

    /**
     * {@code MrsPWorstCaseExecution} 接收前端传递的参数（被查看最坏运行情况的任务的静态标识符、关键级切换配置参数），使用贪心算法计算出系统中各个任务的发布时间，模拟指定任务在MrsP资源共享协议下的运行，以获取其最坏运行情况。
     * 最后以JSON格式将指定任务在MrsP协议下的最坏运行情况传递至前端。
     *
     * @param staticPid 被查看最坏运行情况的任务的静态标识符。
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * @return 指定任务在 MSRP 协议下的最坏运行情况
     */
    @ResponseBody
    @PostMapping(value = "/mrspWorstCase")
    public WorstCaseInformation MrsPWorstCaseExecution(@RequestParam(value = "staticPid") Integer staticPid, @RequestParam(value = "isStartUpSwitch") Boolean isStartUpSwitch, @RequestParam(value = "criticalitySwitchTime") Integer criticalitySwitchTime)
    {
        return com.example.serveside.service.FrontInterfaces.MrsPWorstCaseExecution(staticPid, isStartUpSwitch, criticalitySwitchTime);
    }

    /**
     * {@code MSRPWorstCaseExecution} 接收前端传递的参数（被查看最坏运行情况的任务的静态标识符、关键级切换配置参数），使用贪心算法计算出系统中各个任务的发布时间，模拟指定任务在MSRP资源共享协议下的运行，以获取其最坏运行情况。
     * 最后以JSON格式将指定任务在MSRP协议下的最坏运行情况传递至前端。
     *
     * @param staticPid 被查看最坏运行情况的任务的静态标识符。
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * @return 指定任务在 MSRP 协议下的最坏运行情况
     */
    @ResponseBody
    @PostMapping(value = "/msrpWorstCase")
    public WorstCaseInformation MSRPWorstCaseExecution(@RequestParam(value = "staticPid") Integer staticPid, @RequestParam(value = "isStartUpSwitch") Boolean isStartUpSwitch, @RequestParam(value = "criticalitySwitchTime") Integer criticalitySwitchTime)
    {
        return com.example.serveside.service.FrontInterfaces.MSRPWorstCaseExecution(staticPid, isStartUpSwitch, criticalitySwitchTime);
    }

    /**
     * {@code PWLPWorstCaseExecution} 接收前端传递的参数（被查看最坏运行情况的任务的静态标识符、关键级切换配置参数），使用贪心算法计算出系统中各个任务的发布时间，模拟指定任务在PWLP资源共享协议下的运行，以获取其最坏运行情况。
     * 最后以JSON格式将指定任务在PWLP协议下的最坏运行情况传递至前端。
     *
     * @param staticPid 被查看最坏运行情况的任务的静态标识符。
     * @param isStartUpSwitch 是否开启关键级切换
     * @param criticalitySwitchTime 关键级切换发生的时间点
     * @return 指定任务在 MSRP 协议下的最坏运行情况
     */
    @ResponseBody
    @PostMapping(value = "/pwlpWorstCase")
    public WorstCaseInformation PWLPWorstCaseExecution(@RequestParam(value = "staticPid") Integer staticPid, @RequestParam(value = "isStartUpSwitch") Boolean isStartUpSwitch, @RequestParam(value = "criticalitySwitchTime") Integer criticalitySwitchTime)
    {
        return com.example.serveside.service.FrontInterfaces.PWLPWorstCaseExecution(staticPid, isStartUpSwitch, criticalitySwitchTime);
    }

}
