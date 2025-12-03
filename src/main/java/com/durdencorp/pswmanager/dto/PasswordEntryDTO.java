package com.durdencorp.pswmanager.dto;

import java.time.LocalDateTime;

public class PasswordEntryDTO {
	private Long id;
	private String title;
	private String username;
	private String decryptedPassword; // Solo per visualizzazione
	private String url;
	private String notes;
	private String category;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public PasswordEntryDTO() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDecryptedPassword() {
		return decryptedPassword;
	}

	public void setDecryptedPassword(String decryptedPassword) {
		this.decryptedPassword = decryptedPassword;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "PasswordEntryDTO [id=" + id + ", title=" + title + ", username=" + username + ", decryptedPassword="
				+ decryptedPassword + ", url=" + url + ", notes=" + notes + ", category=" + category + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}

}