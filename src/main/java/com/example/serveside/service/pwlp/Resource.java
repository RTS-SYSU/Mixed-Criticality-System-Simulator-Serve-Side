package com.example.serveside.service.pwlp;

import java.util.LinkedList;
import com.example.serveside.service.CommonUse.BasicResource;


public class Resource {

    /* Keep the task that is waiting for the resource. */
    public LinkedList<ProcedureControlBlock> waitingQueue;

    /* 任务的一些基本信息 */
    public BasicResource basicResource;

    public Resource(BasicResource basicResource) {

        this.basicResource = new BasicResource(basicResource);
        waitingQueue = new LinkedList<>();
    }
}
