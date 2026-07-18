package com.example.distributed_kv_store.storage;

import com.example.distributed_kv_store.cluster.ClusterManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ReplicationManager {
    private final RestTemplate restTemplate;
    private final ClusterManager clusterManager;

    public ReplicationManager(ClusterManager clusterManager){
        this.clusterManager = clusterManager;
        this.restTemplate = new RestTemplate();
    }

    public void replicateToReplicas(String key, String value){
        List<String> replicaNodes = clusterManager.getReplicaNodes(key);
        String currentNode = clusterManager.getCurrentNode();

        for(String node: replicaNodes){
            if(!node.equals(currentNode)){
                try{
                    String url = String.format("http://%s/data/internal/replicate?key=%s", node, key);
                    System.out.println("Replication Successfull");
                    restTemplate.put(url, value);
                }catch (Exception e){
                    System.err.println("Failed to replicate to node " + node + ": " + e.getMessage());
                }
            }
        }
    }

    public void replicateDeleteToReplicas(String key){
        List<String> replicaNodes = clusterManager.getReplicaNodes(key);
        String currentNode = clusterManager.getCurrentNode();

        for(String node: replicaNodes){
            if(!node.equals(currentNode)){
                try{
                    String url = String.format("http://%s/data/internal/replicate/%s", node, key);
                    restTemplate.delete(url);
                }catch (Exception e){
                    System.err.println("Failed to replicate delete to node " + node + ": " + e.getMessage());
                }
            }
        }
    }
    private boolean isLocalNode(String node){
        return node.equals(clusterManager.getCurrentNode());
    }

    public void replicateToNode(String node, String key, String value) {
        String url = String.format("http://%s/data/internal/replicate?key=%s", node, key);
        restTemplate.put(url, value);
    }

    public void replicateToDelete(String node, String key, String value) {
        String url = String.format("http://%s/data/internal/replicate/%s", node, key);
        restTemplate.delete(url, value);
    }

}
