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
    private PasswordEntryRepository passwordEntryRepository;

    public List<PasswordEntry> findAll() {
        return passwordEntryRepository.findAll();
    }

    public Optional<PasswordEntry> findById(Long id) {
        return passwordEntryRepository.findById(id);
    }

    public PasswordEntry save(PasswordEntry passwordEntry) {
        return passwordEntryRepository.save(passwordEntry);
    }

    public void deleteById(Long id) {
        passwordEntryRepository.deleteById(id);
    }
}