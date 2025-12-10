package com.abrams.magic_db.repository;

import com.abrams.magic_db.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // The Primary Key of Inventory is a Long (inventoryId)
}