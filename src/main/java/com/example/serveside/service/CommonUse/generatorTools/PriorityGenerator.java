package com.example.serveside.service.CommonUse.generatorTools;

import com.example.serveside.service.CommonUse.BasicPCB;

import java.util.ArrayList;
import java.util.Comparator;

public class PriorityGenerator {

    // use the deadline monotonic priority assignment protocol to assign priority to task
    public ArrayList<ArrayList<BasicPCB>> assignPrioritiesByDM(ArrayList<ArrayList<BasicPCB>> tasksToAssgin) {
        if (tasksToAssgin == null) {
            return null;
        }

        // this is a shallow copy
        ArrayList<ArrayList<BasicPCB>> tasks = new ArrayList<>(tasksToAssgin);
        for (ArrayList<BasicPCB> task : tasks) {
            new PriorityGenerator().deadlineMonotonicPriorityAssignment(task, task.size());
        }

        return tasks;
    }

    //
    private void deadlineMonotonicPriorityAssignment(ArrayList<BasicPCB> taskset, int numberOfTask) {
        ArrayList<Integer> priorities = generatePriorities(numberOfTask);

        /* deadline monotonic assignment */
        taskset.sort(Comparator.comparingDouble(t -> t.deadline));
        /* 从小到大排序 */
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
            priorities.add(i + SimpleSystemGenerator.MIN_PRIORITY);

        /* 从大到小排序 */
        priorities.sort(Integer::compare);
        return priorities;
    }
}