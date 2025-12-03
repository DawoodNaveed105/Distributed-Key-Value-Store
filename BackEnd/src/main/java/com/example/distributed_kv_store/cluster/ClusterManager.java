package com.example.distributed_kv_store.cluster;

import com.example.distributed_kv_store.hash.ConsistentHashing;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ClusterManager {
    private final List<String> nodes;
    private final ConsistentHashing hashRing;
    private final Set<String> healthyNodes;

    @Value("${cluster.nodes}")
    private String clusterNodesConfig;

    @Value("${server.port:8080}")
    private String currentPort;

    @Value("${cluster.replication.factor}")
    private int replicationFactor;

    public ClusterManager(ConsistentHashing consistentHashing) {
        this.nodes = new CopyOnWriteArrayList<>();
        this.hashRing = consistentHashing;
        this.healthyNodes = ConcurrentHashMap.newKeySet();
    }

    public void addNode(String node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            hashRing.addNode(node);
            healthyNodes.add(node);

            System.out.println("Node added to Cluster");
        }
    }

    public void removeNode(String node) {
        if (nodes.contains(node)) {
            nodes.remove(node);
            hashRing.removeNode(node);
            healthyNodes.remove(node);

            System.out.println("Node removed to Cluster");
        }
    }

    @PostConstruct
    public void initialize(){
        System.out.println(currentPort);
        System.out.println(clusterNodesConfig);

        int port;
        try {
            port = Integer.parseInt(currentPort);
        } catch (NumberFormatException e) {
            port = 8080;
            System.out.println("Invalid port, using default: 8080");
        }

        String[] nodeArray = clusterNodesConfig.split(",");
        for(String node: nodeArray){
            nodes.add(node.trim());
            hashRing.addNode(node.trim());
        }

        String currentNode = "localhost:" + currentPort;
        healthyNodes.add(currentNode);

        System.out.println("Cluster initialized with nodes: " + nodes);
        System.out.println("Current node: " + currentNode);
//        System.out.println("Replication factor: " + replicationFactor);
    }

    public void markNodeHealthy(String node) {
        healthyNodes.add(node);

        System.out.println("Node added to Healthy Nodes");
    }

    public void markNodeUnHealthy(String node) {
        healthyNodes.remove(node);

        System.out.println("Node added to Healthy Nodes");
    }

    public String getResponsibleNode(String key){
        return hashRing.getNode(key);
    }

    public List<String> getReplicaNodes(String key){
        return hashRing.getNodes(key, replicationFactor + 1);
    }
    public List<String> getAllNodes(){
        return new ArrayList<>(nodes);
    }

    public List<String> getAllHealthyNodes(){
        return new ArrayList<>(healthyNodes);
    }

    public void printClusterStatus() {
        System.out.println("=== Cluster Status ===");
        System.out.println("Total nodes: " + nodes.size());
        System.out.println("Healthy nodes: " + healthyNodes.size());
        System.out.println("Replication factor: " + replicationFactor);
        System.out.println("Current node: " + getCurrentNode());
        System.out.println("All nodes: " + nodes);
        System.out.println("Healthy nodes: " + healthyNodes);
    }

    public String getCurrentNode(){
        String currentNode = "localhost:" + currentPort;
        return currentNode;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }
}
