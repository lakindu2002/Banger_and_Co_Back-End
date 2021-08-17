package com.lakindu.bangerandcobackend.dto;

import java.util.List;

public class ScrapeDTO {
    private String vehicleType;
    private List<PriceList> thePriceList;

    public static class PriceList {
        private String vehicleName;
        private double pricePerMonth;
        private double pricePerWeek;
        private double pricePerDay;

        public PriceList() {
        }

        public String getVehicleName() {
            return vehicleName;
        }

        public void setVehicleName(String vehicleName) {
            this.vehicleName = vehicleName;
        }

        public double getPricePerMonth() {
            return pricePerMonth;
        }

        public void setPricePerMonth(double pricePerMonth) {
            this.pricePerMonth = pricePerMonth;
        }

        public double getPricePerWeek() {
            return pricePerWeek;
        }

        public void setPricePerWeek(double pricePerWeek) {
            this.pricePerWeek = pricePerWeek;
            //price per week / 7 = price per day
            double pricePerDay = this.pricePerWeek / 7;
            setPricePerDay(pricePerDay);
        }

        public double getPricePerDay() {
            return pricePerDay;
        }

        public void setPricePerDay(double pricePerDay) {
            this.pricePerDay = pricePerDay;
        }
    }
}
