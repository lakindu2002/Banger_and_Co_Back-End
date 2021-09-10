package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.dto.InquiryReplyDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.serviceinterface.InquiryService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InquiryServiceImplTest {

    @Autowired
    private CreationUtil creationUtil;
    @Autowired
    private InquiryService inquiryService;
    private List<Inquiry> inquiryList = new ArrayList<>();
    private Inquiry inquiryToRemove;
    private Inquiry inquiryToGetDetailedInformationOnAndReply;

    private Logger LOGGER = Logger.getLogger(InquiryServiceImplTest.class.getName());

    @BeforeEach
    void setUp() {
        inquiryList = creationUtil.createInquiries();
        inquiryToRemove = inquiryList.get(0);
        inquiryToGetDetailedInformationOnAndReply = inquiryList.get(1);
    }

    @AfterEach
    void tearDown() {
        inquiryList.clear();
        creationUtil.deleteInquiries();
        inquiryToRemove = null;
        inquiryToGetDetailedInformationOnAndReply = null;
    }

    @Test
    void testShouldCreateAnInquirySuccessfully() {
        InquiryDTO theDTO = new InquiryDTO();
        theDTO.setInquirySubject("Test Subject");
        theDTO.setMessage("Test Message");
        theDTO.setEmailAddress("lakinduhewa@gmail.com");
        theDTO.setContactNumber("0777784512");
        theDTO.setFirstName("John");
        theDTO.setLastName("Doe");

        Inquiry inquiry = inquiryService.saveInquiry(theDTO);
        assertThat(inquiry).isNotNull();
        LOGGER.info("testShouldCreateAnInquirySuccessfully: PASSED");
    }

    @Test
    void testShouldGetAllPendingInquiries() {
        List<InquiryDTO> allPendingInquiries = inquiryService.getAllPendingInquiries();
        assertThat(allPendingInquiries.size()).isGreaterThan(0);
        LOGGER.info("testShouldGetAllPendingInquiries: PASSED");
    }

    @Test
    void testShouldRemoveAnInquirySuccessfully() {
        int idToRemove = inquiryToRemove.getInquiryId();
        assertDoesNotThrow(() -> inquiryService.removeInquiry(idToRemove));
        LOGGER.info("testShouldRemoveAnInquirySuccessfully: PASSED");
    }

    @Test
    void testShouldNotRemoveAnInquiryWhenIdDoesNotExist() {
        int idToRemove = 1000;
        assertThrows(ResourceNotFoundException.class, () -> inquiryService.removeInquiry(idToRemove));
        LOGGER.info("testShouldNotRemoveAnInquiryWhenIdDoesNotExist: PASSED");
    }

    @Test
    void testShouldGetDetailedInquiryInformation() {
        try {
            int idToGetDetailed = inquiryToGetDetailedInformationOnAndReply.getInquiryId();
            InquiryDTO theInquiry = inquiryService.getDetailedInquiry(idToGetDetailed);
            assertThat(theInquiry.getInquiryId()).isEqualTo(idToGetDetailed);
            LOGGER.info("testShouldGetDetailedInquiryInformation: PASSED");
        } catch (Exception e) {
            fail("testShouldGetDetailedInquiryInformation: FAILED");
        }
    }

    @Test
    void testShouldNotGetDetailedInformationWhenIdIsInvalid() {
        int idToGetDetailed = 1000;
        assertThrows(ResourceNotFoundException.class, () -> inquiryService.getDetailedInquiry(idToGetDetailed));
        LOGGER.info("testShouldNotGetDetailedInformationWhenIdIsInvalid: PASSED");
    }
}