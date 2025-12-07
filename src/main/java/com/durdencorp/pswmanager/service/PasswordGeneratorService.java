package com.durdencorp.pswmanager.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PasswordGeneratorService {

	private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	private static final String NUMBERS = "0123456789";
	private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

	private final SecureRandom random = new SecureRandom();

	public String generatePassword(PasswordOptions options) {
		StringBuilder password = new StringBuilder();
		List<String> charCategories = new ArrayList<>();

		if (options.isUppercase()) {
			charCategories.add(UPPERCASE);
			password.append(getRandomChar(UPPERCASE));
		}
		if (options.isLowercase()) {
			charCategories.add(LOWERCASE);
			password.append(getRandomChar(LOWERCASE));
		}
		if (options.isNumbers()) {
			charCategories.add(NUMBERS);
			password.append(getRandomChar(NUMBERS));
		}
		if (options.isSymbols()) {
			charCategories.add(SYMBOLS);
			password.append(getRandomChar(SYMBOLS));
		}

		if (charCategories.isEmpty()) {
			charCategories.add(UPPERCASE + LOWERCASE + NUMBERS);
			password.append(getRandomChar(UPPERCASE + LOWERCASE + NUMBERS));
		}

		String allChars = String.join("", charCategories);
		for (int i = password.length(); i < options.getLength(); i++) {
			password.append(getRandomChar(allChars));
		}

		return shuffleString(password.toString());
	}

	public String generateStrongPassword() {
		PasswordOptions options = new PasswordOptions(16, true, true, true, true);
		return generatePassword(options);
	}

	private char getRandomChar(String characterSet) {
		return characterSet.charAt(random.nextInt(characterSet.length()));
	}

	private String shuffleString(String input) {
		List<Character> characters = new ArrayList<>();
		for (char c : input.toCharArray()) {
			characters.add(c);
		}
		Collections.shuffle(characters);
		StringBuilder result = new StringBuilder();
		for (char c : characters) {
			result.append(c);
		}
		return result.toString();
	}

	public static class PasswordOptions {
		private int length;
		private boolean uppercase;
		private boolean lowercase;
		private boolean numbers;
		private boolean symbols;

		public PasswordOptions(int length, boolean uppercase, boolean lowercase, boolean numbers, boolean symbols) {
			this.length = length;
			this.uppercase = uppercase;
			this.lowercase = lowercase;
			this.numbers = numbers;
			this.symbols = symbols;
		}

		// Getter e Setter
		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public boolean isUppercase() {
			return uppercase;
		}

		public void setUppercase(boolean uppercase) {
			this.uppercase = uppercase;
		}

		public boolean isLowercase() {
			return lowercase;
		}

		public void setLowercase(boolean lowercase) {
			this.lowercase = lowercase;
		}

		public boolean isNumbers() {
			return numbers;
		}

		public void setNumbers(boolean numbers) {
			this.numbers = numbers;
		}

		public boolean isSymbols() {
			return symbols;
		}

		public void setSymbols(boolean symbols) {
			this.symbols = symbols;
		}
	}
}