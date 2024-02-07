package sysu.rtsg.entity;

import com.example.serveside.service.CommonUse.BasicResource;

import java.util.ArrayList;

public class Resource {

	public int id;
	public long csl;
	public int protocol;
	public boolean isGlobal = false;

	public ArrayList<Integer> partitions;
	public ArrayList<SporadicTask> requested_tasks;

	public Resource(int id, long cs_len, int protocol, boolean isGlobal, ArrayList<Integer> partitions, ArrayList<SporadicTask> requested_tasks,
                    ArrayList<SporadicTask> tasks) {
		this.id = id;
		this.csl = cs_len;
		this.isGlobal = isGlobal;
		this.partitions = new ArrayList<>(partitions);
		this.requested_tasks = new ArrayList<>();

		for (int i = 0; i < requested_tasks.size(); i++) {
			int tid = requested_tasks.get(i).id;
			for (int j = 0; j < tasks.size(); j++) {
				if (tasks.get(j).id == tid) {
					this.requested_tasks.add(tasks.get(j));
				}
			}
		}
	}

	public Resource(BasicResource basicResource) {
		this.id = basicResource.id+1;
		this.csl = basicResource.c_low;
		requested_tasks = new ArrayList<>();
		partitions = new ArrayList<>();
		this.protocol = 1;
	}

	public Resource(int id, long cs_len) {
		this.id = id;
		this.csl = cs_len;
		requested_tasks = new ArrayList<>();
		partitions = new ArrayList<>();
		this.protocol = 1;
	}

	@Override
	public String toString() {
		return "R" + this.id + " : cs len = " + this.csl + ", partitions: " + partitions.size() + ", tasks: " + requested_tasks.size() + ", isGlobal: "
				+ isGlobal;
	}

	public int getCeilingForProcessor(ArrayList<ArrayList<SporadicTask>> tasks, int partition) {
		int ceiling = -1;

		for (int k = 0; k < tasks.get(partition).size(); k++) {
			SporadicTask task = tasks.get(partition).get(k);

			if (task.resource_required_index.contains(this.id - 1)) {
				ceiling = task.priority > ceiling ? task.priority : ceiling;
			}
		}

		return ceiling;
	}

	public int getCeilingForProcessor(ArrayList<SporadicTask> tasks) {
		int ceiling = -1;

		for (int k = 0; k < tasks.size(); k++) {
			SporadicTask task = tasks.get(k);

			if (task.resource_required_index.contains(this.id - 1)) {
				ceiling = task.priority > ceiling ? task.priority : ceiling;
			}
		}

		// if (ceiling <= 0) {
		// System.err.println("111the ceiling is <= 0. there must be something
		// wrong. Check it!");
		// System.exit(-1);
		// }

		return ceiling;
	}

}
