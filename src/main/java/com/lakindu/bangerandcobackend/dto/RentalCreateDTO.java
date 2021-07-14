package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

public class RentalCreateDTO {
    private ArrayList<AdditionalEquipmentDTO> equipmentsAddedToRental;

    @NotBlank(message = "Please provide a valid pickup date")
    private String pickupDate;

    @NotBlank(message = "Please provide a valid pickup time")
    private String pickupTime;

    @NotBlank(message = "Please provide a valid return date")
    private String returnDate;

    @NotBlank(message = "Please provide a valid return date")
    private String returnTime;

    @NotNull(message = "Please provide a valid cost")
    private Double totalCostForRental;

    @NotNull(message = "Please provide a valid vehicle")
    private int vehicleToBeRented;

    public RentalCreateDTO() {
        this.equipmentsAddedToRental = new ArrayList<AdditionalEquipmentDTO>();
    }

    public ArrayList<AdditionalEquipmentDTO> getEquipmentsAddedToRental() {
        return equipmentsAddedToRental;
    }

    public void setEquipmentsAddedToRental(ArrayList<AdditionalEquipmentDTO> equipmentsAddedToRental) {
        this.equipmentsAddedToRental = equipmentsAddedToRental;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public double getTotalCostForRental() {
        return totalCostForRental;
    }

    public void setTotalCostForRental(double totalCostForRental) {
        this.totalCostForRental = totalCostForRental;
    }

    public void setTotalCostForRental(Double totalCostForRental) {
        this.totalCostForRental = totalCostForRental;
    }

    public int getVehicleToBeRented() {
        return vehicleToBeRented;
    }

    public void setVehicleToBeRented(int vehicleToBeRented) {
        this.vehicleToBeRented = vehicleToBeRented;
    }
}
