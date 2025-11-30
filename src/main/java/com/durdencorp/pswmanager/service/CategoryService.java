package com.durdencorp.pswmanager.service;

import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.repository.PasswordEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    
    @Autowired
    private PasswordEntryRepository passwordEntryRepository;
    
    // Categorie predefinite
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
        "Generale",
        "Email",
        "Social Media",
        "Lavoro",
        "Banca/Finanza",
        "Shopping",
        "Intrattenimento",
        "Istruzione",
        "Viaggi",
        "Salute"
    );
    
    // Ottiene tutte le categorie (predefinite + quelle usate nel database)
    public List<String> getAllCategories() {
        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        
        // Aggiungi categorie predefinite
        categories.addAll(DEFAULT_CATEGORIES);
        
        // Aggiungi categorie dal database
        List<String> dbCategories = passwordEntryRepository.findAllCategories();
        categories.addAll(dbCategories);
        
        return new ArrayList<>(categories);
    }
    
 // Ottiene le statistiche per categoria
    public Map<String, Long> getCategoryStats() {
        try {
            List<PasswordEntry> allEntries = passwordEntryRepository.findAll();
            
            return allEntries.stream()
                .collect(Collectors.groupingBy(
                    PasswordEntry::getCategory,
                    TreeMap::new,
                    Collectors.counting()
                ));
        } catch (Exception e) {
            System.out.println("Errore nel calcolo delle statistiche categorie: " + e.getMessage());
            return new TreeMap<>();
        }
    }
    
    // Aggiungi una nuova categoria
    public void addCustomCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            // La categoria verrà automaticamente aggiunta quando verrà usata
            System.out.println("Categoria personalizzata aggiunta: " + category);
        }
    }
    
    // Verifica se una categoria esiste
    public boolean categoryExists(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        return getAllCategories().stream()
            .anyMatch(cat -> cat.equalsIgnoreCase(category.trim()));
    }
}