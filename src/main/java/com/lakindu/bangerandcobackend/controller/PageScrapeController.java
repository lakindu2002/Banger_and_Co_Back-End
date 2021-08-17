package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.ScrapeDTO;
import com.lakindu.bangerandcobackend.serviceinterface.WebScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/api/scrape")
@PreAuthorize("isAuthenticated()")
public class PageScrapeController {

    private final WebScraper webScraper;

    @Autowired
    public PageScrapeController(
            @Qualifier("webScraperImpl") WebScraper webScraper
    ) {
        this.webScraper = webScraper;
    }

    @GetMapping(path = "/prices")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<List<ScrapeDTO>> scrapePrices() throws IOException {
        List<ScrapeDTO> scrapedPrices = webScraper.scrapePrices();
        return new ResponseEntity<>(scrapedPrices, HttpStatus.OK);
    }
}
