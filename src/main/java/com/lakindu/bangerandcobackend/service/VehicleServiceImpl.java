package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.CreateVehicleDTO;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.repository.VehicleRepository;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
import com.lakindu.bangerandcobackend.util.FileHandler.CompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.zip.DataFormatException;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeService vehicleTypeService;

    @Autowired
    public VehicleServiceImpl(
            @Qualifier("vehicleRepository") VehicleRepository vehicleRepository,
            @Qualifier("vehicleTypeServiceImpl") VehicleTypeService vehicleTypeService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeService = vehicleTypeService;
    }

    @Override
    @Transactional
    public void createVehicle(CreateVehicleDTO theDTO, MultipartFile vehicleImage) throws ResourceNotFoundException, ResourceAlreadyExistsException, IOException, DataFormatException {
        //method executed to insert a vehicle into the database.

        //retrieve the vehicle type to assign to the vehicle.
        VehicleType theType = vehicleTypeService._getType(theDTO.getVehicleTypeId());

        //check if the license plate already exists in the database.
        Vehicle theVehicleInDB = vehicleRepository.getVehicleByLicensePlateEquals(theDTO.getLicensePlate());

        if (theVehicleInDB == null) {
            //vehicle does not exist, create it
            //compress image before saving.
            ImageHandler theCompressor = new CompressImage();
            byte[] compressedImage = theCompressor.processUnhandledImage(vehicleImage.getBytes()); //call template method
            //this will compress the image via defalter

            //construct an entity that can be used to save in database.
            Vehicle thePersistingEntity = new Vehicle();
            thePersistingEntity.setTheVehicleType(theType);
            thePersistingEntity.setFuelType(theDTO.getFuelType());
            thePersistingEntity.setVehicleName(theDTO.getVehicleName());
            thePersistingEntity.setVehicleImage(compressedImage);
            thePersistingEntity.setTransmission(theDTO.getTransmission());
            thePersistingEntity.setLicensePlate(theDTO.getLicensePlate());

            vehicleRepository.save(thePersistingEntity); //save object in database.
        } else {
            //exists with license plate provided, throw error.
            throw new ResourceAlreadyExistsException("There is already a vehicle at Banger and Co registered with the license plate - " + theDTO.getLicensePlate());
        }
    }
}
