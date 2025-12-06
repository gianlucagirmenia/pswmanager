
package com.durdencorp.pswmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Password strength analysis result")
public class PasswordStrengthResult {

	@Schema(description = "Strength score (0-100)", example = "85")
	private int score;

	@Schema(description = "Strength category", example = "STRONG")
	private String strength;

	@Schema(description = "Suggestions for improvement")
	private String suggestions;

	// Getters e setters
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getStrength() {
		return strength;
	}

	public void setStrength(String strength) {
		this.strength = strength;
	}

	public String getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(String suggestions) {
		this.suggestions = suggestions;
	}
}