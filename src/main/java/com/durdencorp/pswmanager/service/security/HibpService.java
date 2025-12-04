package com.durdencorp.pswmanager.service.security;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HibpService {
    
    private static final String HIBP_API_URL = "https://api.pwnedpasswords.com/range/";
    
    private final RestTemplate restTemplate;
    
    public HibpService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    
    /**
     * Controlla se una password è stata compromessa in data breach
     * Usa k-anonymity per proteggere la password
     */
    public BreachCheckResult checkPassword(String password) {
        try {
            String sha1Hash = sha1(password).toUpperCase();
            String prefix = sha1Hash.substring(0, 5);
            String suffix = sha1Hash.substring(5);
            
            // Chiamata API con solo i primi 5 caratteri
            ResponseEntity<String> response = restTemplate.exchange(
                HIBP_API_URL + prefix,
                HttpMethod.GET,
                createRequestEntity(),
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                int breachCount = parseResponseForSuffix(response.getBody(), suffix);
                
                return new BreachCheckResult(
                    breachCount > 0,
                    breachCount,
                    generateMessage(breachCount),
                    generateSuggestions(breachCount)
                );
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel controllo HIBP: " + e.getMessage());
        }
        
        return BreachCheckResult.error();
    }
    
    /**
     * Controlla batch di password (per performance)
     */
    public Map<String, BreachCheckResult> checkPasswords(List<String> passwords) {
        Map<String, BreachCheckResult> results = new HashMap<>();
        
        // Raggruppa per prefix per minimizzare chiamate API
        Map<String, List<PasswordWithHash>> groupedByPrefix = passwords.stream()
            .map(pwd -> {
                String hash = sha1(pwd).toUpperCase();
                return new PasswordWithHash(pwd, hash, hash.substring(0, 5), hash.substring(5));
            })
            .collect(Collectors.groupingBy(PasswordWithHash::getPrefix));
        
        for (Map.Entry<String, List<PasswordWithHash>> entry : groupedByPrefix.entrySet()) {
            String prefix = entry.getKey();
            List<PasswordWithHash> passwordGroup = entry.getValue();
            
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    HIBP_API_URL + prefix,
                    HttpMethod.GET,
                    createRequestEntity(),
                    String.class
                );
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    String responseBody = response.getBody();
                    
                    for (PasswordWithHash pwd : passwordGroup) {
                        int breachCount = parseResponseForSuffix(responseBody, pwd.getSuffix());
                        
                        results.put(pwd.getPassword(), new BreachCheckResult(
                            breachCount > 0,
                            breachCount,
                            generateMessage(breachCount),
                            generateSuggestions(breachCount)
                        ));
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Errore nel controllo batch HIBP per prefix " + prefix + ": " + e.getMessage());
                // Assegna risultato di errore per ogni password nel gruppo
                passwordGroup.forEach(pwd -> 
                    results.put(pwd.getPassword(), BreachCheckResult.error())
                );
            }
        }
        
        return results;
    }
    
    private HttpEntity<String> createRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "PasswordManager-SpringBoot");
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        return new HttpEntity<>(headers);
    }
    
    private int parseResponseForSuffix(String responseBody, String suffix) {
        // Il formato della risposta è: SUFFIX:COUNT (una per riga)
        return Arrays.stream(responseBody.split("\n"))
            .filter(line -> line.startsWith(suffix))
            .findFirst()
            .map(line -> {
                String[] parts = line.split(":");
                return parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            })
            .orElse(0);
    }
    
    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Errore nel calcolo SHA-1", e);
        }
    }
    
    private String generateMessage(int breachCount) {
        if (breachCount == 0) {
            return "✅ Password sicura - Non trovata in nessun data breach conosciuto";
        } else if (breachCount < 100) {
            return "⚠️ Attenzione - Password trovata in " + breachCount + " data breach";
        } else {
            return "🔴 PERICOLO - Password trovata in " + breachCount + " data breach! CAMBIALA IMMEDIATAMENTE";
        }
    }
    
    private List<String> generateSuggestions(int breachCount) {
        List<String> suggestions = new ArrayList<>();
        
        if (breachCount > 0) {
            suggestions.add("🚨 Questa password è stata compromessa " + breachCount + " volte");
            suggestions.add("🔒 Cambia immediatamente questa password su tutti i servizi");
            suggestions.add("🎲 Usa il generatore di password per crearne una nuova e sicura");
        } else {
            suggestions.add("✓ Password non presente in database di leak conosciuti");
            suggestions.add("✓ Continua a usare password lunghe e complesse");
        }
        
        return suggestions;
    }
    
    // Classe helper interna
    private static class PasswordWithHash {
        private final String password;
        private final String hash;
        private final String prefix;
        private final String suffix;
        
        public PasswordWithHash(String password, String hash, String prefix, String suffix) {
            this.password = password;
            this.hash = hash;
            this.prefix = prefix;
            this.suffix = suffix;
        }
        
        public String getPassword() { return password; }
        public String getHash() { return hash; }
        public String getPrefix() { return prefix; }
        public String getSuffix() { return suffix; }
    }
}