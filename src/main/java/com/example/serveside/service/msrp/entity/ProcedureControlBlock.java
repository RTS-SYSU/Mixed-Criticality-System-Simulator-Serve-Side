package com.example.serveside.service.msrp.entity;

import com.example.serveside.service.CommonUse.BasicPCB;

import java.util.ArrayList;
import java.util.Stack;

public class ProcedureControlBlock {
    public BasicPCB basicPCB;
    public ProcedureControlBlock(BasicPCB basicPCB)
    {
        this.basicPCB = new BasicPCB(basicPCB);
    }

    public ProcedureControlBlock(ProcedureControlBlock copy)
    {
        this.basicPCB = new BasicPCB(copy.basicPCB);
    }
}
