package com.durdencorp.pswmanager.rest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.durdencorp.pswmanager.dto.PasswordEntryDTO;
import com.durdencorp.pswmanager.dto.PasswordEntryForm;
import com.durdencorp.pswmanager.service.PasswordEntryService;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Controller
public class WebController {
    
    @Autowired
    private PasswordEntryService passwordEntryService;
    
    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category, // Aggiungi questo
            Model model) {
        
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return "redirect:/login";
            }
            
            Page<PasswordEntryDTO> passwordPage;
            
            if (category != null && !category.isEmpty()) {
                // Se c'è categoria, usa il filtro per categoria
                if (search != null && !search.trim().isEmpty()) {
                    passwordPage = passwordEntryService.findByCategoryAndSearchPaginated(
                        category, search, PageRequest.of(page, size));
                    model.addAttribute("searchQuery", search);
                } else {
                    passwordPage = passwordEntryService.findByCategoryPaginated(
                        category, PageRequest.of(page, size));
                }
                model.addAttribute("currentCategory", category);
            } else {
                // Nessuna categoria specificata
                if (search != null && !search.trim().isEmpty()) {
                    passwordPage = passwordEntryService.searchPaginated(search, PageRequest.of(page, size));
                    model.addAttribute("searchQuery", search);
                } else {
                    passwordPage = passwordEntryService.findAllPaginated(PageRequest.of(page, size));
                }
            }
            
            // Calcolo informazioni paginazione
            int totalPages = passwordPage.getTotalPages();
            long totalItems = passwordPage.getTotalElements();
            
            model.addAttribute("passwords", passwordPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalItems", totalItems);
            
            // Calcola range visualizzato
            int startItem = page * size + 1;
            int endItem = Math.min((page + 1) * size, (int) totalItems);
            model.addAttribute("startItem", startItem);
            model.addAttribute("endItem", endItem);
            
            // Categorie e statistiche
            model.addAttribute("categories", passwordEntryService.getAllCategories());
            model.addAttribute("categoryStats", passwordEntryService.getCategoryStats());
            
            // Calcola totale generale
            long totalAll = passwordEntryService.countAll();
            model.addAttribute("totalCount", totalAll);
            
            return "passwords/list";
            
        } catch (Exception e) {
            model.addAttribute("passwords", new ArrayList<>());
            model.addAttribute("errorMessage", "Errore nel caricamento: " + e.getMessage());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("categoryStats", new HashMap<>());
            model.addAttribute("totalCount", 0);
            return "passwords/list";
        }
    }
    
    // Mostra il form per inserire una nuova password
    @GetMapping("/new")
    public String showNewForm(Model model) {
        if (!passwordEntryService.isMasterPasswordSet()) {
            return "redirect:/login";
        }
        model.addAttribute("passwordEntryForm", new PasswordEntryForm());
        return "passwords/form";
    }
    
    // Mostra il form per modificare una password esistente
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!passwordEntryService.isMasterPasswordSet()) {
            return "redirect:/login";
        }
        
        try {
            PasswordEntryForm form = passwordEntryService.findByIdForEdit(id);
            model.addAttribute("passwordEntryForm", form);
            return "passwords/form";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/save")
    public String savePassword(@ModelAttribute PasswordEntryForm passwordEntryForm, 
                             RedirectAttributes redirectAttributes) {
        try {
            passwordEntryService.save(passwordEntryForm);
            redirectAttributes.addFlashAttribute("successMessage", 
                passwordEntryForm.getId() == null ? 
                "Password salvata con successo!" : 
                "Password aggiornata con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
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
    
    // Filtra per categoria
    @GetMapping("/category/{category}")
    public String filterByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        try {
            Page<PasswordEntryDTO> passwordPage;
            
            if (search != null && !search.trim().isEmpty()) {
                // Ricerca nella categoria
                passwordPage = passwordEntryService.findByCategoryAndSearchPaginated(
                    category, search, PageRequest.of(page, size));
                model.addAttribute("searchQuery", search);
            } else {
                // Solo categoria
                passwordPage = passwordEntryService.findByCategoryPaginated(
                    category, PageRequest.of(page, size));
            }
            
            // Informazioni paginazione
            int totalPages = passwordPage.getTotalPages();
            long totalItems = passwordPage.getTotalElements();
            long totalAll = passwordEntryService.countAll();
            
            model.addAttribute("passwords", passwordPage.getContent());
            model.addAttribute("currentCategory", category);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("totalCount", totalAll);
            
            // Range visualizzato
            int startItem = page * size + 1;
            int endItem = Math.min((page + 1) * size, (int) totalItems);
            model.addAttribute("startItem", startItem);
            model.addAttribute("endItem", endItem);
            
            model.addAttribute("categories", passwordEntryService.getAllCategories());
            model.addAttribute("categoryStats", passwordEntryService.getCategoryStats());
            
            return "passwords/list";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore nel filtro per categoria: " + e.getMessage());
            // Chiama home con i parametri corretti
            return home(page, size, search, category, model);
        }
    }
    
    // Mostra tutte le categorie
    @GetMapping("/categories")
    public String showCategories(Model model) {
        try {
            model.addAttribute("categories", passwordEntryService.getAllCategories());
            model.addAttribute("categoryStats", passwordEntryService.getCategoryStats());
            return "fragments/categories";
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
    
    @ModelAttribute("buildPageUrl")
    public Function<Integer, String> buildPageUrl(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        
        return targetPage -> {
            StringBuilder url = new StringBuilder("/?");
            
            // Aggiungi parametri
            url.append("page=").append(targetPage);
            url.append("&size=").append(size);
            
            if (search != null && !search.isEmpty()) {
                url.append("&search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8));
            }
            
            if (category != null && !category.isEmpty()) {
                url.append("&category=").append(URLEncoder.encode(category, StandardCharsets.UTF_8));
            }
            
            return url.toString();
        };
    }
    
}