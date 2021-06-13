package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Date;

public class VehicleRentalFilterDTO {
    @NotNull(message = "Please provide a pickup date")
    private Date pickupDate;

    @NotNull(message = "Please provide a return date")
    private Date returnDate;

    @NotNull(message = "Please provide a pickup time")
    private LocalTime pickupTime;

    @NotNull(message = "Please provide a return time")
    private LocalTime returnTime;

    public VehicleRentalFilterDTO() {
    }

    public Date getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(Date pickupDate) {
        this.pickupDate = pickupDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
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
}
