package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "vehicle_type")
public class VehicleType {
    @Id
    @Column(name = "type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //relies on the IdentityGenerator which expects values generated by an identity column in the database,
    //meaning they are auto-incremented.
    private int vehicleTypeId;

    @Column(name = "type_name", length = 255, nullable = false)
    private String typeName;

    @Column(name = "size", length = 75, nullable = false)
    private String size;

    @Column(name = "price_per_day", nullable = false)
    private double pricePerDay; //double recommended by java doc to store floats of SQL type.

    //add a bi-directional relationship to get a list of vehicles for each type.
    //theVehicleType is the object name used to refer a type in the vehicle class.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "theVehicleType")
    private List<Vehicle> vehicleList;

    public VehicleType() {
    }

    public int getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(int vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }
}
