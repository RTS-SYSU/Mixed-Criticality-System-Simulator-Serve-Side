package com.example.serveside.service.mrsp.entity;

import java.util.ArrayList;

public class Resource {

    /* Determine that whether the task is occupied. */
    public boolean isOccupied;

    /* Keep the task that is waiting for the resource. */
    public ArrayList<com.example.serveside.service.mrsp.entity.ProcedureControlBlock> waitingQueue;

    public int id;

    // WCET for resources at low
    public int c_low;

    // WCET for resources at high
    public int c_high;

    public ArrayList<ProcedureControlBlock> requested_tasks;
    public ArrayList<Integer> ceiling;

    public boolean isGlobal = false;


    public Resource(int id, int c_low, int c_high) {
        this.id = id;
        this.c_low = c_low;
        this.c_high = c_high;
        requested_tasks = new ArrayList<>();
        ceiling = new ArrayList<>();
        isOccupied = false;
        waitingQueue = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "R" + this.id + " : cs len = " + this.c_low + ", tasks: " + requested_tasks.size() + ", isGlobal: "
                + isGlobal;
    }

    public int getCeilingForProcessor(ArrayList<ArrayList<SporadicTask>> tasks, int partition) {
        int ceiling = -1;

        for (int k = 0; k < tasks.get(partition).size(); k++) {
            SporadicTask task = tasks.get(partition).get(k);

            if (task.resource_required_index.contains(this.id - 1)) {
                ceiling = Math.max(task.priority, ceiling);
            }
        }

        return ceiling;
    }

    public int getCeilingForProcessor(ArrayList<SporadicTask> tasks) {
        int ceiling = -1;

        for (int k = 0; k < tasks.size(); k++) {
            SporadicTask task = tasks.get(k);

            if (task.resource_required_index.contains(this.id - 1)) {
                ceiling = Math.max(task.priority, ceiling);
            }
        }

        return ceiling;
    }
}