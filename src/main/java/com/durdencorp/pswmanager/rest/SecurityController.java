package com.durdencorp.pswmanager.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.durdencorp.pswmanager.service.PasswordEntryService;
import com.durdencorp.pswmanager.service.security.BreachCheckResult;
import com.durdencorp.pswmanager.service.security.HibpService;

@RestController
@RequestMapping("/api/security")
public class SecurityController {
    
    @Autowired
    private PasswordEntryService passwordEntryService;
    
    @Autowired
    private HibpService hibpService;
    
    /**
     * Controlla una password specifica
     */
    @PostMapping("/check-password")
    public ResponseEntity<BreachCheckResult> checkPassword(@RequestBody Map<String, String> request) {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String password = request.get("password");
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            BreachCheckResult result = hibpService.checkPassword(password);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Scansione completa di tutte le password
     */
    @GetMapping("/full-scan")
    public ResponseEntity<List<PasswordEntryService.PasswordBreachReport>> fullScan() {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<PasswordEntryService.PasswordBreachReport> reports = 
                passwordEntryService.checkAllPasswordsForBreaches();
            
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Solo password compromesse
     */
    @GetMapping("/compromised")
    public ResponseEntity<List<PasswordEntryService.PasswordBreachReport>> getCompromised() {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<PasswordEntryService.PasswordBreachReport> compromised = 
                passwordEntryService.findCompromisedPasswords();
            
            return ResponseEntity.ok(compromised);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Statistiche sicurezza
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSecurityStats() {
        try {
            if (!passwordEntryService.isMasterPasswordSet()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<PasswordEntryService.PasswordBreachReport> allReports = 
                passwordEntryService.checkAllPasswordsForBreaches();
            
            long total = allReports.size();
            long compromised = allReports.stream()
                .filter(r -> r.getBreachCheckResult().isCompromised())
                .count();
            long safe = total - compromised;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPasswords", total);
            stats.put("compromised", compromised);
            stats.put("safe", safe);
            stats.put("riskPercentage", total > 0 ? (compromised * 100.0 / total) : 0);
            stats.put("lastScan", new Date());
            stats.put("overallRisk", compromised == 0 ? "LOW" : 
                     (compromised < 3 ? "MEDIUM" : "HIGH"));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}