package com.example.serveside.service.mrsp.entity;

import java.util.ArrayList;
import com.example.serveside.service.CommonUse.BasicResource;

public class Resource {

    /* Keep the task that is waiting for the resource. */
    public ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingQueue;

    /* 任务的基本信息 */
    public BasicResource basicResource;


    public Resource(BasicResource basicResource) {
        this.basicResource = new BasicResource(basicResource);
        waitingQueue = new ArrayList<>();
    }
}
