package com.example.serveside.service.msrp;

import java.util.ArrayList;
import com.example.serveside.service.CommonUse.BasicResource;


public class Resource {

    /**
     * 资源的FIFO请求队列
     */
    public ArrayList<ProcedureControlBlock> waitingQueue;

    /**
     *  资源信息
     */
    public BasicResource basicResource;

    public Resource(BasicResource basicResource) {

        this.basicResource = new BasicResource(basicResource);
        waitingQueue = new ArrayList<>();
    }
}
