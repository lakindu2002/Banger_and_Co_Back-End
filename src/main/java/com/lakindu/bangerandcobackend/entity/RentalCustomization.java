package com.lakindu.bangerandcobackend.entity;

import javax.persistence.*;

@Entity
@Table(name = "rental_customization")
public class RentalCustomization {
    @Id
    @Column(name = "rental_customization_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rentalCustomizationId;

    @JoinColumn(name = "rental_id", nullable = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Rental theRentalInformation;

    @JoinColumn(name = "equipment_id", nullable = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private AdditionalEquipment equipmentAddedToRental;

    @Column(name = "added_quantity", nullable = false)
    private int quantityAddedForEquipmentInRental;

    public RentalCustomization() {
    }

    public int getRentalCustomizationId() {
        return rentalCustomizationId;
    }

    public void setRentalCustomizationId(int rentalCustomizationId) {
        this.rentalCustomizationId = rentalCustomizationId;
    }

    public Rental getTheRentalInformation() {
        return theRentalInformation;
    }

    public void setTheRentalInformation(Rental theRentalInformation) {
        this.theRentalInformation = theRentalInformation;
    }

    public AdditionalEquipment getEquipmentAddedToRental() {
        return equipmentAddedToRental;
    }

    public void setEquipmentAddedToRental(AdditionalEquipment equipmentAddedToRental) {
        this.equipmentAddedToRental = equipmentAddedToRental;
    }

    public int getQuantityAddedForEquipmentInRental() {
        return quantityAddedForEquipmentInRental;
    }

    public void setQuantityAddedForEquipmentInRental(int quantityAddedForEquipmentInRental) {
        this.quantityAddedForEquipmentInRental = quantityAddedForEquipmentInRental;
    }
}
