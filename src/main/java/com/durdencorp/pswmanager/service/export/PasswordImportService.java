package com.durdencorp.pswmanager.service.export;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.durdencorp.pswmanager.dto.ExportData;
import com.durdencorp.pswmanager.dto.ExportEntry;
import com.durdencorp.pswmanager.dto.ImportResult;
import com.durdencorp.pswmanager.dto.PasswordEntryForm;
import com.durdencorp.pswmanager.exception.ImportException;
import com.durdencorp.pswmanager.service.MasterPasswordEncryption;
import com.durdencorp.pswmanager.service.PasswordEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class PasswordImportService {
    
    @Autowired 
    private PasswordEntryService passwordService;
    
    @Autowired 
    private MasterPasswordEncryption encryption;
    
    private final ObjectMapper objectMapper;
    
    public PasswordImportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.findAndRegisterModules();
    }
    
    public ImportResult importEncryptedJson(String encryptedJson, boolean overwrite) {
        ImportResult result = new ImportResult();
        
        try {
            String json = encryption.decrypt(encryptedJson);
            ExportData data = objectMapper.readValue(json, ExportData.class);
            
            Set<String> processedPairs = new HashSet<>();
            
            for (ExportEntry entry : data.getEntries()) {
                try {
                    String pairKey = entry.getTitle() + "||" + entry.getUsername();
                    
                    if (processedPairs.contains(pairKey)) {
                        result.incrementDuplicates();
                        continue;
                    }
                    
                    processedPairs.add(pairKey);
                    
                    boolean exists = passwordService.existsByTitleAndUsername(
                        entry.getTitle(), entry.getUsername());
                    
                    if (overwrite || !exists) {
                        PasswordEntryForm form = convertToForm(entry);
                        passwordService.save(form);
                        result.incrementImported();
                    } else {
                        result.incrementSkipped();
                    }
                } catch (Exception e) {
                    result.incrementErrors();
                    // Log dettagliato dell'errore
                    System.err.println("Errore import entry: " + e.getMessage());
                }
            }
            
            result.setSuccess(result.getErrorCount() == 0);
            result.setMessage(result.getErrorCount() == 0 ? 
                "Import completato con successo" : 
                "Import completato con " + result.getErrorCount() + " errori");
                
        } catch (Exception e) {
            throw new ImportException("Errore durante l'import del JSON cifrato: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    private PasswordEntryForm convertToForm(ExportEntry entry) {
        PasswordEntryForm form = new PasswordEntryForm();
        form.setTitle(entry.getTitle());
        form.setUsername(entry.getUsername());
        
        // IMPORTANTE: La password è già cifrata, non la decifriamo!
        // Creiamo una password fittizia, poi sostituiamo nel repository
        form.setPlainPassword("temporary_password");
        
        form.setUrl(entry.getUrl());
        form.setNotes(entry.getNotes());
        form.setCategory(entry.getCategory());
        
        return form;
    }
    
    public ImportResult importFromCsv(MultipartFile file, boolean overwrite) {
        ImportResult result = new ImportResult();
        
        if (file.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("File vuoto");
            return result;
        }
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            
            String line;
            boolean firstLine = true;
            Set<String> processedPairs = new HashSet<>();
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Salta l'header
                }
                
                try {
                    // Parsing CSV semplice (per CSV con virgolette)
                    String[] values = parseCsvLine(line);
                    
                    if (values.length < 5) {
                        result.incrementErrors();
                        continue;
                    }
                    
                    String title = values[0].trim();
                    String username = values[1].trim();
                    String pairKey = title + "||" + username;
                    
                    if (processedPairs.contains(pairKey)) {
                        result.incrementDuplicates();
                        continue;
                    }
                    
                    processedPairs.add(pairKey);
                    
                    boolean exists = passwordService.existsByTitleAndUsername(title, username);
                    
                    if (overwrite || !exists) {
                        PasswordEntryForm form = new PasswordEntryForm();
                        form.setTitle(title);
                        form.setUsername(username);
                        form.setPlainPassword(""); // Password vuota per CSV
                        form.setUrl(values[2].trim());
                        form.setCategory(values[3].trim());
                        form.setNotes(values[4].trim());
                        
                        passwordService.save(form);
                        result.incrementImported();
                    } else {
                        result.incrementSkipped();
                    }
                    
                } catch (Exception e) {
                    result.incrementErrors();
                    System.err.println("Errore parsing linea CSV: " + line + " - " + e.getMessage());
                }
            }
            
            result.setSuccess(result.getErrorCount() == 0);
            result.setMessage("Import CSV completato: " + 
                result.getImportedCount() + " importati, " +
                result.getSkippedCount() + " saltati, " +
                result.getErrorCount() + " errori");
                
        } catch (Exception e) {
            throw new ImportException("Errore durante l'import CSV: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    private String[] parseCsvLine(String line) {
        // Semplice parser CSV che gestisce virgolette
        java.util.List<String> values = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        values.add(current.toString());
        return values.toArray(new String[0]);
    }
    
    public ImportResult importFromEncryptedFile(MultipartFile file, boolean overwrite) {
        try {
            String content = new String(file.getBytes(), "UTF-8");
            return importEncryptedJson(content, overwrite);
        } catch (Exception e) {
            throw new ImportException("Errore lettura file cifrato: " + e.getMessage(), e);
        }
    }
}