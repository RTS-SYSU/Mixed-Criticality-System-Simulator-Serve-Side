package com.example.serveside.response;

public class EventTimePoint {
    /* 静态 Pid */
    private Integer staticPid;

    /* 动态 Pid */
    private Integer dynamicPid;

    /* 所发生的事件 */
    private String event;

    /* 事件发生的时间点 */
    private Integer eventTime;

    /* 访问/释放的资源 ID */
    private Integer resourceId;

    /* 构造函数 */
    public EventTimePoint(Integer staticPid, Integer dynamicPid, String event, Integer eventTime, Integer resourceId)
    {
        this.staticPid = staticPid;
        this.dynamicPid = dynamicPid;
        this.event = event;
        this.eventTime = eventTime;
        this.resourceId = resourceId;
    }

    public void setStaticPid(Integer staticPid)
    {
        this.staticPid = staticPid;
    }

    public Integer getStaticPid()
    {
        return this.staticPid;
    }

    public void setDynamicPid(Integer dynamicPid)
    {
        this.dynamicPid = dynamicPid;
    }

    public Integer getDynamicPid()
    {
        return this.dynamicPid;
    }

    public void setEvent(String event)
    {
        this.event = event;
    }

    public String getEvent()
    {
        return this.event;
    }

    public void setEventTime(Integer eventTime)
    {
        this.eventTime = eventTime;
    }

    public Integer getEventTime()
    {
        return this.eventTime;
    }

    public Integer getResourceId() { return this.resourceId; }

    public void setResourceId(Integer resourceId) { this.resourceId = resourceId; }
}
