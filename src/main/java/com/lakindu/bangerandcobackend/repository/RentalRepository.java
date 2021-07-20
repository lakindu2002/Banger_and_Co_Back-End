package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {
    @Query("SELECT theRental FROM Rental theRental WHERE theRental.returnDate=:returnDate AND theRental.returnTime<:returnTime")
    List<Rental> findRentalsAfterCurrentDateTime(LocalDate returnDate, LocalTime returnTime);
}
