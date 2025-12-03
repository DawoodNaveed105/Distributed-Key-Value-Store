package com.example.distributed_kv_store.hash;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HashFunction {
        private final MessageDigest messageDigest;

        public HashFunction(){
            try {
                this.messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 Algorithm not available");
            }
        }

        public int getHash(String key){
            byte[] digest = messageDigest.digest(key.getBytes());
            return ((digest[3] & 0xFF) << 24) |
                    ((digest[2] & 0xFF) << 16) |
                    ((digest[1] & 0xFF) << 8) |
                    (digest[0] & 0xFF);
        }
}
