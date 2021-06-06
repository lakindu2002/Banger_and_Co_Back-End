package com.lakindu.bangerandcobackend.dto;

public class CreateVehicleDTO {
    private String vehicleName;
    private String fuelType;
    private String licensePlate;
    private String transmission;
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
