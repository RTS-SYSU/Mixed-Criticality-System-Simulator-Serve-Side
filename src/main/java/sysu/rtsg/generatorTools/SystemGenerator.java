package sysu.rtsg.generatorTools;

import sysu.rtsg.entity.Resource;
import sysu.rtsg.entity.SporadicTask;
import sysu.rtsg.utils.GeneatorUtils.CS_LENGTH_RANGE;
import sysu.rtsg.utils.GeneatorUtils.RESOURCES_RANGE;

import java.util.ArrayList;
import java.util.Random;

public class SystemGenerator {
	public CS_LENGTH_RANGE cs_len_range;
	long csl = -1;
	public boolean isLogUni;
	public int maxT;
	public int minT;

	public int number_of_max_access;
	public RESOURCES_RANGE range;
	public double rsf;

	public int total_tasks;
	public int total_partitions;
	public double totalUtil;
	boolean print;

	double maxUtilPerCore = 1;

	AllocationGeneator allocation = new AllocationGeneator();

	public SystemGenerator(int minT, int maxT, boolean isPeriodLogUni, int total_partitions, int totalTasks, double rsf, CS_LENGTH_RANGE cs_len_range,
			RESOURCES_RANGE numberOfResources, int number_of_max_access, boolean print) {
		this.minT = minT;
		this.maxT = maxT;
		this.totalUtil = 0.1 * totalTasks;
		this.total_partitions = total_partitions;
		this.total_tasks = totalTasks;
		this.isLogUni = isPeriodLogUni;
		this.cs_len_range = cs_len_range;
		this.range = numberOfResources;
		this.rsf = rsf;
		this.number_of_max_access = number_of_max_access;
		this.print = print;

		if (totalUtil / total_partitions <= 0.5)
			maxUtilPerCore = 0.5;
		else if (totalUtil / total_partitions <= 0.6)
			maxUtilPerCore = 0.6;
		else if (totalUtil / total_partitions <= 0.65)
			maxUtilPerCore = 0.65;
		else
			maxUtilPerCore = 1;
	}

	public SystemGenerator(int minT, int maxT, boolean isPeriodLogUni, int total_partitions, int totalTasks, double rsf, CS_LENGTH_RANGE cs_len_range,
			RESOURCES_RANGE numberOfResources, int number_of_max_access, long csl, boolean print) {
		this.minT = minT;
		this.maxT = maxT;
		this.totalUtil = 0.1 * totalTasks;
		this.total_partitions = total_partitions;
		this.total_tasks = totalTasks;
		this.isLogUni = isPeriodLogUni;
		this.cs_len_range = cs_len_range;
		this.range = numberOfResources;
		this.rsf = rsf;
		this.number_of_max_access = number_of_max_access;
		this.csl = csl;
		this.print = print;

		if (totalUtil / total_partitions <= 0.5)
			maxUtilPerCore = 0.5;
		else if (totalUtil / total_partitions <= 0.6)
			maxUtilPerCore = 0.6;
		else if (totalUtil / total_partitions <= 0.65)
			maxUtilPerCore = 0.65;
		else
			maxUtilPerCore = 1;
	}

	/*
	 * generate task sets for multiprocessor fully partitioned fixed-priority
	 * system
	 */
	public ArrayList<SporadicTask> generateTasks() {
		ArrayList<SporadicTask> tasks = null;
		while (tasks == null) {
			tasks = generateT();
			// if (tasks != null && (allocation.allocateTasks(tasks, null,
			// total_partitions, 0) == null
			// && allocation.allocateTasks(tasks, null, total_partitions, 1) ==
			// null && allocation.allocateTasks(tasks, null, total_partitions,
			// 2) == null
			// && allocation.allocateTasks(tasks, null, total_partitions, 3) ==
			// null))

			if (tasks != null && allocation.allocateTasks(tasks, null, total_partitions, 0) == null)
				tasks = null;
		}
		return tasks;
	}

	private ArrayList<SporadicTask> generateT() {
		int task_id = 1;
		ArrayList<SporadicTask> tasks = new ArrayList<>(total_tasks);
		ArrayList<Long> periods = new ArrayList<>(total_tasks);
		Random random = new Random();

		/* generates random periods */
		while (true) {
			if (!isLogUni) {
				long period = (random.nextInt(maxT - minT) + minT) * 1000;
				if (!periods.contains(period))
					periods.add(period);
			} else {
				double a1 = Math.log(minT);
				double a2 = Math.log(maxT + 1);
				double scaled = random.nextDouble() * (a2 - a1);
				double shifted = scaled + a1;
				double exp = Math.exp(shifted);

				int result = (int) exp;
				result = Math.max(minT, result);
				result = Math.min(maxT, result);

				long period = result * 1000;
				if (!periods.contains(period))
					periods.add(period);
			}

			if (periods.size() >= total_tasks)
				break;
		}
		periods.sort((p1, p2) -> Double.compare(p1, p2));

		/* generate utils */
		UUnifastDiscard unifastDiscard = new UUnifastDiscard(totalUtil, total_tasks, 1000);
		ArrayList<Double> utils = null;
		while (true) {
			utils = unifastDiscard.getUtils();

			double tt = 0;
			for (int i = 0; i < utils.size(); i++) {
				tt += utils.get(i);
			}

			if (utils != null)
				if (utils.size() == total_tasks && tt <= totalUtil)
					break;
		}
		if (print) {
			System.out.print("task utils: ");
			double tt = 0;
			for (int i = 0; i < utils.size(); i++) {
				tt += utils.get(i);
				System.out.print(tt + "   ");
			}
			System.out.println("\n total uitls: " + tt);
		}

		/* generate sporadic tasks */
		for (int i = 0; i < utils.size(); i++) {
			long computation_time = (long) (periods.get(i) * utils.get(i));
			if (computation_time == 0) {
				return null;
			}
			SporadicTask t = new SporadicTask(-1, periods.get(i), computation_time, task_id, utils.get(i));
			task_id++;
			tasks.add(t);
		}
		tasks.sort((p1, p2) -> -Double.compare(p1.util, p2.util));
		return tasks;
	}

	/*
	 * Generate a set of resources.
	 */
	public ArrayList<Resource> generateResources() {
		/* generate resources from partitions/2 to partitions*2 */
		Random ran = new Random();
		int number_of_resources = 0;

		switch (range) {
		case PARTITIONS:
			number_of_resources = total_partitions;
			break;
		case HALF_PARITIONS:
			number_of_resources = total_partitions / 2;
			break;
		case DOUBLE_PARTITIONS:
			number_of_resources = total_partitions * 2;
			break;
		default:
			break;
		}

		ArrayList<Resource> resources = new ArrayList<>(number_of_resources);

		for (int i = 0; i < number_of_resources; i++) {
			long cs_len = 0;
			if (csl == -1) {
				switch (cs_len_range) {
				case VERY_LONG_CSLEN:
					cs_len = ran.nextInt(300 - 200) + 201;
					break;
				case LONG_CSLEN:
					cs_len = ran.nextInt(200 - 100) + 101;
					break;
				case MEDIUM_CS_LEN:
					cs_len = ran.nextInt(100 - 50) + 51;
					break;
				case SHORT_CS_LEN:
					cs_len = ran.nextInt(50 - 15) + 16;
					break;
				case VERY_SHORT_CS_LEN:
					cs_len = ran.nextInt(15) + 1;
					break;
				case RANDOM:
					cs_len = ran.nextInt(300) + 1;
				default:
					break;
				}
			} else
				cs_len = csl;

			Resource resource = new Resource(i + 1, cs_len);
			resources.add(resource);
		}

		resources.sort((r2, r1) -> Long.compare(r1.csl, r2.csl));

		for (int i = 0; i < resources.size(); i++) {
			Resource res = resources.get(i);
			res.id = i + 1;
		}

		return resources;
	}

	public void generateResourceUsage(ArrayList<SporadicTask> tasks, ArrayList<Resource> resources) {
		while (tasks == null)
			tasks = generateTasks();

		int fails = 0;
		Random ran = new Random();
		long number_of_resource_requested_tasks = Math.round(rsf * tasks.size());

		/* Generate resource usage */
		for (long l = 0; l < number_of_resource_requested_tasks; l++) {
			if (fails > 1000) {
				tasks = generateTasks();
				while (tasks == null)
					tasks = generateTasks();
				l = 0;
				fails++;
			}
			int task_index = ran.nextInt(tasks.size());
			while (true) {
				if (tasks.get(task_index).resource_required_index.size() == 0)
					break;
				task_index = ran.nextInt(tasks.size());
			}
			SporadicTask task = tasks.get(task_index);

			/* Find the resources that we are going to access */
			int number_of_requested_resource = ran.nextInt(resources.size()) + 1;
			for (int j = 0; j < number_of_requested_resource; j++) {
				while (true) {
					int resource_index = ran.nextInt(resources.size());
					if (!task.resource_required_index.contains(resource_index)) {
						task.resource_required_index.add(resource_index);
						break;
					}
				}
			}
			task.resource_required_index.sort((r1, r2) -> Integer.compare(r1, r2));

			long total_resource_execution_time = 0;
			for (int k = 0; k < task.resource_required_index.size(); k++) {
				int number_of_requests = ran.nextInt(number_of_max_access) + 1;
				task.number_of_access_in_one_release.add(number_of_requests);
				total_resource_execution_time += number_of_requests * resources.get(task.resource_required_index.get(k)).csl;
			}

			if (total_resource_execution_time > task.WCET) {
				l--;
				task.resource_required_index.clear();
				task.number_of_access_in_one_release.clear();
				fails++;
			} else {
				task.WCET = task.WCET - total_resource_execution_time;
				task.pure_resource_execution_time = total_resource_execution_time;
				if (task.resource_required_index.size() > 0)
					task.hasResource = 1;
			}
		}

	}

	public void PrintAllocatedSystem(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources) {
		System.out.println("----------------------------------------------------");
		if (tasks == null) {
			System.out.println("no tasks generated.");
		} else {
			for (int i = 0; i < tasks.size(); i++) {
				double util = 0;
				for (int j = 0; j < tasks.get(i).size(); j++) {
					SporadicTask task = tasks.get(i).get(j);
					util += ((double) (task.WCET + task.pure_resource_execution_time)) / (double) task.period;
					System.out.println(tasks.get(i).get(j).getInfo());
				}
				System.out.println("util on partition: " + i + " : " + util);
			}
			System.out.println("----------------------------------------------------");

			if (resources != null) {
				System.out.println("****************************************************");
				for (int i = 0; i < resources.size(); i++) {
					System.out.println(resources.get(i).toString());
				}
				System.out.println("****************************************************");

				String resource_usage = "";
				/* print resource usage */
				System.out.println("---------------------------------------------------------------------------------");
				for (int i = 0; i < tasks.size(); i++) {
					for (int j = 0; j < tasks.get(i).size(); j++) {

						SporadicTask task = tasks.get(i).get(j);
						String usage = "T" + task.id + ": ";
						for (int k = 0; k < task.resource_required_index.size(); k++) {
							usage = usage + "R" + resources.get(task.resource_required_index.get(k)).id + " - " + task.number_of_access_in_one_release.get(k)
									+ ";  ";
						}
						usage += "\n";
						if (task.resource_required_index.size() > 0)
							resource_usage = resource_usage + usage;
					}
				}

				System.out.println(resource_usage);
				System.out.println("---------------------------------------------------------------------------------");
			}
		}

	}

	public void printUnallocateSystem(ArrayList<SporadicTask> tasks, ArrayList<Resource> resources) {
		System.out.println("----------------------------------------------------");
		for (int i = 0; i < tasks.size(); i++) {
			System.out.println(tasks.get(i).getInfo());
		}
		System.out.println("----------------------------------------------------");
		System.out.println("****************************************************");
		for (int i = 0; i < resources.size(); i++) {
			System.out.println(resources.get(i).toString());
		}
		System.out.println("****************************************************");

		String resource_usage = "";
		/* print resource usage */
		System.out.println("---------------------------------------------------------------------------------");
		for (int i = 0; i < tasks.size(); i++) {
			SporadicTask task = tasks.get(i);
			String usage = "T" + task.id + ": ";
			for (int k = 0; k < task.resource_required_index.size(); k++) {
				usage = usage + "R" + resources.get(task.resource_required_index.get(k)).id + " - " + task.number_of_access_in_one_release.get(k) + ";  ";
			}
			usage += "\n";
			if (task.resource_required_index.size() > 0)
				resource_usage = resource_usage + usage;

		}
		System.out.println(resource_usage);
		System.out.println("---------------------------------------------------------------------------------");

	}
}
