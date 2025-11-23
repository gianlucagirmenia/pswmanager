package com.durdencorp.pswmanager.repository;

import com.durdencorp.pswmanager.model.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    
    List<PasswordEntry> findByTitleContainingIgnoreCase(String title);
    
    List<PasswordEntry> findByUsername(String username);
    
    List<PasswordEntry> findByCategory(String category);
    
    @Query("SELECT p FROM PasswordEntry p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.notes) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<PasswordEntry> searchAllFields(@Param("query") String query);
    
    List<PasswordEntry> findAllByOrderByTitleAsc();
    
    List<PasswordEntry> findAllByOrderByCreatedAtDesc();
}