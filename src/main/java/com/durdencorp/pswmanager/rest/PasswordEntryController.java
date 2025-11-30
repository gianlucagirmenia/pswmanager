package com.durdencorp.pswmanager.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.service.PasswordEntryService;

@RestController
@RequestMapping("/api/passwords")
public class PasswordEntryController {
    
    @Autowired
    private PasswordEntryService passwordEntryService;
    
    // GET tutte le password
    @GetMapping
    public List<PasswordEntry> getAllPasswords() {
        return passwordEntryService.findAll();
    }
    
    // GET password by ID
    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntry> getPasswordById(@PathVariable Long id) {
        Optional<PasswordEntry> entry = passwordEntryService.findById(id);
        return entry.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    // CREATE nuova password
    @PostMapping
    public PasswordEntry createPassword(@RequestBody PasswordEntry entry) {
        return passwordEntryService.save(entry);
    }
    
    // UPDATE password esistente
    @PutMapping("/{id}")
    public ResponseEntity<PasswordEntry> updatePassword(@PathVariable Long id, @RequestBody PasswordEntry entryDetails) {
        Optional<PasswordEntry> optionalEntry = passwordEntryService.findById(id);
        
        if (optionalEntry.isPresent()) {
            PasswordEntry entry = optionalEntry.get();
            entry.setTitle(entryDetails.getTitle());
            entry.setUsername(entryDetails.getUsername());
            entry.setEncryptedPassword(entryDetails.getEncryptedPassword());
            entry.setUrl(entryDetails.getUrl());
            entry.setNotes(entryDetails.getNotes());
            entry.setCategory(entryDetails.getCategory());
            
            return ResponseEntity.ok(passwordEntryService.save(entry));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE password
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassword(@PathVariable Long id) {
        if (passwordEntryService.findById(id).isPresent()) {
            passwordEntryService.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // SEARCH per titolo
    @GetMapping("/search")
    public List<PasswordEntry> searchByTitle(@RequestParam String title) {
        return passwordEntryService.searchByTitle(title);
    }
    
    /*
    // SEARCH in tutti i campi
    @GetMapping("/search/all")
    public List<PasswordEntry> searchAllFields(@RequestParam String query) {
        return passwordEntryService.searchAllFields(query);
    }
    */
    
    // GET per categoria
    @GetMapping("/category/{category}")
    public List<PasswordEntry> getByCategory(@PathVariable String category) {
        return passwordEntryService.findByCategory(category);
    }
    
    /*
    // GET ordinate per titolo
    @GetMapping("/sorted/title")
    public List<PasswordEntry> getAllSortedByTitle() {
        return passwordEntryService.findAllByOrderByTitleAsc();
    }
    */
    
    /*
    // GET ordinate per data
    @GetMapping("/sorted/date")
    public List<PasswordEntry> getAllSortedByDate() {
        return passwordEntryService.findAllByOrderByCreatedAtDesc();
    }
    */
}