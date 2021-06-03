package com.lakindu.bangerandcobackend.dto;

import com.lakindu.bangerandcobackend.util.validators.SizeCheck;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.text.NumberFormat;
import java.util.Locale;

public class VehicleTypeDTO {
    private int vehicleTypeId;

    @NotBlank(message = "Please provide a type name")
    @Size(min = 1, max = 50, message = "Please keep type name between 1 and 50 characters")
    private String typeName;

    @NotBlank(message = "Please provide a size for the type of vehicle") //not null and trimmed length is greater than 0
    @SizeCheck() //custom validator
    private String size;

    @NotBlank(message = "Please provide a valid price")
    @Digits(integer = 5, fraction = 3, message = "Please provide a maximum of 5 numerics and 3 decimals for the price per day.")
    private String pricePerDay;

    public int getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(int vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(String pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void showCurrencyOnReturn(double pricePerDay) {
        //when setting price to return to the view, use a currency formatter
        NumberFormat theCurrencyFormatter = NumberFormat.getCurrencyInstance(Locale.UK); //get pound currency
        this.pricePerDay = theCurrencyFormatter.format(pricePerDay);
    }
}
