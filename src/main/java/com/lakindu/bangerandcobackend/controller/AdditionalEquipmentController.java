package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController //handle rest interactions (JSON Response)
@RequestMapping(path = "/api/equipment")
public class AdditionalEquipmentController {
    private final AdditionalEquipmentService additionalEquipmentService;

    @Autowired //inject
    public AdditionalEquipmentController(
            @Qualifier("additionalEquipmentServiceImpl") AdditionalEquipmentService additionalEquipmentService) {
        this.additionalEquipmentService = additionalEquipmentService;
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping(path = "/create")
    public ResponseEntity<BangerAndCoResponse> createAdditionalEquipment(
            @Valid @RequestBody AdditionalEquipmentDTO theDTO
    ) throws ResourceAlreadyExistsException {
        //@RequestBody will automatically de-serialize the HTTP Request Body for the AdditionalEquipmentDTO object.

        //call service method to create the additional equipment.
        additionalEquipmentService.createAdditionalEquipment(theDTO);

        return new ResponseEntity<>(
                new BangerAndCoResponse("Additional Equipment Created Successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }
}
