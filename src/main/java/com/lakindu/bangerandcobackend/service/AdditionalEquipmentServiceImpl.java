package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.repository.AdditionalEquipmentRepository;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceCannotBeDeletedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        //insert every record in to a DTO Array and return the array back to the client
        return convertToDTOList(additionalEquipmentRepository.findAll());
    }

    /**
     * Method will convert the data retrieved from the Database into a DTO List
     *
     * @param dbList The list obtained from Database
     * @return The list of DTO that the user can view.
     */
    private List<AdditionalEquipmentDTO> convertToDTOList(List<AdditionalEquipment> dbList) {
        List<AdditionalEquipmentDTO> theReturnList = new ArrayList<>();

        //insert every record in to a DTO Array and return the array back to the client
        for (AdditionalEquipment theEquipment : dbList) {
            AdditionalEquipmentDTO theDTO = new AdditionalEquipmentDTO();
            theDTO.setEquipmentName(theEquipment.getEquipmentName());
            theDTO.setEquipmentQuantity(theEquipment.getEquipmentQuantity());
            theDTO.setEquipmentId(theEquipment.getEquipmentId());
            theDTO.setLKR(theEquipment.getPricePerDay());

            theReturnList.add(theDTO);
        }
        return theReturnList;
    }

    @Override
    public List<AdditionalEquipmentDTO> getAllAvailableAdditionalEquipment() {
        //method will filter all additional equipment that has available stock
        return convertToDTOList(additionalEquipmentRepository.getAllAvailableEquipments());
    }

    @Override
    @Transactional
    public void createAdditionalEquipment(AdditionalEquipmentDTO theDTO) throws ResourceAlreadyExistsException, BadValuePassedException {
        //construct a domain entity that can be persisted in the database
        AdditionalEquipment theDomainEntity = new AdditionalEquipment();
        theDomainEntity.setEquipmentName(theDTO.getEquipmentName().trim());
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
        theDTO.setEquipmentName(theDTO.getEquipmentName().trim());

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
    public void removeEquipment(int equipmentId) throws ResourceNotFoundException, ResourceCannotBeDeletedException {
        //retrieve the equipment to be removed.
        AdditionalEquipment theEquipment = additionalEquipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("The additional equipment that you are trying to delete is not present at Banger and Co"));

        //2 validations must be done before it can be removed.
        //check if there are pending rentals that have this equipment added to it
        //check if there are on-going rentals that have this equipment added to it

        //if it can be deleted, first remove the additional equipment that is going to be removed from the rentals.
        //this will be removed by hibernate automatically.

        checkIfEquipmentHasPendingOrOngoingRentals(theEquipment); //two checks to be done.
        //no exceptions thrown, proceed with deletion
        additionalEquipmentRepository.delete(theEquipment);
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

    /**
     * Method will return the a database object for Additional Equipment for given id
     *
     * @param equipmentId Id to get data for
     * @return The object in database for given Id
     * @throws ResourceNotFoundException thrown when the equipment does not exist.
     */
    @Override
    public AdditionalEquipment _getAdditionalEquipmentById(int equipmentId) throws ResourceNotFoundException {
        return additionalEquipmentRepository.findById(equipmentId).orElseThrow(() -> new ResourceNotFoundException("The equipment does not exist at Banger and Co."));
    }

    @Override
    public void checkIfEquipmentHasPendingOrOngoingRentals(AdditionalEquipment theEquipment) throws ResourceCannotBeDeletedException {
        List<Rental> rentalHavingThisEquipment = theEquipment.getRentalsThatHaveThisEquipment();

        for (Rental eachRental : rentalHavingThisEquipment) {
            //check if the rental is pending
            if (!eachRental.getApproved()) {
                throw new ResourceCannotBeDeletedException("There are pending rentals that have this equipment added to it");
            }
            //check if the rental is approved but not collected
            //in the null check, "if" won't process past the null check
            //first expression is evaluated first
            //The && operator will stop evaluating (from left to right) as soon as it encounters a false.
            if (eachRental.getApproved() && (eachRental.getCollected() != null && !eachRental.getCollected())) {
                throw new ResourceCannotBeDeletedException("There are vehicles having this equipment added to it in rentals that are not yet collected");
            }
            //check if collected, but not returned
            if ((eachRental.getCollected() != null && eachRental.getCollected()) && !eachRental.getReturned()) {
                throw new ResourceCannotBeDeletedException("There are vehicles that are currently on rental that are having this equipment added to it.");
            }

            //if all these pass, it means rental has been returned.
        }
    }

}
