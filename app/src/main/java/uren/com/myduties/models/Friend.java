package uren.com.myduties.models;

public class Friend {
    User user;
    String friendStatus;

    public Friend() {
    }

    public Friend(User user, String friendStatus) {
        this.user = user;
        this.friendStatus = friendStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }
}
