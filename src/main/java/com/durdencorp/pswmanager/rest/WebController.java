package com.durdencorp.pswmanager.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
            model.addAttribute("passwords", passwords != null ? passwords : new ArrayList<>());
            return "index";
            
        } catch (Exception e) {
            // Se c'è un errore (es. master password non impostata), reindirizza al login
            model.addAttribute("passwords", new ArrayList<>());
            model.addAttribute("errorMessage", "Errore nel caricamento: " + e.getMessage());
            return "index";
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
        System.out.println("Password: " + passwordEntry.getEncryptedPassword());
        System.out.println("URL: " + passwordEntry.getUrl());
        System.out.println("Note: " + passwordEntry.getNotes());
        
        try {
            PasswordEntry saved = passwordEntryService.save(passwordEntry);
            System.out.println("DOPO service.save() - ID: " + saved.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                passwordEntry.getId() == null ? "Password salvata con successo!" : "Password aggiornata con successo!");
                
        } catch (Exception e) {
            System.out.println("ERRORE nel salvataggio: " + e.getMessage());
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
    
}