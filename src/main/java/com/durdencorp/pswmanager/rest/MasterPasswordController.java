package com.durdencorp.pswmanager.rest;

import com.durdencorp.pswmanager.service.AuditService;
import com.durdencorp.pswmanager.service.PasswordEntryService;
import com.durdencorp.pswmanager.utils.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Tag(name = "Master Password", description = "Master password verification and management")
public class MasterPasswordController {

	@Autowired
	private PasswordEntryService passwordEntryService;

	@Autowired
	private AuditService auditService;

	@GetMapping("/login")
	public String showLoginPage(Model model, HttpServletRequest request, @RequestParam(required = false) String error) {
		LogUtils.setupRequestContext(request);

		if (passwordEntryService.isMasterPasswordSet()) {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "User already logged in, redirecting to home");
			return "redirect:/";
		}

		HttpSession session = request.getSession();

		Boolean rateLimitError = (Boolean) session.getAttribute("rateLimitError");
		Long retryAfterSeconds = (Long) session.getAttribute("retryAfterSeconds");
		String retryAfterFormatted = (String) session.getAttribute("retryAfterFormatted");

		if (Boolean.TRUE.equals(rateLimitError) && retryAfterSeconds != null) {
			model.addAttribute("rateLimitError", true);
			model.addAttribute("retryAfterSeconds", retryAfterSeconds);
			model.addAttribute("retryAfterFormatted", retryAfterFormatted);

			session.removeAttribute("rateLimitError");
			session.removeAttribute("retryAfterSeconds");
			session.removeAttribute("retryAfterFormatted");

			LogUtils.logSecurity(LogUtils.Level.INFO, "Showing rate limit page for IP: {}, retry after: {}",
					getClientIp(request), retryAfterFormatted);
		}

		if ("rate_limit".equals(error) && !model.containsAttribute("rateLimitError")) {
			model.addAttribute("rateLimitError", true);
			model.addAttribute("genericRateLimitMessage", true);
		}

		Integer remainingAttempts = (Integer) request.getAttribute("remainingAttempts");
		if (remainingAttempts != null) {
			model.addAttribute("remainingAttempts", remainingAttempts);
			LogUtils.logApplication(LogUtils.Level.DEBUG, "Remaining attempts for page: {}", remainingAttempts);
		}

		LogUtils.logApplication(LogUtils.Level.INFO, "Login page displayed");
		LogUtils.clearContext();

		return "master-login";
	}

	@Operation(summary = "Verify master password", description = "Check if the provided master password is correct to unlock the application")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Master password is correct, access granted"),
			@ApiResponse(responseCode = "401", description = "Master password is incorrect"),
			@ApiResponse(responseCode = "429", description = "Too many login attempts") })
	@PostMapping("/login")
	public String verifyMasterPassword(@RequestParam String masterPassword, RedirectAttributes redirectAttributes,
			HttpServletRequest request) {

		long startTime = System.currentTimeMillis();
		String clientIp = getClientIp(request);

		LogUtils.setupRequestContext(request);

		try {
			LogUtils.logApplication(LogUtils.Level.INFO, "Login attempt from IP: {}", clientIp);

			auditService.logMasterPasswordAttempt(request, false, "Tentativo di accesso in corso");

			boolean isValid = passwordEntryService.setAndVerifyMasterPassword(masterPassword);

			if (!isValid) {
				auditService.logMasterPasswordAttempt(request, false, "Master password non valida - Tentativo fallito");

				LogUtils.logSecurity(LogUtils.Level.WARN, "Failed login attempt from IP: {}", clientIp);

				redirectAttributes.addFlashAttribute("errorMessage", "Master password non valida.");
				return "redirect:/login";
			}

			auditService.logMasterPasswordAttempt(request, true, "Accesso effettuato con successo");

			LogUtils.logSecurity(LogUtils.Level.INFO, "Successful login from IP: {}", clientIp);

			LogUtils.logApplication(LogUtils.Level.INFO, "User logged in successfully");

			boolean needsSanitization = passwordEntryService.checkAndSanitizeIfNeeded();

			if (needsSanitization) {
				redirectAttributes.addFlashAttribute("successMessage",
						"Accesso effettuato con successo! ✅ Database sanitizzato automaticamente.");
				auditService.logDatabaseEvent(request, true, "Database sanitizzato automaticamente dopo l'accesso");

				LogUtils.logApplication(LogUtils.Level.INFO, "Database auto-sanitized after login");
			} else {
				redirectAttributes.addFlashAttribute("successMessage", "Accesso effettuato con successo!");
			}

			long duration = System.currentTimeMillis() - startTime;
			LogUtils.logPerformance("login_verification", duration);

			return "redirect:/";

		} catch (Exception e) {
			auditService.logMasterPasswordAttempt(request, false, "Errore durante la verifica: " + e.getMessage());

			LogUtils.logError("MasterPasswordController", "verifyMasterPassword", e);

			redirectAttributes.addFlashAttribute("errorMessage",
					"Si è verificato un errore durante l'accesso. Riprova.");

			return "redirect:/login";
		} finally {
			LogUtils.clearContext();
		}
	}

	@GetMapping("/logout")
	public String logout(RedirectAttributes redirectAttributes, HttpServletRequest request) {
		LogUtils.setupRequestContext(request);

		passwordEntryService.clearSession();
		redirectAttributes.addFlashAttribute("successMessage", "Logout effettuato con successo!");

		LogUtils.logSecurity(LogUtils.Level.INFO, "User logged out from IP: {}", getClientIp(request));
		LogUtils.clearContext();

		return "redirect:/login";
	}

	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader != null) {
			return xfHeader.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}