package uren.com.myduties.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import uren.com.myduties.R;
import uren.com.myduties.models.Contact;

import static uren.com.myduties.constants.StringConstants.APP_INVITATION_LINK;

public class DynamicLinkUtil {

    public static void setAppInvitationLink(final Context context, Fragment fragment){

            Intent intent = new Intent();
            String msg = context.getResources().getString(R.string.CONTACT_INVITE_MESSAGE) + " " + APP_INVITATION_LINK;
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, msg);
            intent.setType("text/plain");
            if (intent.resolveActivity(context.getPackageManager()) != null)
                fragment.startActivity(Intent.createChooser(intent, "Share"));
    }

    public static void setAppInvitationLinkForSms(Context context, Contact contact, Fragment fragment) {
            if (contact != null && contact.getPhoneNumber() != null) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + contact.getPhoneNumber()));
                String msg = context.getResources().getString(R.string.CONTACT_INVITE_MESSAGE) + " " + APP_INVITATION_LINK;
                sendIntent.putExtra("sms_body", msg);
                fragment.startActivity(sendIntent);
            }
    }
}
