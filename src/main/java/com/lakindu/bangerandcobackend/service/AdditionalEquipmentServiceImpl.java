package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.repository.AdditionalEquipmentRepository;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
        return null;
    }

    @Override
    public List<AdditionalEquipmentDTO> getAllAvailableAdditionalEquipment() {
        return null;
    }

    @Override
    @Transactional
    public void createAdditionalEquipment(AdditionalEquipmentDTO theDTO) throws ResourceAlreadyExistsException {
        //construct a domain entity that can be persisted in the database
        AdditionalEquipment theDomainEntity = new AdditionalEquipment();
        theDomainEntity.setEquipmentName(theDTO.getEquipmentName().trim().toUpperCase());
        theDomainEntity.setEquipmentQuantity(theDomainEntity.getEquipmentQuantity());

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
            throw new BadValuePassedException("Please provide a valid equipment ID"
            );
        }

        //check - 1 check if the ID passed is valid. (If equipment being updated exists in database)
        //check - 2 check if an equipment exists with the given name but it should exist on a separate ID and not the one being updated.

        //check - 1
        final AdditionalEquipment theItem = additionalEquipmentRepository.findById(theDTO.getEquipmentId()).orElseThrow(
                () -> new ResourceNotFoundException("The equipment you are trying to update does not exist")
        );

        //check  - 2
        AdditionalEquipment check2 = additionalEquipmentRepository.getItemWithSameNameButAsASeperateEntry(
                theDTO.getEquipmentName(),
                theItem.getEquipmentId()
        );

        if (check2 != null) {
            throw new ResourceAlreadyExistsException("An equipment with the name you provided already exists. Please try again");
        }

        //if all validations pass.
        theItem.setEquipmentName(theDTO.getEquipmentName());
        theItem.setEquipmentQuantity(theDTO.getEquipmentQuantity());

        //update the equipment information.
        additionalEquipmentRepository.save(theItem);
    }

    @Override
    public void removeEquipment(int equipmentId) throws ResourceNotFoundException {

    }
}
