package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Integer> {
    VehicleType findVehicleTypeByTypeNameEqualsAndSizeEquals(
            String typeName,
            String size
    );
}
