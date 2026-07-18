package com.example.distributed_kv_store.controller;

import com.example.distributed_kv_store.cluster.ClusterManager;
import com.example.distributed_kv_store.hash.ConsistentHashing;
import com.example.distributed_kv_store.model.NodeHealth;
import com.example.distributed_kv_store.response.HealthResponse;
import com.example.distributed_kv_store.service.NodeHealthCheckService;
import com.example.distributed_kv_store.storage.KeyValueStore;
import com.example.distributed_kv_store.storage.ReplicationManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.*;

@RestController
@RequestMapping("/data")
public class NodeController {

    private final KeyValueStore keyValueStore;
    private final ClusterManager clusterManager;
    private final ConsistentHashing consistentHashing;
    private final RestTemplate restTemplate;
    private final ReplicationManager replicationManager;
    private final NodeHealthCheckService nodeHealthCheckServie;

    public NodeController(KeyValueStore keyValueStore, ClusterManager clusterManager, ConsistentHashing consistentHashing, ReplicationManager replicationManager, NodeHealthCheckService nodeHealthCheckService) {
        this.keyValueStore = keyValueStore;
        this.clusterManager = clusterManager;
        this.consistentHashing = consistentHashing;
        this.restTemplate = new RestTemplate();
        this.replicationManager = replicationManager;
        this.nodeHealthCheckServie = nodeHealthCheckService;
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> put (@RequestParam String key, @RequestBody String value){
        Map<String, Object> response = new HashMap<>();

        try{
            String responsibleNode = clusterManager.getResponsibleNode(key);
            String currentNode = clusterManager.getCurrentNode();

            if(responsibleNode.equals(currentNode)){
                keyValueStore.put(key, value);
                replicationManager.replicateToReplicas(key, value);

                response.put("status", "success");
                response.put("message", "Key stored successfully");
                response.put("stored_locally", true);
                response.put("responsible_node", responsibleNode);
            }else{
                forwardPutToNode(responsibleNode, key, value);

                response.put("status", "success");
                response.put("message", "Key forwarded to responsible node");
                response.put("stored_locally", false);
                response.put("responsible_node", responsibleNode);
            }
            return ResponseEntity.ok(response);
        }catch (Exception e){
            response.put("status", "error");
            response.put("message", "Failed to store key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{key}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String key){
        Map<String, Object> response = new HashMap<>();
        try{
            String responsibleNode = clusterManager.getResponsibleNode(key);
            String currentNode = clusterManager.getCurrentNode();

            if(responsibleNode.equals(currentNode)){
                String value = keyValueStore.get(key);
                if (value != null) {
                    response.put("status", "success");
                    response.put("key", key);
                    response.put("value", value);
                    response.put("timestamp", keyValueStore.getTimestamp(key));
                    response.put("version", keyValueStore.getVersion(key));
                    response.put("served_from", "local");
                    return ResponseEntity.ok(response);
                }else{
                    response.put("status", "error");
                    response.put("message", "Key not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }else{
                return forwardGetToNode(responsibleNode, key);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String key){
        Map<String, Object> response = new HashMap<>();
        try{
            String responsibleNode = clusterManager.getResponsibleNode(key);
            String currentNode = clusterManager.getCurrentNode();

            if(responsibleNode.equals(currentNode)){
                if(keyValueStore.containsKey(key)){
                    keyValueStore.delete(key);
                    replicationManager.replicateDeleteToReplicas(key);

                    response.put("status", "success");
                    response.put("message", "Key deleted successfully");
                    return ResponseEntity.ok(response);
                }else{
                    response.put("status", "error");
                    response.put("message", "Key not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }else{
                forwardDeleteToNode(responsibleNode, key);

                response.put("status", "success");
                response.put("message", "Delete forwarded to responsible node");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/internal/replicate")
    public ResponseEntity<String> replicatePut(@RequestParam String key, @RequestBody String value) {
        keyValueStore.put(key, value);
        return ResponseEntity.ok("Replication successful");
    }

    @DeleteMapping("/internal/replicate/{key}")
    public ResponseEntity<String> replicateDelete(@PathVariable String key) {
        keyValueStore.delete(key);
        return ResponseEntity.ok("Delete replication successful");
    }

    @GetMapping("/cluster/status")
    public ResponseEntity<Map<String, Object>> getClusterStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("current_node", clusterManager.getCurrentNode());
        status.put("all_nodes", clusterManager.getAllNodes());
        status.put("healthy_nodes", clusterManager.getAllHealthyNodes());
        status.put("ring_size", consistentHashing.getRingSize());
        status.put("local_store_size", keyValueStore.getSize());
        status.put("replication_factor", clusterManager.getReplicationFactor());

        return ResponseEntity.ok(status);
    }

    @PostMapping("/cluster/add/node")
    public ResponseEntity<String> addNode(@RequestParam String node) {
        clusterManager.addNode(node);
        return ResponseEntity.ok("Node added: " + node);
    }

    @GetMapping("/local/keys")
    public ResponseEntity<Map<String, Object>> getLocalKeys() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get all keys from local store
            Map<String, String> allEntries = keyValueStore.getAllEntries();
            List<String> keys = new ArrayList<>(allEntries.keySet());

            // Sort keys alphabetically
            Collections.sort(keys);

            // Prepare response
            response.put("status", "success");
            response.put("current_node", clusterManager.getCurrentNode());
            response.put("total_keys", keys.size());
            response.put("keys", keys);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve local keys: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    private void forwardPutToNode(String node, String key, String value){
        String url = String.format("http://%s/data?key=%s", node, key);
        restTemplate.put(url,value);
    }

    private ResponseEntity<Map<String, Object>> forwardGetToNode(String node, String key){
        String url = String.format("http://%s/data/%s", node, key);
        try{
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = response.getBody();
            responseBody.put("served_from", "remote");
            responseBody.put("responsible_node", node);
            return new ResponseEntity<>(responseBody, response.getStatusCode());
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to retrieve from node " + node + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private void forwardDeleteToNode(String node, String key) {
        String url = String.format("http://%s/data/%s", node, key);
        restTemplate.delete(url);
    }

//    private ResponseEntity<Map<String, HealthResponse>> getHealthyNodes(){
//        try {
//            Map<String, NodeHealth> healthMap = nodeHealthCheckServie.getAllHealhtyNodes();
//
//        }catch(){
//
//        }
//    }

}
