package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.repository.VehicleTypeRepository;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceCannotBeDeletedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleTypeServiceImpl implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    public VehicleTypeServiceImpl(
            @Qualifier("vehicleTypeRepository") VehicleTypeRepository vehicleTypeRepository
    ) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @Override
    public void createVehicleType(VehicleTypeDTO theDTO) throws ResourceAlreadyExistsException {
        //construct a domain entity to persist
        VehicleType thePersistingObject = new VehicleType();
        thePersistingObject.setTypeName(theDTO.getTypeName().toUpperCase().trim());
        thePersistingObject.setSize(theDTO.getSize().toUpperCase().trim());
        thePersistingObject.setPricePerDay(Double.parseDouble(theDTO.getPricePerDay()));

        //check if the same type name exists along with same size, if so do not allow creation.
        VehicleType theChecker = vehicleTypeRepository.findVehicleTypeByTypeNameEqualsAndSizeEquals(
                thePersistingObject.getTypeName(), thePersistingObject.getSize()
        );

        if (theChecker != null) {
            //the data exists in the database, therefore do not allow creation
            throw new ResourceAlreadyExistsException("This type name already exists with the same size of vehicle. Please assign a different name or a different vehicle size");
        } else {
            //if valid
            vehicleTypeRepository.save(thePersistingObject); //save the object in the database.
        }
    }

    @Override
    public List<VehicleTypeDTO> getAllVehicleTypes() {
        List<VehicleType> allTypesInDB = vehicleTypeRepository.findAll();
        List<VehicleTypeDTO> returnList = new ArrayList<>();

        for (VehicleType theType : allTypesInDB) {
            //construct a return to be sent to the client via DTO Pattern.
            returnList.add(constructDTO(theType));
        }

        return returnList; //return the collection back to the controller that can be used to send back to client in ResponseBody.
    }

    @Override
    public VehicleTypeDTO findVehicleTypeById(int id) throws ResourceNotFoundException {
        final Optional<VehicleType> optionalType = vehicleTypeRepository.findById(id);
        VehicleType theType = optionalType.orElseThrow(() -> new ResourceNotFoundException("The vehicle type you wish to access does not exist at Banger and Co."));
        return constructDTO(theType);
    }

    @Override
    public VehicleType _getType(int id) throws ResourceNotFoundException {
        return vehicleTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("The vehicle type provided does not exist at Banger and Co."));
    }

    @Override
    public VehicleTypeDTO constructDTO(VehicleType theType) {
        //construct the return DTO
        VehicleTypeDTO theDTO = new VehicleTypeDTO();
        theDTO.setVehicleTypeId(theType.getVehicleTypeId());
        theDTO.setTypeName(theType.getTypeName());
        theDTO.setSize(theType.getSize());
        theDTO.setLKR(theType.getPricePerDay());
        theDTO.setVehicleCountInType(theType.getVehicleList().size());

        return theDTO; //return the DTO back to the client.
    }

    @Override
    public void removeVehicleType(int id) throws ResourceNotFoundException, ResourceCannotBeDeletedException {
        Optional<VehicleType> optionalVehicleType = vehicleTypeRepository.findById(id);
        if (optionalVehicleType.isPresent()) {
            //the vehicle type is present in the database
            //check if the vehicle type has any vehicles associated to it.
            VehicleType theType = optionalVehicleType.get();
            if (theType.getVehicleList().size() > 0) {
                //there are vehicles associated to the type, therefore it cannot be removed
                throw new ResourceCannotBeDeletedException("There are vehicles associated to the type - " + theType.getSize() + " " + theType.getTypeName() + ". Please assign the vehicles to a new type or remove the vehicles before deleting this vehicle type.");
            } else {
                //no vehicles, can remove
                vehicleTypeRepository.delete(theType); //delete vehicle type from database.
            }
        } else {
            //the vehicle type is not in database.
            throw new ResourceNotFoundException("The Vehicle Type that you are trying to remove does not exist at Banger and Co.");
        }
    }

    @Override
    public void updateVehicleTypeInformation(VehicleTypeDTO theUpdateDTO) throws ResourceNotFoundException {
        //update the vehicle type information in the database.

        //retrieve the type from the database for the ID passed, if not existing thrown the exception.
        VehicleType theTypeInDatabase = vehicleTypeRepository.findById(theUpdateDTO.getVehicleTypeId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("The vehicle type that you wish to update does not exist at Banger and Co.")
                );

        //NO VALIDATIONS ARE REQUIRED AS ONCE A RENTAL HAS BEEN CREATED, THEY WILL BE CHARGED ON THEIR PREVIOUS RATE.
        //BUT DURING UPDATE OF RENTAL, THEY WILL BE CHARGED ON NEW PRICE.

        //if exception has not been thrown, update the price.
        theTypeInDatabase.setPricePerDay(Double.parseDouble(theUpdateDTO.getPricePerDay()));
        vehicleTypeRepository.save(theTypeInDatabase); //update the vehicle.
    }
}
