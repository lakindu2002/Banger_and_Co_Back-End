package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.ScrapeDTO;
import com.lakindu.bangerandcobackend.serviceinterface.WebScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class WebScraperImpl implements WebScraper {

    @Value("${custom.web.scrape.url}")
    private String scrapeUrl;
    private Logger LOGGER;
    //utilize the library JSoup to scrape data off of the Malkey car rentals.
    //no car rental platform has requirements of Banger - Price assigned to Vehicle Type rather than Vehicle.
    //therefore, stuck to Malkey to implement web scraping.

    private Connection networkConnectionToMalkey;

    @PostConstruct
    public void init() {
        networkConnectionToMalkey = Jsoup
                .connect(scrapeUrl)
                .userAgent("Mozilla");
        //open a connection to the malkey self driver rate section using Mozilla User Agent
        LOGGER = Logger.getLogger(WebScraperImpl.class.getName());
    }

    @Override
    public List<ScrapeDTO> scrapePrices() throws IOException {
        Document theLoadedWebPage = networkConnectionToMalkey.get(); //load the scrape HTML (Headless GUI) to scrape the DOM elements
        //retrieve the content on the tag <table class="table selfdriverrates">
        //this contains information about vehicles and rates.
        Elements selectedTable = theLoadedWebPage.select("table.table, selfdriverrates");
        LOGGER.info(selectedTable.html());

        return new ArrayList<>();
    }
}
