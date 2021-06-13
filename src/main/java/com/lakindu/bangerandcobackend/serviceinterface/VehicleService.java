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

    /**
     * Logic behind checking if vehicle is available for rental:
     * First get the list of rentals for every vehicle.
     * If there are no rentals, it is directly added as a vehicle that is available.
     * If there are rentals for the vehicle: the RENTAL Pickup DATE_TIME and Return DATE_TIME is obtained
     * The filtering Pickup DATE_TIME and Return DATE_TIME is obtained
     * Check is done to see if filtering Pickup DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
     * OR
     * Check is done to see if filtering Return DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
     * IF FILTERING IS IN BETWEEN:
     * Check is done to see if the rental is returned, if it is returned, it can be added to view. Else NOT ALLOWED
     *
     * @param theFilterDTO The object containing the REQUESTING Pickup DATE_TIME and Return DATE_TIME
     * @return The list of vehicles that can be rented between time period.
     * @throws DataFormatException Thrown when dates passed from client is invalid.
     * @throws IOException         Thrown due to image decompression issues.
     * @author Lakindu Hewawasam
     * @since 13th June 2021
     */
    List<VehicleShowDTO> getAllVehiclesThatCanBeRentedForGivenPeriod(VehicleRentalFilterDTO theFilterDTO) throws DataFormatException, IOException;
}
