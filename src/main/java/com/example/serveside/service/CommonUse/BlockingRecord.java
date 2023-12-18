package com.example.serveside.service.CommonUse;

import com.example.serveside.service.CommonUse.BasicPCB;

import java.util.ArrayList;
/**
 * 被查看最坏运行情况的任务受到的阻塞和干扰的详细信息。
 * <p>
 *     <strong>注意:  为了使下面注释更具清晰度和详细性，记被查看最差运行情况的任务为sufferTask。</strong>
 * </p>
 */
public class BlockingRecord {
    /**
     * blockingTask 中访问的第{@code ithResource}个资源.
     */
    public int ithResource;

    /**
     * 造成阻塞的资源的标识符。
     */
    public int blockingResourceId;

    /**
     * 正在执行的任务（低优先级任务、高优先级任务、{@code sufferTask}）
     */
    public BasicPCB blockingTask;

    /**
     * blockingTask 是第{@code blockingTaskIthRelease}次释放。
     */
    public int blockingTaskIthRelease;

    /**
     *  其他处理器核心上访问该资源的任务（参与本次阻塞）。
     */
    public ArrayList<BasicPCB> blockingResourceTasks;

    /**
     *  其他处理器核心上访问该资源的任务阻塞 blockingTask 访问资源的时长。
     */
    public ArrayList<Integer> blockingTimes;

    /**
     * 其他 cpu 上访问资源的任务（参与本次阻塞）是第几次任务发布。
     */
    public ArrayList<Integer> blockingResourceTasksReleaseIndex;

    /**
     *  阻塞类型。
     */
    public enum BlockingTypes {arrivalBlockingGlobalResource, arrivalBlockingLocalResource, highPriorityTaskNormalExecution, highPriorityTaskAccessGlobalResource, sufferTaskAccessGlobalResource, sufferTaskNormalExecution}

    /**
     *  本次阻塞对 sufferTask 来说是什么类型。
     */
    public BlockingTypes blockingTypes;

    /* part-high-priority-task 、pure-high-priority-task、high-priority-task-access-global-resource、suffer-task-access-global-resource 会使用到下面属性 */
    public boolean isFirstBlockingRecord;

    /**
     * 本次阻塞开始的时间。
     */
    public int startTime;

    /**
     * 本次阻塞结束的时间。
     */
    public int endTime;
    public BlockingRecord (int _ithResource, int _blockingResourceId, BasicPCB _blockingTask, int _blockingTaskIthRelease, ArrayList<BasicPCB> _blockingResourceTasks, ArrayList<Integer> _blockingTimes,
                           ArrayList<Integer> _blockingResourceTasksReleaseIndex, BlockingTypes _blockingTypes, int _startTime, int _endTime, boolean _isFirstBlockingRecord) {
        this.ithResource = _ithResource;
        this.blockingResourceId = _blockingResourceId;
        this.blockingTask = _blockingTask;
        this.blockingTaskIthRelease = _blockingTaskIthRelease;
        this.blockingResourceTasks = _blockingResourceTasks;
        this.blockingTimes = _blockingTimes;
        this.blockingResourceTasksReleaseIndex = _blockingResourceTasksReleaseIndex;
        this.blockingTypes = _blockingTypes;
        this.isFirstBlockingRecord = _isFirstBlockingRecord;
        this.startTime = _startTime;
        this.endTime = _endTime;
    }
}
