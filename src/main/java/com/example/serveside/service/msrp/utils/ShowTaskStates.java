package com.example.serveside.service.msrp.utils;

import java.util.ArrayList;
import java.util.Comparator;

import com.example.serveside.service.msrp.entity.TaskStateInformation;

public class ShowTaskStates {

    /* Print task states in lifecycle */
    public static void printTaskStates(ArrayList<TaskStateInformation> taskStates) {
        taskStates.sort(Comparator.comparingInt(taskState -> taskState.runningCpuCore));
        System.out.print("\n---------------------------------------------------------------\n");
        System.out.print("                    Task state information\n");
        int cpu = -1;
        for (TaskStateInformation ts : taskStates) {
            if (ts.runningCpuCore != cpu) {
                cpu = ts.runningCpuCore;
                System.out.printf("CPU:%d\n", cpu);
            }
            ts.printTaskStates();
            System.out.print("\n");
        }
    }
}