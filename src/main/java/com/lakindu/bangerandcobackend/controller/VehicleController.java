package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.dto.CreateVehicleDTO;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController //enables controller and a response body
@RequestMapping(path = "/api/vehicle")
@PreAuthorize("isAuthenticated()")
public class VehicleController {
    private final VehicleService vehicleService;

    @Autowired
    public VehicleController(
            @Qualifier("vehicleServiceImpl") VehicleService vehicleService
    ) {
        this.vehicleService = vehicleService;
    }

    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BangerAndCoResponse> createVehicle(
            @RequestParam(name = "vehicleInformation", required = true) String jsonInput,
            @RequestParam(name = "vehicleImage", required = true) MultipartFile vehicleImage
    ) throws JsonProcessingException {

        ObjectMapper theMapper = new ObjectMapper();
        CreateVehicleDTO theDTO = theMapper.readValue(jsonInput, CreateVehicleDTO.class);

        System.out.println(theDTO);
        System.out.println(vehicleImage);
        return new ResponseEntity<>(
                new BangerAndCoResponse("Vehicle created successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }
}
