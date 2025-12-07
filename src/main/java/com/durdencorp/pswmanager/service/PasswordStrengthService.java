package com.durdencorp.pswmanager.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordStrengthService {

	public enum PasswordStrength {
		MOLTO_DEBOLE("🔴 Molto Debole", "#f44336", "#ffcdd2", 1),
		DEBOLE("🟠 Debole", "#ff9800", "#ffe0b2", 2),
		MEDIA("🟡 Media", "#ffc107", "#fff9c4", 3),
		FORTE("🟢 Forte", "#4caf50", "#c8e6c9", 4),
		MOLTO_FORTE("🔵 Molto Forte", "#2196f3", "#bbdefb", 5);

		private final String description;
		private final String color;
		private final String lightColor;
		private final int level;

		PasswordStrength(String description, String color, String lightColor, int level) {
			this.description = description;
			this.color = color;
			this.lightColor = lightColor;
			this.level = level;
		}

		public String getDescription() {
			return description;
		}

		public String getColor() {
			return color;
		}

		public String getLightColor() {
			return lightColor;
		}

		public int getLevel() {
			return level;
		}

		// Metodo per CSS class (da usare nel template)
		public String getCssClass() {
			return "strength-" + name().toLowerCase().replace("_", "-");
		}
	}

	public PasswordStrength analyzeStrength(String password) {
		if (password == null || password.isEmpty()) {
			return PasswordStrength.MOLTO_DEBOLE;
		}

		int score = 0;

		// Lunghezza
		if (password.length() >= 8)
			score++;
		if (password.length() >= 12)
			score++;
		if (password.length() >= 16)
			score++;

		// Complessità
		if (password.matches(".*[a-z].*"))
			score++;
		if (password.matches(".*[A-Z].*"))
			score++;
		if (password.matches(".*[0-9].*"))
			score++;
		if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
			score++;

		// Penalità per pattern comuni
		if (password.matches(".*(.)\\1{2,}.*"))
			score--; // Caratteri ripetuti
		if (password.matches("^[0-9]+$"))
			score--; // Solo numeri
		if (password.matches("^[a-zA-Z]+$"))
			score--; // Solo lettere

		// Assegna il livello in base al punteggio
		if (score <= 2)
			return PasswordStrength.MOLTO_DEBOLE;
		if (score <= 4)
			return PasswordStrength.DEBOLE;
		if (score <= 6)
			return PasswordStrength.MEDIA;
		if (score <= 8)
			return PasswordStrength.FORTE;
		return PasswordStrength.MOLTO_FORTE;
	}

	public String getStrengthTips(String password) {
		StringBuilder tips = new StringBuilder();

		if (password.length() < 8) {
			tips.append("• Usa almeno 8 caratteri\n");
		}
		if (!password.matches(".*[A-Z].*")) {
			tips.append("• Aggiungi lettere maiuscole\n");
		}
		if (!password.matches(".*[a-z].*")) {
			tips.append("• Aggiungi lettere minuscole\n");
		}
		if (!password.matches(".*[0-9].*")) {
			tips.append("• Aggiungi numeri\n");
		}
		if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
			tips.append("• Aggiungi simboli speciali\n");
		}
		if (password.matches(".*(.)\\1{2,}.*")) {
			tips.append("• Evita caratteri ripetuti\n");
		}

		return tips.toString();
	}
}