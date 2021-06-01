package com.lakindu.bangerandcobackend.dto;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO Object for Additional Equipment
 *
 * @author Lakindu Hewawasam
 */
public class AdditionalEquipmentDTO {
    private int equipmentId;

    @NotBlank(message = "Please provide an equipment name")
    @Size(min = 1, max = 255, message = "Please keep equipment size between 1 and 255 characters")
    private String equipmentName;

    @NotNull(message = "Please provide an equipment quantity")
    @Digits(integer = 4, fraction = 0, message = "Please keep quantity to 4 digits maximum (eg:9999)")
    private int equipmentQuantity;

    public AdditionalEquipmentDTO() {
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
