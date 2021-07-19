package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.RentalCreateDTO;
import com.lakindu.bangerandcobackend.dto.RentalShowDTO;
import com.lakindu.bangerandcobackend.dto.VehicleShowDTO;
import com.lakindu.bangerandcobackend.serviceinterface.RentalService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotCreatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/rental")
@PreAuthorize("isAuthenticated()")
public class RentalController {
    private final RentalService rentalService;

    @Autowired
    public RentalController(@Qualifier("rentalServiceImpl") RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping(path = "/makeRental")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<BangerAndCoResponse> makeRental(@Valid @RequestBody RentalCreateDTO theRental) throws ParseException, BadValuePassedException, ResourceNotFoundException, ResourceNotCreatedException {
        rentalService.makeRental(theRental);

        return new ResponseEntity<>(
                new BangerAndCoResponse("The rental was placed successfully. You will receive an email with confirmation. We hope you have an excellent experience at Banger and Co.", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/find/pendingRentals")
    public ResponseEntity<List<RentalShowDTO>> getAllPendingRentals() {
        List<RentalShowDTO> allPendingRentals = rentalService.getAllPendingRentals();
        return new ResponseEntity<>(allPendingRentals, HttpStatus.OK);
    }
}
