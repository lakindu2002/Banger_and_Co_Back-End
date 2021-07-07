package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DTO Object for Additional Equipment
 *
 * @author Lakindu Hewawasam
 */
public class AdditionalEquipmentDTO {
    private int equipmentId;

    @NotBlank(message = "Please provide an equipment name")
    @Size(min = 1, max = 255, message = "Please keep equipment size between 1 and 255 characters")
    @Pattern(regexp = "^[A-Za-z]+", message = "Please ensure that the equipment name only has alphabetical characters and no numerics and spaces.")
    private String equipmentName;

    @NotNull(message = "Please provide an equipment quantity")
    @Digits(integer = 4, fraction = 0, message = "Please keep quantity to 4 digits maximum (eg:9999)")
    private int equipmentQuantity;

    @NotBlank(message = "Please provide a valid price")
    @Digits(integer = 5, fraction = 3, message = "Please provide a maximum of 5 numerics and 3 decimals for the price per day.")
    private String pricePerDay;

    public AdditionalEquipmentDTO() {
    }


    public String getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(String pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void setLKR(double pricePerDay) {
        //%s - any time
        this.pricePerDay = String.format("LKR - %s", pricePerDay);
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
