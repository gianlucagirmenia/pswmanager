
package com.durdencorp.pswmanager.service;

import com.durdencorp.pswmanager.model.AuditLog;
import com.durdencorp.pswmanager.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Logga un tentativo di accesso con master password
     */
    public void logMasterPasswordAttempt(HttpServletRequest request, boolean success, String details) {
        AuditLog log = new AuditLog(
            "MASTER_PASSWORD_ATTEMPT",
            getClientIp(request),
            request.getRequestURI(),
            success
        );
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
        
        // Log a console per debugging
        System.out.println(String.format(
            "AUDIT LOG: %s - IP: %s - Success: %s - Details: %s",
            LocalDateTime.now(),
            getClientIp(request),
            success,
            details
        ));
    }
    
    /**
     * Logga eventi relativi al database
     */
    public void logDatabaseEvent(HttpServletRequest request, boolean success, String details) {
        AuditLog log = new AuditLog(
            "DATABASE_OPERATION",
            getClientIp(request),
            request.getRequestURI(),
            success
        );
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
    }
    
    /**
     * Logga tentativi di rate limit superati
     */
    public void logRateLimitExceeded(HttpServletRequest request, String endpoint) {
        AuditLog log = new AuditLog(
            "RATE_LIMIT_EXCEEDED",
            getClientIp(request),
            endpoint,
            false
        );
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDetails("Troppe richieste dall'IP " + getClientIp(request));
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
        
        System.out.println("AUDIT: Rate limit superato per IP: " + getClientIp(request));
    }
    
    /**
     * Logga operazioni sulle password
     */
    public void logPasswordOperation(HttpServletRequest request, String operation, 
                                     boolean success, String details) {
        AuditLog log = new AuditLog(
            "PASSWORD_" + operation.toUpperCase(),
            getClientIp(request),
            request.getRequestURI(),
            success
        );
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
    }
    
    /**
     * Ottiene statistiche di accesso per un IP
     */
    public AccessStats getAccessStats(String clientIp) {
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        LocalDateTime lastDay = LocalDateTime.now().minusDays(1);
        
        long attemptsLastHour = auditLogRepository.countByClientIpAndTimestampAfterAndEventType(
            clientIp, lastHour, "MASTER_PASSWORD_ATTEMPT");
        
        long attemptsLastDay = auditLogRepository.countByClientIpAndTimestampAfterAndEventType(
            clientIp, lastDay, "MASTER_PASSWORD_ATTEMPT");
        
        return new AccessStats(attemptsLastHour, attemptsLastDay);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
    
    // Classe per statistiche di accesso
    public static class AccessStats {
        private final long attemptsLastHour;
        private final long attemptsLastDay;
        
        public AccessStats(long attemptsLastHour, long attemptsLastDay) {
            this.attemptsLastHour = attemptsLastHour;
            this.attemptsLastDay = attemptsLastDay;
        }
        
        public long getAttemptsLastHour() { return attemptsLastHour; }
        public long getAttemptsLastDay() { return attemptsLastDay; }
    }
}