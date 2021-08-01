package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.RentalCreateDTO;
import com.lakindu.bangerandcobackend.dto.RentalShowDTO;
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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.HashMap;

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

    @GetMapping(path = "/find/allRejected")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<HashMap<String, Object>> getAllRejectedRentals(@RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {
        //method used to get all the rejected rentals at banger and co.
        if (pageNumber == null) {
            pageNumber = 0;
        }
        HashMap<String, Object> returnResults = rentalService.getAllRejectedRentals(pageNumber);
        return new ResponseEntity<>(returnResults, HttpStatus.OK);
    }

    @GetMapping(path = "/find/allApproved")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<HashMap<String, Object>> getAllApprovedRentals(@RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {
        //method used to get all approved rentals
        //all approved rentals means all the rentals that have been approved and can be collected from Banger and Co.
        if (pageNumber == null) {
            pageNumber = 0;
        }
        HashMap<String, Object> returnResults = rentalService.getAllApprovedRentals(pageNumber);
        return new ResponseEntity<>(returnResults, HttpStatus.OK);
    }

    @GetMapping(path = "/find/allCompleted")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<HashMap<String, Object>> getAllCompletedRentalsPastRentals(@RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {
        //method will get all the past rentals at banger and co to show the completed rentals
        //to be a past rental - isApproved, isCollected and isReturned has to be true.
        if (pageNumber == null) {
            pageNumber = 0;
        }
        HashMap<String, Object> returnResults = rentalService.getAllCompletedRentals(pageNumber);
        return new ResponseEntity<>(returnResults, HttpStatus.OK);
    }

    @GetMapping(path = "/find/allOnGoing")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<HashMap<String, Object>> getAllOnGoingRentals(@RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {
        //method used to get all the on-going rentals at Banger and Co.
        //when a rental is on-going; isApprove - TRUE, isCollected - TRUE and isReturned - FALSE;
        if (pageNumber == null) {
            pageNumber = 0;
        }
        HashMap<String, Object> returnResults = rentalService.getAllOnGoingRentals(pageNumber);
        return new ResponseEntity<>(returnResults, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/find/{rentalId}")
    public ResponseEntity<RentalShowDTO> getRentalById(@PathVariable(name = "rentalId", required = true) Integer rentalId) throws Exception {
        RentalShowDTO theRentalDTO = rentalService.getRentalById(rentalId);
        return new ResponseEntity<>(theRentalDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping(path = "/handle/approve")
    public ResponseEntity<BangerAndCoResponse> approveRental(@RequestBody HashMap<String, Integer> theRequestBody) throws BadValuePassedException, ResourceNotFoundException {
        //method will be executed by the administrator to approve a rental.
        //the rental id will be passed into the service method if there is a key and it is not null

        if (theRequestBody.containsKey("rentalId") && theRequestBody.get("rentalId") != null) {
            rentalService.approveRental(theRequestBody.get("rentalId"));
        } else {
            throw new BadValuePassedException("The rental ID was not present to approve the rental.");
        }

        return new ResponseEntity<>(
                new BangerAndCoResponse("The rental has been approved successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping(path = "/handle/reject")
    public ResponseEntity<BangerAndCoResponse> rejectRental(@RequestBody HashMap<String, Object> theRequestBody) throws BadValuePassedException, ResourceNotFoundException {
        //method will be used to reject a rental only if the rental id and the rejected reason is present in the request body.

        if (theRequestBody.containsKey("rentalId") && theRequestBody.get("rentalId") != null) {
            //rental id is present
            if (theRequestBody.containsKey("rejectedReason") && theRequestBody.get("rejectedReason") != null) {
                //rejected reason is present

                if (theRequestBody.get("rejectedReason").toString().trim().length() > 0) {
                    //if there is actual text and not a blank string
                    //the rental id and the rejected reason will be passed into the service method.
                    rentalService.rejectRental(
                            Integer.parseInt(theRequestBody.get("rentalId").toString()),
                            theRequestBody.get("rejectedReason").toString()
                    );
                } else {
                    //reason is a blank string
                    throw new BadValuePassedException("The rejected reason is not present in the request.");
                }
            } else {
                throw new BadValuePassedException("The rejected reason is not present in the request.");
            }
        } else {
            throw new BadValuePassedException("The rental ID was not present to reject the rental.");
        }

        return new ResponseEntity<>(
                new BangerAndCoResponse("The rental has been rejected successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping(path = "/handle/startRental")
    public ResponseEntity<BangerAndCoResponse> startRental(@RequestBody HashMap<String, Integer> theRentalId) throws BadValuePassedException, ResourceNotFoundException {
        //method will be exuected by an admin to start a rental when the customer picks the vehicle up
        if (theRentalId.containsKey("rentalId") && theRentalId.get("rentalId") != null) {
            rentalService.startRental(theRentalId.get("rentalId"));
        } else {
            //either the rental ID is not in request body, or its there as null, therefore its a bad value.
            throw new BadValuePassedException("The rental ID is not present, therefore, the rental could not be started");
        }
        return new ResponseEntity<>(
                new BangerAndCoResponse("The rental has been started successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/find/pending/{username}")
    public ResponseEntity<HashMap<String, Object>> getAllPendingRentalsForCustomer(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "pageNumber") Integer pageNumber
    ) throws Exception {
        if (pageNumber == null) {
            pageNumber = 0; //if passed page number is null, retrieve initial page results.
        }
        HashMap<String, Object> returnList = rentalService.getCustomerPendingRentals(username, pageNumber);
        return new ResponseEntity<>(
                returnList, HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/find/readyToCollect/{username}")
    public ResponseEntity<HashMap<String, Object>> getAllRentalsThatCanBeCollectedForCustomer(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {

        //method executed by the customers to get a list of the rentals of theirs that can be collected.
        if (pageNumber == null) {
            pageNumber = 0; //if passed page number is null, retrieve initial page results.
        }

        HashMap<String, Object> returnList = rentalService.getCustomerCanBeCollectedRentals(username, pageNumber);
        return new ResponseEntity<>(
                returnList, HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/find/completed/{username}")
    public ResponseEntity<HashMap<String, Object>> getAllCompletedRentalsForCustomer(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {

        //method executed by the customers to get a list of the rentals of theirs that have been returned.
        if (pageNumber == null) {
            pageNumber = 0; //if passed page number is null, retrieve initial page results.
        }

        HashMap<String, Object> returnList = rentalService.getCustomerCompletedRentals(username, pageNumber);
        return new ResponseEntity<>(
                returnList, HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/find/onGoing/{username}")
    public ResponseEntity<HashMap<String, Object>> getAllOnGoingRentalsForCustomer(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {

        //method executed by the customers to get a list of the rentals of theirs that are ongoing.
        if (pageNumber == null) {
            pageNumber = 0; //if passed page number is null, retrieve initial page results.
        }

        HashMap<String, Object> returnList = rentalService.getCustomerOnGoingRentals(username, pageNumber);
        return new ResponseEntity<>(
                returnList, HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/find/rejected/{username}")
    public ResponseEntity<HashMap<String, Object>> getAllRejectedRentalsForTheCustomer(
            @PathVariable(name = "username") String username,
            @RequestParam(name = "pageNumber") Integer pageNumber) throws Exception {

        //method executed by the customers to get a list of the rentals of theirs that are rejected.
        if (pageNumber == null) {
            pageNumber = 0; //if passed page number is null, retrieve initial page results.
        }

        HashMap<String, Object> returnList = rentalService.getCustomerRejectedRentals(username, pageNumber);
        return new ResponseEntity<>(
                returnList, HttpStatus.OK
        );
    }
}
