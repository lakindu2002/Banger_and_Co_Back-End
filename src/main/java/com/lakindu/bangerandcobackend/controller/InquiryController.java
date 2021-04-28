package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.serviceinterface.InquiryService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()") //only authenticated can access
@RequestMapping(path = "/api/inquiry")
public class InquiryController {
    private final InquiryService inquiryService;

    @Autowired
    public InquiryController(
            @Qualifier("inquiryServiceImpl") InquiryService inquiryService
    ) {
        this.inquiryService = inquiryService;
    }

    @PreAuthorize("permitAll()") //permit all requests to this endpoint
    @PostMapping(path = "/createInquiry")
    public ResponseEntity<BangerAndCoResponse> createInquiry(@Valid @RequestBody InquiryDTO requestInquiry) {

        requestInquiry.setFirstName(requestInquiry.getFirstName().trim());
        requestInquiry.setLastName(requestInquiry.getLastName().trim());
        requestInquiry.setContactNumber(requestInquiry.getContactNumber().trim());
        requestInquiry.setInquirySubject(requestInquiry.getInquirySubject().trim());
        requestInquiry.setMessage(requestInquiry.getMessage().trim());
        requestInquiry.setEmailAddress(requestInquiry.getEmailAddress().trim().toLowerCase());

        Inquiry savedInquiry = inquiryService.saveInquiry(requestInquiry);

        //return the success message if inquiry is saved successfully
        //if exception occurs will be directed to @ExceptionHandler handling Exception
        BangerAndCoResponse response = new BangerAndCoResponse(
                String.format("Successfully Recorded Inquiry of ID: %s",
                        savedInquiry.getInquiryId()), HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK); //return the response body as JSON
        //JSON Body converted automatically by Jackson Project.
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/all")
    public ResponseEntity<List<InquiryDTO>> getAllPendingInquiries() {
        //an administrator can view all the pending inquiries.
        //inquiry service method will retrieve all the inquiries and return an OK status code to the client
        List<InquiryDTO> allInquiries = inquiryService.getAllPendingInquiries();
        return new ResponseEntity<>(allInquiries, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping(path = "/remove/{id}")
    public ResponseEntity<BangerAndCoResponse> deleteInquiry(@PathVariable(name = "id", required = true) int id) {
        inquiryService.removeInquiry(id);
        return new ResponseEntity<>(
                new BangerAndCoResponse("Inquiry Removed Successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }
}
