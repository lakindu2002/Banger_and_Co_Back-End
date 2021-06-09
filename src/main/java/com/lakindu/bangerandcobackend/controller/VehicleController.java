package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.dto.CreateVehicleDTO;
import com.lakindu.bangerandcobackend.dto.ShowVehicleDTO;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
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
import java.util.List;
import java.util.zip.DataFormatException;

@RestController //enables controller and a response body
@RequestMapping(path = "/api/vehicle")
@PreAuthorize("isAuthenticated()")
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
        CreateVehicleDTO theDTO = theMapper.readValue(jsonInput, CreateVehicleDTO.class);

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
    public ResponseEntity<List<ShowVehicleDTO>> getAllVehicles() throws DataFormatException, IOException {
        //method will return a list of ALL the vehicles available at Banger and Co. and can be viewed by admin.
        List<ShowVehicleDTO> allVehicles = vehicleService.getAllVehicles();
        return new ResponseEntity<>(allVehicles, HttpStatus.OK);
    }
}
