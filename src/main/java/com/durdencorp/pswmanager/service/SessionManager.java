package com.durdencorp.pswmanager.service;

import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

@Component
public class SessionManager {
    
    private static final ThreadLocal<SecretKeySpec> currentKey = new ThreadLocal<>();
    
    public void setMasterPassword(String masterPassword) {
        try {
            System.out.println("=== SESSION MANAGER - Impostazione Master Password ===");
            
            byte[] key = masterPassword.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 128 bit per AES
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            
            currentKey.set(secretKey);
            System.out.println("Chiave AES impostata nel ThreadLocal");
            
        } catch (Exception e) {
            System.out.println("ERRORE in setMasterPassword: " + e.getMessage());
            throw new RuntimeException("Errore nella creazione della chiave", e);
        }
    }
    
    public SecretKeySpec getCurrentKey() {
        SecretKeySpec key = currentKey.get();
        System.out.println("SessionManager - Chiave recuperata: " + (key != null ? "PRESENTE" : "NULL"));
        return key;
    }
    
    public boolean isMasterPasswordSet() {
        boolean isSet = currentKey.get() != null;
        System.out.println("SessionManager - Master password impostata: " + isSet);
        return isSet;
    }
    
    public void clear() {
        currentKey.remove();
        System.out.println("SessionManager - Chiave pulita");
    }
}