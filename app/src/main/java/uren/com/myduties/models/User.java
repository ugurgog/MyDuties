package uren.com.myduties.models;


import java.util.List;

public class User {

    private String userid = null;
    private String name = null;
    private String username = null;
    private String email = null;
    private String profilePhotoUrl = null;
    private Phone phone;
    private List<String> groupIdList;
    private boolean admin;

    public User() {
    }

    public User(String userid, String name, String username, String email, String profilePhotoUrl,
                Phone phone, List<String> groupIdList) {
        this.userid = userid;
        this.name = name;
        this.username = username;
        this.email = email;
        this.profilePhotoUrl = profilePhotoUrl;
        this.phone = phone;
        this.groupIdList = groupIdList;
    }

    public User(String userid) {
        this.userid = userid;
    }

    public User(String userid, String name, String profilePhotoUrl) {
        this.userid = userid;
        this.name = name;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePhotoUrl() {
        return this.profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public List<String> getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(List<String> groupIdList) {
        this.groupIdList = groupIdList;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
