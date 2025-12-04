package com.durdencorp.pswmanager.service.security;

import java.util.List;

public class BreachCheckResult {
    private final boolean compromised;
    private final int breachCount;
    private final String message;
    private final List<String> suggestions;
    private final boolean error;
    
    public BreachCheckResult(boolean compromised, int breachCount, 
                           String message, List<String> suggestions) {
        this.compromised = compromised;
        this.breachCount = breachCount;
        this.message = message;
        this.suggestions = suggestions;
        this.error = false;
    }
    
    private BreachCheckResult(boolean error) {
        this.compromised = false;
        this.breachCount = 0;
        this.message = "⚠️ Impossibile verificare la password (errore di connessione)";
        this.suggestions = List.of("Controlla la tua connessione internet", 
                                  "Riprova più tardi");
        this.error = error;
    }
    
    public static BreachCheckResult error() {
        return new BreachCheckResult(true);
    }
    
    // Getters
    public boolean isCompromised() { return compromised; }
    public int getBreachCount() { return breachCount; }
    public String getMessage() { return message; }
    public List<String> getSuggestions() { return suggestions; }
    public boolean isError() { return error; }
    
    public String getRiskLevel() {
        if (error) return "UNKNOWN";
        if (breachCount == 0) return "LOW";
        if (breachCount < 10) return "MEDIUM";
        if (breachCount < 100) return "HIGH";
        return "CRITICAL";
    }
    
    public String getIcon() {
        if (error) return "⚠️";
        if (breachCount == 0) return "✅";
        if (breachCount < 10) return "🟡";
        if (breachCount < 100) return "🟠";
        return "🔴";
    }
}