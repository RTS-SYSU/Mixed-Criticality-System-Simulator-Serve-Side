package com.example.serveside.response;

import java.util.*;

public class SchedulableInformation
{
    /* MSRP 协议是否可调度 */
    private Boolean msrpSchedulable;

    /* MrsP 协议是否可调度 */
    private Boolean mrspSchedulable;

    /* 任务的基本信息 */
    private List<TaskInformation> taskInformations;

    /* 任务的甘特图信息 */
    private List<TaskGanttInformation> taskGanttInformations;

    /* cpu 的甘特图信息 */
    private List<GanttInformation> cpuGanttInformations;

    public SchedulableInformation(Boolean _MSRPSchedulable, Boolean _MrsPSchedulable, List<TaskInformation> _taskInformations, List<TaskGanttInformation> _taskGanttInformations, List<GanttInformation> _cpuGanttInformations)
    {
        this.msrpSchedulable = _MSRPSchedulable;
        this.mrspSchedulable = _MrsPSchedulable;
        this.taskInformations = _taskInformations;
        this.taskGanttInformations = _taskGanttInformations;
        this.cpuGanttInformations = _cpuGanttInformations;
    }

    public void setMsrpSchedulable(Boolean _MSRPSchedulable)
    {
        this.msrpSchedulable = _MSRPSchedulable;
    }

    public Boolean getMsrpSchedulable()
    {
        return this.msrpSchedulable;
    }

    public void setMrspSchedulable(Boolean _MrsPSchedulable)
    {
        this.mrspSchedulable = _MrsPSchedulable;
    }

    public Boolean getMrspSchedulable()
    {
        return this.mrspSchedulable;
    }

    public void setTaskInformations(List<TaskInformation> _taskInformations) { this.taskInformations = _taskInformations; }

    public List<TaskInformation> getTaskInformations() { return this.taskInformations; }

    public void setTaskGanttInformations(List<TaskGanttInformation> _taskGanttInformations) { this.taskGanttInformations = _taskGanttInformations; }

    public List<TaskGanttInformation> getTaskGanttInformations() { return this.taskGanttInformations; }

    public void setCpuGanttInformations(List<GanttInformation> _cpuGanttInformations) { this.cpuGanttInformations = _cpuGanttInformations; }

    public List<GanttInformation> getCpuGanttInformations() { return this.cpuGanttInformations; }
}
