package com.example.distributed_kv_store.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class ComponentHealth {
    @JsonProperty("status")
    private String status;

    @JsonProperty("details")
    private Map<String, Object> details;

    public ComponentHealth(){
        this.details = new HashMap<>();
    }
    public ComponentHealth(String status) {
        this();
        this.status = status;
    }

    public ComponentHealth(String status, Map<String, Object> details) {
        this.status = status;
        this.details = details != null ? details : new HashMap<>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public void addDetails(String key, Object value){
        this.details.put(key, value);
    }

    public boolean isHealthy(){
        return "UP".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "ComponentHealth{" +
                "status='" + status + '\'' +
                ", details=" + details +
                '}';
    }
}
