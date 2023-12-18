package com.example.serveside.service.pwlp;

import com.example.serveside.service.CommonUse.BasicPCB;
import java.util.*;
public class ProcedureControlBlock {
    public BasicPCB basicPCB;

    /* Constructor function */
    public ProcedureControlBlock(BasicPCB basicPCB)
    {
        this.basicPCB = new BasicPCB(basicPCB);
    }

    public ProcedureControlBlock(com.example.serveside.service.pwlp.ProcedureControlBlock copy) {
        this.basicPCB = new BasicPCB(copy.basicPCB);
    }
}
