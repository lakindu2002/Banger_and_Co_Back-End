package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdditionalEquipmentRepository extends JpaRepository<AdditionalEquipment, Integer> {
    /**
     * Retrieve the additional equipment object that is stored in the database for the given name
     *
     * @param equipmentName The name of the item
     * @return The optional checking if item exists or not.
     */
    Optional<AdditionalEquipment> findAdditionalEquipmentByEquipmentName(String equipmentName);

    @Query("FROM AdditionalEquipment theItem WHERE theItem.equipmentName=:name AND theItem.equipmentId<>:id")
    AdditionalEquipment getItemWithSameNameButAsASeperateEntry(String name, int id);
}
