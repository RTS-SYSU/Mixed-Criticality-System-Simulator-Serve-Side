package com.example.serveside.response;

import java.util.*;

public class TaskGanttInformation
{
    /* 静态 Pid */
    private Integer staticPid;

    /* 动态 Pid */
    private Integer dynamicPid;

    /* 任务运行所在的 CPU 核*/
    private Integer runningCPUCore;

    /* 任务的运行状态 */
    private com.example.serveside.response.GanttInformation taskGanttInformation;

    public void setStaticPid(Integer staticPid)
    {
        this.staticPid = staticPid;
    }

    public Integer getStaticPid()
    {
        return this.staticPid;
    }

    public void setDynamicPid(Integer dynamicPid)
    {
        this.dynamicPid = dynamicPid;
    }

    public Integer getDynamicPid()
    {
        return this.dynamicPid;
    }

    public void setRunningCPUCore(Integer runningCPUCore)
    {
        this.runningCPUCore = runningCPUCore;
    }

    public Integer getRunningCPUCore()
    {
        return this.runningCPUCore;
    }

    public void setTaskGanttInformation(com.example.serveside.response.GanttInformation taskGanttInformation)
    {
        this.taskGanttInformation = taskGanttInformation;
    }

    public com.example.serveside.response.GanttInformation getTaskGanttInformation()
    {
        return this.taskGanttInformation;
    }

    public TaskGanttInformation(Integer staticPid, Integer dynamicPid, Integer runningCPUCore, com.example.serveside.response.GanttInformation taskGanttInformation)
    {
        this.staticPid = staticPid;
        this.dynamicPid = dynamicPid;
        this.runningCPUCore = runningCPUCore;
        this.taskGanttInformation = taskGanttInformation;
    }
}
