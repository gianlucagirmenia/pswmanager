package com.durdencorp.pswmanager.dto;

public class PasswordEntryForm {
	private Long id;
	private String title;
	private String username;
	private String plainPassword;
	private String url;
	private String notes;
	private String category;

	public PasswordEntryForm() {
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

	public String getPlainPassword() {
		return plainPassword;
	}

	public void setPlainPassword(String plainPassword) {
		this.plainPassword = plainPassword;
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

	@Override
	public String toString() {
		return "PasswordEntryForm [id=" + id + ", title=" + title + ", username=" + username + ", plainPassword="
				+ plainPassword + ", url=" + url + ", notes=" + notes + ", category=" + category + "]";
	}

}
