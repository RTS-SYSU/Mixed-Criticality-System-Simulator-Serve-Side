package sysu.rtsg;

import sysu.rtsg.analysis.ApproximationModelForLocalTasks_S_B_Worst;
import sysu.rtsg.analysis.Gain;
import sysu.rtsg.analysis.ResponseTimeAnalysisWithVariousPriorities_S_B_Worst;
import sysu.rtsg.entity.Resource;
import sysu.rtsg.entity.SporadicTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FineGrainedTuningMethod {

    public void FineGrained_PureProfit_Tuning(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources){

        long[][] Ris;

        ResponseTimeAnalysisWithVariousPriorities_S_B_Worst analysis = new ResponseTimeAnalysisWithVariousPriorities_S_B_Worst();

        Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,false,true,true,true,false);

        // 实际上是不可调度的，我们需要获取不可调度的任务，同时更新maxAcceptableResponseTime
        ArrayList<ArrayList<SporadicTask>> UnschedulableTasksList = getUnschedulableTasks(tasks,Ris);

        /*** 纯收益调整 ***/
        for(ArrayList<SporadicTask> UnschedulableTasks : UnschedulableTasksList){
            // 这个处理器上有1个或1个以上的不可调度的任务
            if(UnschedulableTasks.size() != 0){
                // 对于每个调度的任务我们都需要进行处理，接下来就是处理的过程
                for(SporadicTask UnschedulableTask : UnschedulableTasks){
                    // 实际上，这三种操作都是在为一个不可调度的任务服务，需要在最大程度上保证其他任务'可调度的'的情况下让不可调度的任务可调度
                    // 高优先级任务 优先级提升，从次高开始，因为最高优先级任务无法提升
                    ArrayList<ArrayList<ArrayList<Integer>>> IncreasePossiblePriorityList = new ArrayList<>();
                    ArrayList<ArrayList<ArrayList<Gain>>> IncreaseGainList = new ArrayList<>();
                    // 自身 优先级提升
                    ArrayList<ArrayList<Integer>> selfPossiblePriorityList = new ArrayList<>();
                    ArrayList<ArrayList<Gain>> selfGainList = new ArrayList<>();
                    // 低优先级任务 优先级降低
                    ArrayList<ArrayList<ArrayList<Integer>>> DecreasePossiblePriorityList = new ArrayList<>();
                    ArrayList<ArrayList<ArrayList<Gain>>> DecreaseGainList = new ArrayList<>();

                    // 接下来，获取上述三个list
                    getAllTuning(UnschedulableTask,tasks,resources,IncreasePossiblePriorityList,IncreaseGainList,selfPossiblePriorityList,selfGainList,DecreasePossiblePriorityList,DecreaseGainList);

                    // 高优先级任务的“纯收益调整”
                    FineGrained_PureProfit_Tuning_High(UnschedulableTask,tasks.get(UnschedulableTask.partition),IncreasePossiblePriorityList,IncreaseGainList);
                    // 任务自身的"纯收益"调整
                    FineGrained_PureProfit_Tuning_Self(UnschedulableTask,tasks.get(UnschedulableTask.partition),selfPossiblePriorityList,selfGainList);
                    // 低优先级任务的"纯收益"调整
                    FineGrained_PureProfit_Tuning_Low(UnschedulableTask,tasks.get(UnschedulableTask.partition),DecreasePossiblePriorityList,DecreaseGainList);
                }
            }
        }

    }

    public void FineGrained_GreedyProfit_Tuning(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources){

        long[][] Ris;

        ResponseTimeAnalysisWithVariousPriorities_S_B_Worst analysis = new ResponseTimeAnalysisWithVariousPriorities_S_B_Worst();

        Ris = analysis.getResponseTimeByDMPO(tasks,resources,1,false,true,true,true,false);
        // 实际上是不可调度的，我们需要获取不可调度的任务，同时更新maxAcceptableResponseTime
        ArrayList<ArrayList<SporadicTask>> UnschedulableTasksList = getUnschedulableTasks(tasks,Ris);

        for(ArrayList<SporadicTask> UnschedulableTasks : UnschedulableTasksList){
            // 这个处理器上有1个或1个以上的不可调度的任务
            if(UnschedulableTasks.size() != 0){
                // 对于每个调度的任务我们都需要进行处理，接下来就是处理的过程
                for(SporadicTask UnschedulableTask : UnschedulableTasks){
                    // 实际上，这三种操作都是在为一个不可调度的任务服务，需要在最大程度上保证其他任务'可调度的'的情况下让不可调度的任务可调度
                    // 高优先级任务 优先级提升，从次高开始，因为最高优先级任务无法提升
                    ArrayList<ArrayList<ArrayList<Integer>>> IncreasePossiblePriorityList = new ArrayList<>();
                    ArrayList<ArrayList<ArrayList<Gain>>> IncreaseGainList = new ArrayList<>();
                    // 自身 优先级提升
                    ArrayList<ArrayList<Integer>> selfPossiblePriorityList = new ArrayList<>();
                    ArrayList<ArrayList<Gain>> selfGainList = new ArrayList<>();
                    // 低优先级任务 优先级降低
                    ArrayList<ArrayList<ArrayList<Integer>>> DecreasePossiblePriorityList = new ArrayList<>();
                    ArrayList<ArrayList<ArrayList<Gain>>> DecreaseGainList = new ArrayList<>();

                    // 接下来，获取上述三个list
                    getAllTuning(UnschedulableTask,tasks,resources,IncreasePossiblePriorityList,IncreaseGainList,selfPossiblePriorityList,selfGainList,DecreasePossiblePriorityList,DecreaseGainList);

                    // 高优先级任务的"贪心"调整
                    FineGrained_GreedyProfit_Tuning_High(UnschedulableTask,tasks.get(UnschedulableTask.partition),IncreasePossiblePriorityList,IncreaseGainList);
                    // 任务自身的"贪心"调整
                    FineGrained_GreedyProfit_Tuning_Self(UnschedulableTask,tasks.get(UnschedulableTask.partition),selfPossiblePriorityList,selfGainList);
                    // 低优先级任务的"贪心"调整
                    FineGrained_GreedyProfit_Tuning_Low(UnschedulableTask,tasks.get(UnschedulableTask.partition),DecreasePossiblePriorityList,DecreaseGainList);
                }
            }
        }
    }

    private ArrayList<ArrayList<SporadicTask>> getUnschedulableTasks(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
        ArrayList<ArrayList<SporadicTask>> UnschedulableTaskList = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            ArrayList<SporadicTask> UnschedulableTaskSubList = new ArrayList<>();
            for (int j = 0; j < tasks.get(i).size(); j++) {
                if (tasks.get(i).get(j).deadline < Ris[i][j]){
                    UnschedulableTaskSubList.add(tasks.get(i).get(j));
                    tasks.get(i).get(j).maxAcceptableResponseTime = -1;
                } else{
                    // 定义时间松弛slack
                    tasks.get(i).get(j).maxAcceptableResponseTime = (tasks.get(i).get(j).deadline - Ris[i][j]);
                }
            }
            UnschedulableTaskList.add(UnschedulableTaskSubList);
        }
        return UnschedulableTaskList;
    }

    private void getAllTuning(SporadicTask UnschedulableTask, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources,
                                     ArrayList<ArrayList<ArrayList<Integer>>> increasePossiblePriorityList, ArrayList<ArrayList<ArrayList<Gain>>> increaseGainList,
                                     ArrayList<ArrayList<Integer>> selfPossiblePriorityList, ArrayList<ArrayList<Gain>> selfGainList,
                                     ArrayList<ArrayList<ArrayList<Integer>>> decreasePossiblePriorityList, ArrayList<ArrayList<ArrayList<Gain>>> decreaseGainList) {
        ArrayList<ArrayList<Integer>> possiblePriority;
        ArrayList<ArrayList<Gain>> gain;

        ArrayList<SporadicTask> localTasks = tasks.get(UnschedulableTask.partition);

        // BaseRis应该是全部计算，而不是单独计算某个processor上的
        ResponseTimeAnalysisWithVariousPriorities_S_B_Worst analysis = new ResponseTimeAnalysisWithVariousPriorities_S_B_Worst();
        long[][] BaseRis = analysis.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false);

        for(int i = 0 ; i < localTasks.size(); i++){
            SporadicTask localTask = localTasks.get(i);
            if(localTask.priority > UnschedulableTask.priority && localTask.priority != 998){
                possiblePriority = getImprovePossiblePriority(localTask,tasks,resources);
                gain = getImproveGain(localTask,tasks,resources,possiblePriority,BaseRis);
                increasePossiblePriorityList.add(possiblePriority);
                increaseGainList.add(gain);
            } else if(localTask.priority == UnschedulableTask.priority){
                possiblePriority = getImprovePossiblePriority(localTask,tasks,resources);
                gain = getImproveGain(localTask,tasks,resources,possiblePriority,BaseRis);
                // selfPossiblePriorityList = possiblePriority;
                // selfGainList = gain;
                selfPossiblePriorityList.clear();
                if(possiblePriority.size()!=0){
                    selfPossiblePriorityList.addAll(possiblePriority);
                }
                selfGainList.clear();
                if(gain == null){
                    selfGainList = null;
                }
                else if(gain.size() != 0) {
                    selfGainList.addAll(gain);
                }

            } else{
                possiblePriority = getDecreasePossiblePriority(localTask,tasks,resources);
                gain = getDecreaseGain(localTask,tasks,resources,possiblePriority,BaseRis);
                decreasePossiblePriorityList.add(possiblePriority);
                decreaseGainList.add(gain);
            }
        }


    }
    public SporadicTask findTaskById(ArrayList<SporadicTask> tasks, int id){
        for(int i = 0 ; i < tasks.size(); i++){
            if(tasks.get(i).id == id){
                return tasks.get(i);
            }
        }
        return null;
    }

    private ArrayList<ArrayList<Integer>> getImprovePossiblePriority(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources){
        // 这里只是获取优先级
        ArrayList<ArrayList<Integer>> possiblePriority = new ArrayList<>();
        int partition = task.partition;
        ArrayList<SporadicTask> localTasks = tasks.get(partition);
        ArrayList<Integer> priorityCandidate = new ArrayList<>();
        // 对于访问资源的任务，应该返回其可能的优先级（基于任务视角的提升）
        priorityCandidate.add(task.priority);
        for (SporadicTask localTask : localTasks) {
            if (localTask.priority > task.priority) {
                priorityCandidate.add(localTask.priority);
            }
        }

        for(int i = 0 ; i < task.resource_required_index.size(); i++){
            ArrayList<Integer> possiblePriorityForOneResource = new ArrayList<>();
            // 每个任务访问的资源都有一个访问时的优先级，只有在priorityCandidate中大于这个优先级的候选才可以被加入到EIP
            Integer comparedPriorityforOneResource = task.resource_required_priority.get(i);
            for (Integer integer : priorityCandidate) {
                if (integer > comparedPriorityforOneResource) {
                    possiblePriorityForOneResource.add(integer);
                }
            }
            possiblePriority.add(possiblePriorityForOneResource);
        }
        return possiblePriority;
    }

    private ArrayList<ArrayList<Gain>> getImproveGain(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> ImprovePossiblePriority, long[][] BaseRis){
        // 这里只是获取优先级

        if(ImprovePossiblePriority.size() == 0){
            return null;
        }
        ArrayList<ArrayList<Gain>> gain_list = new ArrayList<>();
        int partition = task.partition;

        // 这里用到的是计算单个处理器上的时间
        ApproximationModelForLocalTasks_S_B_Worst approximationModelForLocalTasks = new ApproximationModelForLocalTasks_S_B_Worst();

        for(int i = 0; i < ImprovePossiblePriority.size(); i++){
            if(ImprovePossiblePriority.get(i).size() == 0){
                gain_list.add(null);
                continue;
            }
            ArrayList<Gain> gain_subList = new ArrayList<>();
            for(int j = 0 ; j < ImprovePossiblePriority.get(i).size(); j++){
                // BaseRis = ca.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false, partition);
                // 记录原有的priority情况
                Integer original_resource_required_priority = task.resource_required_priority.get(i);

                task.resource_required_priority.set(i, ImprovePossiblePriority.get(i).get(j));

                long[][] Update_Ris = approximationModelForLocalTasks.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false, partition);

                // 恢复
                task.resource_required_priority.set(i, original_resource_required_priority);

                // 比较Update_Ris和BaseRis
                // System.out.println(Update_Ris[partition]);
                // System.out.println(BaseRis[partition]);
                long self_benefit = -1;
                HashMap<Integer,Long> other_benefit = new HashMap<>();
                for(int index = 0 ; index < tasks.get(partition).size(); index++){
                    // 自己得到了提升
                    if(tasks.get(partition).get(index).id == task.id){
                        self_benefit = Update_Ris[partition][index] - BaseRis[partition][index];
                    }
                    // 其余任务应该得到了下降
                    else{
                        int key = tasks.get(partition).get(index).id;
                        long val = Update_Ris[partition][index] - BaseRis[partition][index];
                        other_benefit.put(key,val);
//						if(self_benefit == 0 && val < 0){
//							System.out.println("?????????");
//							task.resource_required_priority.set(i, original_resource_required_priority);
//							long[][] Update_ = ca.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false, partition);
//							System.out.println("????");
//						}
                    }

                }
                gain_subList.add(new Gain(task.id, self_benefit,other_benefit));
            }

            gain_list.add(gain_subList);
        }

        // Test 组合情况
//		boolean first_flag = false;
//		boolean second_flag = false;
//		Gain A = null;
//		Gain B = null;
//		Gain C = null;
//		Gain ABC = null;
//
//		boolean finish_flag = false;
//		for(int a = 0 ; a < gain_list.size(); a++){
//			if(gain_list.get(a) == null){
//				continue;
//			}
//			if(finish_flag){
//				break;
//			}
//			// 先测试两种组合
//			for(int b = 0 ; b < gain_list.get(a).size(); b++){
//
//				if(!first_flag){
//					if(gain_list.get(a).get(b) != null && gain_list.get(a).get(b).self_benefit < 0){
//						A = gain_list.get(a).get(b);
//						task.resource_required_priority.set(a, ImprovePossiblePriority.get(a).get(b));
//						first_flag = true;
//						break;
//					}
//				}
//				else{
//					if(!second_flag){
//						if(gain_list.get(a).get(b) != null && gain_list.get(a).get(b).self_benefit < 0){
//							B = gain_list.get(a).get(b);
//							task.resource_required_priority.set(a, ImprovePossiblePriority.get(a).get(b));
//							second_flag = true;
//							break;
//						}
//					}
//					else{
//						if(gain_list.get(a).get(b) != null && gain_list.get(a).get(b).self_benefit < 0){
//							C = gain_list.get(a).get(b);
//							task.resource_required_priority.set(a, ImprovePossiblePriority.get(a).get(b));
//							finish_flag = true;
//							break;
//						}
//					}
//				}
//			}
//		}
//
//		long[][] Update_Ris = ca.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false, partition);
//
//
//		long self_benefit = -1;
//		HashMap<Integer,Long> other_benefit = new HashMap<>();
//		for(int index = 0 ; index < tasks.get(partition).size(); index++){
//			// 自己得到了提升
//			if(tasks.get(partition).get(index).id == task.id){
//				self_benefit = Update_Ris[partition][index] - BaseRis[partition][index];
//			}
//			// 其余任务应该得到了下降
//			else{
//				int key = tasks.get(partition).get(index).id;
//				long val = Update_Ris[partition][index] - BaseRis[partition][index];
//				other_benefit.put(key,val);
////						if(self_benefit == 0 && val < 0){
////							System.out.println("?????????");
////							task.resource_required_priority.set(i, original_resource_required_priority);
////							long[][] Update_ = ca.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false, partition);
////							System.out.println("????");
////						}
//			}
//
//		}
//
//		ABC = new Gain(self_benefit,other_benefit);
        return gain_list;

    }

    private ArrayList<ArrayList<Integer>> getDecreasePossiblePriority(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources){
        // 这里只是获取优先级
        ArrayList<ArrayList<Integer>> possiblePriority = new ArrayList<>();
        int partition = task.partition;
        ArrayList<SporadicTask> localTasks = tasks.get(partition);
        ArrayList<Integer> priorityCandidate = new ArrayList<>();
        // 对于访问资源的任务，应该返回其可能的优先级（基于任务视角的提升）
        priorityCandidate.add(task.priority);
        for (SporadicTask localTask : localTasks) {
            if (localTask.priority > task.priority) {
                priorityCandidate.add(localTask.priority);
            }
        }

        for(int i = 0 ; i < task.resource_required_index.size(); i++){
            ArrayList<Integer> possiblePriorityForOneResource = new ArrayList<>();
            // 每个任务访问的资源都有一个访问时的优先级，只有在priorityCandidate中大于这个优先级的候选才可以被加入到EIP
            Integer comparedPriorityforOneResource = task.resource_required_priority.get(i);
            for (Integer integer : priorityCandidate) {
                if (integer < comparedPriorityforOneResource) {
                    possiblePriorityForOneResource.add(integer);
                }
            }
            possiblePriority.add(possiblePriorityForOneResource);
        }
        return possiblePriority;
    }

    private ArrayList<ArrayList<Gain>> getDecreaseGain(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, ArrayList<ArrayList<Integer>> DecreasePossiblePriority, long[][] BaseRis){
        // 这里只是获取优先级

        if(DecreasePossiblePriority.size() == 0){
            return null;
        }
        ArrayList<ArrayList<Gain>> gain_list = new ArrayList<>();
        int partition = task.partition;
        ApproximationModelForLocalTasks_S_B_Worst approximationModelForLocalTasks = new ApproximationModelForLocalTasks_S_B_Worst();
        for(int i = 0; i < DecreasePossiblePriority.size(); i++){
            if(DecreasePossiblePriority.get(i).size() == 0){
                gain_list.add(null);
                continue;
            }
            ArrayList<Gain> gain_subList = new ArrayList<>();
            for(int j = 0 ; j < DecreasePossiblePriority.get(i).size(); j++){
                // BaseRis = ca.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false, partition);
                // 记录原有的priority情况
                Integer original_resource_required_priority = task.resource_required_priority.get(i);

                task.resource_required_priority.set(i, DecreasePossiblePriority.get(i).get(j));

                long[][] Update_Ris = approximationModelForLocalTasks.getResponseTimeByDMPO(tasks,resources,1,false,false,true,true,false, partition);

                // 恢复
                task.resource_required_priority.set(i, original_resource_required_priority);

                // 比较Update_Ris和BaseRis
                // System.out.println(Update_Ris[partition]);
                // System.out.println(BaseRis[partition]);
                long self_benefit = -1;
                HashMap<Integer,Long> other_benefit = new HashMap<>();
                for(int index = 0 ; index < tasks.get(partition).size(); index++){
                    // 自己得到了提升
                    if(tasks.get(partition).get(index).id == task.id){
                        self_benefit = Update_Ris[partition][index] - BaseRis[partition][index];
//						if(self_benefit != 0){
//							System.out.println(1);
//						}
                    }
                    // 其余任务应该得到了下降
                    else{
                        int key = tasks.get(partition).get(index).id;
                        long val = Update_Ris[partition][index] - BaseRis[partition][index];
                        other_benefit.put(key,val);
                    }

                }
                gain_subList.add(new Gain(task.id, self_benefit,other_benefit));
            }

            gain_list.add(gain_subList);
        }
        return gain_list;
    }

    // 纯收益调整函数，分别针对任务自身、高优先级任务、低优先级任务
    private Integer FineGrained_PureProfit_Tuning_Self(SporadicTask UnschedulableTask, ArrayList<SporadicTask> localTasks, ArrayList<ArrayList<Integer>> selfPossiblePriorityList, ArrayList<ArrayList<Gain>> selfGainList){

        Integer tuning_flag = 0;

        if(selfGainList != null && selfGainList.size() != 0){
            for(int bb = 0 ; bb < selfGainList.size(); bb++){
                if(selfGainList.get(bb) != null && selfGainList.get(bb).size() != 0){
                    // 每个优先级调整
                    // 由于我们对于一个资源只能选择一个优先级调整，因此如果有多个可能地“纯收益”优先级调整，我们选择最大的一个
                    long MaxBenefitForUnschedulableTask = -1L;
                    int MaxBenefitForUnschedulableTaskIndex = -1;
                    for(int cc = 0 ; cc <selfGainList.get(bb).size(); cc++){
                        // 得到了一个可能的优先级调整
                        Gain gain = selfGainList.get(bb).get(cc);
                        // 由于是第一次地“纯收益调整”，这里的判断逻辑较为简单
                        if(gain.self_benefit < 0){
                            boolean OkGainFlag = true;
                            Long BenefitForUnschedulableTask = 1L;
                            for (Map.Entry<Integer, Long> entry : gain.other_benefit.entrySet()) {
                                Integer taskId = entry.getKey();
                                Long other_benefit_gain = entry.getValue();
                                if(other_benefit_gain > findTaskById(localTasks,taskId).maxAcceptableResponseTime){
                                    OkGainFlag = false;
                                }
                            }
                            // 这次gain符合"纯收益"调整，
                            if(OkGainFlag){
                                if(gain.self_benefit < MaxBenefitForUnschedulableTask){
                                    MaxBenefitForUnschedulableTask = gain.self_benefit;
                                    MaxBenefitForUnschedulableTaskIndex = cc;
                                }
                            }
                        }
                    }
                    // 调！
                    if(MaxBenefitForUnschedulableTaskIndex != -1){
                        UnschedulableTask.resource_required_priority.set(bb,selfPossiblePriorityList.get(bb).get(MaxBenefitForUnschedulableTaskIndex));
                        tuning_flag++;
                    }
                }
            }
        }

        return tuning_flag;

    }

    private Integer FineGrained_PureProfit_Tuning_High(SporadicTask UnschedulableTask, ArrayList<SporadicTask> localTasks, ArrayList<ArrayList<ArrayList<Integer>>> IncreasePossiblePriorityList, ArrayList<ArrayList<ArrayList<Gain>>> IncreaseGainList){

        Integer tuning_flag = 0;

        for(int aa = 0 ; aa < IncreaseGainList.size(); aa++){
            if(IncreaseGainList.get(aa) != null && IncreaseGainList.get(aa).size() != 0){
                // 每个资源
                for(int bb = 0 ; bb < IncreaseGainList.get(aa).size(); bb++){
                    if(IncreaseGainList.get(aa).get(bb) != null && IncreaseGainList.get(aa).get(bb).size() != 0){
                        // 每个优先级调整
                        // 由于我们对于一个资源只能选择一个优先级调整，因此如果有多个可能地“纯收益”优先级调整，我们选择最大的一个
                        Long MaxBenefitForUnschedulableTask = -1L;
                        int MaxBenefitForUnschedulableTaskIndex = -1;
                        for(int cc = 0 ; cc < IncreaseGainList.get(aa).get(bb).size(); cc++){
                            // 得到了一个可能的优先级调整
                            Gain gain = IncreaseGainList.get(aa).get(bb).get(cc);
                            // 由于是第一次地“纯收益调整”，这里的判断逻辑较为简单
                            if(gain.self_benefit <= findTaskById(localTasks,gain.task_id).maxAcceptableResponseTime){
                                boolean OkGainFlag = true;

                                Long BenefitForUnschedulableTask = 1L;
                                for (Map.Entry<Integer, Long> entry : gain.other_benefit.entrySet()) {
                                    Integer taskId = entry.getKey();
                                    Long other_benefit_gain = entry.getValue();
                                    if(UnschedulableTask.id == taskId && other_benefit_gain >= 0){
                                        OkGainFlag = false;
                                    } else if(UnschedulableTask.id == taskId && other_benefit_gain < 0){
                                        BenefitForUnschedulableTask = other_benefit_gain;
                                    }

                                    if(UnschedulableTask.id != taskId && other_benefit_gain > findTaskById(localTasks,taskId).maxAcceptableResponseTime){
                                        OkGainFlag = false;
                                    }
                                }
                                // 这次gain符合"纯收益"调整，
                                if(OkGainFlag){
                                    if(BenefitForUnschedulableTask < MaxBenefitForUnschedulableTask){
                                        MaxBenefitForUnschedulableTask = BenefitForUnschedulableTask;
                                        MaxBenefitForUnschedulableTaskIndex = cc;
                                    }
                                }
                            }
                        }
                        // 调！
                        if(MaxBenefitForUnschedulableTaskIndex != -1){
                            for(int dd = 0 ; dd < localTasks.size(); dd++){
                                if(localTasks.get(dd).id == IncreaseGainList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex).task_id){
                                    localTasks.get(dd).resource_required_priority.set(bb,IncreasePossiblePriorityList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex));
                                    tuning_flag++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return tuning_flag;

    }

    private Integer FineGrained_PureProfit_Tuning_Low(SporadicTask UnschedulableTask, ArrayList<SporadicTask> localTasks, ArrayList<ArrayList<ArrayList<Integer>>> DecreasePossiblePriorityList, ArrayList<ArrayList<ArrayList<Gain>>> DecreaseGainList){

        Integer tuning_flag = 0;

        for(int aa = 0 ; aa < DecreaseGainList.size(); aa++){
            if(DecreaseGainList.get(aa) != null && DecreaseGainList.get(aa).size() != 0){
                // 每个资源
                for(int bb = 0 ; bb < DecreaseGainList.get(aa).size(); bb++){
                    if(DecreaseGainList.get(aa).get(bb) != null && DecreaseGainList.get(aa).get(bb).size() != 0){
                        // 每个优先级调整
                        // 由于我们对于一个资源只能选择一个优先级调整，因此如果有多个可能地“纯收益”优先级调整，我们选择最大的一个
                        Long MaxBenefitForUnschedulableTask = -1L;
                        int MaxBenefitForUnschedulableTaskIndex = -1;
                        for(int cc = 0 ; cc < DecreaseGainList.get(aa).get(bb).size(); cc++){
                            // 得到了一个可能的优先级调整
                            Gain gain = DecreaseGainList.get(aa).get(bb).get(cc);
                            // 由于是第一次地“纯收益调整”，这里的判断逻辑较为简单
                            if(gain.self_benefit <= 0){
                                boolean OkGainFlag = true;

                                Long BenefitForUnschedulableTask = 1L;
                                for (Map.Entry<Integer, Long> entry : gain.other_benefit.entrySet()) {
                                    Integer taskId = entry.getKey();
                                    Long other_benefit_gain = entry.getValue();
                                    //
                                    if(UnschedulableTask.id == taskId && other_benefit_gain >= 0){
                                        OkGainFlag = false;
                                    } else if(UnschedulableTask.id == taskId && other_benefit_gain < 0){
                                        BenefitForUnschedulableTask = other_benefit_gain;
                                    }

                                    if(UnschedulableTask.id != taskId && other_benefit_gain > 0){
                                        OkGainFlag = false;
                                    }
                                }
                                // 这次gain符合"纯收益"调整，
                                if(OkGainFlag){
                                    if(BenefitForUnschedulableTask < MaxBenefitForUnschedulableTask){
                                        MaxBenefitForUnschedulableTask = BenefitForUnschedulableTask;
                                        MaxBenefitForUnschedulableTaskIndex = cc;
                                    }
                                }
                            }
                        }
                        // 调！
                        if(MaxBenefitForUnschedulableTaskIndex != -1){
                            for(int dd = 0 ; dd < localTasks.size(); dd++){
                                if(localTasks.get(dd).id == DecreaseGainList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex).task_id){
                                    localTasks.get(dd).resource_required_priority.set(bb,DecreasePossiblePriorityList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex));
                                    tuning_flag++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return tuning_flag;

    }
    // 贪心调整函数，分别针对任务自身、高优先级任务、低优先级任务
    private Integer FineGrained_GreedyProfit_Tuning_Self(SporadicTask UnschedulableTask, ArrayList<SporadicTask> localTasks, ArrayList<ArrayList<Integer>> selfPossiblePriorityList, ArrayList<ArrayList<Gain>> selfGainList){

        Integer tuning_flag = 0;

        if(selfGainList != null && selfGainList.size() != 0){
            for(int bb = 0 ; bb < selfGainList.size(); bb++){
                if(selfGainList.get(bb) != null && selfGainList.get(bb).size() != 0){
                    // 每个优先级调整
                    // 由于我们对于一个资源只能选择一个优先级调整，因此如果有多个可能地“纯收益”优先级调整，我们选择最大的一个
                    Long MaxBenefitForUnschedulableTask = -1L;
                    int MaxBenefitForUnschedulableTaskIndex = -1;
                    for(int cc = 0 ; cc <selfGainList.get(bb).size(); cc++){
                        // 得到了一个可能的优先级调整
                        Gain gain = selfGainList.get(bb).get(cc);
                        // 由于是第一次地“纯收益调整”，这里的判断逻辑较为简单
                        if(gain.self_benefit < 0){
                            boolean OkGainFlag = true;
                            Long BenefitForUnschedulableTask = 1L;
                            for (Map.Entry<Integer, Long> entry : gain.other_benefit.entrySet()) {
                                Integer taskId = entry.getKey();
                                Long other_benefit_gain = entry.getValue();
                                if(other_benefit_gain > findTaskById(localTasks,taskId).maxAcceptableResponseTime){
                                    OkGainFlag = false;
                                }
                            }
                            // 这次gain符合"纯收益"调整，
                            if(OkGainFlag){
                                if(gain.self_benefit < MaxBenefitForUnschedulableTask){
                                    MaxBenefitForUnschedulableTask = gain.self_benefit;
                                    MaxBenefitForUnschedulableTaskIndex = cc;
                                }
                            }
                        }
                    }
                    // 调！
                    if(MaxBenefitForUnschedulableTaskIndex != -1){
                        UnschedulableTask.resource_required_priority.set(bb,selfPossiblePriorityList.get(bb).get(MaxBenefitForUnschedulableTaskIndex));
                        tuning_flag++;
                    }
                }
            }
        }

        return tuning_flag;

    }

    private Integer FineGrained_GreedyProfit_Tuning_High(SporadicTask UnschedulableTask, ArrayList<SporadicTask> localTasks, ArrayList<ArrayList<ArrayList<Integer>>> IncreasePossiblePriorityList, ArrayList<ArrayList<ArrayList<Gain>>> IncreaseGainList){

        Integer tuning_flag = 0;

        for(int aa = 0 ; aa < IncreaseGainList.size(); aa++){
            if(IncreaseGainList.get(aa) != null && IncreaseGainList.get(aa).size() != 0){
                // 每个资源
                for(int bb = 0 ; bb < IncreaseGainList.get(aa).size(); bb++){
                    if(IncreaseGainList.get(aa).get(bb) != null && IncreaseGainList.get(aa).get(bb).size() != 0){
                        // 每个优先级调整
                        // 由于我们对于一个资源只能选择一个优先级调整，因此如果有多个可能地“贪心”优先级调整，我们选择最大的一个
                        Long MaxBenefitForUnschedulableTask = -1L;
                        int MaxBenefitForUnschedulableTaskIndex = -1;
                        for(int cc = 0 ; cc < IncreaseGainList.get(aa).get(bb).size(); cc++){
                            // 得到了一个可能的优先级调整
                            Gain gain = IncreaseGainList.get(aa).get(bb).get(cc);
                            // 贪心调整
                            if(gain.self_benefit <= findTaskById(localTasks,gain.task_id).maxAcceptableResponseTime){
                                boolean OkGainFlag = true;

                                Long BenefitForUnschedulableTask = 1L;
                                for (Map.Entry<Integer, Long> entry : gain.other_benefit.entrySet()) {
                                    Integer taskId = entry.getKey();
                                    Long other_benefit_gain = entry.getValue();
                                    if(UnschedulableTask.id == taskId && other_benefit_gain >= 0){
                                        OkGainFlag = false;
                                    } else if(UnschedulableTask.id == taskId && other_benefit_gain < 0){
                                        BenefitForUnschedulableTask = other_benefit_gain;
                                    }

                                    if(UnschedulableTask.id != taskId && other_benefit_gain > findTaskById(localTasks,taskId).maxAcceptableResponseTime){
                                        OkGainFlag = false;
                                    }
                                }
                                // 这次gain符合"纯收益"调整，
                                if(OkGainFlag){
                                    if(BenefitForUnschedulableTask < MaxBenefitForUnschedulableTask){
                                        MaxBenefitForUnschedulableTask = BenefitForUnschedulableTask;
                                        MaxBenefitForUnschedulableTaskIndex = cc;
                                    }
                                }
                            }
                        }
                        // 调！
                        if(MaxBenefitForUnschedulableTaskIndex != -1){
                            for(int dd = 0 ; dd < localTasks.size(); dd++){
                                if(localTasks.get(dd).id == IncreaseGainList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex).task_id){
                                    localTasks.get(dd).resource_required_priority.set(bb,IncreasePossiblePriorityList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex));
                                    tuning_flag++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return tuning_flag;

    }

    private Integer FineGrained_GreedyProfit_Tuning_Low(SporadicTask UnschedulableTask, ArrayList<SporadicTask> localTasks, ArrayList<ArrayList<ArrayList<Integer>>> DecreasePossiblePriorityList, ArrayList<ArrayList<ArrayList<Gain>>> DecreaseGainList){

        Integer tuning_flag = 0;

        for(int aa = 0 ; aa < DecreaseGainList.size(); aa++){
            if(DecreaseGainList.get(aa) != null && DecreaseGainList.get(aa).size() != 0){
                // 每个资源
                for(int bb = 0 ; bb < DecreaseGainList.get(aa).size(); bb++){
                    if(DecreaseGainList.get(aa).get(bb) != null && DecreaseGainList.get(aa).get(bb).size() != 0){
                        // 每个优先级调整
                        // 由于我们对于一个资源只能选择一个优先级调整，因此如果有多个可能地“纯收益”优先级调整，我们选择最大的一个
                        Long MaxBenefitForUnschedulableTask = -1L;
                        int MaxBenefitForUnschedulableTaskIndex = -1;
                        for(int cc = 0 ; cc < DecreaseGainList.get(aa).get(bb).size(); cc++){
                            // 得到了一个可能的优先级调整
                            Gain gain = DecreaseGainList.get(aa).get(bb).get(cc);
                            // 由于是第一次地“纯收益调整”，这里的判断逻辑较为简单
                            if(gain.self_benefit <= (findTaskById(localTasks,gain.task_id).maxAcceptableResponseTime - findTaskById(localTasks,gain.task_id).nowIncreaseResponseTime)){
                                boolean OkGainFlag = true;

                                Long BenefitForUnschedulableTask = 1L;
                                for (Map.Entry<Integer, Long> entry : gain.other_benefit.entrySet()) {
                                    Integer taskId = entry.getKey();
                                    Long other_benefit_gain = entry.getValue();
                                    //
                                    if(UnschedulableTask.id == taskId && other_benefit_gain >= 0){
                                        OkGainFlag = false;
                                    } else if(UnschedulableTask.id == taskId && other_benefit_gain < 0){
                                        BenefitForUnschedulableTask = other_benefit_gain;
                                    }

                                    if(UnschedulableTask.id != taskId && other_benefit_gain > (findTaskById(localTasks,gain.task_id).maxAcceptableResponseTime - findTaskById(localTasks,gain.task_id).nowIncreaseResponseTime)){
                                        OkGainFlag = false;
                                    }
                                }
                                // 这次gain符合"纯收益"调整，
                                if(OkGainFlag){
                                    if(BenefitForUnschedulableTask < MaxBenefitForUnschedulableTask){
                                        MaxBenefitForUnschedulableTask = BenefitForUnschedulableTask;
                                        MaxBenefitForUnschedulableTaskIndex = cc;
                                    }
                                }
                            }
                        }
                        // 调！
                        if(MaxBenefitForUnschedulableTaskIndex != -1){
                            for(int dd = 0 ; dd < localTasks.size(); dd++){
                                if(localTasks.get(dd).id == DecreaseGainList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex).task_id){

                                    localTasks.get(dd).resource_required_priority.set(bb,DecreasePossiblePriorityList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex));
                                    tuning_flag++;

                                    // 还得调nowIncreaseResponseTime啊
                                    Gain AdjustGain = DecreaseGainList.get(aa).get(bb).get(MaxBenefitForUnschedulableTaskIndex);
                                    findTaskById(localTasks,AdjustGain.task_id).nowIncreaseResponseTime += AdjustGain.self_benefit;

                                    for (Map.Entry<Integer, Long> entry : AdjustGain.other_benefit.entrySet()) {
                                        Integer taskId = entry.getKey();
                                        Long other_benefit_gain = entry.getValue();
                                        findTaskById(localTasks,taskId).nowIncreaseResponseTime += other_benefit_gain;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return tuning_flag;

    }

}
