package com.durdencorp.pswmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token")
public class AuthenticationResponse {
 
 @Schema(
     description = "JWT token for authentication",
     example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 )
 private String token;
 
 @Schema(
     description = "User ID",
     example = "123"
 )
 private Long userId;
 
 @Schema(
     description = "Username",
     example = "john_doe"
 )
 private String username;

 // Costruttori
 public AuthenticationResponse() {}
 
 public AuthenticationResponse(String token, Long userId, String username) {
     this.token = token;
     this.userId = userId;
     this.username = username;
 }

 // Getters e Setters
 public String getToken() { return token; }
 public void setToken(String token) { this.token = token; }
 
 public Long getUserId() { return userId; }
 public void setUserId(Long userId) { this.userId = userId; }
 
 public String getUsername() { return username; }
 public void setUsername(String username) { this.username = username; }
}