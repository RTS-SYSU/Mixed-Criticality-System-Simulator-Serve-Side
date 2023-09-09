package com.example.serveside.response;

import java.util.*;

public class ToTalInformation {
    private List<GanttInformation> ganttInformations;

    private List<com.example.serveside.service.msrp.entity.TaskInformation> taskInformations;

    public ToTalInformation(List<GanttInformation> ganttInformations, List<com.example.serveside.service.msrp.entity.TaskInformation> taskInformations)
    {
        this.ganttInformations = ganttInformations;
        this.taskInformations = taskInformations;
    }

    public void setGanttInformations(List<GanttInformation> ganttInformations) { this.ganttInformations = ganttInformations; }

    public List<GanttInformation> getGanttInformations() { return this.ganttInformations; }

    public void setTaskInformations(List<com.example.serveside.service.msrp.entity.TaskInformation> taskInformations) { this.taskInformations = taskInformations; }

    public List<com.example.serveside.service.msrp.entity.TaskInformation> getTaskInformations() { return this.taskInformations; }
}
