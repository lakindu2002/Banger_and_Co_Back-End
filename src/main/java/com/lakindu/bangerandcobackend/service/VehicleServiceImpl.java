package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleCreateDTO;
import com.lakindu.bangerandcobackend.dto.RentalShowDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.dto.VehicleShowDTO;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.repository.VehicleRepository;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
import com.lakindu.bangerandcobackend.util.FileHandler.CompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.DecompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeService vehicleTypeService;

    @Autowired
    public VehicleServiceImpl(
            @Qualifier("vehicleRepository") VehicleRepository vehicleRepository,
            @Qualifier("vehicleTypeServiceImpl") VehicleTypeService vehicleTypeService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeService = vehicleTypeService;
    }

    @Override
    @Transactional
    public void createVehicle(VehicleCreateDTO theDTO, MultipartFile vehicleImage) throws ResourceNotFoundException, ResourceAlreadyExistsException, IOException, DataFormatException {
        //method executed to insert a vehicle into the database.

        //retrieve the vehicle type to assign to the vehicle.
        VehicleType theType = vehicleTypeService._getType(theDTO.getVehicleTypeId());

        //check if the license plate already exists in the database.
        Vehicle theVehicleInDB = vehicleRepository.getVehicleByLicensePlateEquals(theDTO.getLicensePlate());

        if (theVehicleInDB == null) {
            //vehicle does not exist, create it
            //compress image before saving.
            ImageHandler theCompressor = new CompressImage();
            byte[] compressedImage = theCompressor.processUnhandledImage(vehicleImage.getBytes()); //call template method
            //this will compress the image via defalter

            //construct an entity that can be used to save in database.
            Vehicle thePersistingEntity = new Vehicle();
            thePersistingEntity.setTheVehicleType(theType);
            thePersistingEntity.setFuelType(theDTO.getFuelType());
            thePersistingEntity.setVehicleName(theDTO.getVehicleName());
            thePersistingEntity.setVehicleImage(compressedImage);
            thePersistingEntity.setTransmission(theDTO.getTransmission());
            thePersistingEntity.setLicensePlate(theDTO.getLicensePlate());
            thePersistingEntity.setSeatingCapacity(theDTO.getSeatingCapacity());

            vehicleRepository.save(thePersistingEntity); //save object in database.
        } else {
            //exists with license plate provided, throw error.
            throw new ResourceAlreadyExistsException("There is already a vehicle at Banger and Co registered with the license plate - " + theDTO.getLicensePlate());
        }
    }

    @Override
    public List<VehicleShowDTO> getAllVehicles() throws DataFormatException, IOException {
        //method will return a list of all the vehicles that can be viewed by the administrator.
        List<Vehicle> theVehiclesInDatabase = vehicleRepository.findAll();
        List<VehicleShowDTO> theReturnList = new ArrayList<>(); //array list holding DTO to return to the client.

        for (Vehicle eachVehicle : theVehiclesInDatabase) {
            //construct dto before returning back to client.
            //decompress the vehicle image before showing it to construct the original uncompressed image, else wont be rendered properly.
            //construct a DTO and insert to theReturnList
            VehicleShowDTO theDTO = convertToPartialDTO(eachVehicle);

            //show the number of rentals for each vehicle as well.
            List<RentalShowDTO> rentalList = new ArrayList<>();
            for (Rental eachRental : eachVehicle.getRentalsForTheVehicle()) {
                //create a rental dto that can be shown back to the client.
                RentalShowDTO theRentalDTO = new RentalShowDTO();
                //ASSIGN RENTAL INFORMATION!!!!!!!!!!!!!!!!!!! TO DTO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                rentalList.add(theRentalDTO);
            }
            theDTO.setTheRentalsForVehicle(rentalList); //assign the rental list to the vehicle dto.
            theReturnList.add(theDTO);
        }
        return theReturnList;
    }

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
    @Override
    @Transactional
    public List<VehicleShowDTO> getAllVehiclesThatCanBeRentedForGivenPeriod(VehicleRentalFilterDTO theFilterDTO) throws DataFormatException, IOException {
        List<VehicleShowDTO> vehiclesThatCanBeRentedForPeriod = new ArrayList<>();
        List<Vehicle> allVehiclesInDb = vehicleRepository.findAll();

        //retrieve the LocalDateTime of the passed start - date, time & return - date, time.
        LocalDateTime filterPickUpDateTime = LocalDateTime.of(theFilterDTO.getPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theFilterDTO.getPickupTime());
        LocalDateTime filterReturnDateTime = LocalDateTime.of(theFilterDTO.getReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theFilterDTO.getReturnTime());

        for (Vehicle eachVehicle : allVehiclesInDb) {
            //iterate over each vehicle.
            List<Rental> rentalsForEachVehicle = eachVehicle.getRentalsForTheVehicle();
            //boolean to check if the vehicle in loop can be added to the return of the available vehicles.
            boolean canBeAddedToReturn = false;

            VehicleShowDTO vehicleReturnDTO = convertToPartialDTO(eachVehicle);

            if (rentalsForEachVehicle.isEmpty()) {
                //no rentals for the vehicle, can be rented
                canBeAddedToReturn = true;
            } else {
                //if vehicles have rentals present in DB
                for (Rental eachRental : rentalsForEachVehicle) {
                    //get the rental pickup and return date.
                    Date eachRentalPickupDate = eachRental.getPickupDate();
                    Date eachRentalReturnDate = eachRental.getReturnDate();
                    //get the rental pickup and return time.
                    LocalTime eachRentalPickupTime = eachRental.getPickupTime();
                    LocalTime eachRentalReturnTime = eachRental.getReturnTime();

                    //retrieve LocalDateTime of the Rental Pickup - Date, Time and Return - Date, Time.
                    LocalDateTime eachRentalPickupDateTime = LocalDateTime.of(eachRentalPickupDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), eachRentalPickupTime);
                    LocalDateTime eachRentalReturnDateTime = LocalDateTime.of(eachRentalReturnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), eachRentalReturnTime);

                    if (
                            ((filterPickUpDateTime.isAfter(eachRentalPickupDateTime)) && (filterPickUpDateTime.isBefore(eachRentalReturnDateTime)))
                                    ||
                                    ((filterReturnDateTime.isAfter(eachRentalPickupDateTime)) && (filterReturnDateTime.isBefore(eachRentalReturnDateTime)))

                    ) {
                        //there is a rental present between the passed PICKUP Date_Time and RETURN Date_Time
                        //check if that rental has been returned, if returned, can rent again.
                        if (eachRental.getReturned() != null && eachRental.getReturned()) {
                            canBeAddedToReturn = true;
                        }
                    } else {
                        //there is no rental present between the passed PICKUP Date_Time and RETURN Date_Time
                        canBeAddedToReturn = true;
                    }
                }
            }
            if (canBeAddedToReturn) {
                //if all the business validation for availability of vehicle passes, insert it to return list.
                vehiclesThatCanBeRentedForPeriod.add(vehicleReturnDTO);
            }
        }
        return vehiclesThatCanBeRentedForPeriod;
    }

    private VehicleShowDTO convertToPartialDTO(Vehicle theVehicle) throws DataFormatException, IOException {
        VehicleShowDTO vehicleReturnDTO = new VehicleShowDTO();
        vehicleReturnDTO.setVehicleId(theVehicle.getVehicleId());
        vehicleReturnDTO.setLicensePlate(theVehicle.getLicensePlate());
        vehicleReturnDTO.setVehicleName(theVehicle.getVehicleName());
        vehicleReturnDTO.setFuelType(theVehicle.getFuelType());
        vehicleReturnDTO.setTransmission(theVehicle.getTransmission());
        vehicleReturnDTO.setSeatingCapacity(theVehicle.getSeatingCapacity());
        vehicleReturnDTO.setVehicleImage(new DecompressImage().processUnhandledImage(theVehicle.getVehicleImage()));
        vehicleReturnDTO.setVehicleType(vehicleTypeService.constructDTO(theVehicle.getTheVehicleType()));

        return vehicleReturnDTO;
    }
}
