package com.durdencorp.pswmanager.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.durdencorp.pswmanager.dto.PasswordEntryDTO;
import com.durdencorp.pswmanager.dto.PasswordEntryForm;
import com.durdencorp.pswmanager.service.PasswordEntryService;

@RestController
@RequestMapping("/api/passwords")
public class PasswordEntryController {
    
    @Autowired
    private PasswordEntryService passwordEntryService;
    
    // GET tutte le password - RESTITUISCE DTO
    @GetMapping
    public ResponseEntity<List<PasswordEntryDTO>> getAllPasswords() {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            List<PasswordEntryDTO> passwords = passwordEntryService.findAll();
            return ResponseEntity.ok(passwords);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET password by ID - RESTITUISCE DTO
    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntryDTO> getPasswordById(@PathVariable Long id) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Usa il metodo che restituisce DTO
            List<PasswordEntryDTO> allPasswords = passwordEntryService.findAll();
            PasswordEntryDTO password = allPasswords.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
                
            if (password != null) {
                return ResponseEntity.ok(password);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // CREATE nuova password - USA FORM
    @PostMapping
    public ResponseEntity<Long> createPassword(@RequestBody PasswordEntryForm form) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Long savedId = passwordEntryService.save(form);
            return ResponseEntity.ok(savedId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // UPDATE password esistente - USA FORM
    @PutMapping("/{id}")
    public ResponseEntity<Long> updatePassword(@PathVariable Long id, @RequestBody PasswordEntryForm form) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Assicurati che l'ID nel form corrisponda al path
            form.setId(id);
            Long updatedId = passwordEntryService.save(form);
            return ResponseEntity.ok(updatedId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DELETE password
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassword(@PathVariable Long id) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            passwordEntryService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // SEARCH per titolo - RESTITUISCE DTO
    @GetMapping("/search")
    public ResponseEntity<List<PasswordEntryDTO>> searchByTitle(@RequestParam String title) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<PasswordEntryDTO> results = passwordEntryService.searchByTitle(title);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET per categoria - RESTITUISCE DTO
    @GetMapping("/category/{category}")
    public ResponseEntity<List<PasswordEntryDTO>> getByCategory(@PathVariable String category) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<PasswordEntryDTO> results = passwordEntryService.findByCategory(category);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // SEARCH per categoria e query - RESTITUISCE DTO
    @GetMapping("/category/{category}/search")
    public ResponseEntity<List<PasswordEntryDTO>> searchInCategory(
            @PathVariable String category, 
            @RequestParam String query) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<PasswordEntryDTO> results = passwordEntryService.findByCategoryAndSearch(category, query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}