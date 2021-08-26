package com.lakindu.bangerandcobackend.util.scheduledjobs;

import com.lakindu.bangerandcobackend.serviceinterface.RentalService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class ScheduledJobsManagement {
    @Value("${custom.dmv.user}")
    private String dmvUsername;

    @Value("${custom.dmv.password}")
    private String dmvPassword;

    @Value("${custom.dmv.registration}")
    private String dmvRegistrationNumber; //GOES IN THE `Registration` REQUEST HEADER FOR COMPANY VERIFICATION

    @Value("${custom.dmv.endpoint}")
    private String apiEndPoint;

    @Value("${custom.dmv.filename}")
    private String saveFileName;

    private final RentalService rentalService;

    private HttpHeaders httpHeaders;
    private final Logger LOGGER = Logger.getLogger(ScheduledJobsManagement.class.getName());
    private final String folderForCsv = System.getProperty("user.dir") + "/csv";

    @Autowired
    public ScheduledJobsManagement(@Qualifier("rentalServiceImpl") RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostConstruct
    public void constructAuthHeader() {
        //in basic auth: Basic Base64(username:password)
        String credentialsToBeEncoded = String.format("%s:%s", dmvUsername, dmvPassword);
        //create a request header to attach the basic auth token for authentication from dmv server.
        String basicAuthHeader = Base64.getEncoder().encodeToString(credentialsToBeEncoded.getBytes(StandardCharsets.UTF_8));
        httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", String.format("Basic %s", basicAuthHeader));
        httpHeaders.add("Registration", dmvRegistrationNumber); //Registration number must be in request header for authentication.

        File theFolder = new File(folderForCsv);
        if (!theFolder.exists()) {
            theFolder.mkdir(); //create the folder if it doesn't exist.
        }
    }

    //scheduled to run everyday at 12:01AM
    //seconds - minute - hour - day of month - month - day of week
    @Scheduled(cron = "00 01 00 * * *")
    public void fetchDmvCsv() {
        try {
            LOGGER.info("Triggering DMV Get CSV Scheduled Job at: " + new Date());
            RestTemplate dmvCall = new RestTemplate();
            //pass the basic auth header
            HttpEntity<?> headerEntity = new HttpEntity<>(httpHeaders);

            //dmv get call
            ResponseEntity<byte[]> getCsvResponse = dmvCall.exchange(
                    URI.create(apiEndPoint),
                    HttpMethod.GET,
                    headerEntity,
                    byte[].class
            );

            if (getCsvResponse.getStatusCode() == HttpStatus.OK) {

                if (getCsvResponse.hasBody()) {
                    //csv file present
                    File theCsv = new File(folderForCsv + File.separator + saveFileName);
                    LOGGER.info("Attempting to Save In: " + theCsv.getPath());
                    if (theCsv.exists()) {
                        //if csv exists, delete the file
                        theCsv.delete();
                        LOGGER.info("Scheduled Job Deleted Existing CSV at: " + new Date());
                    } else {
                        theCsv.createNewFile(); //create new file
                    }

                    byte[] csvFileFromBody = getCsvResponse.getBody();
                    if (csvFileFromBody != null) {
                        //if the csv file is present, flush the binary data to the licenseList.csv located in CSV directory
                        FileOutputStream theOutputStream = new FileOutputStream(theCsv);
                        theOutputStream.write(new String(csvFileFromBody).getBytes(StandardCharsets.UTF_8)); //write the response body, which is the csv returned from server
                        theOutputStream.flush(); //flush contents of stream to the file
                        theOutputStream.close(); //close the stream
                        LOGGER.info("Scheduled Job Successfully Saved CSV at: " + new Date());
                    } else {
                        LOGGER.warning("CSV Body is null");
                    }

                } else {
                    //if ran into an error (return code anything other than 200)
                    LOGGER.warning("DMV Call Returned No CSV at: " + new Date());
                }
            } else {
                LOGGER.warning("Call Returned With Response: " + getCsvResponse.getStatusCode());
            }
        } catch (Exception ex) {
            LOGGER.warning("We ran into an error while fetching the CSV: " + ex.getMessage());
        }
    }

    @Scheduled(cron = "00 00 23 * * *")
    public void blackListScheduledJob() {
        //create a scheduled job to blacklist customers everyday at 11:00pm
        LOGGER.info("TRIGGERED BLACK LIST SCHEDULED JOB AT: " + new Date());
        try {
            rentalService.blacklistCustomers();
            LOGGER.info("BLACK LIST JOB SUCCESSFULLY COMPLETED AT: " + new Date());
        } catch (ResourceNotFoundException e) {
            LOGGER.warning("BLACK LIST JOB FAILED AT: " + new Date());
            LOGGER.warning("ERROR: " + e.getMessage());
        }
    }
}
