package com.lakindu.bangerandcobackend.dto;

import com.lakindu.bangerandcobackend.util.validators.ConstraintChecker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CreateVehicleDTO {
    @NotBlank(message = "Please provide a vehicle name")
    @Size(min = 1, max = 255, message = "Please keep vehicle name between 1 and 255 characters")
    private String vehicleName;

    @NotBlank(message = "Please provide a fuel type")
    @ConstraintChecker(allowedConstants = "petrol,diesel,hybrid,electric", message = "The fuel type you passed is not acceptable by Banger and Co.")
    private String fuelType;

    @NotBlank(message = "Please provide a license plate")
    //" " between regex denote the SPACE in UK License Plate Format.
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2} [A-Z]{3}$", message = "Please use the UK license plate format")
    @Size(min = 8, max = 8, message = "License plate should be of 8 characters")
    private String licensePlate;

    @NotBlank(message = "Please provide a transmission for the vehicle")
    @ConstraintChecker(allowedConstants = "manual,automatic,triptonic", message = "The transmission type you passed is not acceptable by Banger and Co.")
    private String transmission;

    @NotNull(message = "Please provide a valid vehicle type to assign vehicle to")
    private int vehicleTypeId;

    public CreateVehicleDTO() {
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
