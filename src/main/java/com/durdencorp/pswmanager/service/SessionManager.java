package com.durdencorp.pswmanager.service;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.durdencorp.pswmanager.utils.LogUtils;

@Component
public class SessionManager {

	private static final ThreadLocal<SecretKeySpec> currentKey = new ThreadLocal<>();

	public void setMasterPassword(String masterPassword) {
		try {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "=== SESSION MANAGER - Impostazione Master Password ===");

			byte[] key = masterPassword.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // 128 bit per AES
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

			currentKey.set(secretKey);
			LogUtils.logApplication(LogUtils.Level.DEBUG, "Chiave AES impostata nel ThreadLocal");

		} catch (Exception e) {
			LogUtils.logApplication(LogUtils.Level.DEBUG, "ERRORE in setMasterPassword: " + e.getMessage());
			throw new RuntimeException("Errore nella creazione della chiave", e);
		}
	}

	public SecretKeySpec getCurrentKey() {
		SecretKeySpec key = currentKey.get();
		LogUtils.logApplication(LogUtils.Level.DEBUG,
				"SessionManager - Chiave recuperata: " + (key != null ? "PRESENTE" : "NULL"));
		return key;
	}

	public boolean isMasterPasswordSet() {
		boolean isSet = currentKey.get() != null;
		LogUtils.logApplication(LogUtils.Level.DEBUG, "SessionManager - Master password impostata: " + isSet);
		return isSet;
	}

	public void clear() {
		currentKey.remove();
		LogUtils.logApplication(LogUtils.Level.DEBUG, "SessionManager - Chiave pulita");
	}
}