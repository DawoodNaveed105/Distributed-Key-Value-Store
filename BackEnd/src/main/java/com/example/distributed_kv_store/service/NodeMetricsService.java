package com.example.distributed_kv_store.service;

import com.example.distributed_kv_store.response.ComponentHealth;
import com.example.distributed_kv_store.response.HealthResponse;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class NodeMetricsService {
    @Value("${server.port:8080}")
    private String currentPort;

    public HealthResponse generateHealthResponse(){
        HealthResponse healthResponse = new HealthResponse();
        healthResponse.setStatus("UP");
        healthResponse.setComponents(getComponentHealth());
        healthResponse.setDetails(getNodeDetails());
        healthResponse.setCurrentPort(currentPort);

        return healthResponse;
    }

    private Map<String, ComponentHealth> getComponentHealth(){
        Map<String, ComponentHealth> components = new HashMap<>();

        components.put("Memory", getMemoryComponent());
        components.put("Disk", getDiskComponent());
        components.put("CPU", getCpuComponent());

        return components;
    }

    private ComponentHealth getMemoryComponent(){
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> memDetails = new HashMap<>();
        memDetails.put("Total Memory", osBean.getTotalMemorySize());
        memDetails.put("Available Memory", osBean.getFreeMemorySize());

        double usedPercentage = ((double)(osBean.getTotalMemorySize() - osBean.getFreeMemorySize()) / osBean.getTotalMemorySize()) * 100;
        String status = usedPercentage > 90 ? "WARNING" : "UP";
        return new ComponentHealth(status, memDetails);
    }

    private ComponentHealth getDiskComponent(){
        File root = new File("C:");
        Map<String, Object> diskDetails = new HashMap<>();
        diskDetails.put("Total Space", root.getTotalSpace());
        diskDetails.put("Available Memory", root.getFreeSpace());

        double usedPercentage = ((double)(root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace()) * 100;
        String status = usedPercentage > 95 ? "DOWN" : usedPercentage > 80 ? "WARNING" : "UP";
        return new ComponentHealth(status, diskDetails);
    }

    private ComponentHealth getCpuComponent(){
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> cpuDetails = new HashMap<>();
        cpuDetails.put("CPU Usage", osBean.getProcessCpuLoad() * 100);
        cpuDetails.put("Available Processors", osBean.getAvailableProcessors());

        double cpuLoad = osBean.getProcessCpuLoad() * 100;
        String status = cpuLoad > 90 ? "WARNING" : "UP";
        return new ComponentHealth(status, cpuDetails);
    }

    private Map<String, Object> getNodeDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("Node Name", "localhost:" + currentPort);
        details.put("Timestamp", System.currentTimeMillis());
        return details;
    }
}
