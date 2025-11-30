package com.durdencorp.pswmanager.service;

import com.durdencorp.pswmanager.model.AppConfig;
import com.durdencorp.pswmanager.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class AppConfigService {
    
    private static final String MASTER_PASSWORD_HASH_KEY = "master_password_hash";
    
    @Autowired
    private AppConfigRepository appConfigRepository;
    
    // Salva l'hash della master password (NON la password in chiaro!)
    public void saveMasterPasswordHash(String masterPassword) {
        try {
            String hash = hashPassword(masterPassword);
            AppConfig config = new AppConfig(MASTER_PASSWORD_HASH_KEY, hash);
            appConfigRepository.save(config);
            System.out.println("Hash master password salvato nel database");
        } catch (Exception e) {
            throw new RuntimeException("Errore nel salvataggio dell'hash della master password", e);
        }
    }
    
    // Verifica se la master password è corretta
    public boolean verifyMasterPassword(String attemptedPassword) {
        try {
            Optional<AppConfig> config = appConfigRepository.findByConfigKey(MASTER_PASSWORD_HASH_KEY);
            if (config.isEmpty()) {
                System.out.println("Nessuna master password configurata - primo accesso");
                return true; // Primo accesso, qualsiasi password va bene
            }
            
            String storedHash = config.get().getConfigValue();
            String attemptedHash = hashPassword(attemptedPassword);
            
            boolean isValid = storedHash.equals(attemptedHash);
            System.out.println("Verifica master password: " + (isValid ? "VALIDA" : "NON VALIDA"));
            return isValid;
            
        } catch (Exception e) {
            System.out.println("Errore nella verifica della master password: " + e.getMessage());
            return false;
        }
    }
    
    // Verifica se è il primo accesso (nessuna master password configurata)
    public boolean isFirstAccess() {
        Optional<AppConfig> config = appConfigRepository.findByConfigKey(MASTER_PASSWORD_HASH_KEY);
        boolean firstAccess = config.isEmpty();
        System.out.println("Primo accesso: " + firstAccess);
        return firstAccess;
    }
    
    // Hash della password usando SHA-256
    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }
}