package com.example.distributed_kv_store.response;

import com.example.distributed_kv_store.cluster.ClusterManager;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import com.sun.management.OperatingSystemMXBean;
import org.apache.catalina.Cluster;
import org.springframework.beans.factory.annotation.Value;

public class HealthResponse {

//    @Value("${server.port:8080}")
    private String currentPort;

    @JsonProperty("status")
    private String status;

    @JsonProperty("components")
    private Map<String, ComponentHealth> components;

    @JsonProperty("details")
    private Map<String, Object> details;

    public HealthResponse(){
        this.components = new HashMap<>();
        this.details = new HashMap<>();
    }

    public HealthResponse(String status) {
        this();
        this.status = status;
    }

    public String getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(String currentPort) {
        this.currentPort = currentPort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, ComponentHealth> getComponents() {
        return components;
    }

    public void setComponents(Map<String, ComponentHealth> components) {
        this.components = components;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}

//    public static class ComponentHealth{
//
//        @JsonProperty("status")
//        private String status;
//
//        @JsonProperty("details")
//        private Map<String, Object> details;
//
//        public ComponentHealth(){
//            this.details = new HashMap<>();
//        }
//        public ComponentHealth(String status) {
//            this();
//            this.status = status;
//        }
//
//        public ComponentHealth(String status, Map<String, Object> details) {
//            this.status = status;
//            this.details = details != null ? details : new HashMap<>();
//        }
//
//        public String getStatus() {
//            return status;
//        }
//
//        public void setStatus(String status) {
//            this.status = status;
//        }
//
//        public Map<String, Object> getDetails() {
//            return details;
//        }
//
//        public void setDetails(Map<String, Object> details) {
//            this.details = details;
//        }
//
//        public void addDetails(String key, Object value){
//            this.details.put(key, value);
//        }
//
//        public boolean isHealthy(){
//            return "UP".equalsIgnoreCase(status);
//        }
//
//        @Override
//        public String toString() {
//            return "ComponentHealth{" +
//                    "status='" + status + '\'' +
//                    ", details=" + details +
//                    '}';
//        }
//    }
