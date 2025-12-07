package com.durdencorp.pswmanager.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class LogUtils {

	private static final Logger APPLICATION_LOGGER = LogManager.getLogger("com.durdencorp.pswmanager");
	private static final Logger SECURITY_LOGGER = LogManager.getLogger("com.durdencorp.pswmanager.security");
	private static final Logger AUDIT_LOGGER = LogManager.getLogger("com.durdencorp.pswmanager.audit");

	public enum Level {
		TRACE, DEBUG, INFO, WARN, ERROR, FATAL
	}

	public static void setupRequestContext(HttpServletRequest request) {
		if (request == null)
			return;

		ThreadContext.put("clientIp", getClientIp(request));
		ThreadContext.put("requestPath", request.getRequestURI());
		ThreadContext.put("requestMethod", request.getMethod());
		ThreadContext.put("userAgent", request.getHeader("User-Agent"));
		ThreadContext.put("sessionId", request.getSession(false) != null ? request.getSession().getId() : "no-session");
	}

	public static void setupAuditContext(String userId, String eventType) {
		ThreadContext.put("userId", userId != null ? userId : "anonymous");
		ThreadContext.put("eventType", eventType != null ? eventType : "UNKNOWN");
	}

	public static void clearContext() {
		ThreadContext.clearAll();
	}

	public static void logApplication(Level level, String message, Object... args) {
		log(APPLICATION_LOGGER, level, message, args);
	}

	public static void logSecurity(Level level, String message, Object... args) {
		log(SECURITY_LOGGER, level, message, args);
	}

	public static void logAudit(String action, String details, boolean success) {
		String status = success ? "SUCCESS" : "FAILURE";
		AUDIT_LOGGER.info("{} | {} | {}", action, status, details);
	}

	public static void logLoginAttempt(String username, boolean success, String ip, String reason) {
		Map<String, Object> logData = new HashMap<>();
		logData.put("event", "LOGIN_ATTEMPT");
		logData.put("username", username);
		logData.put("success", success);
		logData.put("ip", ip);
		logData.put("reason", reason);
		logData.put("timestamp", System.currentTimeMillis());

		SECURITY_LOGGER.info("Login attempt: {}", logData);
	}

	public static void logError(String component, String operation, Throwable error) {
		Map<String, Object> errorData = new HashMap<>();
		errorData.put("component", component);
		errorData.put("operation", operation);
		errorData.put("error", error.getMessage());
		errorData.put("errorType", error.getClass().getName());
		errorData.put("stackTrace", getStackTrace(error));

		APPLICATION_LOGGER.error("Critical error: {}", errorData);
	}

	public static void logPerformance(String operation, long durationMs) {
		if (durationMs > 1000) { // Log solo se > 1 secondo
			APPLICATION_LOGGER.warn("Slow operation: {} took {} ms", operation, durationMs);
		} else if (APPLICATION_LOGGER.isDebugEnabled()) {
			APPLICATION_LOGGER.debug("Operation: {} took {} ms", operation, durationMs);
		}
	}

	private static void log(Logger logger, Level level, String message, Object... args) {
		switch (level) {
		case TRACE -> logger.trace(message, args);
		case DEBUG -> logger.debug(message, args);
		case INFO -> logger.info(message, args);
		case WARN -> logger.warn(message, args);
		case ERROR -> logger.error(message, args);
		case FATAL -> logger.fatal(message, args);
		}
	}

	private static String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader != null && !xfHeader.isEmpty()) {
			return xfHeader.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static String getStackTrace(Throwable error) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : error.getStackTrace()) {
			sb.append(element.toString()).append("\n");
		}
		return sb.toString();
	}
}