package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InquiryControllerTest {
    @LocalServerPort
    private int serverPort;
    @Autowired
    private CreationUtil creationUtil;
    private final TestRestTemplate testRestTemplate = new TestRestTemplate();
    private final Logger LOGGER = Logger.getLogger(InquiryControllerTest.class.getName());

    @Test
    void testShouldCreateAnInquirySuccessfully() {
        InquiryDTO create = new InquiryDTO();
        create.setFirstName("John");
        create.setLastName("Doe");
        create.setInquirySubject("Test");
        create.setMessage("Test Message");
        create.setEmailAddress("Test@test.com");
        create.setContactNumber("0774125355");

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "inquiry/createInquiry"),
                HttpMethod.POST,
                new HttpEntity<>(create),
                BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldCreateAnInquirySuccessfully: PASSED");
    }

    @Test
    void testShouldNotCreateAnInquiryWhenSubjectIsMissing() {
        InquiryDTO create = new InquiryDTO();
        create.setFirstName("John");
        create.setLastName("Doe");
        create.setInquirySubject("");
        create.setMessage("Test Message");
        create.setEmailAddress("Test@test.com");
        create.setContactNumber("0774125355");

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "inquiry/createInquiry"),
                HttpMethod.POST,
                new HttpEntity<>(create),
                BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateAnInquiryWhenSubjectIsMissing: PASSED");
    }

    @Test
    void testShouldNotCreateAnInquiryWhenMessageIsMissing() {
        InquiryDTO create = new InquiryDTO();
        create.setFirstName("John");
        create.setLastName("Doe");
        create.setInquirySubject("Test");
        create.setMessage("");
        create.setEmailAddress("Test@test.com");
        create.setContactNumber("0774125355");

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "inquiry/createInquiry"),
                HttpMethod.POST,
                new HttpEntity<>(create),
                BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateAnInquiryWhenMessageIsMissing: PASSED");
    }

    @Test
    void testShouldNotCreateAnInquiryWhenInputsAreInvalid() {
        InquiryDTO create = new InquiryDTO();
        create.setFirstName("");
        create.setLastName("");
        create.setInquirySubject("");
        create.setMessage("Test ");
        create.setEmailAddress("Testtest.com");
        create.setContactNumber("0774125355");

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "inquiry/createInquiry"),
                HttpMethod.POST,
                new HttpEntity<>(create),
                BangerAndCoResponse.class
        );
        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateAnInquiryWhenInputsAreInvalid: PASSED");
    }
}