package com.durdencorp.pswmanager.repository;

import com.durdencorp.pswmanager.model.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
	
}