package com.example.serveside.request;

public class TestConfiguration {
    private Integer testNum;

    private ConfigurationInformation configurationInformation;

    public TestConfiguration(Integer _testNum, ConfigurationInformation _configurationInformation) {
        testNum = _testNum;
        configurationInformation = _configurationInformation;
    }

    public Integer getTestNum() { return this.testNum; }

    public ConfigurationInformation getConfigurationInformation() { return this.configurationInformation; }

    public void setTestNum(Integer _testNum) { this.testNum = _testNum; }

    public void setConfigurationInformation(ConfigurationInformation _configurationInformation) { this.configurationInformation = _configurationInformation; }
}
