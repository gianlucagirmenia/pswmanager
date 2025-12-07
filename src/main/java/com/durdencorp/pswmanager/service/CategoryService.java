package com.durdencorp.pswmanager.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.repository.PasswordEntryRepository;
import com.durdencorp.pswmanager.utils.LogUtils;

@Service
public class CategoryService {

	@Autowired
	private PasswordEntryRepository passwordEntryRepository;

	private static final List<String> DEFAULT_CATEGORIES = Arrays.asList("Generale", "Email", "Social Media", "Lavoro",
			"Banca/Finanza", "Shopping", "Intrattenimento", "Istruzione", "Viaggi", "Salute");

	public List<String> getAllCategories() {
		Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

		categories.addAll(DEFAULT_CATEGORIES);

		List<String> dbCategories = passwordEntryRepository.findAllCategories();
		categories.addAll(dbCategories);

		return new ArrayList<>(categories);
	}

	public Map<String, Long> getCategoryStats() {
		try {
			List<PasswordEntry> allEntries = passwordEntryRepository.findAll();

			return allEntries.stream()
					.collect(Collectors.groupingBy(PasswordEntry::getCategory, TreeMap::new, Collectors.counting()));
		} catch (Exception e) {
			LogUtils.logApplication(LogUtils.Level.DEBUG,
					"Errore nel calcolo delle statistiche categorie: " + e.getMessage());
			return new TreeMap<>();
		}
	}

	public void addCustomCategory(String category) {
		if (category != null && !category.trim().isEmpty()) {
			LogUtils.logApplication(LogUtils.Level.INFO, "Categoria personalizzata aggiunta: " + category);
		}
	}

	public boolean categoryExists(String category) {
		if (category == null || category.trim().isEmpty()) {
			return false;
		}
		return getAllCategories().stream().anyMatch(cat -> cat.equalsIgnoreCase(category.trim()));
	}
}