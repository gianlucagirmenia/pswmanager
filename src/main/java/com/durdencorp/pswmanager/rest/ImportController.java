package com.durdencorp.pswmanager.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.durdencorp.pswmanager.dto.ImportResult;
import com.durdencorp.pswmanager.service.export.PasswordImportService;

@RestController
@RequestMapping("/api/import")
public class ImportController {
    
    @Autowired
    private PasswordImportService importService;
    
    @PostMapping("/encrypted")
    public ResponseEntity<ImportResult> importEncrypted(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        
        try {
            ImportResult result = importService.importFromEncryptedFile(file, overwrite);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ImportResult errorResult = new ImportResult();
            errorResult.setSuccess(false);
            errorResult.setMessage("Errore durante l'import: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
    
    @PostMapping("/csv")
    public ResponseEntity<ImportResult> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        
        try {
            ImportResult result = importService.importFromCsv(file, overwrite);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ImportResult errorResult = new ImportResult();
            errorResult.setSuccess(false);
            errorResult.setMessage("Errore durante l'import CSV: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
}