package uren.com.myduties.models;

import java.io.Serializable;

public class LoginUser implements Serializable {

    private String userId;
    private String email;
    private String username;
    private String name;
    private String profilePhotoUrl;


    public LoginUser() {
        this.userId = "";
        this.email = "";
        this.username = "";
        this.name= "";
        this.profilePhotoUrl = "";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
}
