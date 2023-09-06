package com.example.serveside.response;

import java.util.List;

public class GanttInformation {
    private List<TaskInformation> taskInformations;

    public GanttInformation(List<TaskInformation> taskInformations) {
        this.taskInformations = taskInformations;
    }

    public List<TaskInformation> getTaskInformations() {
        return this.taskInformations;
    }

    public void setTaskInformations(List<TaskInformation> taskInformations) {
        this.taskInformations = taskInformations;
    }
}
