package com.durdencorp.pswmanager.rest;

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
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("passwords", passwordEntryService.findAll());
        model.addAttribute("newPassword", new PasswordEntry()); // Oggetto vuoto
        return "index";
    }
    
    @PostMapping("/add")
    public String addPassword(@ModelAttribute PasswordEntry passwordEntry, RedirectAttributes redirectAttributes) {
        passwordEntryService.save(passwordEntry);
        redirectAttributes.addFlashAttribute("successMessage", "Password salvata con successo!");
        return "redirect:/"; // Redirect ricarica la pagina con oggetto vuoto
    }
    
    @GetMapping("/delete/{id}")
    public String deletePassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        passwordEntryService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Password eliminata con successo!");
        return "redirect:/";
    }
}