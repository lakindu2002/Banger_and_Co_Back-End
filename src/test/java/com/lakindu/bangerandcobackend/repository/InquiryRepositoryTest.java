package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Inquiry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InquiryRepositoryTest {

    @Autowired
    private InquiryRepository inquiryRepository;
    private Inquiry createdInquiry;

    private Logger LOGGER = Logger.getLogger(InquiryRepositoryTest.class.getName());

    @BeforeEach
    void setUp() {
        Inquiry theInquiryOne = new Inquiry();
        theInquiryOne.setInquirySubject("Test");
        theInquiryOne.setMessage("Test");
        theInquiryOne.setFirstName("John");
        theInquiryOne.setLastName("Doe");
        theInquiryOne.setContactNumber("0777790806");
        theInquiryOne.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        theInquiryOne.setEmailAddress("johndoe@gmail.com");
        theInquiryOne.setReplied(false);

        createdInquiry = inquiryRepository.save(theInquiryOne);
    }

    @AfterEach
    void tearDown() {
        createdInquiry = null;
        inquiryRepository.deleteAll();
    }

    @Test
    void testShouldGetAllPendingInquiries() {
        List<Inquiry> allPendingInquiries = inquiryRepository.getAllPendingInquiries();
        assertThat(allPendingInquiries.size()).isEqualTo(1);
        LOGGER.info("testShouldGetAllPendingInquiries: PASSED");
    }

    @Test
    void testShouldGetDetailedInquiry() {
        int inquiryToGet = createdInquiry.getInquiryId();
        Inquiry detailedInquiry = inquiryRepository.getDetailedInquiry(inquiryToGet);
        assertThat(detailedInquiry.getInquiryId()).isEqualTo(inquiryToGet);
        LOGGER.info("testShouldGetDetailedInquiry: PASSED");
    }
}