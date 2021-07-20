package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class VehicleUpdateDTO {

    @NotNull(message = "Please provide a vehicle ID")
    private int vehicleId;

    @NotBlank(message = "Please provide a vehicle name")
    @Size(min = 1, max = 125, message = "Please keep vehicle name between 1 and 125 characters")
    private String vehicleName;

    @NotNull(message = "Please provide a valid vehicle type to assign vehicle to")
    private int vehicleType;

    private byte[] newPicture;

    public VehicleUpdateDTO() {
    }

    public byte[] getNewPicture() {
        return newPicture;
    }

    public void setNewPicture(byte[] newPicture) {
        this.newPicture = newPicture;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }
}
