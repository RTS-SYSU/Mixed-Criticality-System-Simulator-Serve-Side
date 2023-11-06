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
        this.resourceAccessIndex = resourceAccessIndex;

        this.resourceAccessTime = new ArrayList<>();
        for (Integer accessTime : resourceAccessTime)
        {
            this.resourceAccessTime.add(accessTime + releaseTime);
        }
    }

    public TaskInformation(com.example.serveside.service.msrp.entity.ProcedureControlBlock procedureControlBlock, Integer systemClock)
    {
        this.staticPid = procedureControlBlock.basicPCB.staticTaskId;
        this.dynamicPid = procedureControlBlock.basicPCB.dynamicTaskId;
        this.priority = procedureControlBlock.basicPCB.priorities.peek();
        this.criticality = procedureControlBlock.basicPCB.criticality;
        this.resourceAccessIndex = procedureControlBlock.basicPCB.accessResourceIndex;
        this.releaseTime = systemClock;

        this.resourceAccessTime = new ArrayList<>();
        for (Integer accessTime : procedureControlBlock.basicPCB.resourceAccessTime)
        {
            this.resourceAccessTime.add(accessTime + systemClock);
        }
    }

    public TaskInformation(com.example.serveside.service.mrsp.entity.ProcedureControlBlock procedureControlBlock, Integer systemClock)
    {
        this.staticPid = procedureControlBlock.basicPCB.staticTaskId;
        this.dynamicPid = procedureControlBlock.basicPCB.dynamicTaskId;
        this.priority = procedureControlBlock.basicPCB.priorities.peek();
        this.criticality = procedureControlBlock.basicPCB.criticality;
        this.resourceAccessIndex = procedureControlBlock.basicPCB.accessResourceIndex;
        this.releaseTime = systemClock;

        this.resourceAccessTime = new ArrayList<>();
        for (Integer accessTime : procedureControlBlock.basicPCB.resourceAccessTime)
        {
            this.resourceAccessTime.add(accessTime + systemClock);
        }
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
