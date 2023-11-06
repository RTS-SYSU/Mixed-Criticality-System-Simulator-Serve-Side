package com.example.serveside.response;

import java.util.List;

public class GanttInformation {
    /* 记录事件信息*/
    private List<com.example.serveside.response.EventInformation> eventInformations;

    /* 时间轴长度 */
    private Integer timeAxisLength;

    private List<EventTimePoint> eventTimePoints;

    public GanttInformation(List<EventInformation> eventInformations, List<EventTimePoint> eventTimePoints, Integer timeAxisLength) {
        this.eventInformations = eventInformations;
        this.eventTimePoints = eventTimePoints;
        this.timeAxisLength = timeAxisLength;
    }

    public List<EventInformation> getEventInformations() {
        return this.eventInformations;
    }

    public void setEventInformations(List<EventInformation> eventInformations) {
        this.eventInformations = eventInformations;
    }

    public Integer getTimeAxisLength()
    {
        return this.timeAxisLength;
    }

    public void setTimeAxisLength(Integer timeAxisLength)
    {
        this.timeAxisLength = timeAxisLength;
    }

    public List<EventTimePoint> getEventTimePoints() { return this.eventTimePoints; }

    public void setEventTimePoints(List<EventTimePoint> eventTimePoints) { this.eventTimePoints = eventTimePoints; }
}
