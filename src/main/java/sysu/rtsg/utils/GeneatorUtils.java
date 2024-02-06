package sysu.rtsg.utils;

public class GeneatorUtils {

	/* define how long the critical section can be */
	public static enum CS_LENGTH_RANGE {
		VERY_SHORT_CS_LEN, /* 1 - 15 us */
		SHORT_CS_LEN, /* 16 - 50 us */
		MEDIUM_CS_LEN, /* 51 - 100 us */
		LONG_CSLEN, /* 101 - 200 us */
		VERY_LONG_CSLEN, /* 201 - 300 us */
		RANDOM, /* 1 - 300 us */
	};

	/* define how many resources in the system */
	public static enum RESOURCES_RANGE {
		HALF_PARITIONS, /* partitions us */
		PARTITIONS, /* partitions * 2 us */
		DOUBLE_PARTITIONS, /* partitions / 2 us */
	};

	/* define how long the critical section can be */
	public static enum ALLOCATION_POLICY {
		FIRST_FIT, /* fit the task in the first available partition. */
		BEST_FIT, /* fit the task in the partition with largest utilization. */
		WORST_FIT, /*
					 * fit the task in the partition with smallest utilization.
					 */
		NEXT_FIT, /*
					 * fit the task in the next (next to last allocated
					 * partition) available partition.
					 */
		RESOURCE_REQUEST_TASKS_FIT, /*
									 * sort tasks based on resources and number
									 * of requested tasks (tie break by
									 * utilization) and then use FF.
									 */
		RESOURCE_LOCAL_FIT, /*
							 * sort tasks based on resources (total requesting
							 * tasks utilization) and then use FF.
							 */
		RESOURCE_LENGTH_DECREASE_FIT, /*
										 * sort tasks based on resource length
										 * in a decreasing order and then use
										 * FF.
										 */
		RESOURCE_LENGTH_INCREASE_FIT /*
										 * sort tasks based on resource length
										 * in an increasing order and then use
										 * FF.
										 */
	};

	/* define how many resources in the system */
	public static enum PRIORITY_ASSIGNMENT {
		DMPO, /* partitions us */
		OPADI, /* partitions * 2 us */
		SBPO, /* partitions / 2 us */
	};
}
