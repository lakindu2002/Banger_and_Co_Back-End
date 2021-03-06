package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleCreateDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.dto.VehicleShowDTO;
import com.lakindu.bangerandcobackend.dto.VehicleUpdateDTO;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.repository.VehicleRepository;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceCannotBeDeletedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.DataFormatException;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeService vehicleTypeService;
    private final UserService userService;

    @Autowired
    public VehicleServiceImpl(
            @Qualifier("vehicleRepository") VehicleRepository vehicleRepository,
            @Qualifier("vehicleTypeServiceImpl") VehicleTypeService vehicleTypeService,
            @Qualifier("userServiceImpl") UserService userService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeService = vehicleTypeService;
        this.userService = userService;
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
            byte[] compressedImage = new ImageHandler().compressImage(vehicleImage.getBytes());
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
     * IF the Passed PICKUP or RETURN Date_Time is between a RENTAL PICKUP or RETURN DATE_TIME, check if each vehicle for rental is returned, if so vehicle can be rented again
     * IF not, there may be rentals that fall between the PASSED Pickup and Return DATE_TIME, check those rentals.
     * IF rentals fall between Passed PICKUP/RETURN Date_Time, check if they're returned, if so, can be rented again.
     * <p>
     * <b>if customer is less than 25 years old, they can rent only SMALL TOWN CARS as the banger only insured for that age</b>
     *
     * @param theFilterDTO The object containing the REQUESTING Pickup DATE_TIME and Return DATE_TIME
     * @param loggedInUser
     * @return The list of vehicles that can be rented between time period.
     * @throws DataFormatException Thrown when dates passed from client is invalid.
     * @throws IOException         Thrown due to image decompression issues.
     * @author Lakindu Hewawasam
     * @since 13th June 2021
     */
    @Override
    @Transactional
    public List<VehicleShowDTO> getAllVehiclesThatCanBeRentedForGivenPeriod(VehicleRentalFilterDTO theFilterDTO, Authentication loggedInUser) throws DataFormatException, IOException {
        List<VehicleShowDTO> vehiclesThatCanBeRentedForPeriod = new ArrayList<>();
        List<Vehicle> allVehiclesInDb = vehicleRepository.findAll();
        boolean getOnlySmallTownCars = isUserLessThan25Years(loggedInUser);

        //retrieve the LocalDateTime of the passed start - date, time & return - date, time.
        LocalDateTime filterPickUpDateTime = LocalDateTime.of(theFilterDTO.getPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theFilterDTO.getPickupTime());
        LocalDateTime filterReturnDateTime = LocalDateTime.of(theFilterDTO.getReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theFilterDTO.getReturnTime());

        for (Vehicle eachVehicle : allVehiclesInDb) {
            //iterate over each vehicle.

            //if user is a customer and is less than 25 years old, show only small town cars.

            VehicleShowDTO vehicleReturnDTO = convertToPartialDTO(eachVehicle);
            VehicleType theType = eachVehicle.getTheVehicleType();

            //boolean to check if the vehicle in loop can be added to the return of the available vehicles.
            boolean canBeAdded = isVehicleAvailableOnGivenDates(eachVehicle, filterPickUpDateTime, filterReturnDateTime);
            if (canBeAdded) {
                //if all the business validation for availability of vehicle passes, insert it to return list.
                if (getOnlySmallTownCars) {
                    //the user can only rent small town cars, so add to return only if its small town car.
                    if (theType.getSize().equalsIgnoreCase("small") && theType.getTypeName().equalsIgnoreCase("town car")) {
                        vehiclesThatCanBeRentedForPeriod.add(vehicleReturnDTO);
                    }
                } else {
                    //user can rent anything.
                    vehiclesThatCanBeRentedForPeriod.add(vehicleReturnDTO);
                }
            }
        }
        return vehiclesThatCanBeRentedForPeriod;
    }

    private boolean isUserLessThan25Years(Authentication loggedInUser) {
        if (loggedInUser == null) {
            //not logged in, guest
            return false;
        } else {
            //login present.
            User theUserEntity = userService._getUserWithoutDecompression(loggedInUser.getName());

            if (theUserEntity.getUserRole().getRoleName().equalsIgnoreCase("customer")) {
                LocalDate dateOfBirth = theUserEntity.getDateOfBirth().toLocalDate();
                LocalDate currentDate = new Date(System.currentTimeMillis()).toLocalDate();

                if (Period.between(dateOfBirth, currentDate).getYears() < 25) {
                    //if age is less than 25, allow only rental of Small town cars
                    return true;
                } else {
                    //older or equal to 25, can rent anything.
                    return false;
                }
            } else {
                //user is an admin, do not check the less than 25 constraint.
                return false;
            }
        }
    }

    /**
     * Method will validate to check if the vehicle is available on the given pickup and return times.
     *
     * @param theVehicle     The vehicle to check
     * @param pickupDateTime The customer pickup time
     * @param returnDateTime The customer return time
     * @return The boolean to indicate if the vehicle is available or not.
     */
    public boolean isVehicleAvailableOnGivenDates(Vehicle theVehicle, LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        boolean canBeAdded = false;
        List<Rental> rentalsForEachVehicle = theVehicle.getRentalsForTheVehicle();

        if (rentalsForEachVehicle.isEmpty()) {
            //no rentals for the vehicle, can be rented
            canBeAdded = true;
        } else {
            //if vehicles have rentals present in DB
            for (Rental eachRental : rentalsForEachVehicle) {
                //retrieve LocalDateTime of the Rental Pickup - Date, Time and Return - Date, Time.
                LocalDateTime eachRentalPickupDateTime = LocalDateTime.of(eachRental.getPickupDate(), eachRental.getPickupTime());
                LocalDateTime eachRentalReturnDateTime = LocalDateTime.of(eachRental.getReturnDate(), eachRental.getReturnTime());

                //if filtering Pickup DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
                //OR
                //if filtering Return DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
                if (
                        ((pickupDateTime.isAfter(eachRentalPickupDateTime) || pickupDateTime.equals(eachRentalPickupDateTime)) && (pickupDateTime.isBefore(eachRentalReturnDateTime) || pickupDateTime.equals(eachRentalReturnDateTime)))
                                ||
                                ((returnDateTime.isAfter(eachRentalPickupDateTime) || returnDateTime.equals(eachRentalPickupDateTime)) && (returnDateTime.isBefore(eachRentalReturnDateTime) || returnDateTime.equals(eachRentalPickupDateTime)))

                ) {
                    //The filtering Pickup DATE_TIME or Return DATE_TIME is between a rental.
                    if (eachRental.getApproved() != null && !eachRental.getApproved()) {
                        //rental has been rejected, therefore, can be rented again
                        canBeAdded = true;
                    } else if (eachRental.getReturned() != null && eachRental.getReturned()) {
                        //vehicle has been returned, can be rented again
                        canBeAdded = true;
                    } else {
                        //vehicle is pending, or on-going or can be collected.
                        return false;
                    }
                } else {
                    //The filtering Pickup DATE_TIME or Return DATE_TIME is not between a rental.
                    //BUT
                    //there may be rentals present between passed PICKUP Date_Time AND RETURN Date_Time
                    if (eachRentalPickupDateTime.isAfter(pickupDateTime) && eachRentalReturnDateTime.isBefore(returnDateTime)) {
                        //the rental in database is between the passed Pickup-Date_Time and Return Date_Time
                        //The filtering Pickup DATE_TIME or Return DATE_TIME is between a rental.
                        if (eachRental.getApproved() != null && !eachRental.getApproved()) {
                            //rental has been rejected, therefore, can be rented again
                            canBeAdded = true;
                        } else if (eachRental.getReturned() != null && eachRental.getReturned()) {
                            //vehicle has been returned, can be rented again
                            canBeAdded = true;
                        } else {
                            //vehicle is pending, or on-going or can be collected.
                            return false;
                        }
                    } else {
                        //the rental in database is not between passed Pickup-Date_Time and Return Date_Time
                        //therefore it is not conflicting, can be rented.
                        canBeAdded = true;
                    }
                }
            }
        }
        return canBeAdded;
    }

    /**
     * Returns the vehicle for the given id.
     *
     * @param vehicleId The vehicle to retrieve
     * @return The vehicle from the database.
     */
    @Override
    public Vehicle _getVehicleInformation(int vehicleId) throws ResourceNotFoundException {
        return vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("The vehicle you are trying to access does not exist at Banger and Co."));
    }

    @Override
    @Transactional
    public void removeVehicleById(int vehicleId) throws ResourceNotFoundException, ResourceCannotBeDeletedException {
        //first check if vehicle actually exists for the ID.
        Vehicle theVehicleToBeRemoved = vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("The vehicle that you are trying to remove does not exist at Banger and Co."));
        //check if vehicle to be removed has any rental associated to it
        if (theVehicleToBeRemoved.getRentalsForTheVehicle().size() > 0) {
            throw new ResourceCannotBeDeletedException("This vehicle has rentals associated to it, therefore, it cannot be removed");
        }

        vehicleRepository.delete(theVehicleToBeRemoved); //remove the vehicle from the database,
    }

    public void checkIfVehicleHasPendingOrOnGoingRentals(Vehicle theVehicleToBeRemoved) throws ResourceCannotBeDeletedException {
        List<Rental> rentalsForVehicle = theVehicleToBeRemoved.getRentalsForTheVehicle();
        //3 checks to be done
        //check if any rentals are pending for the vehicle
        //check if any rentals are approved but not yet collected
        //check if any rentals are collected but not yet returned

        for (Rental eachRental : rentalsForVehicle) {
            if (eachRental.getApproved() == null) {
                //rentals are pending for the vehicle
                throw new ResourceCannotBeDeletedException(
                        "There are pending rentals for this vehicle. " +
                                "Either reject the rentals or wait till they have been completed"
                );
            }

            if ((eachRental.getApproved()) && (eachRental.getCollected() != null && !eachRental.getCollected())) {
//                rentals are approved but not yet collected
                throw new ResourceCannotBeDeletedException("There are rentals for this vehicle that the customers have not yet collected. Please wait until the customer collects and returns the vehicle");
            }

            if ((eachRental.getCollected() != null && eachRental.getCollected()) && (eachRental.getReturned() != null && !eachRental.getReturned())) {
//                rentals are collected but not yet returned
                throw new ResourceCannotBeDeletedException("Customers are already renting this vehicle and have not yet returned this. Please wait until they return the vehicle");
            }
        }
    }

    /**
     * Method executed to update a vehicle. The vehicle can be updated only if there are no pending or on-going rentals for the vehicle.
     *
     * @param updateObject The new update information
     * @return The updated vehicle information.
     */
    @Override
    public Vehicle updateVehicle(VehicleUpdateDTO updateObject) throws ResourceNotFoundException, DataFormatException, IOException, ResourceCannotBeDeletedException {
        //retrieve the vehicle and the type from the database.
        Vehicle theVehicle = vehicleRepository.findById(updateObject.getVehicleId()).orElseThrow(
                () -> new ResourceNotFoundException("The vehicle that you wish to update does not exist at Banger and Co.")
        );

        VehicleType theType = vehicleTypeService._getType(updateObject.getVehicleType());

        //check if vehicle has any pending or ongoing rentals
        checkIfVehicleHasPendingOrOnGoingRentals(theVehicle);

        //can be updated.
        theVehicle.setTheVehicleType(theType); //update the type
        theVehicle.setVehicleName(updateObject.getVehicleName()); //update the vehicle name
        if (updateObject.getNewPicture() != null) {
            //have new image.
            theVehicle.setVehicleImage(new ImageHandler().compressImage(updateObject.getNewPicture()));
        }
        return vehicleRepository.save(theVehicle);
    }

    @Override
    public VehicleShowDTO getVehicleById(int vehicleId) throws DataFormatException, IOException, ResourceNotFoundException {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
        if (optionalVehicle.isPresent()) {
            return convertToPartialDTO(optionalVehicle.get());
        } else {
            throw new ResourceNotFoundException("The vehicle that you are trying to access does not access at Banger and Co.");
        }
    }

    private VehicleShowDTO convertToPartialDTO(Vehicle theVehicle) throws DataFormatException, IOException {
        VehicleShowDTO vehicleReturnDTO = new VehicleShowDTO();
        vehicleReturnDTO.setVehicleId(theVehicle.getVehicleId());
        vehicleReturnDTO.setLicensePlate(theVehicle.getLicensePlate());
        vehicleReturnDTO.setVehicleName(theVehicle.getVehicleName());
        vehicleReturnDTO.setFuelType(theVehicle.getFuelType());
        vehicleReturnDTO.setTransmission(theVehicle.getTransmission());
        vehicleReturnDTO.setSeatingCapacity(theVehicle.getSeatingCapacity());
        vehicleReturnDTO.setVehicleImage(new ImageHandler().decompressImage(theVehicle.getVehicleImage()));
        vehicleReturnDTO.setVehicleType(vehicleTypeService.constructDTO(theVehicle.getTheVehicleType()));

        return vehicleReturnDTO;
    }
}
