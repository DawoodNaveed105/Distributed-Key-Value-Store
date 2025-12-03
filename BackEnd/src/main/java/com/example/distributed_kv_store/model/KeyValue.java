package com.example.distributed_kv_store.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyValue {
    private Map<String, String> store;
    private Map<String, Long> timeStamp;
    private Map<String, Integer> versions;

    public void setVersions(String key) {
        versions.put(key, versions.getOrDefault(key, 0) + 1);
    }

    public void setTimeStamp(String key) {
        timeStamp.put(key, System.currentTimeMillis());
    }

    public void setStore(String key, String value) {
        store.put(key, value);
    }

    public KeyValue() {
        this.store = new ConcurrentHashMap<>();
        this.timeStamp = new ConcurrentHashMap<>();
        this.versions = new ConcurrentHashMap<>();
    }

    public Map<String, String> getStore() {
        return store;
    }

    public Map<String, Long> getTimeStamp() {
        return timeStamp;
    }

    public Map<String, Integer> getVersions() {
        return versions;
    }

    public String get(String key){
        return store.get(key);
    }

    public boolean containsKey(String key){
        return store.containsKey(key);
    }
    public void putAll(Map<String, String> entries){
        store.putAll(entries);
        long currentTime = System.currentTimeMillis();
        entries.keySet().forEach((key) -> {
            setTimeStamp(key);
            setVersions(key);
        });

    }
    public void clearStore(){
        store.clear();
    }

    public void clearTimeStamp(){
        timeStamp.clear();
    }

    public void clearVersions(){
        versions.clear();
    }
}
