package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;

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
    void createAdditionalEquipment(AdditionalEquipmentDTO theDTO) throws ResourceAlreadyExistsException, BadValuePassedException;

    /**
     * Used to update the details of the additional equipment
     *
     * @param theDTO The object to containing the information to update
     * @throws ResourceNotFoundException      The exception thrown when the equipment with ID passed cannot be located in the database
     * @throws BadValuePassedException        Thrown when the client passes an equipment ID of 0
     * @throws ResourceAlreadyExistsException Thrown when the equipment with passed name already exists in database for a different ID
     */
    void updateEquipment(AdditionalEquipmentDTO theDTO) throws ResourceNotFoundException, BadValuePassedException, ResourceAlreadyExistsException;

    /**
     * Used to remove additional equipment from the database when no item has been taken for a rental
     *
     * @param equipmentId The ID to delete the equipment from
     * @throws ResourceNotFoundException Thrown when the ID passed does not exist in the database.
     */
    void removeEquipment(int equipmentId) throws ResourceNotFoundException;

    /**
     * Method used to retrieve equipment information by the ID.
     *
     * @param equipmentId The ID to get the information for
     * @return The object containing the information based on the ID passed.
     */
    AdditionalEquipmentDTO getEquipmentByID(int equipmentId) throws ResourceNotFoundException;
}
