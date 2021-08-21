package com.lakindu.bangerandcobackend.util.scheduledjobs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ScheduledJobsManagement {
    @Value("${custom.dmv.user}")
    private String dmvUsername;

    @Value("${custom.dmv.password}")
    private String dmvPassword;

    @Value("${custom.dmv.endpoint}")
    private String apiEndPoint;

    private String basicAuthHeader;
    private HttpHeaders httpHeaders;

    @PostConstruct
    public void constructAuthHeader() {
        //in basic auth: Basic Base64(username:password)
        String credentialsToBeEncoded = String.format("%s:%s", dmvUsername, dmvPassword);
        //create a request header to attach the basic auth token for authentication from dmv server.
        basicAuthHeader = Base64.getEncoder().encodeToString(credentialsToBeEncoded.getBytes(StandardCharsets.UTF_8));
        httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", String.format("Basic %s", basicAuthHeader));
    }

    //cron(Minutes Hours Day-of-month Month Day-of-week Year)
    //scheduled to run everyday at 12:01AM
    @Scheduled(cron = "* * * * * *")
    public void fetchDmvCsv() {
    }
}
