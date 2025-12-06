package com.durdencorp.pswmanager.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.durdencorp.pswmanager.model.AuditLog;
import com.durdencorp.pswmanager.repository.AuditLogRepository;

@Controller
public class AdminController {

    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @GetMapping("/admin/audit-logs")
    public String viewAuditLogs(Model model) {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        // Usa il metodo corretto
        List<AuditLog> logs = auditLogRepository.findByTimestampAfter(last24Hours);
        
        model.addAttribute("logs", logs);
        model.addAttribute("totalLogs", logs.size());
        
        // Statistiche
        long failedAttempts = logs.stream()
            .filter(log -> "MASTER_PASSWORD_ATTEMPT".equals(log.getEventType()) && !log.isSuccess())
            .count();
        
        long successfulLogins = logs.stream()
            .filter(log -> "MASTER_PASSWORD_ATTEMPT".equals(log.getEventType()) && log.isSuccess())
            .count();
        
        model.addAttribute("failedAttempts", failedAttempts);
        model.addAttribute("successfulLogins", successfulLogins);
        
        return "admin/audit-logs";
    }
}