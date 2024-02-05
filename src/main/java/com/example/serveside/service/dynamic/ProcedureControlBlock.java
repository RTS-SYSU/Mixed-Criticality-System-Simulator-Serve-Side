package com.example.serveside.service.dynamic;

import com.example.serveside.service.CommonUse.BasicPCB;

import java.util.ArrayList;
public class ProcedureControlBlock {
    public BasicPCB basicPCB;

    /* 任务访问资源时获得的优先级提升
    *  1. 任务访问local-resource时优先级提升到resource-ceiling-priority
    *  2. 任务自旋等待global-resource时优先级提升到指定的priority
    *  */
    public ArrayList<Integer> resourceRequiredPriorities;

    /* Constructor function */
    public ProcedureControlBlock(BasicPCB basicPCB, ArrayList<Integer> _resourceRequiredPriorities)
    {
        this.basicPCB = new BasicPCB(basicPCB);
        this.resourceRequiredPriorities = _resourceRequiredPriorities;
    }

    public ProcedureControlBlock(ProcedureControlBlock copy) {
        this.basicPCB = new BasicPCB(copy.basicPCB);
        this.resourceRequiredPriorities = copy.resourceRequiredPriorities;
    }
}
