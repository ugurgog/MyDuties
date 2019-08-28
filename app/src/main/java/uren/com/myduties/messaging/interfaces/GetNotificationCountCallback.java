package uren.com.myduties.messaging.interfaces;

public interface GetNotificationCountCallback {
    void onReadCount(int count);
    void onSendCount(int count);
    void onDeleteCount(int count);
    void onNotifStatus(String status);
    void onClusterNotifStatus(String status);
    void onFailed(String errMessage);
}
