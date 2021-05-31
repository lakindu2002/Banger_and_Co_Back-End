package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalEquipmentRepository extends JpaRepository<AdditionalEquipment, Integer> {
}
