package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "rental")
public class Rental {
    @Id
    @Column(name = "rental_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rentalId;

    @Column(name = "pickup_date", nullable = false)
    private Date pickupDate;

    @Column(name = "return_date", nullable = false)
    private Date returnDate;

    @Column(name = "pickup_time", nullable = false)
    private LocalTime pickupTime;

    @Column(name = "return_time", nullable = false)
    private LocalTime returnTime;

    @Column(name = "total_cost", nullable = false)
    private double totalCost;

    @Column(name = "is_returned", nullable = true)
    private Boolean isReturned;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @Column(name = "is_collected", nullable = true)
    private Boolean isCollected;

    @Column(name = "is_late_return_requested", nullable = true)
    private Boolean isLateReturnRequested;

    @Column(name = "is_late_return_approved", nullable = true)
    private Boolean isLateReturnApproved;

    @Lob()
    @Column(name = "driving_license", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] drivingLicense;

    @Lob()
    @Column(name = "other_identity", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] otherIdentity;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH}, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_username", nullable = false) //map foreign key.
    private User theCustomerRenting;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH}, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false) //map the foreign key.
    private Vehicle vehicleOnRental;

    //map many to many
    @ManyToMany()
    @JoinTable(
            name = "rental_customization",
            joinColumns = @JoinColumn(name = "rental_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "equipment_id", nullable = false)
    )
    private List<AdditionalEquipment> equipmentsAddedToRental;

    public Rental() {
    }

    public int getRentalId() {
        return rentalId;
    }

    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
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

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
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

    public Boolean getLateReturnApproved() {
        return isLateReturnApproved;
    }

    public void setLateReturnApproved(Boolean lateReturnApproved) {
        isLateReturnApproved = lateReturnApproved;
    }

    public byte[] getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(byte[] drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    public byte[] getOtherIdentity() {
        return otherIdentity;
    }

    public void setOtherIdentity(byte[] otherIdentity) {
        this.otherIdentity = otherIdentity;
    }

    public User getTheCustomerRenting() {
        return theCustomerRenting;
    }

    public void setTheCustomerRenting(User theCustomerRenting) {
        this.theCustomerRenting = theCustomerRenting;
    }

    public Vehicle getVehicleOnRental() {
        return vehicleOnRental;
    }

    public void setVehicleOnRental(Vehicle vehicleOnRental) {
        this.vehicleOnRental = vehicleOnRental;
    }

    public List<AdditionalEquipment> getEquipmentsAddedToRental() {
        return equipmentsAddedToRental;
    }

    public void setEquipmentsAddedToRental(List<AdditionalEquipment> equipmentsAddedToRental) {
        this.equipmentsAddedToRental = equipmentsAddedToRental;
    }
}
