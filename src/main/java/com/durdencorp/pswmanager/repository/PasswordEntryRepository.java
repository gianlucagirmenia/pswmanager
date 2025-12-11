package com.durdencorp.pswmanager.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.durdencorp.pswmanager.model.PasswordEntry;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {

	List<PasswordEntry> findByTitleContainingIgnoreCase(String title);

	List<PasswordEntry> findByUsername(String username);

	@Query("SELECT p FROM PasswordEntry p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.notes) LIKE LOWER(CONCAT('%', :query, '%'))")
	List<PasswordEntry> searchAllFields(@Param("query") String query);

	List<PasswordEntry> findAllByOrderByTitleAsc();

	List<PasswordEntry> findAllByOrderByCreatedAtDesc();

	@Query("SELECT new com.durdencorp.pswmanager.model.PasswordEntry(p.id, p.title, p.encryptedPassword) FROM PasswordEntry p WHERE p.encryptedPassword IS NOT NULL")
	List<PasswordEntry> findAllEncrypted();

	List<PasswordEntry> findByCategory(String category);

	List<PasswordEntry> findByCategoryOrderByTitleAsc(String category);

	@Query("SELECT DISTINCT p.category FROM PasswordEntry p WHERE p.category IS NOT NULL ORDER BY p.category")
	List<String> findAllCategories();

	@Query("SELECT p FROM PasswordEntry p WHERE p.category = :category AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.username) LIKE LOWER(CONCAT('%', :query, '%')))")
	List<PasswordEntry> findByCategoryAndSearchQuery(@Param("category") String category, @Param("query") String query);
	
	Page<PasswordEntry> findByCategory(String category, Pageable pageable);
    Page<PasswordEntry> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<PasswordEntry> findByCategoryAndTitleContainingIgnoreCase(String category, String title, Pageable pageable);
    
    long countByCategory(String category);
    
    @Query("SELECT COUNT(p) FROM PasswordEntry p WHERE p.title = :title AND p.username = :username")
    long countByTitleAndUsername(@Param("title") String title, @Param("username") String username);
    
    @Query("SELECT p.id FROM PasswordEntry p WHERE p.title = :title AND p.username = :username")
    Long findIdByTitleAndUsername(@Param("title") String title, @Param("username") String username);
    
    
}