package uren.com.myduties.messaging.models;


public class FCMItems {

    String otherUserDeviceToken;
    String title;
    String body;

    public String getOtherUserDeviceToken() {
        return otherUserDeviceToken;
    }

    public void setOtherUserDeviceToken(String otherUserDeviceToken) {
        this.otherUserDeviceToken = otherUserDeviceToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
