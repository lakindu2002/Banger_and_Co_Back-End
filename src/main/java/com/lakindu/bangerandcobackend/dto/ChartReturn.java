package com.lakindu.bangerandcobackend.dto;

import java.util.ArrayList;
import java.util.List;

public class ChartReturn {
    private String month;
    private int count;
    private double totalForTheMonth;
    private List<RentalShowDTO> rentals = new ArrayList<>();

    public ChartReturn() {
    }

    public ChartReturn(String month) {
        this.month = month;
    }

    public double getTotalForTheMonth() {
        return totalForTheMonth;
    }

    public void setTotalForTheMonth(double totalForTheMonth) {
        this.totalForTheMonth = totalForTheMonth;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<RentalShowDTO> getRentals() {
        return rentals;
    }

    public void setRentals(List<RentalShowDTO> rentals) {
        this.rentals = rentals;
    }

    public void addRental(RentalShowDTO eachRental) {
        rentals.add(eachRental);
    }
}
