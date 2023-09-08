package com.example.serveside.response;

import java.util.List;

public class GanttInformation {
    private List<EventInformation> eventInformations;

    /* 时间轴长度 */
    private Integer timeAxisLength;

    private List<CPUEventTimePoint> cpuEventTimePoints;

    public GanttInformation(List<EventInformation> eventInformations, List<CPUEventTimePoint> cpuEventTimePoints, Integer timeAxisLength) {
        this.eventInformations = eventInformations;
        this.cpuEventTimePoints = cpuEventTimePoints;
        this.timeAxisLength = timeAxisLength;
    }

    public List<EventInformation> getEventInformations() {
        return this.eventInformations;
    }

    public void setEventInformations(List<EventInformation> eventInformations, List<CPUEventTimePoint> cpuEventTimePoints) {
        this.eventInformations = eventInformations;
        this.cpuEventTimePoints = cpuEventTimePoints;
    }

    public Integer getTimeAxisLength()
    {
        return this.timeAxisLength;
    }

    public void setTimeAxisLength(Integer timeAxisLength)
    {
        this.timeAxisLength = timeAxisLength;
    }

    public List<CPUEventTimePoint> getCpuEventTimePoints() { return this.cpuEventTimePoints; }

    public void setCpuEventTimePoints(List<CPUEventTimePoint> cpuEventTimePoints) { this.cpuEventTimePoints = cpuEventTimePoints; }
}
