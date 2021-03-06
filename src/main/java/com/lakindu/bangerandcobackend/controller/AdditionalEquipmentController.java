package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceCannotBeDeletedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController //handle rest interactions (JSON Response)
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/api/equipment")
public class AdditionalEquipmentController {
    private final AdditionalEquipmentService additionalEquipmentService;

    @Autowired //inject
    public AdditionalEquipmentController(
            @Qualifier("additionalEquipmentServiceImpl") AdditionalEquipmentService additionalEquipmentService) {
        this.additionalEquipmentService = additionalEquipmentService;
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')") //can be executed only by the administrator.
    @PostMapping(path = "/create")
    public ResponseEntity<BangerAndCoResponse> createAdditionalEquipment(
            @Valid @RequestBody AdditionalEquipmentDTO theDTO
    ) throws ResourceAlreadyExistsException, BadValuePassedException {
        //method used to create additional equipment by the administrator.
        //@RequestBody will automatically de-serialize the HTTP Request Body for the AdditionalEquipmentDTO object.

        //call service method to create the additional equipment.
        additionalEquipmentService.createAdditionalEquipment(theDTO);

        return new ResponseEntity<>(
                new BangerAndCoResponse("The additional equipment was created successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping(path = "/update")
    public ResponseEntity<BangerAndCoResponse> updateAdditionalEquipment(
            @Valid @RequestBody AdditionalEquipmentDTO theUpdateDTO
    ) throws ResourceNotFoundException, BadValuePassedException, ResourceAlreadyExistsException {
        //method used to update the additional equipment information by the administrator.
        //allows updating of the equipment name and quantity.

        //call the service method
        additionalEquipmentService.updateEquipment(theUpdateDTO);

        //return success 200 back to client.
        return new ResponseEntity<>(
                new BangerAndCoResponse("The information was updated successfully",
                        HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/all")
    public ResponseEntity<List<AdditionalEquipmentDTO>> getAllAdditionalEquipment() {
        //retrieve all the information from the database
        List<AdditionalEquipmentDTO> theList = additionalEquipmentService.getAllAdditionalEquipment();
        //return it back in the response body to the client.
        return new ResponseEntity<>(
                theList,
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/get/{id}")
    public ResponseEntity<AdditionalEquipmentDTO> getEquipmentById(@PathVariable(name = "id", required = true) int equipmentId) throws ResourceNotFoundException {
        //method used to retrieve additional equipment by the id to display on update for the administrator.
        AdditionalEquipmentDTO theViewingDTO = additionalEquipmentService.getEquipmentByID(equipmentId);
        //if successfully retrieved.
        return new ResponseEntity<>(theViewingDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping(path = "/remove/{id}")
    public ResponseEntity<BangerAndCoResponse> removeEquipmentById(@PathVariable(name = "id", required = true) int id) throws ResourceNotFoundException, ResourceCannotBeDeletedException {
        //method executed by the administrator of Banger and Co to remove the additional equipment from the system.
        additionalEquipmentService.removeEquipment(id);
        return new ResponseEntity<>(
                new BangerAndCoResponse("The additional equipment has been removed from Banger and Co successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/find/available")
    public ResponseEntity<List<AdditionalEquipmentDTO>> getAllAvailableAdditionalEquipment() {
        //method executed by the customer to view all available additional equipment to add to their rental.
        List<AdditionalEquipmentDTO> availableEquipment = additionalEquipmentService.getAllAvailableAdditionalEquipment();
        return new ResponseEntity<>(availableEquipment, HttpStatus.OK);
    }
}
