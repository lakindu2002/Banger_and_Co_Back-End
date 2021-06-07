package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;
import java.util.List;

/**
 * Entity class to provide ORM to Additional Equipment table in MySQL Database
 *
 * @author Lakindu Hewawasam
 */
@Entity
@Table(name = "additional_equipment")
public class AdditionalEquipment {
    @Id
    @Column(name = "equipment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int equipmentId;

    @Column(name = "equipment_name", nullable = false, unique = true, length = 255)
    private String equipmentName;

    @Column(name = "equipment_quantity", nullable = false)
    private int equipmentQuantity;

    //many to many relationship between rental and this entity.
    @ManyToMany()
    @JoinTable(
            name = "rental_customization",
            joinColumns = @JoinColumn(name = "equipment_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "rental_id", nullable = false)
    )
    private List<Rental> rentalsThatHaveThisEquipment;

    public AdditionalEquipment() {
    }

    public List<Rental> getRentalsThatHaveThisEquipment() {
        return rentalsThatHaveThisEquipment;
    }

    public void setRentalsThatHaveThisEquipment(List<Rental> rentalsThatHaveThisEquipment) {
        this.rentalsThatHaveThisEquipment = rentalsThatHaveThisEquipment;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public int getEquipmentQuantity() {
        return equipmentQuantity;
    }

    public void setEquipmentQuantity(int equipmentQuantity) {
        this.equipmentQuantity = equipmentQuantity;
    }
}
