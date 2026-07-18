package com.example.distributed_kv_store.storage;

import com.example.distributed_kv_store.model.KeyValue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KeyValueStore {

    private final KeyValue keyValue;

    public KeyValueStore() {
        this.keyValue = new KeyValue();
    }

    public void put(String key, String value){
        keyValue.setStore(key, value);
        keyValue.setTimeStamp(key);
        keyValue.setVersions(key);
    }

    public String get(String key){
        return keyValue.get(key);
    }

    public int getSize(){
        return keyValue.getStore().size();
    }

    public boolean containsKey(String key){
        return keyValue.containsKey(key);
    }

    public void delete(String key){
        keyValue.getStore().remove(key);
        keyValue.getTimeStamp().remove(key);
        keyValue.getVersions().remove(key);
    }

    public Map<String, Long> getTimeStamps() {
        return keyValue.getTimeStamp();
    }

    public Map<String, Integer> getVersions() {
        return keyValue.getVersions();
    }

    public Map<String, String> getAllEntries(){
        return new ConcurrentHashMap<>(keyValue.getStore());
    }

    public Long getTimestamp(String key) {
        return getTimeStamps().get(key);
    }

    public Integer getVersion(String key) {
        return getVersions().get(key);
    }

    public void clear(){
        keyValue.clearStore();
        keyValue.clearTimeStamp();
        keyValue.clearVersions();
    }

    public void putAll(Map<String, String> entries){
        keyValue.putAll(entries);
    }
}

