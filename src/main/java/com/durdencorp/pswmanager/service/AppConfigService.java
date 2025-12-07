package com.durdencorp.pswmanager.service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.durdencorp.pswmanager.model.AppConfig;
import com.durdencorp.pswmanager.repository.AppConfigRepository;
import com.durdencorp.pswmanager.utils.LogUtils;

@Service
@Transactional
public class AppConfigService {

	private static final String MASTER_PASSWORD_HASH_KEY = "master_password_hash";

	@Autowired
	private AppConfigRepository appConfigRepository;

	public void saveMasterPasswordHash(String masterPassword) {
		try {
			String hash = hashPassword(masterPassword);
			AppConfig config = new AppConfig(MASTER_PASSWORD_HASH_KEY, hash);
			appConfigRepository.save(config);
			LogUtils.logApplication(LogUtils.Level.INFO, "Hash master password salvato nel database");
		} catch (Exception e) {
			throw new RuntimeException("Errore nel salvataggio dell'hash della master password", e);
		}
	}

	public boolean verifyMasterPassword(String attemptedPassword) {
		try {
			Optional<AppConfig> config = appConfigRepository.findByConfigKey(MASTER_PASSWORD_HASH_KEY);
			if (config.isEmpty()) {
				LogUtils.logApplication(LogUtils.Level.INFO, "Nessuna master password configurata - primo accesso");
				return true; // Primo accesso, qualsiasi password va bene
			}

			String storedHash = config.get().getConfigValue();
			String attemptedHash = hashPassword(attemptedPassword);

			boolean isValid = storedHash.equals(attemptedHash);
			LogUtils.logApplication(LogUtils.Level.INFO,
					"Verifica master password: " + (isValid ? "VALIDA" : "NON VALIDA"));
			return isValid;

		} catch (Exception e) {
			LogUtils.logApplication(LogUtils.Level.INFO,
					"Errore nella verifica della master password: " + e.getMessage());
			return false;
		}
	}

	public boolean isFirstAccess() {
		Optional<AppConfig> config = appConfigRepository.findByConfigKey(MASTER_PASSWORD_HASH_KEY);
		boolean firstAccess = config.isEmpty();
		LogUtils.logApplication(LogUtils.Level.INFO, "Primo accesso: " + firstAccess);
		return firstAccess;
	}

	private String hashPassword(String password) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(password.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(hash);
	}
}