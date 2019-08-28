package uren.com.myduties.messaging.interfaces;

import uren.com.myduties.messaging.models.TokenInfo;

public interface GetDeviceTokenCallback {
    void onSuccess(TokenInfo tokenInfo);
}
