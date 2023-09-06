package com.example.serveside.response;

import java.util.List;

public class GanttInformation {
    private List<EventInformation> eventInformations;

    /* 时间轴长度 */
    private Integer timeAxisLength;

    public GanttInformation(List<EventInformation> eventInformations, Integer timeAxisLength) {
        this.eventInformations = eventInformations;
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
}
