package com.durdencorp.pswmanager.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.service.PasswordEntryService;

@Controller
public class WebController {
    
    @Autowired
    private PasswordEntryService passwordEntryService;
    
 // Pagina principale con la lista
    @GetMapping("/")
    public String home(Model model) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return "redirect:/login";
            }
            
            List<PasswordEntry> passwords = passwordEntryService.findAll();
            int totalCount = passwords != null ? passwords.size() : 0;
            
            model.addAttribute("passwords", passwords != null ? passwords : new ArrayList<>());
            model.addAttribute("totalCount", totalCount); // AGGIUNGI QUESTO
            
            // AGGIUNGI SEMPRE LE STATISTICHE DELLE CATEGORIE
            model.addAttribute("categories", passwordEntryService.getAllCategories());
            model.addAttribute("categoryStats", passwordEntryService.getCategoryStats());
            
            return "index";
            
        } catch (Exception e) {
            model.addAttribute("passwords", new ArrayList<>());
            model.addAttribute("totalCount", 0); // ANCHE IN CASO DI ERRORE
            model.addAttribute("errorMessage", "Errore nel caricamento: " + e.getMessage());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("categoryStats", new HashMap<>());
            return "index";
        }
    }
    
    // Filtra per categoria
    @GetMapping("/category/{category}")
    public String filterByCategory(@PathVariable String category, Model model) {
        try {
            List<PasswordEntry> passwords = passwordEntryService.findByCategory(category);
            int filteredCount = passwords.size();
            
            model.addAttribute("passwords", passwords);
            model.addAttribute("currentCategory", category);
            model.addAttribute("filteredCount", filteredCount); // CONTEggio FILTRATO
            model.addAttribute("categories", passwordEntryService.getAllCategories());
            model.addAttribute("categoryStats", passwordEntryService.getCategoryStats());
            
            // Calcola il totale generale per il badge "Tutte"
            List<PasswordEntry> allPasswords = passwordEntryService.findAll();
            model.addAttribute("totalCount", allPasswords.size());
            
            return "index";
        } catch (SecurityException e) {
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore nel filtro per categoria: " + e.getMessage());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("categoryStats", new HashMap<>());
            model.addAttribute("totalCount", 0);
            return home(model);
        }
    }
    
    // Mostra il form per inserire una nuova password
    @GetMapping("/new")
    public String showNewForm(Model model) {
        if (!passwordEntryService.isMasterPasswordSet()) {
            return "redirect:/login";
        }
        model.addAttribute("passwordEntry", new PasswordEntry());
        model.addAttribute("isEdit", false);
        return "password-form";
    }
    
    // Mostra il form per modificare una password esistente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!passwordEntryService.isMasterPasswordSet()) {
            return "redirect:/login";
        }
        
        try {
            PasswordEntry passwordEntry = passwordEntryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID password non valido:" + id));
            model.addAttribute("passwordEntry", passwordEntry);
            model.addAttribute("isEdit", true);
            return "password-form";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/save")
    public String savePassword(@ModelAttribute PasswordEntry passwordEntry, 
                             RedirectAttributes redirectAttributes) {
        
        System.out.println("=== CONTROLLER SAVE - INIZIO ===");
        System.out.println("Master password impostata: " + passwordEntryService.isMasterPasswordSet());
        System.out.println("Titolo: " + passwordEntry.getTitle());
        System.out.println("Username: " + passwordEntry.getUsername());
        System.out.println("Password (IN CHIARO dal form): " + passwordEntry.getEncryptedPassword());
        System.out.println("URL: " + passwordEntry.getUrl());
        System.out.println("Note: " + passwordEntry.getNotes());
        
        try {
            PasswordEntry saved = passwordEntryService.save(passwordEntry);
            System.out.println("DOPO service.save() - ID: " + saved.getId());
            System.out.println("Password nel record salvato: " + saved.getEncryptedPassword());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                passwordEntry.getId() == null ? "Password salvata con successo!" : "Password aggiornata con successo!");
                
        } catch (Exception e) {
            System.out.println("ERRORE CRITICO nel salvataggio: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        
        System.out.println("=== CONTROLLER SAVE - FINE ===");
        return "redirect:/";
    }
    
    // Elimina una password
    @GetMapping("/delete/{id}")
    public String deletePassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!passwordEntryService.isMasterPasswordSet()) {
            return "redirect:/login";
        }
        
        try {
            passwordEntryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Password eliminata con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nell'eliminazione: " + e.getMessage());
        }
        return "redirect:/";
    }
    
    // Ricerca all'interno di una categoria
    @GetMapping("/category/{category}/search")
    public String searchInCategory(@PathVariable String category, 
                                 @RequestParam String query, 
                                 Model model) {
        try {
            List<PasswordEntry> passwords = passwordEntryService.findByCategoryAndSearch(category, query);
            model.addAttribute("passwords", passwords);
            model.addAttribute("currentCategory", category);
            model.addAttribute("searchQuery", query);
            model.addAttribute("categories", passwordEntryService.getAllCategories());
            model.addAttribute("categoryStats", passwordEntryService.getCategoryStats());
            return "index";
        } catch (SecurityException e) {
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore nella ricerca: " + e.getMessage());
            return home(model);
        }
    }
    
    // Mostra tutte le categorie
    @GetMapping("/categories")
    public String showCategories(Model model) {
        try {
            model.addAttribute("categories", passwordEntryService.getAllCategories());
            model.addAttribute("categoryStats", passwordEntryService.getCategoryStats());
            return "categories";
        } catch (SecurityException e) {
            return "redirect:/login";
        }
    }
    
    @PostMapping("/admin/sanitize")
    public String sanitizeDatabase(RedirectAttributes redirectAttributes) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return "redirect:/login";
            }
            
            passwordEntryService.sanitizeAndReencryptAll();
            redirectAttributes.addFlashAttribute("successMessage", "✅ Database sanitizzato con successo! Tutte le password sono state verificate e ricifrate se necessario.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Errore nella sanitizzazione: " + e.getMessage());
        }
        return "redirect:/";
    }
    
}