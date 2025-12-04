package com.durdencorp.pswmanager.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.durdencorp.pswmanager.service.PasswordEntryService;

@Controller
public class SecurityWebController {
    
    @Autowired
    private PasswordEntryService passwordEntryService;
    
    @GetMapping("/security")
    public String securityDashboard(Model model) {
        if (!passwordEntryService.isMasterPasswordSet()) {
            return "redirect:/login";
        }
        
        try {
            List<PasswordEntryService.PasswordBreachReport> compromised = 
                passwordEntryService.findCompromisedPasswords();
            
            model.addAttribute("compromisedCount", compromised.size());
            model.addAttribute("hasBreaches", !compromised.isEmpty());
            
        } catch (Exception e) {
            model.addAttribute("error", "Impossibile caricare i dati di sicurezza");
        }
        
        return "security";
    }
}