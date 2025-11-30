package com.durdencorp.pswmanager.service;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class SessionService {
    
    private SecretKeySpec secretKey;
    
    public boolean setMasterPassword(String masterPassword) {
        try {
            System.out.println("=== SESSION SERVICE - Impostazione Master Password ===");
            
            // Genera la chiave AES dalla master password
            byte[] key = masterPassword.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            this.secretKey = new SecretKeySpec(key, "AES");
            
            System.out.println("Chiave AES impostata nella sessione");
            return true;
            
        } catch (Exception e) {
            System.out.println("ERRORE in setMasterPassword: " + e.getMessage());
            this.secretKey = null;
            return false;
        }
    }
    
    public SecretKeySpec getCurrentKey() {
        return secretKey;
    }
    
    public boolean isMasterPasswordSet() {
        return secretKey != null;
    }
    
    public void clear() {
        this.secretKey = null;
        System.out.println("SessionService - Chiave pulita");
    }
}