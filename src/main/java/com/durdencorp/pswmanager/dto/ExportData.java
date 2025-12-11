package com.durdencorp.pswmanager.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ExportData {
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime exportDate;
	private int entryCount;
	private String exportVersion = "1.0";
	private List<ExportEntry> entries;

	// Costruttori
	public ExportData() {
	}

	public ExportData(LocalDateTime exportDate, int entryCount, List<ExportEntry> entries) {
		this.exportDate = exportDate;
		this.entryCount = entryCount;
		this.entries = entries;
	}

	// Getters e Setters
	public LocalDateTime getExportDate() {
		return exportDate;
	}

	public void setExportDate(LocalDateTime exportDate) {
		this.exportDate = exportDate;
	}

	public int getEntryCount() {
		return entryCount;
	}

	public void setEntryCount(int entryCount) {
		this.entryCount = entryCount;
	}

	public String getExportVersion() {
		return exportVersion;
	}

	public void setExportVersion(String exportVersion) {
		this.exportVersion = exportVersion;
	}

	public List<ExportEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<ExportEntry> entries) {
		this.entries = entries;
	}
}