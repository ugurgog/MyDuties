package uren.com.myduties.messaging;

import android.content.Context;

import uren.com.myduties.dbManagement.TokenDBHelper;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.messaging.interfaces.MessageSentFCMCallback;
import uren.com.myduties.messaging.models.FCMItems;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.User;

public class NotificationHandler {

    public static void sendUserNotification(Context context, User whoWillSend, User whoMsgWillReceive, String title, String body) {

        if (whoMsgWillReceive == null) return;
        if (whoWillSend == null) return;
        if (title == null || title.trim().isEmpty()) return;
        if (body == null || body.trim().isEmpty()) return;
        if (whoWillSend.getUserid().equals(whoMsgWillReceive.getUserid()))
            return;

        TokenDBHelper.getUserTokenByuserId(whoMsgWillReceive.getUserid(), new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                if (object != null) {
                    FCMItems fcmItems = new FCMItems();
                    fcmItems.setBody(body);
                    fcmItems.setOtherUserDeviceToken((String) object);
                    fcmItems.setTitle(title);
                    fcmItems.setPhotoUrl(whoWillSend.getProfilePhotoUrl());

                    SendMessageToFCM.sendMessage(context,
                            fcmItems,
                            new MessageSentFCMCallback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onFailed(Exception e) {

                                }
                            });
                }
            }

            @Override
            public void onFailed(String message) {

            }
        });
    }

    public static void sendNotificationToGroupParticipants(Context context, User whoWillSend, Group group, String title, String body) {

        if (group == null) return;
        if (whoWillSend == null) return;
        if (title == null || title.trim().isEmpty()) return;
        if (body == null || body.trim().isEmpty()) return;
        if (group.getMemberList() == null || group.getMemberList().size() == 0) return;

        for (User user : group.getMemberList()) {
            if (!user.getUserid().equals(whoWillSend.getUserid()))
                TokenDBHelper.getUserTokenByuserId(user.getUserid(), new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {
                        if (object != null) {
                            FCMItems fcmItems = new FCMItems();
                            fcmItems.setBody(body);
                            fcmItems.setOtherUserDeviceToken((String) object);
                            fcmItems.setTitle(title);
                            fcmItems.setPhotoUrl(whoWillSend.getProfilePhotoUrl());

                            SendMessageToFCM.sendMessage(context,
                                    fcmItems,
                                    new MessageSentFCMCallback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onFailed(Exception e) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailed(String message) {

                    }
                });
        }
    }

}
