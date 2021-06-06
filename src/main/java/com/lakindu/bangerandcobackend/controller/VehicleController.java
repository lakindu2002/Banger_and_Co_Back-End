package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<BangerAndCoResponse> createVehicle() {
        return new ResponseEntity<>(
                new BangerAndCoResponse("Vehicle created successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }
}
