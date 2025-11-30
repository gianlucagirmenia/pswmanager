package com.durdencorp.pswmanager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.repository.PasswordEntryRepository;

@Service
@Transactional
public class PasswordEntryService {
    
    @Autowired
    private PasswordEntryRepository repository;
    
    @Autowired
    private MasterPasswordEncryption encryptionUtil;
    
    @Autowired
    private AppConfigService appConfigService;
    
    public boolean setAndVerifyMasterPassword(String masterPassword) {
        return encryptionUtil.setAndVerifyMasterPassword(masterPassword);
    }
    
    public boolean isFirstAccess() {
        return appConfigService.isFirstAccess();
    }
    
    public boolean isMasterPasswordSet() {
        return encryptionUtil.isMasterPasswordSet();
    }
    
    public PasswordEntry save(PasswordEntry entry) {
        System.out.println("=== SERVICE SAVE - INIZIO ===");
        System.out.println("Master password impostata: " + isMasterPasswordSet());
        System.out.println("Password originale: " + entry.getEncryptedPassword());
        
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        // Salva una copia della password originale
        String originalPassword = entry.getEncryptedPassword();
        
        // CIFRA la password prima di salvarla
        if (originalPassword != null && !originalPassword.isEmpty()) {
            try {
                System.out.println("Prima di cifrare...");
                String encryptedPassword = encryptionUtil.encrypt(originalPassword);
                System.out.println("Password cifrata: " + encryptedPassword);
                
                // Crea una NUOVA entity per evitare problemi di caching
                PasswordEntry entryToSave = new PasswordEntry();
                entryToSave.setTitle(entry.getTitle());
                entryToSave.setUsername(entry.getUsername());
                entryToSave.setEncryptedPassword(encryptedPassword); // Usa la password cifrata
                entryToSave.setUrl(entry.getUrl());
                entryToSave.setNotes(entry.getNotes());
                entryToSave.setCategory(entry.getCategory());
                
                if (entry.getId() != null) {
                    entryToSave.setId(entry.getId());
                }
                
                PasswordEntry saved = repository.save(entryToSave);
                repository.flush(); // Forza il salvataggio immediato
                
                System.out.println("Record salvato nel DB con ID: " + saved.getId());
                System.out.println("Password nel DB: " + saved.getEncryptedPassword());
                System.out.println("=== SERVICE SAVE - FINE ===");
                
                return saved;
                
            } catch (Exception e) {
                System.out.println("ERRORE durante la cifratura: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        
        // Se la password è nulla, salva normalmente
        PasswordEntry saved = repository.save(entry);
        repository.flush();
        return saved;
    }
    
    public Optional<PasswordEntry> findById(Long id) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        Optional<PasswordEntry> entry = repository.findById(id);
        if (entry.isPresent()) {
            // Decifra la password quando leggi
            PasswordEntry decryptedEntry = entry.get();
            if (decryptedEntry.getEncryptedPassword() != null && !decryptedEntry.getEncryptedPassword().isEmpty()) {
                String decryptedPassword = encryptionUtil.decrypt(decryptedEntry.getEncryptedPassword());
                decryptedEntry.setEncryptedPassword(decryptedPassword);
            }
            return Optional.of(decryptedEntry);
        }
        return entry;
    }
    
    @Transactional(readOnly = true)
    public List<PasswordEntry> findAll() {
        System.out.println("=== SERVICE FIND ALL (READ ONLY) ===");
        
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findAll();
        System.out.println("Record trovati nel DB: " + entries.size());
        
        // Crea una lista per le entity di visualizzazione
        List<PasswordEntry> viewEntries = new ArrayList<>();
        
        for (PasswordEntry original : entries) {
            // Crea una copia per la visualizzazione
            PasswordEntry viewEntry = new PasswordEntry();
            viewEntry.setId(original.getId());
            viewEntry.setTitle(original.getTitle());
            viewEntry.setUsername(original.getUsername());
            viewEntry.setUrl(original.getUrl());
            viewEntry.setNotes(original.getNotes());
            viewEntry.setCreatedAt(original.getCreatedAt());
            viewEntry.setUpdatedAt(original.getUpdatedAt());
            viewEntry.setCategory(original.getCategory());
            
            // Decifra la password SOLO per la visualizzazione
            if (original.getEncryptedPassword() != null && !original.getEncryptedPassword().isEmpty()) {
                try {
                    String decryptedPassword = encryptionUtil.decrypt(original.getEncryptedPassword());
                    viewEntry.setEncryptedPassword(decryptedPassword);
                    System.out.println("Password decifrata per visualizzazione: " + original.getTitle());
                } catch (Exception e) {
                    System.out.println("ERRORE decifratura per '" + original.getTitle() + "': " + e.getMessage());
                    viewEntry.setEncryptedPassword("*** ERRORE ***");
                }
            } else {
                viewEntry.setEncryptedPassword(original.getEncryptedPassword());
            }
            
            viewEntries.add(viewEntry);
        }
        
        return viewEntries;
    }
    
    public String testEncryption(String data) {
        return encryptionUtil.encrypt(data);
    }
    
    public String testDecryption(String encryptedData) {
        return encryptionUtil.decrypt(encryptedData);
    }
    
    public void deleteById(Long id) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        repository.deleteById(id);
    }
    
    // Altri metodi (search, findByCategory, etc.) devono essere modificati similmente
    public List<PasswordEntry> searchByTitle(String title) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findByTitleContainingIgnoreCase(title);
        return entries.stream().map(entry -> {
            if (entry.getEncryptedPassword() != null && !entry.getEncryptedPassword().isEmpty()) {
                String decryptedPassword = encryptionUtil.decrypt(entry.getEncryptedPassword());
                entry.setEncryptedPassword(decryptedPassword);
            }
            return entry;
        }).toList();
    }
    
    public List<PasswordEntry> findByCategory(String category) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findByCategory(category);
        return entries.stream().map(entry -> {
            if (entry.getEncryptedPassword() != null && !entry.getEncryptedPassword().isEmpty()) {
                String decryptedPassword = encryptionUtil.decrypt(entry.getEncryptedPassword());
                entry.setEncryptedPassword(decryptedPassword);
            }
            return entry;
        }).toList();
    }
    
    public void clearSession() {
        encryptionUtil.clear();
    }
    
}