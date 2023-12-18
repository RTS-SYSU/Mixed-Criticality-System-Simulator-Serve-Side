package com.example.serveside.response;

import java.util.*;
import com.example.serveside.service.CommonUse.BasicResource;

/**
 * {@code ResourceInformation} 是后端向前端传递信息的载体，用于传输系统中资源的信息。
 * <p>
 *     {@code ResourceInformation} 描述系统中系统的信息，包含资源标识符、低关键级下使用资源的时长、高关键级下使用资源的时长、资源类型以及访问资源的任务。
 * </p>
 */
public class ResourceInformation {
    /**
     * 资源标识符。
     */
    private Integer resourceId;

    /**
     * 低关键级下任务执行资源的时长。
     */
    private Integer c_low;

    /**
     * 高关键级下任务执行资源的时长。
     */
    private Integer c_high;

    /**
     * 资源的类型。
     * <p>
     *     当 {@code isGlobalResource} 为 true 时，资源是全局资源。
     *     当 {@code isGlobalResource} 为 false 时，资源是局部资源。
     * </p>
     */
    private String isGlobalResource;

    /**
     * 访问资源的任务。
     */
    private List<Integer> accessTasks;


    /**
     * {@code ResourceInformation}的构造函数
     */
    public ResourceInformation(BasicResource basicResource) {
        this.resourceId = basicResource.id;
        this.c_low = basicResource.c_low;
        this.c_high = basicResource.c_high;
        if (basicResource.isGlobal)
            this.isGlobalResource = "true";
        else
            this.isGlobalResource = "false";
    }

    /* 以下均是上面属性的 setter 和 getter 函数。 */
    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public void setC_low(Integer c_low) {
        this.c_low = c_low;
    }

    public void setC_high(Integer c_high) {
        this.c_high = c_high;
    }

    public void setAccessTasks(List<Integer> accessTasks) {
        this.accessTasks = accessTasks;
    }

    public Integer getResourceId() { return this.resourceId; }

    public Integer getC_low() { return this.c_low; }

    public Integer getC_high() { return this.c_high; }

    public List<Integer> getAccessTasks() { return this.accessTasks; }

    public String getIsGlobalResource() { return this.isGlobalResource; }

    public void setIsGlobalResource(String isGlobalResource) { this.isGlobalResource = isGlobalResource; }
}
