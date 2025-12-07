package com.durdencorp.pswmanager.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.durdencorp.pswmanager.service.PasswordGeneratorService;
import com.durdencorp.pswmanager.service.PasswordStrengthService;
import com.durdencorp.pswmanager.utils.LogUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/password-tools")
@Tag(name = "Password Tools", description = "Password generation and analysis tools")
public class PasswordToolsController {

	@Autowired
	private PasswordGeneratorService passwordGeneratorService;

	@Autowired
	private PasswordStrengthService passwordStrengthService;

	@Operation(summary = "Generate a strong password", description = "Generate a cryptographically secure random password")
	@ApiResponse(responseCode = "200", description = "Generated password", content = @Content(schema = @Schema(implementation = String.class)))
	@GetMapping("/generate")
	public String generateStrongPassword() {
		return passwordGeneratorService.generateStrongPassword();
	}

	@PostMapping("/generate-custom")
	public String generateCustomPassword(@RequestBody PasswordGeneratorService.PasswordOptions options) {
		return passwordGeneratorService.generatePassword(options);
	}

	@GetMapping("/analyze")
	public Map<String, Object> analyzePassword(@RequestParam String password) {
		LogUtils.logApplication(LogUtils.Level.INFO, "******* ANALISI PASSWORD *******");
		LogUtils.logApplication(LogUtils.Level.INFO, "Password ricevuta: " + (password != null ? "***" : "null"));

		try {
			PasswordStrengthService.PasswordStrength strength = passwordStrengthService.analyzeStrength(password);
			String tips = passwordStrengthService.getStrengthTips(password);

			LogUtils.logApplication(LogUtils.Level.INFO, "Risultato analisi: " + strength.getDescription());
			LogUtils.logApplication(LogUtils.Level.INFO, "Tips: " + tips);

			Map<String, Object> result = new HashMap<>();
			result.put("description", strength.getDescription());
			result.put("color", strength.getColor());
			result.put("level", strength.getLevel());
			result.put("tips", tips);

			return result;

		} catch (Exception e) {
			LogUtils.logApplication(LogUtils.Level.INFO, "ERRORE nell'analisi: " + e.getMessage());
			e.printStackTrace();

			Map<String, Object> error = new HashMap<>();
			error.put("description", "Errore");
			error.put("color", "red");
			error.put("level", 0);
			error.put("tips", "Errore nell'analisi della password");
			return error;
		}
	}

	public static class PasswordAnalysisResult {
		private PasswordStrengthService.PasswordStrength strength;
		private String tips;

		public PasswordAnalysisResult() {
		}

		public PasswordAnalysisResult(PasswordStrengthService.PasswordStrength strength, String tips) {
			this.strength = strength;
			this.tips = tips;
		}

		public PasswordStrengthService.PasswordStrength getStrength() {
			return strength;
		}

		public void setStrength(PasswordStrengthService.PasswordStrength strength) {
			this.strength = strength;
		}

		public String getTips() {
			return tips;
		}

		public void setTips(String tips) {
			this.tips = tips;
		}
	}
}