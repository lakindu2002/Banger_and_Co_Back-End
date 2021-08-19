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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class WebScraperImpl implements WebScraper {

    @Value("${custom.web.scrape.url}")
    private String scrapeUrl;
    //utilize the library JSoup to scrape data off of the Malkey car rentals.
    //no car rental platform has requirements of Banger - Price assigned to Vehicle Type rather than Vehicle.
    //therefore, stuck to Malkey to implement web scraping.

    private Connection networkConnectionToMalkey;
    private NumberFormat numberFormat;

    @PostConstruct
    public void init() {
        networkConnectionToMalkey = Jsoup
                .connect(scrapeUrl)
                .userAgent("Mozilla")
                .timeout(60000); //timeout after 1 minute.
        //open a connection to the malkey self driver rate section using Mozilla User Agent
        numberFormat = NumberFormat.getCurrencyInstance();
    }

    @Override
    public List<ScrapeDTO> scrapePrices() throws IOException, ParseException {
        List<ScrapeDTO> scrapedPrices = new ArrayList<>();

        //load the web page of the self drive rates for vehicles.
        Document theLoadedWebPage = networkConnectionToMalkey.get(); //load the scrape HTML (Headless GUI) to scrape the DOM elements

        Elements selectedTable = theLoadedWebPage.select("table.table, selfdriverrates"); //<table class="table selfdriverrates">
        Elements selectedTableBody = selectedTable.select("tbody"); //select the table body of the table.
        Elements allVehicleTypes = selectedTableBody.select("td.lightgray, align-left"); //<td class="lightgray alignleft">


        //insert the vehicle types onto the return collection first.
        for (Element eachType : allVehicleTypes) {
            ScrapeDTO eachVehicleTypeAndVehicles = new ScrapeDTO();
            //initially assign empty vehicle collection for each type
            eachVehicleTypeAndVehicles.setTheVehicleInformation(new ArrayList<>());

            String vehicleTypeOnDom = eachType.text().trim(); //type text present in the <td class="lightgray align-left">
            if (vehicleTypeOnDom.length() == 0) {
                //handle exceptions as vehicle type is not valid on the DOM
                eachVehicleTypeAndVehicles.setVehicleType("Type Not Available");
            } else {
                //assign the type.
                eachVehicleTypeAndVehicles.setVehicleType(vehicleTypeOnDom);
            }
            scrapedPrices.add(eachVehicleTypeAndVehicles);  //add each pojo for a scrape onto the return list that can be used in the below for loop
        }

        //add the vehicle information for each type by utilizing the scrapedPrices array list
        List<ScrapeDTO.VehicleInformation> vehicleInformationForType = new ArrayList<>(); //assignment changed by referring to array list assigned in each ScrapeDTO

        //iterate over each row in the table
        for (Element eachRow : selectedTableBody.select("tr")) {
            Elements tdPerRow = eachRow.getElementsByTag("td"); //retrieve all <td> per <tr>
            ScrapeDTO.VehicleInformation eachVehicle = new ScrapeDTO.VehicleInformation(); //each <tr> would comprise of one vehicle so one vehicle per <tr>

            //iterate over each <td> in each <tr>
            for (Element eachTd : tdPerRow) {
                if (eachTd.hasClass("lightgray align-left")) {
                    //the vehicle type <td> has this class so element is actually containing the type of vehicle
                    vehicleInformationForType = getVehicleInformationForType(eachTd.text().toLowerCase(), scrapedPrices);
                } else {
                    //actual vehicle information
                    Element vehicleNameElement = eachRow.child(0); //first child : first <td> is always vehicle name

                    if (!vehicleNameElement.text().equalsIgnoreCase("Mercedes, BMW & Continental Cars")) {
                        //do not add the mercedes to return as there are no prices for the mercedes.
                        Element ratePerMonthElement = eachRow.child(1);
                        Element ratePerWeekElement = eachRow.child(2);
                        //not utilizing the last child, excess mileage.

                        if (vehicleNameElement.text().trim().length() == 0) {
                            //if no vehicle name, do not assign vehicle to the return
                            eachVehicle.setVehicleName(null);
                        } else {
                            eachVehicle.setVehicleName(vehicleNameElement.text().trim());
                        }

                        if (ratePerMonthElement.text().trim().length() == 0) {
                            //in DOM no rate per month, so set as 0
                            eachVehicle.setPricePerMonth(0);
                        } else {
                            //append a $ to support parsing to double by the NumberFormatter.
                            String retrievedRatePerMonth = String.format(Locale.ENGLISH, "$%s", ratePerMonthElement.text().trim());
                            eachVehicle.setPricePerMonth(numberFormat.parse(retrievedRatePerMonth).doubleValue());
                        }

                        if (ratePerWeekElement.text().trim().length() == 0) {
                            //in DOM no rate per week, so set as 0
                            eachVehicle.setPricePerWeek(0);
                        } else {
                            //append a $ to support parsing to double by the NumberFormatter.
                            String retrievedRatePerWeek = String.format(Locale.ENGLISH, "$%s", ratePerWeekElement.text().trim());
                            eachVehicle.setPricePerWeek(numberFormat.parse(retrievedRatePerWeek).doubleValue()); //auto trigger price per day calculation.
                        }
                    }
                }
            }
            if (eachVehicle.getVehicleName() != null) {
                //if the vehicle name is present then add the data to the final return.
                //in benz, vehicle name is not set so left as null hence null check is done.
                vehicleInformationForType.add(eachVehicle);
            }
        }

        //calculate average price per day
        scrapedPrices.forEach(ScrapeDTO::calculateAveragePricePerDay);
        return scrapedPrices;
    }

    private List<ScrapeDTO.VehicleInformation> getVehicleInformationForType(String providedType, List<ScrapeDTO> scrapedPrices) {
        for (ScrapeDTO eachDTO : scrapedPrices) {
            //if the type provided in the <td> of the DOM is the same type found in an object of scrapedPrices, return the supporting vehicle list for the type.
            if (eachDTO.getVehicleType().equalsIgnoreCase(providedType)) {
                return eachDTO.getTheVehicleInformation();
            }
        }
        return new ArrayList<>();
    }
}


    /*
    Sample Output dump

        <td colspan="4" class="lightgray align-left"> GENERAL CARS </td>
        ----------------------------------
        <td class="text-left percent-40">Suzuki Alto - Premium - Manual</td>
        <td class="text-center percent-22">49,500.00</td>
        <td class="text-center percent-22">15,000.00</td>
        <td class="text-center percent-22">30.00</td>
        ----------------------------------
        <td class="text-left percent-40">Suzuki Alto K10 - Auto</td>
        <td class="text-center percent-22">55,000.00</td>
        <td class="text-center percent-22">18,500.00</td>
        <td class="text-center percent-22">35.00</td>
        ----------------------------------
        <td class="text-left percent-40">Suzuki Celerio - Auto</td>
        <td class="text-center percent-22">59,500.00</td>
        <td class="text-center percent-22">20,000.00</td>
        <td class="text-center percent-22">35.00</td>
        ----------------------------------
        <td class="text-left percent-40">Perodua (Daihatsu) Axia - Auto</td>
        <td class="text-center percent-22">69,500.00</td>
        <td class="text-center percent-22">22,500.00</td>
        <td class="text-center percent-22">35.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Prius C/ Aqua - Auto</td>
        <td class="text-center percent-22">85,000.00</td>
        <td class="text-center percent-22">27,500.00</td>
        <td class="text-center percent-22">49.50</td>
        ----------------------------------
        <td colspan="4" class="lightgray align-left"> PREMIUM CARS </td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Corolla Axio/ NZE141</td>
        <td class="text-center percent-22">85,000.00</td>
        <td class="text-center percent-22">27,500.00</td>
        <td class="text-center percent-22">49.50</td>
        ----------------------------------
        <td class="text-left percent-40">Perodua Bezza Prime Sedan - Auto (2017)</td>
        <td class="text-center percent-22">85,000.00</td>
        <td class="text-center percent-22">27,500.00</td>
        <td class="text-center percent-22">49.50</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Allion NZT 260</td>
        <td class="text-center percent-22">120,000.00</td>
        <td class="text-center percent-22">40,000.00</td>
        <td class="text-center percent-22">60.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Axio NKR 165</td>
        <td class="text-center percent-22">135,000.00</td>
        <td class="text-center percent-22">45,000.00</td>
        <td class="text-center percent-22">65.00</td>
        ----------------------------------
        <td colspan="4" class="lightgray align-left"> LUXURY CARS </td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Premio</td>
        <td class="text-center percent-22">175,000.00</td>
        <td class="text-center percent-22">59,500.00</td>
        <td class="text-center percent-22">85.00</td>
        ----------------------------------
        <td>Mercedes, BMW &amp; Continental Cars</td>
        <td colspan="3" class="text-center">By Special Arrangement</td>
        ----------------------------------
        <td colspan="4" class="lightgray align-left"> VANS </td>
        ----------------------------------
        <td class="text-left percent-40">Mitsubishi-l 300-(6-9 Psgr) Dual A/C</td>
        <td class="text-center percent-22">89,500.00</td>
        <td class="text-center percent-22">30,000.00</td>
        <td class="text-center percent-22">45.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Regius - Petrol Auto (9 Pax) Dual A/C</td>
        <td class="text-center percent-22">95,000.00</td>
        <td class="text-center percent-22">35,000.00</td>
        <td class="text-center percent-22">50.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Regius Diesel Auto (9 Pax) Dual A/C</td>
        <td class="text-center percent-22">120,000.00</td>
        <td class="text-center percent-22">40,000.00</td>
        <td class="text-center percent-22">55.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Hiace 184 Commuter (9 Pax) Dual A/C</td>
        <td class="text-center percent-22">120,000.00</td>
        <td class="text-center percent-22">40,000.00</td>
        <td class="text-center percent-22">55.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Hiace New Luxury (15 Pax) Line A/C </td>
        <td class="text-center percent-22">165,000.00</td>
        <td class="text-center percent-22">55,000.00</td>
        <td class="text-center percent-22">65.00</td>
        ----------------------------------
        <td colspan="4" class="lightgray align-left"> SUV &amp; 4WD </td>
        ----------------------------------
        <td class="text-left percent-40">Mitsubishi L200 Double Cab 4wd</td>
        <td class="text-center percent-22">145,000.00</td>
        <td class="text-center percent-22">50,000.00</td>
        <td class="text-center percent-22">65.00</td>
        ----------------------------------
        <td class="text-left percent-40">Daihatsu Terios 4WD</td>
        <td class="text-center percent-22">120,000.00</td>
        <td class="text-center percent-22">40,000.00</td>
        <td class="text-center percent-22">65.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Rav 4 / Kia Sportage</td>
        <td class="text-center percent-22">145,000.00</td>
        <td class="text-center percent-22">50,000.00</td>
        <td class="text-center percent-22">85.00</td>
        ----------------------------------
        <td class="text-left percent-40">Suzuki Vitara</td>
        <td class="text-center percent-22">175,000.00</td>
        <td class="text-center percent-22">60,000.00</td>
        <td class="text-center percent-22">85.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Rush 7 Seater</td>
        <td class="text-center percent-22">195,000.00 </td>
        <td class="text-center percent-22">65,000.00</td>
        <td class="text-center percent-22">95.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Landcruiser Prado – TRJ 120</td>
        <td class="text-center percent-22">265,000.00</td>
        <td class="text-center percent-22">95,000.00</td>
        <td class="text-center percent-22">120.00</td>
        ----------------------------------
        <td class="text-left percent-40">Toyota Prado 150/ Sahara 100 V8</td>
        <td class="text-center percent-22">350,000.00</td>
        <td class="text-center percent-22">115,000.00</td>
        <td class="text-center percent-22">145.00</td>
        ----------------------------------
     */
