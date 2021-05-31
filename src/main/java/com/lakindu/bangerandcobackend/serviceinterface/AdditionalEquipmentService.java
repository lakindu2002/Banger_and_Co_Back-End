package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;

import java.util.List;

public interface AdditionalEquipmentService {
    /**
     * Retrieve all the additional equipment objects persisted on database
     *
     * @return The list of DTOs that can be sent back to the client
     */
    List<AdditionalEquipmentDTO> getAllAdditionalEquipment();

    /**
     * Retrieves a list of all the additional equipment with a quantity greater than 0
     *
     * @return The DTOs of available equipment that can be sent back to the client
     */
    List<AdditionalEquipmentDTO> getAllAvailableAdditionalEquipment();


    /**
     * Used to create an additional equipment entry
     *
     * @throws ResourceAlreadyExistsException Thrown when an equipment with the passed already exists in the database
     */
    void createAdditionalEquipment(AdditionalEquipmentDTO theDTO) throws ResourceAlreadyExistsException;

    /**
     * Used to update the details of the additional equipment
     *
     * @param theDTO The object to containing the information to update
     * @throws ResourceNotFoundException The exception thrown when the equipment with ID passed cannot be located in the database
     */
    void updateEquipment(AdditionalEquipmentDTO theDTO) throws ResourceNotFoundException;

    /**
     * Used to remove additional equipment from the database when no item has been taken for a rental
     *
     * @param equipmentId The ID to delete the equipment from
     * @throws ResourceNotFoundException Thrown when the ID passed does not exist in the database.
     */
    void removeEquipment(int equipmentId) throws ResourceNotFoundException;
}
