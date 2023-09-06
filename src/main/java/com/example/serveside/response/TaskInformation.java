package com.example.serveside.response;

public class TaskInformation {
    /* 静态 Pid */
    private Integer staticPid;

    /* 动态 Pid */
    private Integer dynamicPid;

    /* 任务在 CPU 上的状态 */
    private String state;

    /* 任务开始占据 CPU 的时间 */
    private Integer startTime;

    /* 任务结束占据 CPU 的时间 */
    private Integer endTime;

    /* 构造函数 */
    public TaskInformation(Integer staticPid, Integer dynamicPid, String state, Integer startTime, Integer endTime)
    {
        this.staticPid = staticPid;
        this.dynamicPid = dynamicPid;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getStaticPid() {
        return this.staticPid;
    }

    public Integer getDynamicPid() {
        return this.dynamicPid;
    }

    public String getState() {
        return this.state;
    }

    public Integer getStartTime() {
        return this.startTime;
    }

    public Integer getEndTime() {
        return this.endTime;
    }

    public void setStaticPid(Integer staticPid) {
        this.staticPid = staticPid;
    }

    public void setDynamicPid(Integer dynamicPid) {
        this.dynamicPid = dynamicPid;
    }

    public void setState(String state){
        this.state = state;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }
}
