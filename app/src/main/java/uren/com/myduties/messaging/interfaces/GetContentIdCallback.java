package uren.com.myduties.messaging.interfaces;

public interface GetContentIdCallback {
    void onSuccess(String contentId);
    void onError(String errMessage);
}
