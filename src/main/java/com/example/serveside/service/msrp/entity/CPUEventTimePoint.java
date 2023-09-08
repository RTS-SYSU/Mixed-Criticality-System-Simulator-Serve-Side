package com.example.serveside.service.msrp.entity;

public class CPUEventTimePoint
{
    /* Time of the event. */
    /* The time when the event started. */
    public int eventTime;

    /* Dynamic task id. */
    public int dynamicTaskId;

    /* Static task id. */
    public int staticTaskId;

    /* State what event happened. */
    public String event;

    /* The resource id that the task apply for. */
    public int resourceId;

    public CPUEventTimePoint(int eventTime, int staticTaskId, int dynamicTaskId, String event)
    {
        this.eventTime = eventTime;
        this.dynamicTaskId = dynamicTaskId;
        this.staticTaskId = staticTaskId;
        this.event = event;
        this.resourceId = -1;
    }

    public CPUEventTimePoint(int eventTime, int staticTaskId, int dynamicTaskId, String event, int resourceId)
    {
        this.eventTime = eventTime;
        this.dynamicTaskId = dynamicTaskId;
        this.staticTaskId = staticTaskId;
        this.event = event;
        this.resourceId = resourceId;
    }
}
