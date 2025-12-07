package com.durdencorp.pswmanager.service;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.durdencorp.pswmanager.utils.LogUtils;

@Service
@SessionScope
public class SessionService {
    
    private SecretKeySpec secretKey;
    
    public boolean setMasterPassword(String masterPassword) {
        try {
            LogUtils.logApplication(LogUtils.Level.DEBUG, "=== SESSION SERVICE - Impostazione Master Password ===");
            
            // Genera la chiave AES dalla master password
            byte[] key = masterPassword.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            this.secretKey = new SecretKeySpec(key, "AES");
            
            LogUtils.logApplication(LogUtils.Level.DEBUG, "Chiave AES impostata nella sessione");
            return true;
            
        } catch (Exception e) {
            LogUtils.logApplication(LogUtils.Level.DEBUG, "ERRORE in setMasterPassword: " + e.getMessage());
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
        LogUtils.logApplication(LogUtils.Level.DEBUG, "SessionService - Chiave pulita");
    }
}