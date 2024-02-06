package sysu.rtsg;

import sysu.rtsg.analysis.ResponseTimeAnalysisWithVariousPriorities_B_S;
import sysu.rtsg.entity.Resource;
import sysu.rtsg.entity.SporadicTask;
import sysu.rtsg.utils.AnalysisUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class GASolver {
	public String name = "";
	Random ran = new Random(System.currentTimeMillis());
	ArrayList<ArrayList<SporadicTask>> tasks;
	ArrayList<Resource> resources;
	ResponseTimeAnalysisWithVariousPriorities_B_S analysis = new ResponseTimeAnalysisWithVariousPriorities_B_S();
	public int[] bestGene = null;
	int[][] nextGenes;
	int[][] parentGenes;
	int[][] elitismGene;
	int[] elitismGeneIndex;
	long[] rtFitness;
	long[] schedFitness;

	/****************** GA Properties ******************/
	int PROTOCOL_SIZE = 5;
	boolean isPrint;
	int population;
	int elitismSize;
	int crossoverPoint;
	double crossoverRate;
	double mutationRate;
	int maxGeneration;
	int randomBound = 65535;
	public int currentGeneration = 0;
	int toumamentSize1, toumamentSize2;


	public int bestAllocation = -1;
	public int bestProtocol = -1; // 1 MrsP; 2 FIFONP; 3 FIFOP; 0 combined.
	public int bestPriority = -1;


	/****************** GA Properties ******************/

	public GASolver(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, int population, int maxGeneration, int elitismSize, int crossoverPoint, double crossoverRate, double mutationRate,
					int toumamentSize1, int toumamentSize2, boolean isPrint, String name) {
		this.isPrint = isPrint;
		this.tasks = tasks;
		this.resources = resources;
		this.population = population;
		this.maxGeneration = maxGeneration;
		this.elitismSize = elitismSize;
		this.crossoverPoint = crossoverPoint;
		this.crossoverRate = crossoverRate;
		this.mutationRate = mutationRate;
		this.toumamentSize1 = toumamentSize1;
		this.toumamentSize2 = toumamentSize2;
		this.name = name;

		nextGenes = new int[population][resources.size()];
		parentGenes = new int[population][resources.size()];
		elitismGene = new int[elitismSize][resources.size()];
		elitismGeneIndex = new int[elitismSize];

		schedFitness = new long[population];
		rtFitness = new long[population];

		for (int i = 0; i < population; i++) {
			schedFitness[i] = -1;
		}
	}

	public int solve(boolean useGA) {

		getFirstGene();
		getFitness(nextGenes, schedFitness, rtFitness);

		if (bestGene != null) {
			bestProtocol = 1;
			int firstchorm = bestGene[0];
			for (int i = 1; i < resources.size(); i++) {
				if (bestGene[i] != firstchorm) {
					bestProtocol = 0;
					break;
				}
			}

			if (isPrint)
				System.out.println(name + " " + "new combination schedulable   Gene: " + currentGeneration + "   Sol: " + Arrays.toString(bestGene)
						+ " protocol: " + bestProtocol + " allocation: " + bestAllocation + " priority: " + bestPriority);
			return 1;
		}

		while (currentGeneration <= maxGeneration) {
			long[] sched_temp = new long[population];
			long[] rt_temp = new long[population];
			for (int i = 0; i < population; i++) {
				sched_temp[i] = -1;
			}
			currentGeneration++;
			for (int i = 0; i < population; i++) {
				for (int j = 0; j < resources.size(); j++) {
					parentGenes[i][j] = nextGenes[i][j];
				}
			}
			if (useGA) {
				// 精英选择的size，这些精英直接进入下一代
				int nextGeneIndex = elitismSize;
				for (int i = 0; i < elitismSize; i++) {
					for (int j = 0; j < resources.size(); j++) {
						nextGenes[i][j] = elitismGene[i][j];
						sched_temp[i] = schedFitness[elitismGeneIndex[i]];
						rt_temp[i] = rtFitness[elitismGeneIndex[i]];
					}
				}


				// 锦标赛
				for (int i = nextGeneIndex; i < population; i++) {
					ArrayList<ArrayList<Long>> toumament1 = new ArrayList<>();
					ArrayList<ArrayList<Long>> toumament2 = new ArrayList<>();

					for (int j = 0; j < toumamentSize1; j++) {
						int randomIndex = ran.nextInt(population);
						ArrayList<Long> randomGeneFitness = new ArrayList<>();
						randomGeneFitness.add(schedFitness[randomIndex]);
						randomGeneFitness.add(rtFitness[randomIndex]);
						randomGeneFitness.add((long) randomIndex);
						toumament1.add(randomGeneFitness);
					}
					for (int j = 0; j < toumamentSize2; j++) {
						int randomIndex = ran.nextInt(population);
						ArrayList<Long> randomGeneFitness = new ArrayList<>();
						randomGeneFitness.add(schedFitness[randomIndex]);
						randomGeneFitness.add(rtFitness[randomIndex]);
						randomGeneFitness.add((long) randomIndex);
						toumament2.add(randomGeneFitness);
					}
					toumament1.sort((l1, l2) -> compareFitness(l1, l2));
					toumament2.sort((l1, l2) -> compareFitness(l1, l2));
					long index1 = toumament1.get(0).get(2), index2 = toumament2.get(0).get(2);

					double crossover = ran.nextDouble();
					if (crossover <= crossoverRate) {
						int crosspoint1 = ran.nextInt(resources.size() - 1) + 1;
						int crosspoint2 = ran.nextInt(resources.size() - 1) + 1;

						int[] gene1 = parentGenes[(int) index1];
						int[] gene2 = parentGenes[(int) index2];

//						int[] newGene1 = new int[resources.size() + 1];
//						int[] newGene2 = new int[resources.size() + 1];


						int[] newGene1 = new int[resources.size()];
						int[] newGene2 = new int[resources.size()];

						if (crossoverPoint == 1 || crosspoint1 == crosspoint2) {
							for (int j = 0; j < resources.size(); j++) {
								if (j < crosspoint1) {
									newGene1[j] = gene1[j];
									newGene2[j] = gene2[j];
								} else {
									newGene1[j] = gene2[j];
									newGene2[j] = gene1[j];
								}
							}
						} else {
							int a = -1, b = -1;
							if (crosspoint1 < crosspoint2) {
								a = crosspoint1;
								b = crosspoint2;
							} else {
								a = crosspoint2;
								b = crosspoint1;
							}

							for (int j = 0; j < resources.size(); j++) {
								if (j < a) {
									newGene1[j] = gene1[j];
									newGene2[j] = gene2[j];
								} else if (j >= a && j <= b) {
									newGene1[j] = gene2[j];
									newGene2[j] = gene1[j];
								} else {
									newGene1[j] = gene1[j];
									newGene2[j] = gene2[j];
								}
							}
						}

						long[] Fit1 = computeFirstFitness(newGene1);
						long[] Fit2 = computeFirstFitness(newGene2);
						ArrayList<Long> gene1fitness = new ArrayList<>();
						ArrayList<Long> gene2fitness = new ArrayList<>();
						gene1fitness.add(Fit1[0]);
						gene1fitness.add(Fit1[1]);
						gene1fitness.add((long) 1);
						gene2fitness.add(Fit2[0]);
						gene2fitness.add(Fit2[1]);
						gene2fitness.add((long) 2);

						if (compareFitness(gene1fitness, gene2fitness) <= 0) {
							nextGenes[i] = newGene1;
							sched_temp[i] = Fit1[0];
							rt_temp[i] = Fit1[1];
						} else {
							nextGenes[i] = newGene2;
							sched_temp[i] = Fit2[0];
							rt_temp[i] = Fit2[1];
						}

					} else {
						if (compareFitness(toumament1.get(0), toumament2.get(0)) <= 0) {
							long index = toumament1.get(0).get(2);
							nextGenes[i] = parentGenes[(int) index];
							sched_temp[i] = schedFitness[(int) index];
							rt_temp[i] = rtFitness[(int) index];
						} else {
							long index = toumament2.get(0).get(2);
							nextGenes[i] = parentGenes[(int) index];
							sched_temp[i] = schedFitness[(int) index];
							rt_temp[i] = rtFitness[(int) index];
						}
					}

					// 突变操作（概率）
					double mute = ran.nextDouble();
					if (mute < mutationRate) {
						int muteindex1 = ran.nextInt(resources.size());
						int muteindex2 = ran.nextInt(resources.size());
						int temp = nextGenes[i][muteindex1];
						nextGenes[i][muteindex1] = nextGenes[i][muteindex2];
						nextGenes[i][muteindex2] = temp;
						sched_temp[i] = -1;
					}
				}
			} else {
				for (int i = 0; i < nextGenes.length; i++) {
					for (int j = 0; j < nextGenes[i].length; j++) {
						nextGenes[i][j] = ran.nextInt(randomBound) % 5 + 1;
					}
				}
			}

			long maxindex = getFitness(nextGenes, sched_temp, rt_temp);

			if (bestGene != null) {

				bestProtocol = 1;
				int firstchorm = bestGene[0];
				for (int i = 1; i < resources.size(); i++) {
					if (bestGene[i] != firstchorm) {
						bestProtocol = 0;
						break;
					}
				}

				// 这里代表着粗粒度就找到解决方案了；
				if (isPrint)
					System.out.println(name + " " + "new combination schedulable   Gene: " + currentGeneration + "   Sol: " + Arrays.toString(bestGene)
							+ " protocol: " + bestProtocol + " allocation: " + bestAllocation + " priority: " + bestPriority);
				return 1;
			}
			else{
				// 这里代表着粗粒度不行，那么我们就细粒度的找；
				int[] sol = nextGenes[(int) maxindex];

				// 对应更新protocol
				for(int index = 0 ; index < sol.length; index++){
					resources.get(index).protocol = sol[index];
				}
				updateResourceRequiredPri(tasks,resources);
				FineGrainedTuningMethod fineGrainedTuningMethod = new FineGrainedTuningMethod();
				// 纯收益调整
				fineGrainedTuningMethod.FineGrained_PureProfit_Tuning(tasks,resources);
				long[][] Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
				if(isSystemSchedulable(tasks,Ris)){
					System.out.println("GA Pure Succeed!");
					return 2;
				}
				// 贪心调整
				fineGrainedTuningMethod.FineGrained_PureProfit_Tuning(tasks,resources);
				Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,true,true,true,true,false);
				if(isSystemSchedulable(tasks,Ris)){
					System.out.println("GA Greedy Succeed!");
					return 3;
				}
			}


		}
		if (isPrint)
			System.out.println(name + " " + "not schedulable with in " + maxGeneration + " generations. GA finish");
		return -1;
	}

	private void getFirstGene() {
		for (int i = 0; i < PROTOCOL_SIZE; i++) {
			for (int j = 0; j < resources.size(); j++) {
				nextGenes[i][j] = i + 1;
			}
		}

		for (int i = PROTOCOL_SIZE; i < nextGenes.length; i++) {
			for (int j = 0; j < resources.size(); j++) {
				nextGenes[i][j] = ran.nextInt(randomBound) % 5 + 1;
			}
		}
	}

	long getFitness(int[][] gene, long[] sched, long[] rt) {
		ArrayList<ArrayList<Long>> fitness = new ArrayList<>();

		for (int i = 0; i < gene.length; i++) {
			long fit[] = null;
			if (sched[i] == -1){
				fit = computeFirstFitness(gene[i]);
			}
			else {
				fit = new long[2];
				fit[0] = sched[i];
				fit[1] = rt[i];
			}
			schedFitness[i] = fit[0];
			rtFitness[i] = fit[1];

			if (schedFitness[i] == 0) {
				bestGene = gene[i];
				return -1;
			}

			ArrayList<Long> fitnessofGene = new ArrayList<>();
			fitnessofGene.add(fit[0]);
			fitnessofGene.add(fit[1]);
			fitnessofGene.add((long) i);
			fitness.add(fitnessofGene);
		}

		fitness.sort((l1, l2) -> compareFitness(l1, l2));

		for (int i = 0; i < elitismSize; i++) {
			long index = fitness.get(i).get(2);
			elitismGene[i] = nextGenes[(int) index];
			elitismGeneIndex[i] = (int) index;
		}

		long maxindex = fitness.get(0).get(2);
		if (isPrint)
			System.out.println(name + " " + "Generation " + currentGeneration + "   maxsched: " + fitness.get(0).get(0) + " maxrt: " + fitness.get(0).get(1)
					+ "    GENE: " + Arrays.toString(nextGenes[(int) maxindex]));
		return maxindex;
	}

	private long[] computeFirstFitness(int[] gene) {
		int sched_fitness = 0;
		long rt_fitness = 0;
//		if (gene.length != resources.size() || gene[resources.size()] >= 8) {
//			System.err.println(" gene length error! or alloc gene error");
//			System.exit(-1);
//		}
		for (int i = 0; i < resources.size(); i++) {
			resources.get(i).protocol = gene[i];
		}
		updateResourceRequiredPri(tasks,resources);

		long[][] Ris = analysis.getResponseTimeByDMPO(tasks, resources, AnalysisUtils.extendCalForGA, false, true, true, true, false);


		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				// 如果某个任务错过了截止日期
				if (tasks.get(i).get(j).deadline < Ris[i][j]) {
					sched_fitness++;
					rt_fitness += Ris[i][j] - tasks.get(i).get(j).deadline;
				}
			}
		}

		if (sched_fitness == 0) {
			bestPriority = 0;
		}

		long[] fitness = new long[2];
		fitness[0] = sched_fitness;
		fitness[1] = rt_fitness;

		return fitness;
	}

	int compareFitness(ArrayList<Long> a, ArrayList<Long> b) {
		long a0 = a.get(0), a1 = a.get(1), a2 = a.get(2);
		long b0 = b.get(0), b1 = b.get(1), b2 = b.get(2);
		if (a0 < b0)
			return -1;
		if (a0 > b0)
			return 1;

		if (a0 == b0) {
			if (a1 < b1)
				return -1;
			if (a1 > b1)
				return 1;

			if (a1 == b1) {
				if (a2 < b2)
					return -1;
				if (a2 > b2)
					return 1;
				if (a2 == b2)
					return 0;
			}
		}

		System.err.println("comparator error!" + " a0:  " + a.get(0) + " a1:  " + a.get(1) + " a2:  " + a.get(2) + " b0:  " + b.get(0) + " b1:  " + b.get(1)
				+ " b2:  " + b.get(2));
		System.err.println(a0 == b0);
		System.err.println(a1 == b1);
		System.err.println(a2 == b2);

		System.exit(-1);
		return 100;
	}

	private void updateResourceRequiredPri(ArrayList<ArrayList<SporadicTask>> generatedTaskSets, ArrayList<Resource> resources){
		for(int i = 0 ; i < generatedTaskSets.size(); i++){
			for(int j = 0 ; j < generatedTaskSets.get(i).size(); j++){
				SporadicTask task = generatedTaskSets.get(i).get(j);
				ArrayList<Integer> resourceIndexList = task.resource_required_index;
				task.resource_required_priority = new ArrayList<>();
				for (int k = 0 ; k < resourceIndexList.size(); k++){
					Resource re = resources.get(resourceIndexList.get(k));
					int max_i_pri = 0;
					// 遍历第i号处理器上的所有任务，找到该处理器上所有任务的最高优先级
					for(int w = 0 ; w < generatedTaskSets.get(i).size(); w++) {
						if(generatedTaskSets.get(i).get(w).priority > max_i_pri){
							max_i_pri = generatedTaskSets.get(i).get(w).priority;
						}
					}

					int protocol = re.protocol;
					boolean isGlobal = re.isGlobal;
					int ceiling_pri = re.getCeilingForProcessor(generatedTaskSets,task.partition);

					if(!isGlobal){
						task.resource_required_priority.add(ceiling_pri);
					}
					else{
						switch (protocol) {
							case 1:
								task.resource_required_priority.add(max_i_pri + 1);
								break;
							case 2:
								task.resource_required_priority.add(task.priority);
								break;
							case 3:
								task.resource_required_priority.add((int) Math.ceil(task.priority + ((double)(ceiling_pri - task.priority) / 2.0)));
								break;
							case 4:
							case 6:
								task.resource_required_priority.add(ceiling_pri);
								break;
							case 5:
							case 7:
								task.resource_required_priority.add((int) Math.ceil(ceiling_pri + (max_i_pri - ceiling_pri) / 2.0));
								break;
							default:
								System.err.print("NO RULES");
								System.exit(-1);
						}

					}

				}
			}

		}
	}


	public static boolean isSystemSchedulable(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				if (tasks.get(i).get(j).deadline < Ris[i][j]){
					return false;
				}
			}
		}
		return true;
	}

}
