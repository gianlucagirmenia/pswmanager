package com.durdencorp.pswmanager.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.durdencorp.pswmanager.service.export.PasswordExportService;

@RestController
@RequestMapping("/api/export")
public class ExportController {
	
	private static final MediaType CSV_MEDIA_TYPE = 
		    MediaType.parseMediaType("text/csv; charset=utf-8");
    
    @Autowired
    private PasswordExportService exportService;
    
    @GetMapping("/encrypted")
    public ResponseEntity<ByteArrayResource> exportEncrypted() {
        try {
            String encryptedData = exportService.exportToEncryptedJson();
            
            ByteArrayResource resource = new ByteArrayResource(encryptedData.getBytes("UTF-8"));
            
            String filename = "passwords-backup-" + 
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".enc";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
                
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'export: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/csv")
    public ResponseEntity<ByteArrayResource> exportCsv() {
        try {
            byte[] csvData = exportService.exportToCsv();
            
            ByteArrayResource resource = new ByteArrayResource(csvData);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"passwords-metadata-" + 
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".csv\"")
                .contentType(CSV_MEDIA_TYPE)
                .body(resource);
                
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'export CSV: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/json")
    public ResponseEntity<ByteArrayResource> exportJson() {
        try {
            String jsonData = exportService.exportToJson();
            
            ByteArrayResource resource = new ByteArrayResource(jsonData.getBytes("UTF-8"));
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"passwords-data-" + 
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);
                
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'export JSON: " + e.getMessage(), e);
        }
    }
}