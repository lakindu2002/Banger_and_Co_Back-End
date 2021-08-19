package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.ScrapeDTO;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface WebScraper {
    List<ScrapeDTO> scrapePrices() throws IOException, ParseException;
}
