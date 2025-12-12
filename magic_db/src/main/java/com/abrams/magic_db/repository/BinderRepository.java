package com.abrams.magic_db.repository;

import com.abrams.magic_db.model.Binder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BinderRepository extends JpaRepository<Binder, Long>, JpaSpecificationExecutor<Binder> {
    // Find a specific card in a user's binder
    Optional<Binder> findByUserIdAndCardId(Long userId, UUID cardId);

    //Search a user's binder
    Page<Binder> findByUserIdAndCardNameContainingIgnoreCase(Long userId, String name, Pageable pageable);
    
    // Get all cards for a user
    List<Binder> findByUserId(Long userId);
}