package com.durdencorp.pswmanager.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_entries")
public class PasswordEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String username;

	@Column(name = "encrypted_password", nullable = false, length = 500)
	private String encryptedPassword;

	@Column(length = 1000)
	private String url;

	@Column(length = 2000)
	private String notes;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "category", nullable = false)
	private String category = "Generale";

	public PasswordEntry() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.category = "Generale";
	}

	public PasswordEntry(String title, String username, String encryptedPassword) {
		this();
		this.title = title;
		this.username = username;
		this.encryptedPassword = encryptedPassword;
	}

	public PasswordEntry(String title, String username, String encryptedPassword, String url, String notes) {
		this();
		this.title = title;
		this.username = username;
		this.encryptedPassword = encryptedPassword;
		this.url = url;
		this.notes = notes;
	}

	public PasswordEntry(String title, String username, String encryptedPassword, String url, String notes,
			String category) {
		this();
		this.title = title;
		this.username = username;
		this.encryptedPassword = encryptedPassword;
		this.url = url;
		this.notes = notes;
		this.category = category;
	}

	public PasswordEntry(Long id, String title, String encryptedPassword) {
		this.id = id;
		this.title = title;
		this.encryptedPassword = encryptedPassword;
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
		this.updatedAt = LocalDateTime.now();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		this.updatedAt = LocalDateTime.now();
	}

	public String getEncryptedPassword() {
		return encryptedPassword;
	}

	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
		this.updatedAt = LocalDateTime.now();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		this.updatedAt = LocalDateTime.now();
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
		this.updatedAt = LocalDateTime.now();
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category != null ? category : "Generale";
		this.updatedAt = LocalDateTime.now();
	}

	// Metodo pre-update
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// toString per debugging
	@Override
	public String toString() {
		return "PasswordEntry{" + "id=" + id + ", title='" + title + '\'' + ", username='" + username + '\''
				+ ", encryptedPassword='" + encryptedPassword + '\'' + ", url='" + url + '\'' + ", notes='" + notes
				+ '\'' + ", category='" + category + '\'' + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ '}';
	}
}