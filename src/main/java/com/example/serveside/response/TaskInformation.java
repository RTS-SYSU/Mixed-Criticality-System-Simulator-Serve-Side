package com.example.serveside.response;

import java.util.ArrayList;

public class TaskInformation
{
    /* 静态 Pid */
    private Integer staticPid;

    /* 动态 Pid */
    private Integer dynamicPid;

    /* 优先级 */
    private Integer priority;

    /* 关键级 */
    private Integer criticality;

    /* 任务发布时间 */
    private Integer releaseTime;

    /* 资源访问的时间 */
    private ArrayList<Integer> resourceAccessTime;

    /* 访问的资源 */
    private ArrayList<Integer> resourceAccessIndex;

    public TaskInformation(Integer staticPid, Integer dynamicPid, Integer priority, Integer criticality, Integer releaseTime, ArrayList<Integer> resourceAccessTime, ArrayList<Integer> resourceAccessIndex)
    {
        this.staticPid = staticPid;
        this.dynamicPid = dynamicPid;
        this.priority = priority;
        this.criticality = criticality;
        this.releaseTime = releaseTime;
        this.resourceAccessTime = resourceAccessTime;
        this.resourceAccessIndex = resourceAccessIndex;
    }

    public TaskInformation(com.example.serveside.service.msrp.entity.ProcedureControlBlock procedureControlBlock, Integer systemClock)
    {
        this.staticPid = procedureControlBlock.staticTaskId;
        this.dynamicPid = procedureControlBlock.dynamicTaskId;
        this.priority = procedureControlBlock.priorities.peek();
        this.criticality = procedureControlBlock.criticality;
        this.resourceAccessTime = procedureControlBlock.resourceAccessTime;
        this.resourceAccessIndex = procedureControlBlock.accessResourceIndex;
        this.releaseTime = systemClock;
    }

    public TaskInformation(com.example.serveside.service.mrsp.entity.ProcedureControlBlock procedureControlBlock, Integer systemClock)
    {
        this.staticPid = procedureControlBlock.staticTaskId;
        this.dynamicPid = procedureControlBlock.dynamicTaskId;
        this.priority = procedureControlBlock.priorities.peek();
        this.criticality = procedureControlBlock.criticality;
        this.resourceAccessTime = procedureControlBlock.resourceAccessTime;
        this.resourceAccessIndex = procedureControlBlock.accessResourceIndex;
        this.releaseTime = systemClock;
    }

    public Integer getStaticPid() {
        return this.staticPid;
    }

    public Integer getDynamicPid() {
        return this.dynamicPid;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public Integer getCriticality() {
        return this.criticality;
    }

    public Integer getReleaseTime() {
        return this.releaseTime;
    }

    public ArrayList<Integer> getResourceAccessTime() { return this.resourceAccessTime; }

    public ArrayList<Integer> getResourceAccessIndex() { return this.resourceAccessIndex; }

    public void setStaticPid(Integer staticPid) {
        this.staticPid = staticPid;
    }

    public void setDynamicPid(Integer dynamicPid) {
        this.dynamicPid = dynamicPid;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setCriticality(Integer criticality) {
        this.criticality = criticality;
    }

    public void setReleaseTime(Integer releaseTime) { this.releaseTime = releaseTime; }

    public void setResourceAccessTime(ArrayList<Integer> resourceAccessTime) { this.resourceAccessTime = resourceAccessTime; }

    public void setResourceAccessIndex(ArrayList<Integer> resourceAccessIndex) { this.resourceAccessIndex = resourceAccessIndex; }
}
