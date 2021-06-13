package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.dto.VehicleCreateDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.dto.VehicleShowDTO;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.*;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.InputValidNotValidatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Validator;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

@RestController //enables controller and a response body
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/api/vehicle")
public class VehicleController {
    private final VehicleService vehicleService;
    private final Validator validator;

    @Autowired
    public VehicleController(
            @Qualifier("vehicleServiceImpl") VehicleService vehicleService,
            @Qualifier("defaultValidator") Validator validator) {
        this.vehicleService = vehicleService;
        this.validator = validator;
    }

    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<BangerAndCoResponse> createVehicle(
            @RequestParam(name = "vehicleInformation", required = true) String jsonInput,
            @RequestParam(name = "vehicleImage", required = true) MultipartFile vehicleImage
    ) throws IOException, InputValidNotValidatedException, ResourceNotFoundException, ResourceAlreadyExistsException,
            DataFormatException {
        //method will create a vehicle at Banger and Co by the admin

        ObjectMapper theMapper = new ObjectMapper();
        VehicleCreateDTO theDTO = theMapper.readValue(jsonInput, VehicleCreateDTO.class);

        DataBinder theBinder = new DataBinder(theDTO); //bind a CreateVehicleDTO
        theBinder.setValidator((org.springframework.validation.Validator) validator); //assign the Java default validator
        theBinder.validate(); //validate the DTO

        BindingResult errorList = theBinder.getBindingResult(); // get error occurred in validation.
        if (errorList.hasErrors()) {
            for (ObjectError err : errorList.getAllErrors())
                System.out.println(err.getDefaultMessage());
            //if there are errors.
            throw new InputValidNotValidatedException("Please provide valid inputs during vehicle creation.", errorList);
        }

        vehicleService.createVehicle(theDTO, vehicleImage);

        return new ResponseEntity<>(
                new BangerAndCoResponse("Vehicle created successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @GetMapping(path = "/all")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<List<VehicleShowDTO>> getAllVehicles() throws DataFormatException, IOException {
        //method will return a list of ALL the vehicles available at Banger and Co. and can be viewed by admin.
        List<VehicleShowDTO> allVehicles = vehicleService.getAllVehicles();
        return new ResponseEntity<>(allVehicles, HttpStatus.OK);
    }

    //get all vehicles that can be rented for the specified pickup/return date and pickup/return time.
    @GetMapping(path = "/getRentableVehicles")
    @PreAuthorize("permitAll()")
    //do not need authorization to access this endpoint.
    public ResponseEntity<List<VehicleShowDTO>> getAllVehiclesThatCanBeRentedForGivenPeriod(
            @RequestParam(value = "pickupDate", required = true) String pickupDate,
            @RequestParam(value = "returnDate", required = true) String returnDate,
            @RequestParam(value = "pickupTime", required = true) String pickupTime,
            @RequestParam(value = "returnTime", required = true) String returnTime
    ) throws InputValidNotValidatedException, ParseException, DataFormatException, IOException {
        //method executed by GUESTS and CUSTOMERS to view a list of all available vehicles for the given Pickup DATE_TIME and Return DATE_TIME

        //construct a DTO to validate the passed data in the request parameter
        VehicleRentalFilterDTO theFilterDTO = new VehicleRentalFilterDTO();
        theFilterDTO.setPickupDate(new SimpleDateFormat("yyyy-MM-dd").parse(pickupDate));
        theFilterDTO.setReturnDate(new SimpleDateFormat("yyyy-MM-dd").parse(returnDate));
        theFilterDTO.setPickupTime(LocalTime.parse(pickupTime));
        theFilterDTO.setReturnTime(LocalTime.parse(returnTime));

        DataBinder theBinder = new DataBinder(theFilterDTO); //bind the VehicleFilterDTO to the DataBinder and validate its data.
        theBinder.setValidator((org.springframework.validation.Validator) validator);
        theBinder.validate();

        BindingResult theValidatedResult = theBinder.getBindingResult();
        if (theValidatedResult.hasErrors()) {
            //if the passed inputs have any validation errors
            throw new InputValidNotValidatedException("Please provide valid inputs for the filter", theValidatedResult);
        }

        //validated successfully, retrieve data from database.
        List<VehicleShowDTO> availableVehicles = vehicleService.getAllVehiclesThatCanBeRentedForGivenPeriod(theFilterDTO);
        return new ResponseEntity<>(availableVehicles, HttpStatus.OK);
    }
}
