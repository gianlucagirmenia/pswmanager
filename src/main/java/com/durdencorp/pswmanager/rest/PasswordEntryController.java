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
import org.springframework.web.bind.annotation.RestController;

import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.service.PasswordEntryService;

@RestController
@RequestMapping("/api/passwords")
public class PasswordEntryController {

    @Autowired
    private PasswordEntryService passwordEntryService;

    @GetMapping
    public List<PasswordEntry> getAllPasswordEntries() {
        return passwordEntryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntry> getPasswordEntryById(@PathVariable Long id) {
        Optional<PasswordEntry> passwordEntry = passwordEntryService.findById(id);
        return passwordEntry.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public PasswordEntry createPasswordEntry(@RequestBody PasswordEntry passwordEntry) {
        return passwordEntryService.save(passwordEntry);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PasswordEntry> updatePasswordEntry(@PathVariable Long id, @RequestBody PasswordEntry passwordEntryDetails) {
        Optional<PasswordEntry> optionalPasswordEntry = passwordEntryService.findById(id);
        if (optionalPasswordEntry.isPresent()) {
            PasswordEntry passwordEntry = optionalPasswordEntry.get();
            passwordEntry.setTitle(passwordEntryDetails.getTitle());
            passwordEntry.setUsername(passwordEntryDetails.getUsername());
            passwordEntry.setPassword(passwordEntryDetails.getPassword());
            passwordEntry.setUrl(passwordEntryDetails.getUrl());
            passwordEntry.setNotes(passwordEntryDetails.getNotes());
            return ResponseEntity.ok(passwordEntryService.save(passwordEntry));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePasswordEntry(@PathVariable Long id) {
        if (passwordEntryService.findById(id).isPresent()) {
            passwordEntryService.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
