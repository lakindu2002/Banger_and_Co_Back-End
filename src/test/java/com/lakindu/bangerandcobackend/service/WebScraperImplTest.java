package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.ScrapeDTO;
import com.lakindu.bangerandcobackend.serviceinterface.WebScraper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebScraperImplTest {

    @Autowired
    @Qualifier("webScraperImpl")
    private WebScraper webScraper;
    private Logger LOGGER = Logger.getLogger(WebScraperImplTest.class.getName());

    @Test
    void testShouldScrapePricesFromMalkey() {
        try {
            List<ScrapeDTO> scrapeDTOS = webScraper.scrapePrices();
            assertThat(scrapeDTOS.size()).isGreaterThan(0);
            LOGGER.info("testShouldScrapePricesFromMalkey: PASSED");
        } catch (IOException | ParseException e) {
            fail("testShouldScrapePricesFromMalkey: FAILED");
        }
    }
}