package com.example.distributed_kv_store.model;

import java.time.LocalDateTime;
import java.util.Map;

public class NodeHealth {
    private String nodeUrl;
    private String status;
    private long responseTime;
    private long lastChechked;
    private Map<String, Object> healthDetails;
    private String errorMessage;

    public NodeHealth(String nodeUrl, String status, long responseTime, long lastChechked, Map<String, Object> healthDetails) {
        this.nodeUrl = nodeUrl;
        this.status = status;
        this.responseTime = responseTime;
        this.lastChechked = lastChechked;
        this.healthDetails = healthDetails;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getLastChechked() {
        return lastChechked;
    }

    public void setLastChechked(long lastChechked) {
        this.lastChechked = lastChechked;
    }

    public Object getHealthDetails() {
        return healthDetails;
    }

    public void setHealthDetails(Map<String, Object> healthDetails) {
        this.healthDetails = healthDetails;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isHealthy(){
        return "UP".equals(status);
    }

}
