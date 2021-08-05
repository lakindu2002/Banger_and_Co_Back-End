package com.lakindu.bangerandcobackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalShowDTO {
    private int rentalId;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private LocalTime pickupTime;
    private LocalTime returnTime;
    private double totalCostForRental;
    private VehicleShowDTO vehicleToBeRented;
    private List<AdditionalEquipmentDTO> equipmentsAddedToRental;
    private UserDTO customerUsername;
    private Boolean isReturned;
    private Boolean isApproved;
    private Boolean isCollected;
    private Boolean isLateReturnRequested;

    private String timeLeftForRental; //will comprise of an output string to display on customer dashboard to indicate time left on their rental

    public RentalShowDTO() {
    }


    public void setTimeLeftForRental(String timeLeftForRental) {
        this.timeLeftForRental = timeLeftForRental;
    }

    public String getTimeLeftForRental() {
        return timeLeftForRental;
    }

    public void calculateTimeLeftForRental() {
        LocalDate now = LocalDate.now();
        LocalDateTime currentDateTime = LocalDateTime.of(now, LocalTime.now());
        LocalDateTime returnExact = LocalDateTime.of(returnDate, returnTime);

        if (returnDate.equals(now)) {
            //return date is today
            this.timeLeftForRental = currentDateTime.until(returnExact, ChronoUnit.HOURS) + " Hour(s) Left";
        } else {
            //get in days
            this.timeLeftForRental = currentDateTime.until(returnExact, ChronoUnit.DAYS) + " Day(s) Left";
        }
    }

    public int getRentalId() {
        return rentalId;
    }

    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public LocalTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalTime returnTime) {
        this.returnTime = returnTime;
    }

    public double getTotalCostForRental() {
        return totalCostForRental;
    }

    public void setTotalCostForRental(double totalCostForRental) {
        this.totalCostForRental = totalCostForRental;
    }

    public VehicleShowDTO getVehicleToBeRented() {
        return vehicleToBeRented;
    }

    public void setVehicleToBeRented(VehicleShowDTO vehicleToBeRented) {
        this.vehicleToBeRented = vehicleToBeRented;
    }

    public List<AdditionalEquipmentDTO> getEquipmentsAddedToRental() {
        return equipmentsAddedToRental;
    }

    public void setEquipmentsAddedToRental(List<AdditionalEquipmentDTO> equipmentsAddedToRental) {
        this.equipmentsAddedToRental = equipmentsAddedToRental;
    }

    public UserDTO getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(UserDTO customerUsername) {
        this.customerUsername = customerUsername;
    }

    public Boolean getReturned() {
        return isReturned;
    }

    public void setReturned(Boolean returned) {
        isReturned = returned;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public Boolean getCollected() {
        return isCollected;
    }

    public void setCollected(Boolean collected) {
        isCollected = collected;
    }

    public Boolean getLateReturnRequested() {
        return isLateReturnRequested;
    }

    public void setLateReturnRequested(Boolean lateReturnRequested) {
        isLateReturnRequested = lateReturnRequested;
    }
}
