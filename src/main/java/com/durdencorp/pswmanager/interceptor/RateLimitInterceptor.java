package com.durdencorp.pswmanager.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static class RequestTracker {
        int count;
        LocalDateTime windowStart;
        
        RequestTracker() {
            this.count = 1;
            this.windowStart = LocalDateTime.now();
        }
        
        boolean canMakeRequest(int maxRequests, int windowMinutes) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowEnd = windowStart.plusMinutes(windowMinutes);
            
            // Se la finestra temporale è scaduta, resetta
            if (now.isAfter(windowEnd)) {
                count = 1;
                windowStart = now;
                return true;
            }
            
            // Controlla se abbiamo superato il limite
            if (count < maxRequests) {
                count++;
                return true;
            }
            
            return false;
        }
        
        long getSecondsUntilReset(int windowMinutes) {
            LocalDateTime windowEnd = windowStart.plusMinutes(windowMinutes);
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isAfter(windowEnd)) {
                return 0;
            }
            
            return java.time.Duration.between(now, windowEnd).getSeconds();
        }
        
        int getRemainingAttempts(int maxRequests) {
            return Math.max(0, maxRequests - count);
        }
    }
    
    // Storage per i tracker
    private final Map<String, RequestTracker> loginTrackers = new ConcurrentHashMap<>();
    
    // Configurazione da application.yml
    @Value("${rate.limit.master-password.max-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${rate.limit.master-password.window-minutes:15}")
    private int loginWindowMinutes;

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        System.out.println("=== RATE LIMIT INTERCEPTOR ===");
        System.out.println("Path: " + requestPath + " | Method: " + requestMethod + " | IP: " + clientIp);
        
        // Applica rate limiting SOLO per POST a /login (tentativi di login)
        if ("POST".equalsIgnoreCase(requestMethod) && requestPath.equals("/login")) {
            System.out.println("Applicando rate limiting per POST /login");
            return handleLoginRequest(clientIp, request, response);
        }
        
        // Per GET /login, aggiungi remainingAttempts alla request se esiste
        if ("GET".equalsIgnoreCase(requestMethod) && requestPath.equals("/login")) {
            handleGetLoginRequest(clientIp, request);
        }
        
        // Per tutte le altre richieste, permetti
        return true;
    }
    
    private boolean handleLoginRequest(String clientIp, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String trackerKey = "login:" + clientIp;
        
        RequestTracker tracker = loginTrackers.computeIfAbsent(
            trackerKey, k -> new RequestTracker());
        
        System.out.println("Tentativo login #" + tracker.count + " per IP: " + clientIp);
        
        if (tracker.canMakeRequest(maxLoginAttempts, loginWindowMinutes)) {
            // Richiesta permessa
            int remaining = tracker.getRemainingAttempts(maxLoginAttempts);
            System.out.println("✅ Login permesso. Tentativi rimanenti: " + remaining);
            
            // Aggiungi tentativi rimanenti come attributo di request (per il controller)
            request.setAttribute("remainingAttempts", remaining);
            return true;
        } else {
            // Rate limit superato
            long retryAfter = tracker.getSecondsUntilReset(loginWindowMinutes);
            System.out.println("❌ Rate limit SUPERATO per IP: " + clientIp);
            System.out.println("Tentativi: " + tracker.count + " | Bloccato per: " + retryAfter + " secondi");
            
            // Salva nella sessione per mostrare nella pagina
            request.getSession().setAttribute("rateLimitError", true);
            request.getSession().setAttribute("retryAfterSeconds", retryAfter);
            request.getSession().setAttribute("retryAfterFormatted", formatTime(retryAfter));
            
            // Reindirizza alla pagina di login con parametro di errore
            response.sendRedirect(request.getContextPath() + "/login?error=rate_limit");
            return false;
        }
    }
    
    private void handleGetLoginRequest(String clientIp, HttpServletRequest request) {
        String trackerKey = "login:" + clientIp;
        RequestTracker tracker = loginTrackers.get(trackerKey);
        
        if (tracker != null) {
            int remaining = tracker.getRemainingAttempts(maxLoginAttempts);
            System.out.println("GET /login - Tentativi rimanenti per IP " + clientIp + ": " + remaining);
            
            // Aggiungi alla request per il controller
            request.setAttribute("remainingAttempts", remaining);
        }
    }
    
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " secondi";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minuto" + (minutes > 1 ? "i" : "");
        } else {
            long hours = seconds / 3600;
            return hours + " ora" + (hours > 1 ? "e" : "");
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}