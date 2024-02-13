package com.example.serveside.response;

public class SchedulabilityTestResult
{
    private Integer msrpSchedulabilityNum;

    private Integer mrspSchedulabilityNum;

    private Integer pwlpSchedulabilityNum;

    private Integer dynamicSchedulabilityNum;

    private Integer totalNum;

    public SchedulabilityTestResult(Integer _msrpSchedulabilityNum, Integer _mrspSchedulabilityNum, Integer _pwlpSchedulabilityNum, Integer _dynamicSchedulabilityNum, Integer _totalNum)
    {
        this.msrpSchedulabilityNum = _msrpSchedulabilityNum;
        this.mrspSchedulabilityNum = _mrspSchedulabilityNum;
        this.pwlpSchedulabilityNum = _pwlpSchedulabilityNum;
        this.dynamicSchedulabilityNum = _dynamicSchedulabilityNum;
        this.totalNum = _totalNum;
    }

    public Integer getMsrpSchedulabilityNum() { return this.msrpSchedulabilityNum; }

    public Integer getMrspSchedulabilityNum() { return this.mrspSchedulabilityNum; }

    public Integer getPwlpSchedulabilityNum() { return this.pwlpSchedulabilityNum; }

    public Integer getDynamicSchedulabilityNum() { return this.dynamicSchedulabilityNum; }

    public Integer getTotalNum() { return this.totalNum; }

    public void setMsrpSchedulabilityNum(Integer _msrpSchedulabilityNum) { this.msrpSchedulabilityNum = _msrpSchedulabilityNum; }

    public void setMrspSchedulabilityNum(Integer _mrspSchedulabilityNum) { this.mrspSchedulabilityNum = _mrspSchedulabilityNum; }

    public void setPwlpSchedulabilityNum(Integer _pwlpSchedulabilityNum) { this.pwlpSchedulabilityNum = _pwlpSchedulabilityNum; }

    public void setDynamicSchedulabilityNum(Integer _dynamicSchedulabilityNum) { this.dynamicSchedulabilityNum = _dynamicSchedulabilityNum; }

    public void setTotalNum(Integer _totalNum) { this.totalNum = _totalNum; }
}
