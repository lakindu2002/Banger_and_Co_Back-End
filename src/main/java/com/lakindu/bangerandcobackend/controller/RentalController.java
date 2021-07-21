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
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

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
    public ResponseEntity<HashMap<String, Object>> getAllPendingRentals(@RequestParam(name = "pageNumber", required = false) Integer pageNumber) throws Exception {
        //if a page number is not provided, take the page number as 0 to get the first page results
        if (pageNumber == null) {
            pageNumber = 0;
        }
        HashMap<String, Object> allPendingRentalsWithPageToken = rentalService.getAllPendingRentals(pageNumber);
        return new ResponseEntity<>(allPendingRentalsWithPageToken, HttpStatus.OK);
    }
}
