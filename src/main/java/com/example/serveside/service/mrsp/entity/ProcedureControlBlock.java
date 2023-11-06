package com.example.serveside.service.mrsp.entity;

import java.util.ArrayList;
import java.util.Stack;
import com.example.serveside.service.CommonUse.BasicPCB;

public class ProcedureControlBlock {
    public BasicPCB basicPCB;

    /* 迁移之后所在的运行 CPU */
    public int immigrateRunningCpuCore;

    /* task 是否在帮助其他任务完成资源访问 */
    public boolean isHelp;

    /* Constructor function */
    public ProcedureControlBlock(BasicPCB basicPCB)
    {
        this.basicPCB = new BasicPCB(basicPCB);

        immigrateRunningCpuCore = -1;
        isHelp = false;
    }

    public ProcedureControlBlock(ProcedureControlBlock copy)
    {
        this.basicPCB = new BasicPCB(copy.basicPCB);

        immigrateRunningCpuCore = -1;
        isHelp = false;
    }
}
