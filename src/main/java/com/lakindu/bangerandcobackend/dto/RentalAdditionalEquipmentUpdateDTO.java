package com.lakindu.bangerandcobackend.dto;


import javax.validation.constraints.NotNull;
import java.util.List;

public class RentalAdditionalEquipmentUpdateDTO {
    @NotNull(message = "Please provide a valid rental to update additional equipments for")
    private Integer rental;
    @NotNull(message = "Please provide an equipment list")
    private List<AdditionalEquipmentDTO> updateEquipments;

    public RentalAdditionalEquipmentUpdateDTO() {
    }

    public Integer getRental() {
        return rental;
    }

    public void setRental(Integer rental) {
        this.rental = rental;
    }

    public List<AdditionalEquipmentDTO> getUpdateEquipments() {
        return updateEquipments;
    }

    public void setUpdateEquipments(List<AdditionalEquipmentDTO> updateEquipments) {
        this.updateEquipments = updateEquipments;
    }
}
