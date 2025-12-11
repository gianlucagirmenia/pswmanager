package com.durdencorp.pswmanager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.durdencorp.pswmanager.dto.PasswordEntryDTO;
import com.durdencorp.pswmanager.dto.PasswordEntryForm;
import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.repository.PasswordEntryRepository;
import com.durdencorp.pswmanager.service.security.BreachCheckResult;
import com.durdencorp.pswmanager.service.security.HibpService;
import com.durdencorp.pswmanager.utils.LogUtils;

@Service
@Transactional
public class PasswordEntryService {

	@Autowired
	private PasswordEntryRepository repository;

	@Autowired
	private MasterPasswordEncryption encryptionUtil;

	@Autowired
	private AppConfigService appConfigService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private HibpService hibpService;

	public boolean setAndVerifyMasterPassword(String masterPassword) {
		return encryptionUtil.setAndVerifyMasterPassword(masterPassword);
	}

	public boolean isFirstAccess() {
		return appConfigService.isFirstAccess();
	}

	public boolean isMasterPasswordSet() {
		return encryptionUtil.isMasterPasswordSet();
	}

	public Long save(PasswordEntryForm form) {
		PasswordEntry entity;

		if (form.getId() != null) {
			entity = repository.findById(form.getId())
					.orElseThrow(() -> new IllegalArgumentException("Record non trovato"));
		} else {
			entity = new PasswordEntry();
		}

		entity.setTitle(form.getTitle());
		entity.setUsername(form.getUsername());
		entity.setUrl(form.getUrl());
		entity.setNotes(form.getNotes());
		entity.setCategory(form.getCategory());

		if (form.getPlainPassword() != null && !form.getPlainPassword().isEmpty()) {
			String encrypted = encryptionUtil.encrypt(form.getPlainPassword());
			entity.setEncryptedPassword(encrypted);
		} else if (form.getId() == null) {
			throw new IllegalArgumentException("La password è obbligatoria per nuovi record");
		}

		PasswordEntry saved = repository.save(entity);
		return saved.getId();
	}

	@Transactional(readOnly = true)
	public List<PasswordEntryDTO> findAll() {
		List<PasswordEntry> entities = repository.findAll();
		return entities.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	private PasswordEntryDTO convertToDTO(PasswordEntry entity) {
		PasswordEntryDTO dto = new PasswordEntryDTO();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setUsername(entity.getUsername());
		dto.setUrl(entity.getUrl());
		dto.setNotes(entity.getNotes());
		dto.setCategory(entity.getCategory());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setUpdatedAt(entity.getUpdatedAt());

		try {
			String decrypted = encryptionUtil.decrypt(entity.getEncryptedPassword());
			dto.setDecryptedPassword(decrypted);
		} catch (Exception e) {
			dto.setDecryptedPassword("[ERRORE DECIFRATURA]");
		}

		return dto;
	}

	@Transactional(readOnly = true)
	public PasswordEntryForm findByIdForEdit(Long id) {
		PasswordEntry entity = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Record non trovato"));

		PasswordEntryForm form = new PasswordEntryForm();
		form.setId(entity.getId());
		form.setTitle(entity.getTitle());
		form.setUsername(entity.getUsername());
		form.setUrl(entity.getUrl());
		form.setNotes(entity.getNotes());
		form.setCategory(entity.getCategory());

		try {
			String decrypted = encryptionUtil.decrypt(entity.getEncryptedPassword());
			form.setPlainPassword(decrypted);
		} catch (Exception e) {
			form.setPlainPassword("");
			LogUtils.logApplication(LogUtils.Level.DEBUG, "ERRORE decifratura per ID " + id + ": " + e.getMessage());
		}

		return form;
	}

	public String testEncryption(String data) {
		return encryptionUtil.encrypt(data);
	}

	public String testDecryption(String encryptedData) {
		return encryptionUtil.decrypt(encryptedData);
	}

	public void deleteById(Long id) {
		repository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public List<PasswordEntryDTO> searchByTitle(String title) {
		if (!isMasterPasswordSet()) {
			throw new IllegalStateException("Master password non impostata");
		}

		List<PasswordEntry> entries = repository.findByTitleContainingIgnoreCase(title);
		return entries.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	public void clearSession() {
		encryptionUtil.clear();
	}

	@Transactional(readOnly = true)
	public List<PasswordEntryDTO> findByCategory(String category) {
		if (!isMasterPasswordSet()) {
			throw new IllegalStateException("Master password non impostata");
		}

		List<PasswordEntry> entries = repository.findByCategory(category);
		return entries.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	public List<String> getAllCategories() {
		return categoryService.getAllCategories();
	}

	public Map<String, Long> getCategoryStats() {
		return categoryService.getCategoryStats();
	}

	@Transactional(readOnly = true)
	public List<PasswordEntryDTO> findByCategoryAndSearch(String category, String query) {
		if (!isMasterPasswordSet()) {
			throw new IllegalStateException("Master password non impostata");
		}

		List<PasswordEntry> entries = repository.findByCategoryAndSearchQuery(category, query);
		return entries.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Transactional
	public void sanitizeAndReencryptAll() {
		LogUtils.logApplication(LogUtils.Level.DEBUG, "=== SANITIZZAZIONE AUTOMATICA DI TUTTI I RECORD ===");

		List<PasswordEntry> allEntries = repository.findAll();
		int fixedCount = 0;
		int alreadyEncryptedCount = 0;
		int errorCount = 0;

		for (PasswordEntry entry : allEntries) {
			String currentPassword = entry.getEncryptedPassword();

			if (currentPassword != null && !currentPassword.isEmpty()) {
				try {
					encryptionUtil.decrypt(currentPassword);
					alreadyEncryptedCount++;

				} catch (Exception e) {
					try {
						String encryptedPassword = encryptionUtil.encrypt(currentPassword);
						entry.setEncryptedPassword(encryptedPassword);
						repository.save(entry);
						fixedCount++;

						LogUtils.logApplication(LogUtils.Level.DEBUG,
								"✅ Sanitizzato: " + entry.getTitle() + " (ID: " + entry.getId() + ")");

					} catch (Exception encryptionError) {
						errorCount++;
						LogUtils.logApplication(LogUtils.Level.DEBUG, "❌ Errore nella sanitizzazione di "
								+ entry.getTitle() + ": " + encryptionError.getMessage());
					}
				}
			}
		}

		repository.flush();

		LogUtils.logApplication(LogUtils.Level.DEBUG, "=== REPORT SANITIZZAZIONE ===");
		LogUtils.logApplication(LogUtils.Level.DEBUG, "Record totali: " + allEntries.size());
		LogUtils.logApplication(LogUtils.Level.DEBUG, "✅ Già cifrati correttamente: " + alreadyEncryptedCount);
		LogUtils.logApplication(LogUtils.Level.DEBUG, "🔄 Ricifrati: " + fixedCount);
		LogUtils.logApplication(LogUtils.Level.DEBUG, "❌ Errori: " + errorCount);
		LogUtils.logApplication(LogUtils.Level.DEBUG, "=== FINE REPORT ===");
	}

	@Transactional
	public boolean checkAndSanitizeIfNeeded() {
		LogUtils.logApplication(LogUtils.Level.DEBUG, "=== VERIFICA AUTOMATICA SANITIZZAZIONE ===");

		if (!isMasterPasswordSet()) {
			throw new IllegalStateException("Master password non impostata");
		}

		List<PasswordEntry> allEntries = repository.findAll();
		boolean foundClearText = false;

		for (PasswordEntry entry : allEntries) {
			String currentPassword = entry.getEncryptedPassword();

			if (currentPassword != null && !currentPassword.isEmpty()) {
				try {
					encryptionUtil.decrypt(currentPassword);
				} catch (Exception e) {
					foundClearText = true;
					break;
				}
			}
		}

		if (foundClearText) {
			LogUtils.logApplication(LogUtils.Level.DEBUG,
					"Trovate password in chiaro, eseguo sanitizzazione automatica");
			sanitizeAndReencryptAll();
			return true;
		}

		LogUtils.logApplication(LogUtils.Level.DEBUG,
				"Nessuna password in chiaro trovata, sanitizzazione non necessaria");
		return false;
	}

	/**
	 * Controlla una singola password contro HIBP
	 */
	public BreachCheckResult checkPasswordBreach(String password) {
		return hibpService.checkPassword(password);
	}

	/**
	 * Controlla TUTTE le password nel database
	 */
	@Transactional(readOnly = true)
	public List<PasswordBreachReport> checkAllPasswordsForBreaches() {
		List<PasswordEntry> allEntries = repository.findAll();
		List<PasswordBreachReport> reports = new ArrayList<>();

		List<String> passwords = allEntries.stream().map(entry -> {
			try {
				return encryptionUtil.decrypt(entry.getEncryptedPassword());
			} catch (Exception e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());

		Map<String, BreachCheckResult> results = hibpService.checkPasswords(passwords);

		for (PasswordEntry entry : allEntries) {
			try {
				String decryptedPassword = encryptionUtil.decrypt(entry.getEncryptedPassword());
				BreachCheckResult result = results.get(decryptedPassword);

				if (result != null) {
					reports.add(new PasswordBreachReport(entry.getId(), entry.getTitle(), entry.getUsername(),
							entry.getUrl(), result));
				}

			} catch (Exception e) {
				// Ignora errori di decifratura
			}
		}

		return reports;
	}

	/**
	 * Trova password compromesse
	 */
	@Transactional(readOnly = true)
	public List<PasswordBreachReport> findCompromisedPasswords() {
		return checkAllPasswordsForBreaches().stream().filter(report -> report.getBreachCheckResult().isCompromised())
				.collect(Collectors.toList());
	}
	
	public Page<PasswordEntryDTO> findAllPaginated(Pageable pageable) {
        Page<PasswordEntry> page = repository.findAll(pageable);
        return page.map(this::convertToDTO);
    }
	
	public Page<PasswordEntryDTO> findByCategoryPaginated(String category, Pageable pageable) {
        Page<PasswordEntry> page = repository.findByCategory(category, pageable);
        return page.map(this::convertToDTO);
    }
	
	public Page<PasswordEntryDTO> searchPaginated(String query, Pageable pageable) {
        Page<PasswordEntry> page = repository.findByTitleContainingIgnoreCase(query, pageable);
        return page.map(this::convertToDTO);
    }
	
	public Page<PasswordEntryDTO> findByCategoryAndSearchPaginated(String category, String query, Pageable pageable) {
        Page<PasswordEntry> page = repository.findByCategoryAndTitleContainingIgnoreCase(category, query, pageable);
        return page.map(this::convertToDTO);
    }
	
	public long countAll() {
        return repository.count();
    }
	
	public long countByCategory(String category) {
        return repository.countByCategory(category);
    }

	public static class PasswordBreachReport {
		private final Long entryId;
		private final String title;
		private final String username;
		private final String url;
		private final BreachCheckResult breachCheckResult;

		public PasswordBreachReport(Long entryId, String title, String username, String url,
				BreachCheckResult breachCheckResult) {
			this.entryId = entryId;
			this.title = title;
			this.username = username;
			this.url = url;
			this.breachCheckResult = breachCheckResult;
		}

		public Long getEntryId() {
			return entryId;
		}

		public String getTitle() {
			return title;
		}

		public String getUsername() {
			return username;
		}

		public String getUrl() {
			return url;
		}

		public BreachCheckResult getBreachCheckResult() {
			return breachCheckResult;
		}

		public String getRiskColor() {
			switch (breachCheckResult.getRiskLevel()) {
			case "CRITICAL":
				return "#dc3545"; // Rosso
			case "HIGH":
				return "#fd7e14"; // Arancione
			case "MEDIUM":
				return "#ffc107"; // Giallo
			case "LOW":
				return "#28a745"; // Verde
			default:
				return "#6c757d"; // Grigio
			}
		}
	}

}