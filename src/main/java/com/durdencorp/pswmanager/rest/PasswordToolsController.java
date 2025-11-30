package com.durdencorp.pswmanager.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.durdencorp.pswmanager.service.PasswordGeneratorService;
import com.durdencorp.pswmanager.service.PasswordStrengthService;

@RestController
@RequestMapping("/api/password-tools")
public class PasswordToolsController {
    
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    
    @Autowired
    private PasswordStrengthService passwordStrengthService;
    
    // Genera una password forte
    @GetMapping("/generate")
    public String generateStrongPassword() {
        return passwordGeneratorService.generateStrongPassword();
    }
    
    // Genera una password personalizzata
    @PostMapping("/generate-custom")
    public String generateCustomPassword(@RequestBody PasswordGeneratorService.PasswordOptions options) {
        return passwordGeneratorService.generatePassword(options);
    }
    
 // Analizza la forza di una password
    @GetMapping("/analyze")
    public Map<String, Object> analyzePassword(@RequestParam String password) {
        System.out.println("=== ANALISI PASSWORD ===");
        System.out.println("Password ricevuta: " + (password != null ? "***" : "null"));
        
        try {
            PasswordStrengthService.PasswordStrength strength = passwordStrengthService.analyzeStrength(password);
            String tips = passwordStrengthService.getStrengthTips(password);
            
            System.out.println("Risultato analisi: " + strength.getDescription());
            System.out.println("Tips: " + tips);
            
            // Ritorna una Map invece dell'oggetto custom
            Map<String, Object> result = new HashMap<>();
            result.put("description", strength.getDescription());
            result.put("color", strength.getColor());
            result.put("level", strength.getLevel());
            result.put("tips", tips);
            
            return result;
            
        } catch (Exception e) {
            System.out.println("ERRORE nell'analisi: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("description", "Errore");
            error.put("color", "red");
            error.put("level", 0);
            error.put("tips", "Errore nell'analisi della password");
            return error;
        }
    }
    
    // MODIFICA la classe interna per essere static e avere costruttore vuoto
    public static class PasswordAnalysisResult {
        private PasswordStrengthService.PasswordStrength strength;
        private String tips;
        
        // Costruttore vuoto (necessario per Jackson)
        public PasswordAnalysisResult() {}
        
        public PasswordAnalysisResult(PasswordStrengthService.PasswordStrength strength, String tips) {
            this.strength = strength;
            this.tips = tips;
        }
        
        // Getter e Setter (IMPORTANTE: devono essere public)
        public PasswordStrengthService.PasswordStrength getStrength() { return strength; }
        public void setStrength(PasswordStrengthService.PasswordStrength strength) { this.strength = strength; }
        
        public String getTips() { return tips; }
        public void setTips(String tips) { this.tips = tips; }
    }
}