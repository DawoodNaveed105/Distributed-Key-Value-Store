package com.example.distributed_kv_store.hash;

import java.util.Objects;
import java.util.TreeMap;

public class VirtualNode implements Comparable<VirtualNode>{
    private final int hash;
    private final String physicalNodeId;
    private final int replicaNumber;
    public VirtualNode(int hash, String physicalNodeId, int replicaNumber) {
        this.hash = hash;
        this.physicalNodeId = physicalNodeId;
        this.replicaNumber = replicaNumber;
    }

    public int getHash() {
        return hash;
    }

    public String getPhysicalNodeId() {
        return physicalNodeId;
    }

    public int getReplicaNumber() {
        return replicaNumber;
    }

    public String getVirtualNodeId(){
        return physicalNodeId + "#" + replicaNumber;
    }

    public static VirtualNode create(String physicalNodeId, int replicaNumber, HashFunction hashFunction) {
        String virtualNodeKey = physicalNodeId + "#" + replicaNumber;
        int hash = hashFunction.getHash(virtualNodeKey);
        return new VirtualNode(hash, physicalNodeId, replicaNumber);
    }

    @Override
    public int compareTo(VirtualNode other){
        return Integer.compare(this.hash, other.hash);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VirtualNode that = (VirtualNode) o;
        return hash == that.hash &&
                replicaNumber == that.replicaNumber &&
                physicalNodeId.equals(that.physicalNodeId);
    }

    @Override
    public String toString() {
        return String.format("VirtualNode{hash=%d, physical='%s', replica=%d}",
                hash, physicalNodeId, replicaNumber);
    }
}
