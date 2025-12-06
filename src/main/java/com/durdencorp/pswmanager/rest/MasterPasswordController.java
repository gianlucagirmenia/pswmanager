package com.durdencorp.pswmanager.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.durdencorp.pswmanager.service.AuditService;
import com.durdencorp.pswmanager.service.PasswordEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@Tag(name = "Master Password", description = "Master password verification and management")
public class MasterPasswordController {

	@Autowired
	private PasswordEntryService passwordEntryService;
	
	@Autowired
    private AuditService auditService;

	// Pagina di login con master password
	@GetMapping("/login")
	public String showLoginPage(Model model, HttpServletRequest request,
	                           @RequestParam(required = false) String error) {
	    
	    if (passwordEntryService.isMasterPasswordSet()) {
	        return "redirect:/";
	    }
	    
	    HttpSession session = request.getSession();
	    
	    // 1. Gestisci rate limit error dalla sessione (se l'interceptor ha bloccato)
	    Boolean rateLimitError = (Boolean) session.getAttribute("rateLimitError");
	    Long retryAfterSeconds = (Long) session.getAttribute("retryAfterSeconds");
	    String retryAfterFormatted = (String) session.getAttribute("retryAfterFormatted");
	    
	    if (Boolean.TRUE.equals(rateLimitError) && retryAfterSeconds != null) {
	        model.addAttribute("rateLimitError", true);
	        model.addAttribute("retryAfterSeconds", retryAfterSeconds);
	        model.addAttribute("retryAfterFormatted", retryAfterFormatted);
	        
	        // Pulisci la sessione
	        session.removeAttribute("rateLimitError");
	        session.removeAttribute("retryAfterSeconds");
	        session.removeAttribute("retryAfterFormatted");
	        
	        System.out.println("Mostrando pagina con errore rate limit: " + retryAfterFormatted);
	    }
	    
	    // 2. Controlla anche il parametro URL
	    if ("rate_limit".equals(error) && !model.containsAttribute("rateLimitError")) {
	        model.addAttribute("rateLimitError", true);
	        model.addAttribute("genericRateLimitMessage", true);
	    }
	    
	    // 3. Leggi remainingAttempts dalla request (aggiunto dall'interceptor)
	    Integer remainingAttempts = (Integer) request.getAttribute("remainingAttempts");
	    if (remainingAttempts != null) {
	        model.addAttribute("remainingAttempts", remainingAttempts);
	        System.out.println("Tentativi rimanenti per la pagina: " + remainingAttempts);
	    }
	    
	    return "master-login"; // Il tuo template esistente
	}

	@Operation(summary = "Verify master password", description = "Check if the provided master password is correct to unlock the application")
	@ApiResponses({ 
	    @ApiResponse(responseCode = "200", description = "Master password is correct, access granted"),
	    @ApiResponse(responseCode = "401", description = "Master password is incorrect"),
	    @ApiResponse(responseCode = "429", description = "Too many login attempts") 
	})
	@PostMapping("/login")
	public String verifyMasterPassword(@RequestParam String masterPassword, 
	                                   RedirectAttributes redirectAttributes,
	                                   HttpServletRequest request) {
	    
	    try {
	        String clientIp = getClientIp(request);
	        System.out.println("=== CONTROLLER LOGIN - Tentativo da IP: " + clientIp + " ===");
	        
	        auditService.logMasterPasswordAttempt(request, false, "Tentativo di accesso in corso");

	        boolean isValid = passwordEntryService.setAndVerifyMasterPassword(masterPassword);

	        if (!isValid) {
	            // Logga tentativo fallito
	            auditService.logMasterPasswordAttempt(request, false, 
	                "Master password non valida - Tentativo fallito");
	            
	            System.out.println("❌ Tentativo fallito da IP " + clientIp);
	            
	            redirectAttributes.addFlashAttribute("errorMessage",
	                    "Master password non valida.");
	            return "redirect:/login";
	        }
	        
	        auditService.logMasterPasswordAttempt(request, true, "Accesso effettuato con successo");
	        System.out.println("✅ Accesso RIUSCITO da IP: " + clientIp);

	        // ESECUZIONE AUTOMATICA DELLA SANITIZZAZIONE
	        boolean needsSanitization = passwordEntryService.checkAndSanitizeIfNeeded();

	        if (needsSanitization) {
	            redirectAttributes.addFlashAttribute("successMessage",
	                    "Accesso effettuato con successo! ✅ Database sanitizzato automaticamente.");
	            auditService.logDatabaseEvent(request, true, 
	                    "Database sanitizzato automaticamente dopo l'accesso");
	        } else {
	            redirectAttributes.addFlashAttribute("successMessage", "Accesso effettuato con successo!");
	        }

	        return "redirect:/";

	    } catch (Exception e) {
	        auditService.logMasterPasswordAttempt(request, false, 
	                "Errore durante la verifica: " + e.getMessage());
	        System.out.println("ERRORE nel login: " + e.getMessage());
	        
	        redirectAttributes.addFlashAttribute("errorMessage",
	                "Si è verificato un errore durante l'accesso. Riprova.");
	        
	        return "redirect:/login";
	    }
	}
	
	/**
	 * Ottiene l'IP del client
	 */
	private String getClientIp(HttpServletRequest request) {
	    String xfHeader = request.getHeader("X-Forwarded-For");
	    if (xfHeader != null) {
	        return xfHeader.split(",")[0].trim();
	    }
	    return request.getRemoteAddr();
	}

	@GetMapping("/logout")
	public String logout(RedirectAttributes redirectAttributes) {
		passwordEntryService.clearSession();
		redirectAttributes.addFlashAttribute("successMessage", "Logout effettuato con successo!");
		return "redirect:/login";
	}
}