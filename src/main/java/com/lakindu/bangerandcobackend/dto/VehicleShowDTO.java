package com.lakindu.bangerandcobackend.dto;

import java.util.List;

public class VehicleShowDTO {
    private int vehicleId;
    private String licensePlate;
    private String vehicleName;
    private String fuelType;
    private String transmission;
    private int seatingCapacity;
    private byte[] vehicleImage;
    private VehicleTypeDTO vehicleType;
    private List<RentalShowDTO> theRentalsForVehicle; //will be null if users are generally viewing vehicle information
    //will only carry data when a rental related function is executed.


    public VehicleShowDTO() {
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
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

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public byte[] getVehicleImage() {
        return vehicleImage;
    }

    public void setVehicleImage(byte[] vehicleImage) {
        this.vehicleImage = vehicleImage;
    }

    public VehicleTypeDTO getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleTypeDTO vehicleType) {
        this.vehicleType = vehicleType;
    }

    public List<RentalShowDTO> getTheRentalsForVehicle() {
        return theRentalsForVehicle;
    }

    public void setTheRentalsForVehicle(List<RentalShowDTO> theRentalsForVehicle) {
        this.theRentalsForVehicle = theRentalsForVehicle;
    }
}
