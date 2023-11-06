package com.example.serveside.service.CommonUse;

import com.example.serveside.service.mrsp.entity.ProcedureControlBlock;

import java.util.ArrayList;

public class BasicResource {
    /* Determine that whether the task is occupied. */
    public boolean isOccupied;

    public int id;

    // WCET for resources at low
    public int c_low;

    // WCET for resources at high
    public int c_high;

    public ArrayList<Integer> ceiling;

    public boolean isGlobal = false;


    public BasicResource(int id, int c_low, int c_high) {
        this.id = id;
        this.c_low = c_low;
        this.c_high = c_high;
        ceiling = new ArrayList<>();
        isOccupied = false;
    }

    /* 克隆一个 BasicResource */
    public BasicResource(BasicResource basicResource)
    {
        this.isOccupied = basicResource.isOccupied;
        this.id = basicResource.id;
        this.c_low = basicResource.c_low;
        this.c_high = basicResource.c_high;
        this.isGlobal = basicResource.isGlobal;

        // 深度克隆 ArrayList<Integer>
        this.ceiling = new ArrayList<>();
        this.ceiling.addAll(basicResource.ceiling);

    }
}
