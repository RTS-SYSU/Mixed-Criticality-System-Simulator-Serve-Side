package com.example.serveside.request;


/**
 * {@code ConfigurationInformation}表示前端向后端传递的系统环境配置参数。
 */
public class ConfigurationInformation {
    /**
     * 处理器核心个数
     */
    private Integer totalCPUNum;

    /**
     * 一个处理器核心平均分配的任务数
     */
    private Integer numberOfTaskInAPartition;

    /**
     * 任务最小的最短发布时间间隔。
     */
    private Integer minPeriod;

    /**
     * 任务最大的最短发布时间间隔。
     */
    private Integer maxPeriod;

    /**
     * 一个任务访问同一资源的最大次数。
     */
    private Integer numberOfMaxAccessToOneResource;

    /**
     * 访问资源的任务占所有任务的比例。
     */
    private Double resourceSharingFactor;

    /**
     * 系统生成的资源的类型。
     */
    private String resourceType;

    /**
     * 系统资源个数。
     */
    private String resourceNum;

    /**
     * 系统模拟运行过程中是否开启关键级切换。
     * <p>
     * 该属性是一个布尔值，用于表示系统模拟运行过程中是否开启关键级切换。
     * 当 {@code isStartUpSwitch} 为 true 时，表示系统开启关键级切换。
     * 当 {@code isStartUpSwitch} 为 false 时，表示系统关闭关键级切换。
     */
    private Boolean isStartUpSwitch;

    /**
     * 系统完成关键级切换的时间点。
     * <p>
     * 该属性是一个整数，用于表示系统完成关键级切换的时间点。
     * 当 {@code criticalitySwitchTime} 为 -1 时，表示系统没有发生关键级切换。
     * 当 {@code criticalitySwitchTime} 大于 0 时，表示系统在 {@code criticalitySwitchTime} 发生关键级切换。
     */

    /* 以下均是上面属性的 setter 和 getter 函数。 */
    private Integer criticalitySwitchTime;

    public Boolean getIsStartUpSwitch() {
        return isStartUpSwitch;
    }

    public Integer getCriticalitySwitchTime() {
        return criticalitySwitchTime;
    }

    public void setIsStartUpSwitch(Boolean startUpSwitch) {
        this.isStartUpSwitch = startUpSwitch;
    }

    public void setCriticalitySwitchTime(Integer criticalitySwitchTime) {
        this.criticalitySwitchTime = criticalitySwitchTime;
    }

    public Integer getTotalCPUNum() {
        return this.totalCPUNum;
    }

    public void setTotalCpuCoreNum(Integer totalCPUCoreNum) {
        this.totalCPUNum = totalCPUCoreNum;
    }

    public Integer getNumberOfTaskInAPartition() {
        return this.numberOfTaskInAPartition;
    }

    public void setNumberOfTaskInAPartition(Integer numberOfTaskInAPartition) {
        this.numberOfTaskInAPartition = numberOfTaskInAPartition;
    }

    public Integer getMinPeriod() { return this.minPeriod; }

    public void setMinPeriod(Integer minPeriod) { this.minPeriod = minPeriod; }

    public Integer getMaxPeriod() { return this.maxPeriod; }

    public void setMaxPeriod(Integer maxPeriod) { this.maxPeriod = maxPeriod; }

    public Integer getNumberOfMaxAccessToOneResource() { return this.numberOfMaxAccessToOneResource; }

    public void setNumberOfMaxAccessToOneResource(Integer numberOfMaxAccessToOneResource) { this.numberOfMaxAccessToOneResource = numberOfMaxAccessToOneResource; }

    public Double getResourceSharingFactor() { return this.resourceSharingFactor; }

    public void setResourceSharingFactor(Double resourceSharingFactor) { this.resourceSharingFactor = resourceSharingFactor; }

    public String getResourceType() { return this.resourceType; }

    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceNum() { return this.resourceNum; }

    public void setResourceNum(String resourceNum) { this.resourceNum = resourceNum; }
}
