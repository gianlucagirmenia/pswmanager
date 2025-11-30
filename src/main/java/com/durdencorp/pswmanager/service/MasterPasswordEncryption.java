package com.durdencorp.pswmanager.service;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MasterPasswordEncryption {
    
	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private AppConfigService appConfigService;
    
    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16]; // 16 byte per AES
        random.nextBytes(iv);
        return iv;
    }
    
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
            System.out.println("Dati da cifrare: '" + data + "' (lunghezza: " + (data != null ? data.length() : 0) + ")");
            
            if (data == null) return null;
            if (!isMasterPasswordSet()) {
                throw new IllegalStateException("Master password non impostata");
            }
            
            SecretKeySpec secretKey = sessionService.getCurrentKey();
            if (secretKey == null) {
                throw new IllegalStateException("Chiave di cifratura non disponibile");
            }
            
            // Genera IV casuale
            byte[] iv = generateIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            
            // Combina IV + dati cifrati per la decifratura
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            
            String result = Base64.getEncoder().encodeToString(combined);
            
            System.out.println("Cifratura COMPLETATA: '" + data + "' -> " + result.substring(0, Math.min(20, result.length())) + "...");
            return result;
            
        } catch (Exception e) {
            System.out.println("ERRORE in encrypt: " + e.getMessage());
            e.printStackTrace();
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
            
            // VERIFICA SE È BASE64 VALIDO
            if (!isValidBase64(encryptedData)) {
                System.out.println("ATTENZIONE: Dati non in formato Base64 valido, restituisco in chiaro");
                return encryptedData; // Restituisce i dati così come sono
            }
            
            SecretKeySpec secretKey = sessionService.getCurrentKey();
            if (secretKey == null) {
                throw new IllegalStateException("Chiave di cifratura non disponibile");
            }
            
            // Decodifica e separa IV dai dati cifrati
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            // Verifica lunghezza minima
            if (combined.length < 16) {
                System.out.println("ATTENZIONE: Dati cifrati troppo corti, restituisco in chiaro");
                return encryptedData;
            }
            
            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);
            
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String result = new String(decryptedBytes, "UTF-8");
            
            System.out.println("Decifratura COMPLETATA: -> '" + result + "'");
            return result;
            
        } catch (Exception e) {
            System.out.println("ERRORE in decrypt: " + e.getMessage());
            System.out.println("Dati problematici: " + (encryptedData != null ? encryptedData : "null"));
            
            // In caso di errore, restituisci i dati originali
            System.out.println("ATTENZIONE: Restituisco dati in chiaro a causa di errore di decifratura");
            return encryptedData;
        }
    }

    // Metodo per verificare se una stringa è Base64 valido
    private boolean isValidBase64(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        
        // I caratteri Base64 validi sono: A-Z, a-z, 0-9, +, /, = (padding)
        // Controlla se ci sono caratteri non Base64
        String base64Pattern = "^[A-Za-z0-9+/]*={0,2}$";
        if (!str.matches(base64Pattern)) {
            return false;
        }
        
        // Verifica lunghezza (deve essere multiplo di 4 per Base64)
        if (str.length() % 4 != 0) {
            return false;
        }
        
        return true;
    }
    
    public void clear() {
        sessionService.clear();
        System.out.println("MasterPasswordEncryption - Sessione pulita");
    }
}