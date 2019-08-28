package uren.com.myduties.messaging;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import uren.com.myduties.MainActivity;
import uren.com.myduties.R;

import static uren.com.myduties.constants.StringConstants.FB_CHILD_DEVICE_TOKEN;
import static uren.com.myduties.constants.StringConstants.FB_CHILD_TOKEN;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        /*if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getNotification() != null)
                sendNotificationForMessaging(remoteMessage);
        }*/

        if (remoteMessage.getNotification() != null)
            sendNotificationForMessaging(remoteMessage);
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            String userid = firebaseAuth.getCurrentUser().getUid();
            if (!userid.isEmpty())
                sendRegistrationToServer(token, userid);
        }
    }

    public static void sendRegistrationToServer(String token, String userid) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(FB_CHILD_DEVICE_TOKEN)
                .child(userid);

        final Map<String, Object> values = new HashMap<>();
        values.put(FB_CHILD_TOKEN, token);

        database.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    private void sendNotificationForMessaging(RemoteMessage remoteMessage) {
        String messageBody = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
        String messageTitle = remoteMessage.getNotification().getTitle();
        //String messageType = remoteMessage.getData().get(FCM_MESSAGE_TYPE);

        Intent intent = new Intent(this, MainActivity.class);

        /*if (messageType != null && !messageType.isEmpty())
            intent.putExtra(FCM_MESSAGE_TYPE, messageType);*/

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationBuilder.setSmallIcon(R.mipmap.app_notif_icon);
        notificationBuilder.setColor(getResources().getColor(R.color.DodgerBlue, null));

        if (messageTitle != null && !messageTitle.isEmpty())
            notificationBuilder.setContentTitle(messageTitle);

        if (messageBody != null && !messageBody.isEmpty())
            notificationBuilder.setContentText(messageBody);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }

        Objects.requireNonNull(notificationManager).notify(NotificationID.getID(), notificationBuilder.build());
    }
}