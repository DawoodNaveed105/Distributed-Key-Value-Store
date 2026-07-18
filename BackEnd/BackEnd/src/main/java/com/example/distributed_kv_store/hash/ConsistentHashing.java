package com.example.distributed_kv_store.hash;

import com.sun.source.tree.Tree;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConsistentHashing {
    private final TreeMap<Integer, String> ring;
    private final int virtualNodesPerServer;
    private final HashFunction hashFunction = new HashFunction();

    public ConsistentHashing(){
        this.ring = new TreeMap<>();
        this.virtualNodesPerServer = 150;
    }

    public ConsistentHashing(int virtualNodesPerServer){
        this.ring = new TreeMap<>();
        this.virtualNodesPerServer = virtualNodesPerServer;
    }

    public void addNode(String node){
        for(int i = 0; i < virtualNodesPerServer; i++){
//            String virtualNode = node + "#" + i;
            VirtualNode virtualNode = VirtualNode.create(node, i, hashFunction);
            ring.put(virtualNode.getHash(), node);
        }
        System.out.println("Added node: " + node + " with " + virtualNodesPerServer + " Virtual Nodes Per Server");
    }

    public void removeNode(String node){
        Iterator<Map.Entry<Integer, String>> iterator = ring.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer, String> entry = iterator.next();
            if(entry.getValue().equals(node)){
                iterator.remove();
            }
        }
        System.out.println("Removed Node: " + node);
    }

    public String getNode(String key){
        if(ring.isEmpty()){
            throw new IllegalStateException("No Nodes available in the ring. ");
        }
        int hash = hashFunction.getHash(key);
        SortedMap<Integer, String> tailmap = ring.tailMap(hash);

        Integer nodeHash = tailmap.isEmpty() ? ring.firstKey() : tailmap.firstKey();
        return ring.get(nodeHash);
    }

    public List<String> getNodes(String key, int count ){
        if(count > ring.size()){
            throw new IllegalArgumentException("Given Number of Nodes exceed the Number of Nodes in the ring. ");
        }

        Set<String> nodes = new LinkedHashSet<>();
        int hash = hashFunction.getHash(key);

        String primaryNode = getNode(key);
        nodes.add(primaryNode);

        SortedMap<Integer, String> tailMap = ring.tailMap(hash);
        Iterator<Map.Entry<Integer, String>> iterator = tailMap.isEmpty() ? ring.entrySet().iterator() : tailMap.entrySet().iterator();

        while(nodes.size() < count && iterator.hasNext()){
            Map.Entry<Integer, String> entry = iterator.next();
            nodes.add(entry.getValue());
        }

        if(nodes.size() < count){
            iterator = ring.entrySet().iterator();
            while (nodes.size() < count && iterator.hasNext()) {
                Map.Entry<Integer, String> entry = iterator.next();
                nodes.add(entry.getValue());
            }
        }
        return new ArrayList<>(nodes);
    }

    public Set<String> getAllNodes(){
        return new HashSet<>(ring.values());
    }

    public int getRingSize(){
        return ring.size();
    }

    public void printRing(){
        System.out.println("=== Consistent Hash Ring ===");
        for(Map.Entry<Integer, String> entry: ring.entrySet()){
            System.out.println(entry);
        }
        System.out.println("Total Virtual Nodes: " + ring.size());
    }
}
