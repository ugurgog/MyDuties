package uren.com.myduties.models;

public class Country {
    private String code = null;
    private String name = null;
    private String dial_code = null;

    public Country() {
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDialCode() {
        return this.dial_code;
    }

    public void setDialCode(String dial_code) {
        this.dial_code = dial_code;
    }
}