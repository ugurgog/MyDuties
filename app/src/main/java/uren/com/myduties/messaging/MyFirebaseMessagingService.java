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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import uren.com.myduties.MainActivity;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.TokenDBHelper;
import uren.com.myduties.utils.BitmapConversion;

import static uren.com.myduties.constants.StringConstants.FB_CHILD_DEVICE_TOKEN;
import static uren.com.myduties.constants.StringConstants.FB_CHILD_TOKEN;
import static uren.com.myduties.constants.StringConstants.FCM_CODE_PHOTO_URL;

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

        if (remoteMessage.getNotification() != null) {
            String photoUrl;
            try {
                photoUrl = remoteMessage.getData().get(FCM_CODE_PHOTO_URL);
            } finally {

            }
            new GetNotification(remoteMessage).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, photoUrl);
        }
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
                TokenDBHelper.sendTokenToServer(token, userid);
        }
    }

    private void sendNotificationForMessaging(RemoteMessage remoteMessage, Bitmap bitmap) {
        String messageBody = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
        String messageTitle = remoteMessage.getNotification().getTitle();

        Intent intent = new Intent(this, MainActivity.class);

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

        if (bitmap != null)
            notificationBuilder.setLargeIcon(bitmap);

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

    public class GetNotification extends AsyncTask<String, Void, Void> {

        RemoteMessage remoteMessage;

        public GetNotification(RemoteMessage remoteMessage) {
            this.remoteMessage = remoteMessage;
        }

        @Override
        protected Void doInBackground(String... urls) {

            try {
                String photoUrl = urls[0];
                Bitmap myBitmap = null;

                if (photoUrl != null && !photoUrl.isEmpty()) {
                    URL url = new URL(urls[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    myBitmap = BitmapConversion.getBitmapFromInputStream(input, getApplicationContext(), 350, 350);
                }

                sendNotificationForMessaging(remoteMessage, myBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}