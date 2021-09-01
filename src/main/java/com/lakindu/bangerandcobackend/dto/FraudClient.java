package com.lakindu.bangerandcobackend.dto;

import java.time.LocalTime;
import java.util.Date;

public class FraudClient {
    //column names of SQL View being executed in the stored procedure.
    private String first_name;
    private String last_name;
    private String driving_license_number;
    private String claim_id;
    private String claim_type;
    private Date date_of_accident;
    private LocalTime time_of_accident;
    private double claim_amount;

    public FraudClient() {
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getDriving_license_number() {
        return driving_license_number;
    }

    public void setDriving_license_number(String driving_license_number) {
        this.driving_license_number = driving_license_number;
    }

    public String getClaim_id() {
        return claim_id;
    }

    public void setClaim_id(String claim_id) {
        this.claim_id = claim_id;
    }

    public String getClaim_type() {
        return claim_type;
    }

    public void setClaim_type(String claim_type) {
        this.claim_type = claim_type;
    }

    public Date getDate_of_accident() {
        return date_of_accident;
    }

    public void setDate_of_accident(Date date_of_accident) {
        this.date_of_accident = date_of_accident;
    }

    public LocalTime getTime_of_accident() {
        return time_of_accident;
    }

    public void setTime_of_accident(LocalTime time_of_accident) {
        this.time_of_accident = time_of_accident;
    }

    public double getClaim_amount() {
        return claim_amount;
    }

    public void setClaim_amount(double claim_amount) {
        this.claim_amount = claim_amount;
    }

    @Override
    public String toString() {
        return "FraudClient{" +
                "first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", driving_license_number='" + driving_license_number + '\'' +
                ", claim_id='" + claim_id + '\'' +
                ", claim_type='" + claim_type + '\'' +
                ", date_of_accident=" + date_of_accident +
                ", time_of_accident=" + time_of_accident +
                ", claim_amount=" + claim_amount +
                '}';
    }
}
