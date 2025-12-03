package com.durdencorp.pswmanager.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.durdencorp.pswmanager.dto.PasswordEntryDTO;
import com.durdencorp.pswmanager.dto.PasswordEntryForm;
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
    
    public Long save(PasswordEntryForm form) {
        PasswordEntry entity;
        
        if (form.getId() != null) {
            // MODIFICA: carica l'entity esistente
            entity = repository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Record non trovato"));
        } else {
            // NUOVO: crea nuova entity
            entity = new PasswordEntry();
        }
        
        // Aggiorna campi base
        entity.setTitle(form.getTitle());
        entity.setUsername(form.getUsername());
        entity.setUrl(form.getUrl());
        entity.setNotes(form.getNotes());
        entity.setCategory(form.getCategory());
        
        // CIFRA la password dal form (sempre in chiaro qui)
        if (form.getPlainPassword() != null && !form.getPlainPassword().isEmpty()) {
            String encrypted = encryptionUtil.encrypt(form.getPlainPassword());
            entity.setEncryptedPassword(encrypted);
        } else if (form.getId() == null) {
            // Nuovo record senza password: errore
            throw new IllegalArgumentException("La password è obbligatoria per nuovi record");
        }
        // Se è modifica e password vuota, mantieni quella esistente
        
        PasswordEntry saved = repository.save(entity);
        return saved.getId();
    }
    
    @Transactional(readOnly = true)
    public List<PasswordEntryDTO> findAll() {
        List<PasswordEntry> entities = repository.findAll();
        return entities.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private PasswordEntryDTO convertToDTO(PasswordEntry entity) {
        PasswordEntryDTO dto = new PasswordEntryDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setUsername(entity.getUsername());
        dto.setUrl(entity.getUrl());
        dto.setNotes(entity.getNotes());
        dto.setCategory(entity.getCategory());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        // Decifra SOLO per il DTO
        try {
            String decrypted = encryptionUtil.decrypt(entity.getEncryptedPassword());
            dto.setDecryptedPassword(decrypted);
        } catch (Exception e) {
            dto.setDecryptedPassword("[ERRORE DECIFRATURA]");
        }
        
        return dto;
    }
    
    // Metodo per trovare by ID (per modifica)
    @Transactional(readOnly = true)
    public PasswordEntryForm findByIdForEdit(Long id) {
        PasswordEntry entity = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Record non trovato"));
        
        PasswordEntryForm form = new PasswordEntryForm();
        form.setId(entity.getId());
        form.setTitle(entity.getTitle());
        form.setUsername(entity.getUsername());
        form.setUrl(entity.getUrl());
        form.setNotes(entity.getNotes());
        form.setCategory(entity.getCategory());
        
        // Decifra per il form di modifica
        try {
            String decrypted = encryptionUtil.decrypt(entity.getEncryptedPassword());
            form.setPlainPassword(decrypted);
        } catch (Exception e) {
            form.setPlainPassword("");
            System.out.println("ERRORE decifratura per ID " + id + ": " + e.getMessage());
        }
        
        return form;
    }
    
    public String testEncryption(String data) {
        return encryptionUtil.encrypt(data);
    }
    
    public String testDecryption(String encryptedData) {
        return encryptionUtil.decrypt(encryptedData);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<PasswordEntryDTO> searchByTitle(String title) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findByTitleContainingIgnoreCase(title);
        return entries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public void clearSession() {
        encryptionUtil.clear();
    }
    
    @Transactional(readOnly = true)
    public List<PasswordEntryDTO> findByCategory(String category) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findByCategory(category);
        return entries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<String> getAllCategories() {
        return categoryService.getAllCategories();
    }
    
    public Map<String, Long> getCategoryStats() {
        return categoryService.getCategoryStats();
    }
    
    @Transactional(readOnly = true)
    public List<PasswordEntryDTO> findByCategoryAndSearch(String category, String query) {
        if (!isMasterPasswordSet()) {
            throw new IllegalStateException("Master password non impostata");
        }
        
        List<PasswordEntry> entries = repository.findByCategoryAndSearchQuery(category, query);
        return entries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
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