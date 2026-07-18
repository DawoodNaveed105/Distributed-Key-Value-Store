package com.example.distributed_kv_store.service;

import com.example.distributed_kv_store.cluster.ClusterManager;
import com.example.distributed_kv_store.model.NodeHealth;
import com.example.distributed_kv_store.response.HealthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeHealthCheckService {
    private final Map<String, NodeHealth> nodeHealthMap = new ConcurrentHashMap<>();
    private final ClusterManager clusterManager;
    private final RestTemplate restTemplate;

    public NodeHealthCheckService(RestTemplate restTemplate, ClusterManager clusterManager) {
        this.restTemplate = restTemplate;
        this.clusterManager = clusterManager;
    }

    @Scheduled(fixedRate = 10000)
    public void checkAllNodes(){

        for(String node: clusterManager.getAllNodes()){
            checkNodeHealth(node);
        }
    }

    public Map<String, NodeHealth> getAllHealhtyNodes(){
        Map<String, NodeHealth> healthyNodes = new ConcurrentHashMap<>();
        for(String node: clusterManager.getAllNodes()){
            if(isNodeHealthy(node))
                healthyNodes.put(node, nodeHealthMap.get(node));
        }
        return healthyNodes;
    }

    public void checkNodeHealth(String node){
        long startTime = System.currentTimeMillis();

        String url = String.format("http:%s/actuator/health", node);
        ResponseEntity<HealthResponse> response = restTemplate.getForEntity(url, HealthResponse.class);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        String status = response.getBody().getStatus();

        NodeHealth nodeHealth = new NodeHealth(
                url,
                status,
                responseTime,
                startTime,
                response.getBody().getDetails()
        );

        nodeHealthMap.put(node, nodeHealth);
    }

    public boolean isNodeHealthy(String node){
        NodeHealth nodeHealth = nodeHealthMap.get(node);
        return nodeHealth != null && nodeHealth.isHealthy();
    }

}
