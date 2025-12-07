package com.durdencorp.pswmanager.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.durdencorp.pswmanager.dto.PasswordEntryDTO;
import com.durdencorp.pswmanager.dto.PasswordEntryForm;
import com.durdencorp.pswmanager.model.PasswordEntry;
import com.durdencorp.pswmanager.service.PasswordEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/passwords")
@CrossOrigin(origins = "http://localhost:6969")
@Tag(name = "Password Management", description = "APIs for managing password entries")
@SecurityRequirement(name = "bearerAuth")
public class PasswordEntryController {

	@Autowired
	private PasswordEntryService passwordEntryService;

	@Operation(summary = "Get all passwords for current user", description = "Retrieve all password entries for the authenticated user")
	@ApiResponse(responseCode = "200", description = "List of password entries retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PasswordEntry.class))))
	@GetMapping
	public ResponseEntity<List<PasswordEntryDTO>> getAllPasswords() {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			List<PasswordEntryDTO> passwords = passwordEntryService.findAll();
			return ResponseEntity.ok(passwords);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "Get password by ID", description = "Retrieve a specific password entry by its ID")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Password entry found", content = @Content(schema = @Schema(implementation = PasswordEntry.class))),
			@ApiResponse(responseCode = "404", description = "Password entry not found"),
			@ApiResponse(responseCode = "403", description = "Not authorized to access this password") })
	@GetMapping("/{id}")
	public ResponseEntity<PasswordEntryDTO> getPasswordById(@PathVariable Long id) {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			List<PasswordEntryDTO> allPasswords = passwordEntryService.findAll();
			PasswordEntryDTO password = allPasswords.stream().filter(p -> p.getId().equals(id)).findFirst()
					.orElse(null);

			if (password != null) {
				return ResponseEntity.ok(password);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "Create new password entry", description = "Add a new password entry for the current user")
	@ApiResponse(responseCode = "200", description = "Password entry created successfully", content = @Content(schema = @Schema(implementation = PasswordEntry.class)))
	@PostMapping
	public ResponseEntity<Long> createPassword(@RequestBody PasswordEntryForm form) {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			Long savedId = passwordEntryService.save(form);
			return ResponseEntity.ok(savedId);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<Long> updatePassword(@PathVariable Long id, @RequestBody PasswordEntryForm form) {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			form.setId(id);
			Long updatedId = passwordEntryService.save(form);
			return ResponseEntity.ok(updatedId);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePassword(@PathVariable Long id) {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			passwordEntryService.deleteById(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/search")
	public ResponseEntity<List<PasswordEntryDTO>> searchByTitle(@RequestParam String title) {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			List<PasswordEntryDTO> results = passwordEntryService.searchByTitle(title);
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/category/{category}")
	public ResponseEntity<List<PasswordEntryDTO>> getByCategory(@PathVariable String category) {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			List<PasswordEntryDTO> results = passwordEntryService.findByCategory(category);
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/category/{category}/search")
	public ResponseEntity<List<PasswordEntryDTO>> searchInCategory(@PathVariable String category,
			@RequestParam String query) {
		try {
			if (!passwordEntryService.isMasterPasswordSet()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			List<PasswordEntryDTO> results = passwordEntryService.findByCategoryAndSearch(category, query);
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}