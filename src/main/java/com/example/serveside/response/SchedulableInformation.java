package com.example.serveside.response;

import com.example.serveside.service.CommonUse.BasicPCB;

import java.util.*;


/**
 * {@code ResourceInformation} 是后端向前端传递信息的载体，用于后端向前端传递在一次模拟运行中不同资源共享协议的可调度性、系统生成的任务和资源信息。
 */
public class SchedulableInformation
{
    /**
     *  MSRP 协议是否可调度。
     */
    private Boolean msrpSchedulable;

    /**
     *  MrsP 协议是否可调度。
     */
    private Boolean mrspSchedulable;

    /**
     *  PWLP 协议是否可调度。
     */
    private Boolean pwlpSchedulable;

    /**
     *  系统任务的信息。
     */
    private List<TaskInformation> taskInformations;

    /**
     *  系统资源的信息。
     */
    private List<ResourceInformation> resourceInformations;

    /**
     * 任务对应的甘特图信息（只有任务标识符和处理器信息，执行状态等为空）。
     */
    private List<TaskGanttInformation> taskGanttInformations;

    /**
     * 处理器对应的甘特图信息（只提供处理器个数）。
     */
    private List<GanttInformation> cpuGanttInformations;

    public SchedulableInformation(Boolean _MSRPSchedulable, Boolean _MrsPSchedulable, Boolean _PWLPSchedulable, ArrayList<BasicPCB> _taskInformations, ArrayList<ResourceInformation> _resourceInformations, List<TaskGanttInformation> _taskGanttInformations, List<GanttInformation> _cpuGanttInformations)
    {
        this.msrpSchedulable = _MSRPSchedulable;
        this.mrspSchedulable = _MrsPSchedulable;
        this.pwlpSchedulable = _PWLPSchedulable;
        this.resourceInformations = _resourceInformations;
        this.taskGanttInformations = _taskGanttInformations;
        this.cpuGanttInformations = _cpuGanttInformations;

        this.taskInformations = new ArrayList<>();
        for (BasicPCB taskInformation : _taskInformations) {
            this.taskInformations.add(new TaskInformation(taskInformation));
        }
    }

    /* 以下均是上面属性的 setter 和 getter 函数。 */

    public void setResourceInformations(List<ResourceInformation> _resourceInformations) { this.resourceInformations = _resourceInformations; }

    public List<ResourceInformation> getResourceInformations() { return this.resourceInformations; }

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
    public void setPWLPSchedulable(Boolean _PWLPSchedulable) { this.pwlpSchedulable = _PWLPSchedulable; }

    public Boolean getPWLPSchedulable() { return this.pwlpSchedulable; }

    public void setTaskInformations(List<TaskInformation> _taskInformations) { this.taskInformations = _taskInformations; }

    public List<TaskInformation> getTaskInformations() { return this.taskInformations; }

    public void setTaskGanttInformations(List<TaskGanttInformation> _taskGanttInformations) { this.taskGanttInformations = _taskGanttInformations; }

    public List<TaskGanttInformation> getTaskGanttInformations() { return this.taskGanttInformations; }

    public void setCpuGanttInformations(List<GanttInformation> _cpuGanttInformations) { this.cpuGanttInformations = _cpuGanttInformations; }

    public List<GanttInformation> getCpuGanttInformations() { return this.cpuGanttInformations; }

}
