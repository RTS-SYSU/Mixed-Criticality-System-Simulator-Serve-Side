package com.example.serveside.service.mrsp.generatorTools;

import com.example.serveside.service.mrsp.entity.MixedCriticalSystem;
import com.example.serveside.service.mrsp.entity.ProcedureControlBlock;

import java.util.ArrayList;
import java.util.Comparator;

public class PriorityGenerator {

    // use the deadline monotonic priority assignment protocol to assign priority to task
    public ArrayList<ArrayList<ProcedureControlBlock>> assignPrioritiesByDM(ArrayList<ArrayList<ProcedureControlBlock>> tasksToAssgin) {
        if (tasksToAssgin == null) {
            return null;
        }

        // this is a shallow copy
        ArrayList<ArrayList<ProcedureControlBlock>> tasks = new ArrayList<>(tasksToAssgin);
        for (ArrayList<ProcedureControlBlock> task : tasks) {
            new PriorityGenerator().deadlineMonotonicPriorityAssignment(task, task.size());
        }

        return tasks;
    }

    //
    private void deadlineMonotonicPriorityAssignment(ArrayList<ProcedureControlBlock> taskset, int numberOfTask) {
        ArrayList<Integer> priorities = generatePriorities(numberOfTask);

        /* deadline monotonic assignment */
        taskset.sort(Comparator.comparingDouble(t -> t.deadline));
        priorities.sort((p1, p2) -> -Integer.compare(p1, p2));
        for (int i = 0; i < taskset.size(); i++) {
            taskset.get(i).priorities.add(priorities.get(i));
            taskset.get(i).basePriority = priorities.get(i);
        }
    }

    // deadline are equal to period
    private ArrayList<Integer> generatePriorities(int number) {
        ArrayList<Integer> priorities = new ArrayList<>();
        for (int i = 0; i < number; i++)
            priorities.add(MixedCriticalSystem.MAX_PRIORITY - (i + 1) * 2);
        return priorities;
    }
}