package com.durdencorp.pswmanager.service;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MasterPasswordEncryption {
    
    private static final String ALGORITHM = "AES";
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private AppConfigService appConfigService;
    
    public boolean setAndVerifyMasterPassword(String masterPassword) {
        try {
            System.out.println("=== VERIFICA MASTER PASSWORD ===");
            
            // 1. Verifica che la password sia corretta
            boolean isValid = appConfigService.verifyMasterPassword(masterPassword);
            if (!isValid) {
                System.out.println("Master password NON valida");
                return false;
            }
            
            // 2. Se è il primo accesso, salva l'hash della password
            if (appConfigService.isFirstAccess()) {
                appConfigService.saveMasterPasswordHash(masterPassword);
                System.out.println("Primo accesso - Hash master password salvato");
            }
            
            // 3. Imposta la chiave di cifratura nella sessione
            boolean success = sessionService.setMasterPassword(masterPassword);
            if (!success) {
                System.out.println("Impossibile impostare la chiave di cifratura");
                return false;
            }
            
            System.out.println("Master password verificata e impostata con successo");
            return true;
            
        } catch (Exception e) {
            System.out.println("ERRORE in setAndVerifyMasterPassword: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isMasterPasswordSet() {
        return sessionService.isMasterPasswordSet();
    }
    
    public String encrypt(String data) {
        try {
            System.out.println("=== ENCRYPT ===");
            System.out.println("Dati da cifrare: " + data);
            
            if (data == null) return null;
            if (!isMasterPasswordSet()) {
                throw new IllegalStateException("Master password non impostata");
            }
            
            SecretKeySpec secretKey = sessionService.getCurrentKey();
            if (secretKey == null) {
                throw new IllegalStateException("Chiave di cifratura non disponibile");
            }
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            String result = Base64.getEncoder().encodeToString(encryptedBytes);
            
            System.out.println("Cifratura COMPLETATA: " + data + " -> " + result);
            return result;
            
        } catch (Exception e) {
            System.out.println("ERRORE in encrypt: " + e.getMessage());
            throw new RuntimeException("Errore nella cifratura: " + e.getMessage(), e);
        }
    }
    
    public String decrypt(String encryptedData) {
        try {
            System.out.println("=== DECRYPT ===");
            System.out.println("Dati da decifrare: " + (encryptedData != null ? encryptedData.substring(0, Math.min(20, encryptedData.length())) + "..." : "null"));
            
            if (encryptedData == null) return null;
            if (!isMasterPasswordSet()) {
                throw new IllegalStateException("Master password non impostata");
            }
            
            SecretKeySpec secretKey = sessionService.getCurrentKey();
            if (secretKey == null) {
                throw new IllegalStateException("Chiave di cifratura non disponibile");
            }
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            String result = new String(decryptedBytes, "UTF-8");
            
            System.out.println("Decifratura COMPLETATA: -> " + result);
            return result;
            
        } catch (Exception e) {
            System.out.println("ERRORE in decrypt: " + e.getMessage());
            throw new RuntimeException("Errore nella decifratura: " + e.getMessage(), e);
        }
    }
    
    public void clear() {
        sessionService.clear();
        System.out.println("MasterPasswordEncryption - Sessione pulita");
    }
}