package uren.com.myduties.messaging.interfaces;

public interface MessageSentFCMCallback {
    void onSuccess();
    void onFailed(Exception e);
}
