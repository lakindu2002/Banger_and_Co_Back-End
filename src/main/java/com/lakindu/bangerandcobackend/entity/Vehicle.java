package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;

@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private int vehicleId;

    @Column(name = "license_plate", unique = true, length = 255, nullable = false)
    private String licensePlate;

    @Column(name = "vehicle_name", length = 255, nullable = false)
    private String vehicleName;

    @Column(name = "fuel_type", length = 255, nullable = false)
    private String fuelType;

    @Column(name = "transmission", length = 255, nullable = false)
    private String transmission;

    @Lob
    //column definition defines the SQL Fragment to be used when creating the Column of MediumBlob
    //like when this column is being created, strictly place creating "MediumBlob" type.
    @Column(name = "vehicle_image", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] vehicleImage;

    //define a many to relationship to enable relationship between vehicle and vehicle type.
    //detach - detach child entity from persistence context.
    //a vehicle cannot exist without belonging to a type.
    @ManyToOne(cascade = {CascadeType.DETACH}, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
            name = "vehicle_type_id",
            nullable = false
    )
    private VehicleType theVehicleType;

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

    public VehicleType getTheVehicleType() {
        return theVehicleType;
    }

    public void setTheVehicleType(VehicleType theVehicleType) {
        this.theVehicleType = theVehicleType;
    }
}
