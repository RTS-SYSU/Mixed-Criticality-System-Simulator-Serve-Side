package sysu.rtsg.analysis;

import java.util.ArrayList;

public class RL_communication {
    public Boolean is_system_schedulable;

    public Boolean is_pureFineGrained_system_schedulable;

    public Boolean is_greedyFineGrained_system_schedulable;

    public Integer unschedulable_task_number;
//    private ArrayList<Long> unschedulable_task_R;
//
//    private ArrayList<Long> unschedulable_task_D;
    public ArrayList<ArrayList<Long>> unschedulable_task_D_R;
    public ArrayList<ArrayList<Integer>> unschedulable_resource_index;
    public ArrayList<ArrayList<Integer>> unschedulable_resource_number;

    public RL_communication(Boolean is_system_schedulable,
                            Integer unschedulable_task_number,
                            ArrayList<ArrayList<Long>> unschedulable_task_D_R,
                            ArrayList<ArrayList<Integer>> unschedulable_resource_index,
                            ArrayList<ArrayList<Integer>> unschedulable_resource_number,
                            Boolean is_pureFineGrained_system_schedulable,
                            Boolean is_greedyFineGrained_system_schedulable){
        this.is_system_schedulable = is_system_schedulable;
        this.unschedulable_task_number = unschedulable_task_number;
        this.unschedulable_task_D_R = unschedulable_task_D_R;
        this.unschedulable_resource_index = unschedulable_resource_index;
        this.unschedulable_resource_number = unschedulable_resource_number;
        this.is_pureFineGrained_system_schedulable = is_pureFineGrained_system_schedulable;
        this.is_greedyFineGrained_system_schedulable = is_greedyFineGrained_system_schedulable;
    }
}
