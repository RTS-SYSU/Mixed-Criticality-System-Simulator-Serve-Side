package com.example.serveside.service.dynamic;

import com.example.serveside.service.CommonUse.BasicResource;

import java.util.ArrayList;

public class Resource {
    /* Keep the task that is waiting for the resource. */
    public ArrayList<ProcedureControlBlock> waitingQueue;

    /* 任务的基本信息 */
    public BasicResource basicResource;


    public Resource(BasicResource basicResource) {
        this.basicResource = new BasicResource(basicResource);
        waitingQueue = new ArrayList<>();
    }
}
