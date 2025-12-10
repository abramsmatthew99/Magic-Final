package com.abrams.magic_db.repository;

import com.abrams.magic_db.model.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetRepository extends JpaRepository<Set, String> {
    // Primary Key is String (the set code "neo")
}