package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController //provides @Controller and @ResponseBody (as a return)
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/api/vehicleType")
public class VehicleTypeController {
    private final VehicleTypeService vehicleTypeService;

    @Autowired
    public VehicleTypeController(
            //inject the implementation of interface to the controller.
            @Qualifier("vehicleTypeServiceImpl") VehicleTypeService vehicleTypeService
    ) {
        this.vehicleTypeService = vehicleTypeService;
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping(path = "/create")
    public ResponseEntity<BangerAndCoResponse> createVehicleType(@Valid @RequestBody VehicleTypeDTO theDTO) throws ResourceAlreadyExistsException {
        //@Valid triggers bean validation and will execute method if the request body has been validated successfully
        //@RequestBody will automatically bind the json object into the VehicleTypeDTO via jackson (de-serialize)
        //maps  JSON object properties to fields and calls the setters.

        vehicleTypeService.createVehicleType(theDTO);

        return new ResponseEntity<>(
                new BangerAndCoResponse("Vehicle type created successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/findAll")
    public ResponseEntity<List<VehicleTypeDTO>> getAllVehicleTypes() {
        //response entity = http response
        List<VehicleTypeDTO> theTypes = vehicleTypeService.getAllVehicleTypes();
        return new ResponseEntity<>(theTypes, HttpStatus.OK); //ok = 200 status code
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/find/{id}")
    public ResponseEntity<VehicleTypeDTO> findById(@PathVariable(name = "id", required = true) int id) throws ResourceNotFoundException {
        //get the vehicle type dto via the ID
        VehicleTypeDTO theType = vehicleTypeService.findVehicleTypeById(id);
        return new ResponseEntity<>(theType, HttpStatus.OK); //ok = 200 status code
    }
}
