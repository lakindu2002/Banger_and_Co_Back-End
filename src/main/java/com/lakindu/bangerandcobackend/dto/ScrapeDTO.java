package com.lakindu.bangerandcobackend.dto;

import java.util.List;

public class ScrapeDTO {
    private String vehicleType;
    private List<VehicleInformation> theVehicleInformation;
    private double averagePricePerDay;

    public String getVehicleType() {
        return vehicleType;
    }

    public double getAveragePricePerDay() {
        return averagePricePerDay;
    }

    public void setAveragePricePerDay(double averagePricePerDay) {
        this.averagePricePerDay = averagePricePerDay;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public List<VehicleInformation> getTheVehicleInformation() {
        return theVehicleInformation;
    }

    public void setTheVehicleInformation(List<VehicleInformation> theVehicleInformation) {
        this.theVehicleInformation = theVehicleInformation;
    }

    public void calculateAveragePricePerDay() {
        double totalCostPerDayPerType = 0;
        //calculate average price per day
        for (VehicleInformation eachVehicle : this.theVehicleInformation) {
            totalCostPerDayPerType = totalCostPerDayPerType + eachVehicle.getPricePerDay();
        }
        //average
        this.averagePricePerDay = totalCostPerDayPerType / theVehicleInformation.size(); //total/size = average
    }

    public static class VehicleInformation {
        private String vehicleName;
        private double pricePerMonth;
        private double pricePerWeek;
        private double pricePerDay;

        public VehicleInformation() {
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
