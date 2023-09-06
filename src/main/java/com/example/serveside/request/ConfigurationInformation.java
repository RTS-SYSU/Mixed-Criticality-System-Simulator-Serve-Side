package com.example.serveside.request;

public class ConfigurationInformation {
    /* CPU个数 */
    private Integer totalCpuCoreNum;

    /* 每一个 CPU 上有几个任务在运行 */
    private Integer numberOfTaskInAPartition;

    public Integer getTotalCpuCoreNum() {
        return this.totalCpuCoreNum;
    }

    public void setTotalCpuCoreNum(Integer totalCpuCoreNum) {
        this.totalCpuCoreNum = totalCpuCoreNum;
    }

    public Integer getNumberOfTaskInAPartition() {
        return this.numberOfTaskInAPartition;
    }

    public void setNumberOfTaskInAPartition(Integer numberOfTaskInAPartition) {
        this.numberOfTaskInAPartition = numberOfTaskInAPartition;
    }
}
