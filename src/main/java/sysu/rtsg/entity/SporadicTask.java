package sysu.rtsg.entity;

import com.example.serveside.service.CommonUse.BasicPCB;
import com.example.serveside.service.CommonUse.BasicResource;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SporadicTask {
	public int id;
	public long WCET;
	public long period;
	public long deadline;
	public int partition;
	public int priority;
	public double util;

	public long pure_resource_execution_time = 0;
	public ArrayList<Integer> resource_required_index;
	public ArrayList<Integer> resource_required_priority;
	public ArrayList<Integer> number_of_access_in_one_release;

	public long spin_delay_by_preemptions = 0;
	public double np_section = 0;
	public long Ri = 0, spin = 0, interference = 0, local = 0, indirectspin = 0, total_blocking = 0;

	public double implementation_overheads = 0, blocking_overheads = 0;
	public double mrsp_arrivalblocking_overheads = 0, fifonp_arrivalblocking_overheads = 0, fifop_arrivalblocking_overheads = 0;
	public double migration_overheads_plus = 0;

	public long addition_slack_by_newOPA = 0;

	public long test_delay = 0;

	public double[] mrsp = null;
	public double[] fifonp = null;
	public double[] fifop = null;

	public int hasResource = 0;

	// 是否动态更新？
	public long maxAcceptableResponseTime;

	public long nowIncreaseResponseTime = 0;

	// public Map<Set<Integer>, Long> sortedwww = null;

	public SporadicTask(BasicPCB task, ArrayList<BasicResource> totalResources) {
		this.priority = task.basePriority;
		this.period = task.period;
		this.WCET = task.WCCT_low;
		this.deadline = this.period;
		this.partition = task.baseRunningCpuCore;
		this.id = task.staticTaskId+1;
		this.util = task.utilization;

		this.pure_resource_execution_time = 0;
		this.resource_required_index = new ArrayList<>();
		for (int resourceIndex : task.accessResourceIndex) {
			if (!this.resource_required_index.contains(resourceIndex)) {
				this.resource_required_index.add(resourceIndex);
			}
			this.pure_resource_execution_time += totalResources.get(resourceIndex).c_low;
		}
		this.resource_required_index.sort((r1, r2) -> Integer.compare(r1, r2));

		resource_required_priority = new ArrayList<>();

		number_of_access_in_one_release = new ArrayList<>();
		for (int i = 0; i < this.resource_required_index.size(); ++i) {
			int occurNum = 0;
			for (int j = 0; j < task.accessResourceIndex.size(); ++j) {
				if (this.resource_required_index.get(i) == task.accessResourceIndex.get(j)) {
					++occurNum;
				}
			}
			number_of_access_in_one_release.add(occurNum);
		}


		Ri = 0;
		spin = 0;
		interference = 0;
		local = 0;

	}

	public SporadicTask(int priority, long period, long WCET, int partition, int id, double util, long pure_resource_execution_time,
                        ArrayList<Integer> resource_required_index, ArrayList<Integer> number_of_access_in_one_release, int hasResource) {
		this.priority = priority;
		this.period = period;
		this.WCET = WCET;
		this.deadline = period;
		this.partition = partition;
		this.id = id;
		this.util = util;
		this.pure_resource_execution_time = pure_resource_execution_time;

		this.resource_required_index = new ArrayList<>(resource_required_index);
		this.resource_required_priority = new ArrayList<>();
		this.number_of_access_in_one_release = new ArrayList<>(number_of_access_in_one_release);
		this.hasResource = hasResource;

		Ri = 0;
		spin = 0;
		interference = 0;
		local = 0;
	}

	public SporadicTask(int priority, long period, long WCET, int partition, int id, double util) {
		this.priority = priority;
		this.period = period;
		this.WCET = WCET;
		this.deadline = period;
		this.partition = partition;
		this.id = id;
		this.util = util;

		resource_required_index = new ArrayList<>();
		resource_required_priority = new ArrayList<>();
		number_of_access_in_one_release = new ArrayList<>();

		Ri = 0;
		spin = 0;
		interference = 0;
		local = 0;
	}

	public SporadicTask(int priority, long period, long WCET, int id, double util) {
		this.priority = priority;
		this.period = period;
		this.WCET = WCET;
		this.deadline = period;
		this.id = id;
		this.partition = -1;
		this.util = util;

		resource_required_index = new ArrayList<>();
		resource_required_priority = new ArrayList<>();
		number_of_access_in_one_release = new ArrayList<>();

		Ri = 0;
		spin = 0;
		interference = 0;
		local = 0;
	}

	public String RTA() {
		return "T" + this.id + " : R = " + this.Ri + ", S = " + this.spin + ", I = " + this.interference + ", A = " + this.local + ". is schedulable: "
				+ (Ri <= deadline);
	}

	public String getInfo() {
		DecimalFormat df = new DecimalFormat("#.#######");
		return "T" + this.id + " : T = " + this.period + ", C = " + this.WCET + ", PRET: " + this.pure_resource_execution_time + ", D = " + this.deadline
				+ ", Priority = " + this.priority + ", Partition = " + this.partition + ", Util: " + Double.parseDouble(df.format(util));
	}

	@Override
	public String toString() {
		return "T" + this.id;
	}
}
