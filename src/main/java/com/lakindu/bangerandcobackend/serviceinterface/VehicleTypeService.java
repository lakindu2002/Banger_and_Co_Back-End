package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceCannotBeDeletedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;

import java.util.List;

public interface VehicleTypeService {
    /**
     * Method will be used to create a new vehicle type into the system
     *
     * @param theDTO The object being created
     * @throws ResourceAlreadyExistsException Exception thrown when type name with same size exists in database.
     */
    void createVehicleType(VehicleTypeDTO theDTO) throws ResourceAlreadyExistsException;

    /**
     * Method used to retrieve all vehicle types at Banger and Co.
     *
     * @return Returns all type records from the database.
     */
    List<VehicleTypeDTO> getAllVehicleTypes();

    VehicleTypeDTO findVehicleTypeById(int id) throws ResourceNotFoundException;

    VehicleType _getType(int id) throws ResourceNotFoundException;

    VehicleTypeDTO constructDTO(VehicleType theType);

    /**
     * Allows the vehicle type to be removed from banger and co only when there are no vehicles associated to the type.
     */
    void removeVehicleType(int id) throws ResourceNotFoundException, ResourceCannotBeDeletedException;

    void updateVehicleTypeInformation(VehicleTypeDTO theUpdateDTO) throws ResourceNotFoundException;
}
