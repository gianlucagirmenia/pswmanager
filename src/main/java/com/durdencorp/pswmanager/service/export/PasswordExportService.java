package com.durdencorp.pswmanager.service.export;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.durdencorp.pswmanager.dto.ExportData;
import com.durdencorp.pswmanager.dto.ExportEntry;
import com.durdencorp.pswmanager.dto.PasswordEntryDTO;
import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.service.MasterPasswordEncryption;
import com.durdencorp.pswmanager.service.PasswordEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class PasswordExportService {
    
    @Autowired 
    private PasswordEntryService passwordService;
    
    @Autowired 
    private MasterPasswordEncryption encryption;
    
    private final ObjectMapper objectMapper;
    
    public PasswordExportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.findAndRegisterModules();
    }
    
    public String exportToEncryptedJson() throws Exception {
        List<PasswordEntry> entries = passwordService.findAllRaw();
        ExportData exportData = new ExportData(
            LocalDateTime.now(),
            entries.size(),
            entries.stream()
                .map(this::convertToExportEntry)
                .collect(Collectors.toList())
        );
        
        String json = objectMapper.writeValueAsString(exportData);
        return encryption.encrypt(json);
    }
    
    private ExportEntry convertToExportEntry(PasswordEntry entry) {
        ExportEntry exportEntry = new ExportEntry();
        exportEntry.setTitle(entry.getTitle());
        exportEntry.setUsername(entry.getUsername());
        exportEntry.setEncryptedPassword(entry.getEncryptedPassword()); // Già cifrata
        exportEntry.setUrl(entry.getUrl());
        exportEntry.setNotes(entry.getNotes());
        exportEntry.setCategory(entry.getCategory());
        exportEntry.setCreatedAt(entry.getCreatedAt());
        exportEntry.setUpdatedAt(entry.getUpdatedAt());
        return exportEntry;
    }
    
    public byte[] exportToCsv() {
        List<PasswordEntryDTO> entries = passwordService.findAll();
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("\"Titolo\",\"Username\",\"URL\",\"Categoria\",\"Note\",\"Data Creazione\",\"Data Aggiornamento\"\n");
        
        // Dati
        for (PasswordEntryDTO entry : entries) {
            String notes = entry.getNotes() != null ? 
                entry.getNotes().replace("\"", "\"\"") : "";
            
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                entry.getTitle() != null ? entry.getTitle().replace("\"", "\"\"") : "",
                entry.getUsername() != null ? entry.getUsername().replace("\"", "\"\"") : "",
                entry.getUrl() != null ? entry.getUrl().replace("\"", "\"\"") : "",
                entry.getCategory() != null ? entry.getCategory().replace("\"", "\"\"") : "",
                notes,
                entry.getCreatedAt() != null ? entry.getCreatedAt().toString() : "",
                entry.getUpdatedAt() != null ? entry.getUpdatedAt().toString() : ""
            ));
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    // Export JSON non cifrato (per debug o uso interno)
    public String exportToJson() throws Exception {
        List<PasswordEntry> entries = passwordService.findAllRaw();
        ExportData exportData = new ExportData(
            LocalDateTime.now(),
            entries.size(),
            entries.stream()
                .map(this::convertToExportEntry)
                .collect(Collectors.toList())
        );
        
        return objectMapper.writeValueAsString(exportData);
    }
}