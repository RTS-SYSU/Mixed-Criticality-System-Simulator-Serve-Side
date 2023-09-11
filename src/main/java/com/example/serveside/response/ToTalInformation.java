package com.example.serveside.response;

import java.util.*;

public class ToTalInformation {
    /* CPU 甘特图的信息 */
    private List<GanttInformation> cpuGanttInformations;

    /* 任务信息 */
    private List<com.example.serveside.service.msrp.entity.TaskInformation> taskInformations;

    /* 任务甘特图信息 */
    private List<TaskGanttInformation> taskGanttInformations;

    public ToTalInformation(List<GanttInformation> cpuGanttInformations, List<com.example.serveside.service.msrp.entity.TaskInformation> taskInformations, List<com.example.serveside.response.TaskGanttInformation> taskGanttInformations)
    {
        this.cpuGanttInformations = cpuGanttInformations;
        this.taskInformations = taskInformations;
        this.taskGanttInformations = taskGanttInformations;
    }

    public void setGanttInformations(List<GanttInformation> cpuGanttInformations) { this.cpuGanttInformations = cpuGanttInformations; }

    public List<GanttInformation> getCpuGanttInformations() { return this.cpuGanttInformations; }

    public void setTaskInformations(List<com.example.serveside.service.msrp.entity.TaskInformation> taskInformations) { this.taskInformations = taskInformations; }

    public List<com.example.serveside.service.msrp.entity.TaskInformation> getTaskInformations() { return this.taskInformations; }

    public void setTaskGanttInformations(List<com.example.serveside.response.TaskGanttInformation> taskGanttInformations) { this.taskGanttInformations = taskGanttInformations; }

    public List<com.example.serveside.response.TaskGanttInformation> getTaskGanttInformations() { return this.taskGanttInformations; }
}
