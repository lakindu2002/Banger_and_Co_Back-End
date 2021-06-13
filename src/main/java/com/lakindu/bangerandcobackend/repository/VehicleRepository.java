package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Vehicle getVehicleByLicensePlateEquals(String licensePlate);
}
