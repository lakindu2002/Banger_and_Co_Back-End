package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;

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
}
