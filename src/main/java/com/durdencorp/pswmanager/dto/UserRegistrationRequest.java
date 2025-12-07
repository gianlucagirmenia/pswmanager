package com.durdencorp.pswmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request data")
public class UserRegistrationRequest {

	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	@Schema(description = "Username for the new account", example = "john_doe", minLength = 3, maxLength = 50)
	private String username;

	@NotBlank(message = "Email is required")
	@Email(message = "Email should be valid")
	@Schema(description = "Email address", example = "john@example.com")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must be at least 6 characters long")
	@Schema(description = "Password for the account", example = "MySecurePassword123!", minLength = 6)
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
