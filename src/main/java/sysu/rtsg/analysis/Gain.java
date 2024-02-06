package sysu.rtsg.analysis;

import java.util.HashMap;

public class Gain {

    public int task_id;
    public long self_benefit;
    // id-收益的键值对儿
    public HashMap<Integer, Long> other_benefit;

    public Gain(int task_id, long self_benefit, HashMap<Integer, Long> other_benefit){
        this.task_id = task_id;
        this.self_benefit = self_benefit;
        this.other_benefit = other_benefit;
    }
}
