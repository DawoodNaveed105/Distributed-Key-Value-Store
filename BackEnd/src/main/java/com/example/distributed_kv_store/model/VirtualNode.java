package com.example.distributed_kv_store.model;

import com.example.distributed_kv_store.hash.HashFunction;

public class VirtualNode{
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
    public String toString() {
        return String.format("VirtualNode{hash=%d, physical='%s', replica=%d}",
                hash, physicalNodeId, replicaNumber);
    }
}
