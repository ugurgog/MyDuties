package uren.com.myduties.models;

import java.math.BigDecimal;

public class Phone {

    private String countryCode;
    private String dialCode ;
    private long phoneNumber;

    public Phone() {
    }

    public Phone(String countryCode, String dialCode, long phoneNumber) {
        this.countryCode = countryCode;
        this.dialCode = dialCode;
        this.phoneNumber = phoneNumber;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDialCode() {
        return this.dialCode;
    }

    public void setDialCode(String dialCode) {
        this.dialCode = dialCode;
    }

    public long getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
