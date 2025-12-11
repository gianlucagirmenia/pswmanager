package com.durdencorp.pswmanager.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ImportResult {
	private boolean success;
	private String message;
	private int importedCount;
	private int skippedCount;
	private int errorCount;
	private int duplicateCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime importDate;

	// Costruttori
	public ImportResult() {
		this.importDate = LocalDateTime.now();
	}

	public ImportResult(int imported, int skipped, int errors) {
		this.importedCount = imported;
		this.skippedCount = skipped;
		this.errorCount = errors;
		this.success = errors == 0;
		this.message = errors == 0 ? "Import completato con successo" : "Import completato con errori";
		this.importDate = LocalDateTime.now();
	}

	// Getters e Setters
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getImportedCount() {
		return importedCount;
	}

	public void setImportedCount(int importedCount) {
		this.importedCount = importedCount;
	}

	public int getSkippedCount() {
		return skippedCount;
	}

	public void setSkippedCount(int skippedCount) {
		this.skippedCount = skippedCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public int getDuplicateCount() {
		return duplicateCount;
	}

	public void setDuplicateCount(int duplicateCount) {
		this.duplicateCount = duplicateCount;
	}

	public LocalDateTime getImportDate() {
		return importDate;
	}

	public void setImportDate(LocalDateTime importDate) {
		this.importDate = importDate;
	}

	// Metodi utili
	public void incrementImported() {
		this.importedCount++;
	}

	public void incrementSkipped() {
		this.skippedCount++;
	}

	public void incrementErrors() {
		this.errorCount++;
	}

	public void incrementDuplicates() {
		this.duplicateCount++;
	}

	public int getTotalProcessed() {
		return importedCount + skippedCount + errorCount;
	}
}