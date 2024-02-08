package com.example.serveside.response;

import com.example.serveside.request.ConfigurationInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class LogInformation {
    public ConfigurationInformation configurationInformation;

    public List<TaskInformation> taskInformations;

    public List<ResourceInformation> resourceInformations;

    /**
     *  MSRP 协议是否可调度。
     */
    public Boolean msrpSchedulable;

    /**
     *  MrsP 协议是否可调度。
     */
    public Boolean mrspSchedulable;

    /**
     *  PWLP 协议是否可调度。
     */
    public Boolean pwlpSchedulable;

    /**
     *  动态资源共享协议是否可调度。
     */
    public Boolean dynamicSchedulable;

    /**
     * 任务发布的时间点。
     * */
    public TreeMap<Integer, ArrayList<Integer>> taskReleaseTimes;

    public ToTalInformation msrpTotalInformation;

    public ToTalInformation mrspTotalInformation;

    public ToTalInformation pwlpTotalInformation;

    public ToTalInformation dynamicTotalInformation;



    public LogInformation(ConfigurationInformation _configurationInformation, SchedulableInformation schedulableInformation, TreeMap<Integer, ArrayList<Integer>> _taskReleaseTimes,
                          ToTalInformation _msrpTotalInformation, ToTalInformation _mrspTotalInformation, ToTalInformation _pwlpTotalInformation, ToTalInformation _dynamicTotalInformation)
    {
        configurationInformation = _configurationInformation;
        taskInformations = schedulableInformation.getTaskInformations();
        resourceInformations = schedulableInformation.getResourceInformations();
        msrpSchedulable = schedulableInformation.getMsrpSchedulable();
        mrspSchedulable = schedulableInformation.getMrspSchedulable();
        pwlpSchedulable = schedulableInformation.getPWLPSchedulable();
        dynamicSchedulable = schedulableInformation.getDynamicSchedulable();
        taskReleaseTimes = _taskReleaseTimes;
        msrpTotalInformation = _msrpTotalInformation;
        mrspTotalInformation = _mrspTotalInformation;
        pwlpTotalInformation = _pwlpTotalInformation;
        dynamicTotalInformation = _dynamicTotalInformation;
    }
}
