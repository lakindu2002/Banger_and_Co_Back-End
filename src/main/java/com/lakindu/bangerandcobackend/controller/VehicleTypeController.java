package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
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
        //method will create a vehicle type by the information passed by the administrator.

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
        //method will return a list of all the available vehicle types available at banger and co.
        //response entity = http response
        List<VehicleTypeDTO> theTypes = vehicleTypeService.getAllVehicleTypes();
        return new ResponseEntity<>(theTypes, HttpStatus.OK); //ok = 200 status code
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/find/{id}")
    public ResponseEntity<VehicleTypeDTO> findById(@PathVariable(name = "id", required = true) int id) throws ResourceNotFoundException {
        //method will be used to find a vehicle type of a given id.
        //get the vehicle type dto via the ID
        VehicleTypeDTO theType = vehicleTypeService.findVehicleTypeById(id);
        return new ResponseEntity<>(theType, HttpStatus.OK); //ok = 200 status code
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping(path = "/remove/{id}")
    public ResponseEntity<BangerAndCoResponse> removeById(@PathVariable(name = "id", required = true) int id) throws ResourceNotFoundException, ResourceCannotBeDeletedException {
        //allows the vehicle type to be removed from the database only when there are no vehicles associated to the type.
        vehicleTypeService.removeVehicleType(id);
        return new ResponseEntity<>(
                new BangerAndCoResponse("Vehicle Type Removed Successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }
}
