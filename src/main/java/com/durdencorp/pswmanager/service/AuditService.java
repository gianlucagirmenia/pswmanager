package com.durdencorp.pswmanager.service;

import com.durdencorp.pswmanager.model.AuditLog;
import com.durdencorp.pswmanager.repository.AuditLogRepository;
import com.durdencorp.pswmanager.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public void logMasterPasswordAttempt(HttpServletRequest request, boolean success, String details) {
        String clientIp = getClientIp(request);
        
        LogUtils.setupAuditContext("master_password", "MASTER_PASSWORD_ATTEMPT");
        LogUtils.logAudit("MASTER_PASSWORD_ATTEMPT", 
            String.format("IP: %s, Success: %s, Details: %s", clientIp, success, details), 
            success);
        
        LogUtils.logLoginAttempt("master_password", success, clientIp, details);
        
        AuditLog log = new AuditLog(
            "MASTER_PASSWORD_ATTEMPT",
            clientIp,
            request.getRequestURI(),
            success
        );
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
    }
    
    public void logDatabaseEvent(HttpServletRequest request, boolean success, String details) {
        String clientIp = getClientIp(request);
        
        LogUtils.setupAuditContext("system", "DATABASE_OPERATION");
        LogUtils.logAudit("DATABASE_OPERATION", 
            String.format("IP: %s, Success: %s, Details: %s", clientIp, success, details), 
            success);
        
        AuditLog log = new AuditLog(
            "DATABASE_OPERATION",
            clientIp,
            request.getRequestURI(),
            success
        );
        log.setDetails(details);
        auditLogRepository.save(log);
    }
    
    public void logRateLimitExceeded(HttpServletRequest request, String endpoint) {
        String clientIp = getClientIp(request);
        
        LogUtils.setupAuditContext(clientIp, "RATE_LIMIT_EXCEEDED");
        LogUtils.logAudit("RATE_LIMIT_EXCEEDED", 
            String.format("IP %s exceeded rate limit on %s", clientIp, endpoint), 
            false);
        
        LogUtils.logSecurity(LogUtils.Level.WARN, 
            "Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
        
        AuditLog log = new AuditLog(
            "RATE_LIMIT_EXCEEDED",
            clientIp,
            endpoint,
            false
        );
        log.setDetails("Too many requests from this IP");
        auditLogRepository.save(log);
    }
    
    public void logPasswordOperation(HttpServletRequest request, String operation, 
                                     boolean success, String details) {
        String clientIp = getClientIp(request);
        
        LogUtils.setupAuditContext(clientIp, "PASSWORD_" + operation.toUpperCase());
        LogUtils.logAudit("PASSWORD_OPERATION", 
            String.format("Operation: %s, IP: %s, Success: %s, Details: %s", 
                operation, clientIp, success, details), 
            success);
        
        AuditLog log = new AuditLog(
            "PASSWORD_" + operation.toUpperCase(),
            clientIp,
            request.getRequestURI(),
            success
        );
        log.setDetails(details);
        auditLogRepository.save(log);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}