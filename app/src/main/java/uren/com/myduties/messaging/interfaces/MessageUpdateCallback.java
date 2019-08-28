package uren.com.myduties.messaging.interfaces;

public interface MessageUpdateCallback {
    void onComplete();
    void onFailed(String errMessage);
}
