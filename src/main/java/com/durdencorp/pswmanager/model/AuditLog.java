// src/main/java/com/durdencorp/pswmanager/model/AuditLog.java
package com.durdencorp.pswmanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_type", nullable = false)
	private String eventType; // MASTER_PASSWORD_ATTEMPT, DATABASE_OPERATION, etc.

	@Column(name = "client_ip")
	private String clientIp;

	@Column(name = "user_agent", length = 500)
	private String userAgent;

	@Column(name = "request_path")
	private String requestPath;

	@Column(name = "details", length = 2000)
	private String details;

	@Column(name = "timestamp", nullable = false)
	private LocalDateTime timestamp;

	@Column(name = "success")
	private boolean success;

	// Costruttori
	public AuditLog() {
		this.timestamp = LocalDateTime.now();
	}

	public AuditLog(String eventType, String clientIp, String requestPath, boolean success) {
		this();
		this.eventType = eventType;
		this.clientIp = clientIp;
		this.requestPath = requestPath;
		this.success = success;
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}