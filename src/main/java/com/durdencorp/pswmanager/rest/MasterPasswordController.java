package com.durdencorp.pswmanager.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.durdencorp.pswmanager.service.PasswordEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Master Password", description = "Master password verification and management")
public class MasterPasswordController {

	@Autowired
	private PasswordEntryService passwordEntryService;

	// Pagina di login con master password
	@GetMapping("/login")
	public String showLoginPage() {
		if (passwordEntryService.isMasterPasswordSet()) {
			return "redirect:/";
		}
		return "master-login";
	}

	@Operation(summary = "Verify master password", description = "Check if the provided master password is correct to unlock the application")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Master password is correct, access granted"),
			@ApiResponse(responseCode = "401", description = "Master password is incorrect") })
	@PostMapping("/login")
	public String verifyMasterPassword(@RequestParam String masterPassword, RedirectAttributes redirectAttributes) {
		try {
			System.out.println("=== CONTROLLER LOGIN - Verifica Master Password ===");

			boolean isValid = passwordEntryService.setAndVerifyMasterPassword(masterPassword);

			if (!isValid) {
				throw new RuntimeException("Master password non valida");
			}

			// ESECUZIONE AUTOMATICA DELLA SANITIZZAZIONE
			boolean needsSanitization = passwordEntryService.checkAndSanitizeIfNeeded();

			if (needsSanitization) {
				redirectAttributes.addFlashAttribute("successMessage",
						"Accesso effettuato con successo! ✅ Database sanitizzato automaticamente.");
			} else {
				redirectAttributes.addFlashAttribute("successMessage", "Accesso effettuato con successo!");
			}

			return "redirect:/";

		} catch (Exception e) {
			System.out.println("ERRORE nel login: " + e.getMessage());
			redirectAttributes.addFlashAttribute("errorMessage",
					"Master password non valida. Inserisci la password corretta.");
			return "redirect:/login";
		}
	}

	@GetMapping("/logout")
	public String logout(RedirectAttributes redirectAttributes) {
		passwordEntryService.clearSession();
		redirectAttributes.addFlashAttribute("successMessage", "Logout effettuato con successo!");
		return "redirect:/login";
	}
}