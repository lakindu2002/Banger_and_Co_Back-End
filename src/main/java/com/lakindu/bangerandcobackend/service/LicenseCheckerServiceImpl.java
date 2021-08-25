package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.LicenseCheckerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

@Service
public class LicenseCheckerServiceImpl implements LicenseCheckerService {
    private String csvFilePath = System.getProperty("user.dir") + File.separator + "csv";
    @Value("${custom.dmv.filename}")
    private String fileName;
    private File theCsvFile;


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
    public HashMap<String, String> isLicenseSuspendedLostStolen(User theCustomer) throws IOException {
        HashMap<String, String> theStatusHolder = new HashMap<>();
        if (theCsvFile.exists()) {
            //csv exists
            HashMap<String, String> csvOutput = loadCsvData(); //read the csv.
            return theStatusHolder;
        } else {
            //csv does not exist.
            throw new IOException("The File Provided By The Department of Motor Vehicles Could Not Be Located");
        }
    }

    private HashMap<String, String> loadCsvData() throws IOException {
        //read the content of the csv file and load the data onto a hashmap
        HashMap<String, String> csvData = new HashMap<>();

        //use buffered reader to read line by line from the csv file
        //the file contains two columns
        BufferedReader theReader = new BufferedReader(new FileReader(theCsvFile));

        String readLine; //expected line read : LICENSE,STATUS
        while ((readLine = theReader.readLine()) != null) {
            //while not at the end of the file, when at the end of the file: readLine returns null.

            //since csv, seperated by a COMMA so split by comma to obtain the license number and status.
            String[] licenseAndStatus = readLine.split(",");//split by the comma delimiter
            if (licenseAndStatus.length == 2) {
                //if two indexes are present.
                csvData.put(licenseAndStatus[0], licenseAndStatus[1].toLowerCase().trim()); //attach as USER,STATUS
            }
        }
        theReader.close(); //close the read upon reading successfully.
        return csvData;
    }
}
