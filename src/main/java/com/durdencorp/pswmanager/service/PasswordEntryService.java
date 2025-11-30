package com.durdencorp.pswmanager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private CategoryService categoryService;
    
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
        
        String originalPassword = entry.getEncryptedPassword();
        
        // Crea una NUOVA entity per evitare problemi di caching
        PasswordEntry entryToSave = new PasswordEntry();
        entryToSave.setTitle(entry.getTitle());
        entryToSave.setUsername(entry.getUsername());
        entryToSave.setUrl(entry.getUrl());
        entryToSave.setNotes(entry.getNotes());
        entryToSave.setCategory(entry.getCategory());
        
        if (entry.getId() != null) {
            entryToSave.setId(entry.getId());
        }
        
        // SEMPRE CIFRA la password (non fare verifiche rischiose)
        if (originalPassword != null && !originalPassword.isEmpty()) {
            try {
                System.out.println("Cifrando la password prima del salvataggio...");
                String encryptedPassword = encryptionUtil.encrypt(originalPassword);
                entryToSave.setEncryptedPassword(encryptedPassword);
                System.out.println("Password cifrata con successo");
                
            } catch (Exception e) {
                System.out.println("ERRORE CRITICO durante la cifratura: " + e.getMessage());
                throw new RuntimeException("Impossibile cifrare la password: " + e.getMessage(), e);
            }
        } else {
            entryToSave.setEncryptedPassword(originalPassword);
        }
        
        PasswordEntry saved = repository.save(entryToSave);
        repository.flush();
        
        System.out.println("Record salvato nel DB con ID: " + saved.getId());
        System.out.println("=== SERVICE SAVE - FINE ===");
        
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
    
    public void clearSession() {
        encryptionUtil.clear();
    }
    
    public List<PasswordEntry> findByCategory(String category) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findByCategory(category);
        return entries.stream().map(entry -> {
            if (entry.getEncryptedPassword() != null && !entry.getEncryptedPassword().isEmpty()) {
                try {
                    String decryptedPassword = encryptionUtil.decrypt(entry.getEncryptedPassword());
                    entry.setEncryptedPassword(decryptedPassword);
                } catch (Exception e) {
                    System.out.println("ERRORE decifratura per '" + entry.getTitle() + "': " + e.getMessage());
                }
            }
            return entry;
        }).toList();
    }
    
    public List<String> getAllCategories() {
        return categoryService.getAllCategories();
    }
    
    public Map<String, Long> getCategoryStats() {
        return categoryService.getCategoryStats();
    }
    
    public List<PasswordEntry> findByCategoryAndSearch(String category, String query) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findByCategoryAndSearchQuery(category, query);
        return entries.stream().map(entry -> {
            if (entry.getEncryptedPassword() != null && !entry.getEncryptedPassword().isEmpty()) {
                try {
                    String decryptedPassword = encryptionUtil.decrypt(entry.getEncryptedPassword());
                    entry.setEncryptedPassword(decryptedPassword);
                } catch (Exception e) {
                    System.out.println("ERRORE decifratura per '" + entry.getTitle() + "': " + e.getMessage());
                }
            }
            return entry;
        }).toList();
    }
    
    @Transactional
    public void sanitizeAndReencryptAll() {
        System.out.println("=== SANITIZZAZIONE E RICIFRATURA DI TUTTI I RECORD ===");
        
        List<PasswordEntry> allEntries = repository.findAll();
        int fixedCount = 0;
        int alreadyEncryptedCount = 0;
        
        for (PasswordEntry entry : allEntries) {
            String currentPassword = entry.getEncryptedPassword();
            
            if (currentPassword != null && !currentPassword.isEmpty()) {
                try {
                    // Verifica se è cifrata correttamente
                    String testDecrypt = encryptionUtil.decrypt(currentPassword);
                    // Se non lancia eccezione, è già cifrata correttamente
                    alreadyEncryptedCount++;
                    System.out.println("✓ " + entry.getTitle() + " - già cifrata correttamente");
                    
                } catch (Exception e) {
                    // Se fallisce, la password è in chiaro - ricifrala
                    System.out.println("✗ " + entry.getTitle() + " - password in chiaro, ricifro...");
                    
                    String encryptedPassword = encryptionUtil.encrypt(currentPassword);
                    entry.setEncryptedPassword(encryptedPassword);
                    repository.save(entry);
                    fixedCount++;
                }
            }
        }
        
        repository.flush();
        System.out.println("Sanitizzazione completata: " + fixedCount + " record ricifrati, " + alreadyEncryptedCount + " già corretti");
    }
    
}