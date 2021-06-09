package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.repository.AdditionalEquipmentRepository;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdditionalEquipmentServiceImpl implements AdditionalEquipmentService {

    private final AdditionalEquipmentRepository additionalEquipmentRepository;

    @Autowired
    public AdditionalEquipmentServiceImpl(
            @Qualifier("additionalEquipmentRepository") AdditionalEquipmentRepository additionalEquipmentRepository
    ) {
        this.additionalEquipmentRepository = additionalEquipmentRepository;
    }

    @Override
    public List<AdditionalEquipmentDTO> getAllAdditionalEquipment() {
        //retrieve all data from database
        List<AdditionalEquipment> theDatabaseList = additionalEquipmentRepository.findAll();
        List<AdditionalEquipmentDTO> theReturnList = new ArrayList<>();

        //insert every record in to a DTO Array and return the array back to the client
        for (AdditionalEquipment theEquipment : theDatabaseList) {
            AdditionalEquipmentDTO theDTO = new AdditionalEquipmentDTO();
            theDTO.setEquipmentName(theEquipment.getEquipmentName());
            theDTO.setEquipmentQuantity(theEquipment.getEquipmentQuantity());
            theDTO.setEquipmentId(theEquipment.getEquipmentId());
            theDTO.showCurrencyOnReturn(theEquipment.getPricePerDay());

            theReturnList.add(theDTO);
        }
        return theReturnList;
    }

    @Override
    public List<AdditionalEquipmentDTO> getAllAvailableAdditionalEquipment() {
        return null;
    }

    @Override
    @Transactional
    public void createAdditionalEquipment(AdditionalEquipmentDTO theDTO) throws ResourceAlreadyExistsException, BadValuePassedException {
        //construct a domain entity that can be persisted in the database
        AdditionalEquipment theDomainEntity = new AdditionalEquipment();
        theDomainEntity.setEquipmentName(theDTO.getEquipmentName().trim().toUpperCase());
        theDomainEntity.setEquipmentQuantity(theDTO.getEquipmentQuantity());
        theDomainEntity.setPricePerDay(Double.parseDouble(theDTO.getPricePerDay()));

        if (theDomainEntity.getEquipmentQuantity() == 0) {
            //if the quantity being added while creating a new item is at 0, it cannot be added
            throw new BadValuePassedException("The quantity at hand cannot be 0 when creating a new additional equipment for rental vehicles.");
        }

        //check if the equipment already exists in the database, if it exists throw an exception handled by @RestControllerAdvice
        final Optional<AdditionalEquipment> theOptionalEquipment = additionalEquipmentRepository.findAdditionalEquipmentByEquipmentName(theDomainEntity.getEquipmentName());
        if (theOptionalEquipment.isPresent()) {
            throw new ResourceAlreadyExistsException(String.format(
                    "The additional equipment with the name '%s' already exists at Banger and Co.",
                    theDomainEntity.getEquipmentName()
            ));
        }

        //persist the entity on the database.
        additionalEquipmentRepository.save(theDomainEntity);
    }

    @Override
    public void updateEquipment(AdditionalEquipmentDTO theDTO) throws ResourceNotFoundException, BadValuePassedException, ResourceAlreadyExistsException {
        theDTO.setEquipmentName(theDTO.getEquipmentName().trim().toUpperCase());

        if (theDTO.getEquipmentId() == 0) {
            throw new BadValuePassedException("Please provide a valid equipment ID");
        }

        //check - 1 check if the ID passed is valid. (If equipment being updated exists in database)
        //check - 2 check if an equipment exists with the given name but it should exist on a separate ID and not the one being updated.

        //check - 1
        final AdditionalEquipment updatingItem = additionalEquipmentRepository.findById(theDTO.getEquipmentId()).orElseThrow(
                () -> new ResourceNotFoundException("The equipment you are trying to update does not exist")
        );

        //check  - 2
        AdditionalEquipment itemNameExistingInDifferentId = additionalEquipmentRepository.getItemWithSameNameButAsASeperateEntry(
                theDTO.getEquipmentName(),
                updatingItem.getEquipmentId()
        );

        if (itemNameExistingInDifferentId != null) {
            throw new ResourceAlreadyExistsException("An equipment with the name you provided already exists. Please try again");
        }

        //if all validations pass.
        updatingItem.setEquipmentName(theDTO.getEquipmentName());
        updatingItem.setEquipmentQuantity(theDTO.getEquipmentQuantity());

        //update the equipment information.
        additionalEquipmentRepository.save(updatingItem);
    }

    @Override
    public void removeEquipment(int equipmentId) throws ResourceNotFoundException {

    }

    @Override
    public AdditionalEquipmentDTO getEquipmentByID(int equipmentId) throws ResourceNotFoundException {
        //query database and check if optional is present, if not throw a custom not found exception
        AdditionalEquipment theEquipment = additionalEquipmentRepository.findById(equipmentId).orElseThrow(
                () -> new ResourceNotFoundException("The Additional Equipment for the ID you passed does not exist")
        );

        //construct a return DTO
        AdditionalEquipmentDTO theReturningDTO = new AdditionalEquipmentDTO();
        theReturningDTO.setEquipmentId(theEquipment.getEquipmentId());
        theReturningDTO.setEquipmentName(theEquipment.getEquipmentName());
        theReturningDTO.setEquipmentQuantity(theEquipment.getEquipmentQuantity());
        theReturningDTO.setPricePerDay(String.valueOf(theEquipment.getPricePerDay()));

        return theReturningDTO; //return the object to the controller that will return the object via serialization back to client
    }
}
