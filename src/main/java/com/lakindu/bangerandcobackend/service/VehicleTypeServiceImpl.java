package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.repository.VehicleTypeRepository;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
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
        thePersistingObject.setTypeName(theDTO.getTypeName().toUpperCase());
        thePersistingObject.setSize(theDTO.getSize().toUpperCase());
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
            VehicleTypeDTO theDTO = new VehicleTypeDTO();
            //construct a return to be sent to the client via DTO Pattern.
            theDTO.setVehicleTypeId(theType.getVehicleTypeId());
            theDTO.setTypeName(theType.getTypeName());
            theDTO.showCurrencyOnReturn(theType.getPricePerDay());
            theDTO.setSize(theType.getSize());

            returnList.add(theDTO);
        }

        return returnList; //return the collection back to the controller that can be used to send back to client in ResponseBody.
    }

    @Override
    public VehicleTypeDTO findVehicleTypeById(int id) throws ResourceNotFoundException {
        final Optional<VehicleType> optionalType = vehicleTypeRepository.findById(id);
        VehicleType theType = optionalType.orElseThrow(() -> new ResourceNotFoundException("The vehicle type you wish to access does not exist at Banger and Co."));

        //construct the return DTO
        VehicleTypeDTO theDTO = new VehicleTypeDTO();
        theDTO.setVehicleTypeId(theType.getVehicleTypeId());
        theDTO.setTypeName(theType.getTypeName());
        theDTO.setSize(theType.getSize());
        theDTO.showCurrencyOnReturn(theType.getPricePerDay());

        return theDTO; //return the DTO back to the client.
    }

    @Override
    public VehicleType _getType(int id) throws ResourceNotFoundException {
        return vehicleTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("The vehicle type provided does not exist at Banger and Co."));
    }
}
