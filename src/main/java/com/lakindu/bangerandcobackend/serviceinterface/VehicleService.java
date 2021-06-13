package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.VehicleCreateDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.dto.VehicleShowDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

public interface VehicleService {
    void createVehicle(VehicleCreateDTO theDTO, MultipartFile vehicleImage) throws ResourceNotFoundException, ResourceAlreadyExistsException, IOException, DataFormatException;

    /**
     * This method will return a list of all the vehicles available at Banger and Co that will be viewed only via an administrator.
     *
     * @return All vehicle information in the database.
     * @throws DataFormatException Thrown by java.util.zip
     * @throws IOException         Thrown by java.util.zip
     */
    List<VehicleShowDTO> getAllVehicles() throws DataFormatException, IOException;

    List<VehicleShowDTO> getAllVehiclesThatCanBeRentedForGivenPeriod(VehicleRentalFilterDTO theFilterDTO);
}
