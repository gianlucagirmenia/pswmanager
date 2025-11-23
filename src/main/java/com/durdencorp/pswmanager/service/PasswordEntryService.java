package com.durdencorp.pswmanager.service;

import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.repository.PasswordEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PasswordEntryService {
    
    @Autowired
    private PasswordEntryRepository repository;
    
    public List<PasswordEntry> findAll() {
        return repository.findAll();
    }
    
    public Optional<PasswordEntry> findById(Long id) {
        return repository.findById(id);
    }
    
    public PasswordEntry save(PasswordEntry entry) {
        return repository.save(entry);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public List<PasswordEntry> searchByTitle(String title) {
        return repository.findByTitleContainingIgnoreCase(title);
    }
    
    public List<PasswordEntry> searchAllFields(String query) {
        return repository.searchAllFields(query);
    }
    
    public List<PasswordEntry> findByCategory(String category) {
        return repository.findByCategory(category);
    }
    
    public List<PasswordEntry> findAllByOrderByTitleAsc() {
        return repository.findAllByOrderByTitleAsc();
    }
    
    public List<PasswordEntry> findAllByOrderByCreatedAtDesc() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}