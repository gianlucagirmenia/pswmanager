package com.durdencorp.pswmanager.repository;

import com.durdencorp.pswmanager.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findByTimestampAfter(LocalDateTime timestamp);

	// Query per IP e tipo evento
	@Query("SELECT a FROM AuditLog a WHERE a.clientIp = :clientIp AND a.timestamp > :timestamp")
	List<AuditLog> findByClientIpAndTimestampAfter(@Param("clientIp") String clientIp,
			@Param("timestamp") LocalDateTime timestamp);

	// Query per tipo evento
	@Query("SELECT a FROM AuditLog a WHERE a.eventType = :eventType AND a.timestamp > :timestamp")
	List<AuditLog> findByEventTypeAndTimestampAfter(@Param("eventType") String eventType,
			@Param("timestamp") LocalDateTime timestamp);

	// Conta tentativi per IP, tipo evento e periodo
	@Query("SELECT COUNT(a) FROM AuditLog a WHERE a.clientIp = :clientIp AND a.timestamp > :timestamp AND a.eventType = :eventType")
	long countByClientIpAndTimestampAfterAndEventType(@Param("clientIp") String clientIp,
			@Param("timestamp") LocalDateTime timestamp, @Param("eventType") String eventType);
}