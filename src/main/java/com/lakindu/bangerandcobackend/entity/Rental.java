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
    private float totalCost;

    @Column(name = "is_returned", nullable = true)
    private boolean isReturned;

    @Column(name = "is_approved", nullable = true)
    private boolean isApproved;

    @Column(name = "is_collected", nullable = true)
    private boolean isCollected;

    @Column(name = "can_be_collected", nullable = true)
    private boolean canBeCollected;

    @Column(name = "is_late_return_requested", nullable = true)
    private boolean isLateReturnRequested;

    @Column(name = "is_late_return_approved", nullable = true)
    private boolean isLateReturnApproved;

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

    public List<AdditionalEquipment> getEquipmentsAddedToRental() {
        return equipmentsAddedToRental;
    }

    public void setEquipmentsAddedToRental(List<AdditionalEquipment> equipmentsAddedToRental) {
        this.equipmentsAddedToRental = equipmentsAddedToRental;
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

    public float getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(float totalCost) {
        this.totalCost = totalCost;
    }

    public boolean isReturned() {
        return isReturned;
    }

    public void setReturned(boolean returned) {
        isReturned = returned;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        isCollected = collected;
    }

    public boolean isCanBeCollected() {
        return canBeCollected;
    }

    public void setCanBeCollected(boolean canBeCollected) {
        this.canBeCollected = canBeCollected;
    }

    public boolean isLateReturnRequested() {
        return isLateReturnRequested;
    }

    public void setLateReturnRequested(boolean lateReturnRequested) {
        isLateReturnRequested = lateReturnRequested;
    }

    public boolean isLateReturnApproved() {
        return isLateReturnApproved;
    }

    public void setLateReturnApproved(boolean lateReturnApproved) {
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
}
