package com.example.serveside.response;

/**
 * {@code WorstCaseInformation} 是后端向前端传递信息的载体，用于传输在指定任务的最坏运行情况下系统的可调度性以及前端绘制处理器和任务甘特图所需信息。
 * <p>
 *     {@code WorstCaseInformation} 包含系统可调度性、所有处理器的甘特图信息、所有任务执行的甘特图信息以及系统发生关键级切换的时间点。
 * </p>
 */
public class WorstCaseInformation {
    /**
     * 系统可调度性。
     */
    private Boolean schedulable;

    /**
    * 所有处理器的甘特图信息、所有任务执行的甘特图信息以及系统发生关键级切换的时间点
    */
    private ToTalInformation totalInformation;

    /**
     * {@code WorstCaseInformation} 构造函数。
     */
    public WorstCaseInformation(Boolean _schedulable, ToTalInformation _totalInformation) {
        this.schedulable = _schedulable;
        this.totalInformation = _totalInformation;
    }

    /* 以下均是上面属性的 setter 和 getter 函数。 */
    public void setSchedulable(Boolean _schedulable) {
        this.schedulable = _schedulable;
    }

    public void setTotalInformation(ToTalInformation _totalInformation) {
        this.totalInformation = _totalInformation;
    }

    public Boolean getSchedulable() { return this.schedulable; }

    public ToTalInformation getTotalInformation() { return this.totalInformation; }

}
