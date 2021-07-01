package com.lakindu.bangerandcobackend.dto;

import com.lakindu.bangerandcobackend.util.validators.ConstraintChecker;

import javax.validation.constraints.*;

public class VehicleCreateDTO {
    @NotBlank(message = "Please provide a vehicle name")
    @Size(min = 1, max = 125, message = "Please keep vehicle name between 1 and 125 characters")
    private String vehicleName;

    @NotBlank(message = "Please provide a fuel type")
    @ConstraintChecker(allowedConstants = "petrol,diesel,hybrid,electric", message = "The fuel type you passed is not acceptable by Banger and Co.")
    private String fuelType;

    @NotBlank(message = "Please provide a license plate")
    //uk - ^[A-Z]{2}[0-9]{2} [A-Z]{3}$
    //lk - ^[A-Z]{2,3}-[0-9]{4}$
    @Pattern(regexp = "^[A-Z]{2,3}-[0-9]{4}$", message = "Please use the UK license plate format")
    @Size(min = 8, max = 8, message = "License plate should be of 8 characters")
    private String licensePlate;

    @NotBlank(message = "Please provide a transmission for the vehicle")
    @ConstraintChecker(allowedConstants = "manual,automatic,triptonic", message = "The transmission type you passed is not acceptable by Banger and Co.")
    private String transmission;

    @Digits(integer = 3, fraction = 0, message = "Please keep seating capacity to 3 integers")
    private int seatingCapacity;

    @NotNull(message = "Please provide a valid vehicle type to assign vehicle to")
    private int vehicleTypeId;

    public VehicleCreateDTO() {
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public int getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(int vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    @Override
    public String toString() {
        return "CreateVehicleDTO{" +
                "vehicleName='" + vehicleName + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                ", transmission='" + transmission + '\'' +
                ", vehicleTypeId=" + vehicleTypeId +
                '}';
    }
}
