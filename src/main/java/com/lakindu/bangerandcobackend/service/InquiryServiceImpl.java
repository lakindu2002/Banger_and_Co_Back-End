package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.dto.InquiryReplyDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.repository.InquiryRepository;
import com.lakindu.bangerandcobackend.serviceinterface.InquiryService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.mailsender.MailSender;
import com.lakindu.bangerandcobackend.util.mailsender.MailSenderHelper;
import com.lakindu.bangerandcobackend.util.mailsender.MailTemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
//create service layer class for cross interaction of services
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MailSender theMailSender;
    private final UserService userService;

    //autowire the inquiry repository for dependency injection
    //autowire the inquiryRepository bean
    @Autowired
    public InquiryServiceImpl(@Qualifier("inquiryRepository") InquiryRepository inquiryRepository,
                              @Qualifier("mailSender") MailSender theMailSender,
                              @Qualifier("userServiceImpl") UserService userService) {
        this.inquiryRepository = inquiryRepository;
        this.theMailSender = theMailSender;
        this.userService = userService;
    }

    @Override
    public Inquiry saveInquiry(InquiryDTO requestInquiry) {
        Inquiry theSubmittingInquiry = new Inquiry();
        //construct the entity object to be persisted onto the database via JPA
        theSubmittingInquiry.calculateLodgedTime();
        theSubmittingInquiry.setReplied(false);
        theSubmittingInquiry.setFirstName(requestInquiry.getFirstName());
        theSubmittingInquiry.setLastName(requestInquiry.getLastName());
        theSubmittingInquiry.setInquirySubject(requestInquiry.getInquirySubject());
        theSubmittingInquiry.setMessage(requestInquiry.getMessage());
        theSubmittingInquiry.setContactNumber(requestInquiry.getContactNumber());
        theSubmittingInquiry.setEmailAddress(requestInquiry.getEmailAddress().toLowerCase());

        //method to save the inquiry in the database
        //let the JAVA EE - Java Persistence API handle the underlying SQL
        return inquiryRepository.save(theSubmittingInquiry);
    }

    @Override
    public List<InquiryDTO> getAllPendingInquiries() {
        //service layer method used to get inquiries that are pending
        final List<Inquiry> inquiryList = inquiryRepository.getAllPendingInquiries();
        List<InquiryDTO> theInquiryReturn = new ArrayList<>();

        for (Inquiry eachInquiry : inquiryList) {
            theInquiryReturn.add(getTheReturnConstructed(eachInquiry));
        }
        return theInquiryReturn;
    }

    @Override
    public void removeInquiry(int id) throws ResourceNotFoundException {
        if (inquiryRepository.existsById(id)) {
            inquiryRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("The inquiry that you requested to be removed could not be found");
        }
    }

    @Override
    public InquiryDTO getDetailedInquiry(int id) throws ResourceNotFoundException {
        Inquiry theInquiry = inquiryRepository.getDetailedInquiry(id);
        if (theInquiry == null) {
            //if inquiry is not present in DB, show the user a 404 error
            throw new ResourceNotFoundException("The inquiry that you requested could not be found");
        } else {
            //if inquiry object is there in database, return it to the client as a DTO
            return getTheReturnConstructed(theInquiry);
        }
    }

    private InquiryDTO getTheReturnConstructed(Inquiry theInquiry) {
        //map entity to DTO
        InquiryDTO theDTO = new InquiryDTO();

        theDTO.setInquiryId(theInquiry.getInquiryId());
        theDTO.setFirstName(theInquiry.getFirstName());
        theDTO.setLastName(theInquiry.getLastName());
        theDTO.setInquirySubject(theInquiry.getInquirySubject());
        theDTO.setMessage(theInquiry.getMessage());
        theDTO.setEmailAddress(theInquiry.getEmailAddress());
        theDTO.setContactNumber(theInquiry.getContactNumber());
        theDTO.setReplied(theInquiry.isReplied());
        theDTO.setCreatedAt(theInquiry.getCreatedAt());
        theDTO.setResolvedByUsername(null);

        return theDTO;
    }

    @Override
    public void replyToInquiry(InquiryReplyDTO replyDTO, String inquiryReply, Authentication theAuthentication) throws Exception {
        Inquiry theInquiry = inquiryRepository.getDetailedInquiry(Integer.parseInt(replyDTO.getInquiryId()));
        if (theInquiry == null) {
            throw new ResourceNotFoundException("The inquiry does not exist");
        } else {
            //send am email to the client that lodged the email
            theInquiry.setReplied(true); //set the inquiry status to resolved.
            theInquiry.setResolvedBy(userService._getUserWithoutDecompression(theAuthentication.getName()));
            MailSenderHelper theHelper = new MailSenderHelper();
            theHelper.setInquiryReply(inquiryReply);
            theHelper.setTheInquiry(theInquiry);
            theHelper.setSubject(String.format("Replying to Your Inquiry - %s", theInquiry.getInquirySubject()));
            theHelper.setTemplateName(MailTemplateType.INQUIRY_REPLY);

            inquiryRepository.save(theInquiry); //resolve the inquiry in the database
            theMailSender.sendMail(theHelper); //send the mail to the client to lodged the inquiry
        }
    }
}
