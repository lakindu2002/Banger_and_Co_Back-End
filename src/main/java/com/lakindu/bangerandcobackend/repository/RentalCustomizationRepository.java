package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.RentalCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalCustomizationRepository extends JpaRepository<RentalCustomization, Integer> {
    void deleteRentalCustomizationsByTheRentalInformationEquals(Rental theRental);
}
