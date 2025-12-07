package com.durdencorp.pswmanager.service;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.durdencorp.pswmanager.utils.LogUtils;

@Component
public class MasterPasswordEncryption {

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

	@Autowired
	private SessionService sessionService;

	@Autowired
	private AppConfigService appConfigService;

	private byte[] generateIV() {
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[16];
		random.nextBytes(iv);
		return iv;
	}

	public boolean setAndVerifyMasterPassword(String masterPassword) {
		try {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "=== VERIFICA MASTER PASSWORD ===");

			boolean isValid = appConfigService.verifyMasterPassword(masterPassword);
			if (!isValid) {
				LogUtils.logApplication(LogUtils.Level.DEBUG, "Master password NON valida");
				return false;
			}

			if (appConfigService.isFirstAccess()) {
				appConfigService.saveMasterPasswordHash(masterPassword);
				LogUtils.logApplication(LogUtils.Level.DEBUG, "Primo accesso - Hash master password salvato");
			}

			boolean success = sessionService.setMasterPassword(masterPassword);
			if (!success) {
				LogUtils.logApplication(LogUtils.Level.DEBUG, "Impossibile impostare la chiave di cifratura");
				return false;
			}

			LogUtils.logApplication(LogUtils.Level.DEBUG, "Master password verificata e impostata con successo");
			return true;

		} catch (Exception e) {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "ERRORE in setAndVerifyMasterPassword: " + e.getMessage());
			return false;
		}
	}

	public boolean isMasterPasswordSet() {
		return sessionService.isMasterPasswordSet();
	}

	public String encrypt(String data) {
		try {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "=== ENCRYPT ===");
			LogUtils.logApplication(LogUtils.Level.DEBUG,
					"Dati da cifrare: '" + data + "' (lunghezza: " + (data != null ? data.length() : 0) + ")");

			if (data == null)
				return null;
			if (!isMasterPasswordSet()) {
				throw new IllegalStateException("Master password non impostata");
			}

			SecretKeySpec secretKey = sessionService.getCurrentKey();
			if (secretKey == null) {
				throw new IllegalStateException("Chiave di cifratura non disponibile");
			}

			byte[] iv = generateIV();
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));

			byte[] combined = new byte[iv.length + encryptedBytes.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

			String result = Base64.getEncoder().encodeToString(combined);

			LogUtils.logApplication(LogUtils.Level.DEBUG, "Cifratura COMPLETATA: '" + data + "' -> "
					+ result.substring(0, Math.min(20, result.length())) + "...");
			return result;

		} catch (Exception e) {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "ERRORE in encrypt: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Errore nella cifratura: " + e.getMessage(), e);
		}
	}

	public String decrypt(String encryptedData) {
		try {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "=== DECRYPT ===");
			LogUtils.logApplication(LogUtils.Level.DEBUG,
					"Dati da decifrare: " + (encryptedData != null
							? encryptedData.substring(0, Math.min(20, encryptedData.length())) + "..."
							: "null"));

			if (encryptedData == null)
				return null;
			if (!isMasterPasswordSet()) {
				throw new IllegalStateException("Master password non impostata");
			}

			if (!isValidBase64(encryptedData)) {
				LogUtils.logApplication(LogUtils.Level.DEBUG,
						"ERRORE: Dati non in formato Base64 valido: " + encryptedData);
				throw new IllegalArgumentException("Dati cifrati non validi - formato Base64 corrotto");
			}

			SecretKeySpec secretKey = sessionService.getCurrentKey();
			if (secretKey == null) {
				throw new IllegalStateException("Chiave di cifratura non disponibile");
			}

			byte[] combined = Base64.getDecoder().decode(encryptedData);

			if (combined.length < 16) {
				throw new IllegalArgumentException("Dati cifrati troppo corti per contenere IV");
			}

			byte[] iv = new byte[16];
			byte[] encryptedBytes = new byte[combined.length - 16];
			System.arraycopy(combined, 0, iv, 0, 16);
			System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);

			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			String result = new String(decryptedBytes, "UTF-8");

			LogUtils.logApplication(LogUtils.Level.DEBUG, "Decifratura COMPLETATA: -> '" + result + "'");
			return result;

		} catch (Exception e) {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "ERRORE CRITICO in decrypt: " + e.getMessage());
			LogUtils.logApplication(LogUtils.Level.DEBUG,
					"Dati problematici: " + (encryptedData != null ? encryptedData : "null"));

			throw new RuntimeException("Errore irreversibile nella decifratura: " + e.getMessage(), e);
		}
	}

	private boolean isValidBase64(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}

		String base64Pattern = "^[A-Za-z0-9+/]*={0,2}$";
		if (!str.matches(base64Pattern)) {
			return false;
		}

		if (str.length() % 4 != 0) {
			return false;
		}

		return true;
	}

	public void clear() {
		sessionService.clear();
		LogUtils.logApplication(LogUtils.Level.DEBUG, "MasterPasswordEncryption - Sessione pulita");
	}
}