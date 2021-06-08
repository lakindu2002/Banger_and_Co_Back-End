package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.CreateVehicleDTO;
import com.lakindu.bangerandcobackend.dto.ShowVehicleDTO;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

public interface VehicleService {
    void createVehicle(CreateVehicleDTO theDTO, MultipartFile vehicleImage) throws ResourceNotFoundException, ResourceAlreadyExistsException, IOException, DataFormatException;

    /**
     * This method will return a list of all the vehicles available at Banger and Co that will be viewed only via an administrator.
     *
     * @return All vehicle information in the database.
     * @throws DataFormatException Thrown by java.util.zip
     * @throws IOException         Thrown by java.util.zip
     */
    List<ShowVehicleDTO> getAllVehicles() throws DataFormatException, IOException;
}
