package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {

    /**
     * Retrieves a list of all rentals that have the passed additional equipment associated to it.
     *
     * @param theEquipment The additional equipment searching in the rentals
     * @return The list of rentals containing this equipment added to it.
     */
    List<Rental> findRentalsByEquipmentsAddedToRentalEquals(AdditionalEquipment theEquipment);
}
