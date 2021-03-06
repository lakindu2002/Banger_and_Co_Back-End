package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.DmvValidatorService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class DmvValidatorServiceImpl implements DmvValidatorService {
    private final String csvFilePath = System.getProperty("user.dir") + File.separator + "csv";
    @Value("${custom.dmv.filename}")
    private String fileName;
    private File theCsvFile;
    private final Logger LOGGER = Logger.getLogger(DmvValidatorServiceImpl.class.getName());

    @PostConstruct
    public void init() {
        theCsvFile = new File(csvFilePath + File.separator + fileName);
    }

    /**
     * Method will be used to load the CSV will from the DMV and to check if the customer's license is present.
     * If so, their, rental will be rejected.
     *
     * @param theCustomer The customer trying to make the rental
     * @return The Hashmap containing key: `license status`: VALID, STOLEN, LOST, SUSPENDED
     * @throws IOException Exceptions thrown when reading the file.
     */
    @Override
    public HashMap<String, String> isLicenseSuspendedLostStolen(User theCustomer) throws IOException, ResourceNotFoundException {
        boolean isFound = false;
        if (theCustomer == null) {
            throw new ResourceNotFoundException("The customer was not included for checking against DMV");
        }

        HashMap<String, String> theStatusHolder = new HashMap<>();

        if (theCsvFile.exists()) {
            //csv exists
            String customerLicenseNumber = theCustomer.getDrivingLicenseNumber();
            HashMap<String, String> csvOutput = loadCsvData(); //read the csv.
            for (Map.Entry<String, String> entrySet : csvOutput.entrySet()) {
                //iterate over the hashmap by using the Entry Set, and check if the customer license number is present in the list.
                String eachLicenseNumber = entrySet.getKey();
                String status = entrySet.getValue();

                if (customerLicenseNumber.equalsIgnoreCase(eachLicenseNumber)) {
                    //if license number on iteration matches the license number of the customer
                    //the license is stored in the dmv, therefore, cannot make rental.
                    LOGGER.info("CUSTOMER LICENSE STATUS: " + status.toUpperCase());
                    theStatusHolder.put(DMVType.STATUS_TYPE.value, status); //insert the type of state on the license.
                    isFound = true; //license was found in the DMV
                    break;
                }
            }
            if (!isFound) {
                //license not in DMV list, valid license
                theStatusHolder.put(DMVType.STATUS_TYPE.value, DMVType.VALID.value);
            }
        } else {
            //no csv, return a valid license.
            theStatusHolder.put(DMVType.STATUS_TYPE.value, DMVType.VALID.value);
        }
        return theStatusHolder;
    }

    /**
     * Method will open a stream to the CSV file, if existing, and will retrieve all records of the CSV Will.
     * <br>
     * It will iterate over each entry and insert each license number and status on a hashmap
     * <br>
     *
     * @return Key - <b>License Number</b> and Value - <b>Status (Lost, Reported, Stolen)</b>
     * @throws IOException Throws during the file input
     */
    private HashMap<String, String> loadCsvData() throws IOException {
        //read the content of the csv file and load the data onto a hashmap
        HashMap<String, String> csvData = new HashMap<>();

        //use buffered reader to read line by line from the csv file
        //the file contains two columns
        //csv is saved in utf-8 so read in utf-8 as well.
        BufferedReader theReader = new BufferedReader(new InputStreamReader(new FileInputStream(theCsvFile), StandardCharsets.UTF_8));

        String readLine; //expected line read : LICENSE,STATUS
        while ((readLine = theReader.readLine()) != null) {
            //while not at the end of the file, when at the end of the file: readLine returns null.
            readLine = readLine.trim(); //trim to cut trailing and leading white spaces.
            if (readLine.length() > 0) {
                //since csv, seperated by a COMMA so split by comma to obtain the license number and status.
                String[] licenseAndStatus = readLine.split(",");//split by the comma delimiter
                if (licenseAndStatus.length == 2) {
                    //if two indexes are present.
                    String status = "";
                    switch (licenseAndStatus[1].toLowerCase().trim()) {
                        case "suspended": {
                            status = DMVType.SUSPENDED.value;
                            break;
                        }
                        case "lost": {
                            status = DMVType.LOST.value;
                            break;
                        }
                        case "stolen": {
                            status = DMVType.STOLEN.value;
                            break;
                        }
                    }
                    if (status.length() > 0) {
                        csvData.put(licenseAndStatus[0].trim(), status); //attach as USER,STATUS
                    }
                }
            }
        }
        theReader.close(); //close the read upon reading successfully.
        return csvData;
    }

    enum DMVType {
        STATUS_TYPE("STATUS"),
        SUSPENDED("suspended"),
        STOLEN("stolen"),
        LOST("lost"),
        VALID("valid");

        String value;

        DMVType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
