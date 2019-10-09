package uren.com.myduties.messaging;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uren.com.myduties.R;
import uren.com.myduties.messaging.interfaces.MessageSentFCMCallback;
import uren.com.myduties.messaging.models.FCMItems;

import static uren.com.myduties.constants.StringConstants.FCM_CODE_BODY;
import static uren.com.myduties.constants.StringConstants.FCM_CODE_DATA;
import static uren.com.myduties.constants.StringConstants.FCM_CODE_NOTIFICATION;
import static uren.com.myduties.constants.StringConstants.FCM_CODE_PHOTO_URL;
import static uren.com.myduties.constants.StringConstants.FCM_CODE_TITLE;
import static uren.com.myduties.constants.StringConstants.FCM_CODE_TO;
import static uren.com.myduties.constants.StringConstants.FCM_MESSAGE_URL;

public class SendMessageToFCM {

    static OkHttpClient mClient = new OkHttpClient();

    public static void sendMessage(final Context context,
                                   final FCMItems fcmItems,
                                   final MessageSentFCMCallback messageSentFCMCallback) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put(FCM_CODE_BODY, fcmItems.getBody());
                    notification.put(FCM_CODE_TITLE, fcmItems.getTitle());
                    notification.put(FCM_CODE_PHOTO_URL, fcmItems.getPhotoUrl());

                    JSONObject data = new JSONObject();
                    data.put(FCM_CODE_PHOTO_URL, fcmItems.getPhotoUrl());

                    root.put(FCM_CODE_NOTIFICATION, notification);
                    root.put(FCM_CODE_DATA, data);
                    root.put(FCM_CODE_TO, fcmItems.getOtherUserDeviceToken());

                    String result = postToFCM(root.toString(), context, messageSentFCMCallback);
                    return result;
                } catch (Exception e) {
                    messageSentFCMCallback.onFailed(e);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    //Toast.makeText(context, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();

                    if(failure > 0)
                        messageSentFCMCallback.onFailed(new Exception("Message Send Failed!"));
                    else
                        messageSentFCMCallback.onSuccess();

                } catch (Exception e) {
                    messageSentFCMCallback.onFailed(e);
                    e.printStackTrace();
                    //Toast.makeText(context, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public static String postToFCM(String bodyString, Context context,
                                   MessageSentFCMCallback messageSentFCMCallback) throws IOException {
        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, bodyString);
            Request request = new Request.Builder()
                    .url(FCM_MESSAGE_URL)
                    .post(body)
                    .addHeader("Authorization", "key=" + context.getResources().getString(R.string.FCM_SERVER_KEY))
                    .build();
            Response response = mClient.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (Exception e) {
            messageSentFCMCallback.onFailed(e);
            e.printStackTrace();
        }
        return "";
    }
}
