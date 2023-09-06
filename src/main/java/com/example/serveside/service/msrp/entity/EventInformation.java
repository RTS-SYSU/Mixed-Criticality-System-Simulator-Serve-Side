package com.example.serveside.service.msrp.entity;

public class EventInformation
{
    public int systemCriticality;

    /* Time of the event. */
    /* The time when the event started. */
    public int startTime;

    /* The time when the event ended. */
    public int endTime;

    /* Show that the task is waiting for global resource. */
    public boolean spin;

    /* Determine that the task is accessing the global resource or not. */
    public boolean isAccessGlobalResource;

    /* Determine that the task is accessing the local resource or not. */
    public boolean isAccessLocalResource;

    /* Request resource index. */
    public int requestResourceIndex;

    /* Dynamic task id. */
    public int dynamicTaskId;

    /* Static task id. */
    public int staticTaskId;

    public EventInformation()
    {
        startTime = 0;
        endTime = 0;
        spin = false;
        isAccessGlobalResource = isAccessLocalResource = false;
        requestResourceIndex = -1;
        dynamicTaskId = staticTaskId = -1;
        systemCriticality = 0;
    }

    public EventInformation(int _startTime, ProcedureControlBlock runningTask, int criticality_indicator)
    {
        startTime = _startTime;
        dynamicTaskId = runningTask.dynamicTaskId;
        staticTaskId = runningTask.staticTaskId;
        spin = runningTask.spin;
        isAccessGlobalResource = runningTask.isAccessGlobalResource;
        isAccessLocalResource = runningTask.isAccessLocalResource;

        requestResourceIndex = runningTask.isAccessGlobalResource || runningTask.isAccessLocalResource || runningTask.spin ? runningTask.accessResourceIndex.get(runningTask.requestResourceTh ) : -1;

        systemCriticality = criticality_indicator;
    }
}
