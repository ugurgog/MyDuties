package uren.com.myduties.login;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.Map;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_countryCode;
import static uren.com.myduties.constants.StringConstants.fb_child_dialCode;
import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_phone;
import static uren.com.myduties.constants.StringConstants.fb_child_phoneNumber;
import static uren.com.myduties.constants.StringConstants.fb_child_profilePhotoUrl;
import static uren.com.myduties.constants.StringConstants.fb_child_username;
import static uren.com.myduties.constants.StringConstants.fb_child_users;

public class AccountHolderInfo {

    private static AccountHolderInfo accountHolderInfo = null;
    private static CompleteCallback mCompleteCallback;
    private static User user;
    private static FirebaseAuth firebaseAuth;

    public static void getInstance(CompleteCallback completeCallback) {

        mCompleteCallback = completeCallback;

        if (accountHolderInfo == null) {
            user = new User();
            accountHolderInfo = new AccountHolderInfo();
        } else
            mCompleteCallback.onComplete(user);
    }

    public AccountHolderInfo() {
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth == null) {
            mCompleteCallback.onFailed(String.valueOf(R.string.UNEXPECTED_ERROR));
        } else {
            Crashlytics.setUserIdentifier(getUserIdFromFirebase());
            getUserInfo();
        }
    }

    public static String getUserIdFromFirebase() {
        return firebaseAuth.getCurrentUser().getUid();
    }

    public static void setInstance(AccountHolderInfo instance) {
        accountHolderInfo = instance;
    }

    public static String getUserID() {
        return getUserIdFromFirebase();
    }

    private void getUserInfo() {

        UserDBHelper.getUser(getUserIdFromFirebase(), new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                User user = (User) object;

                if(user != null)
                    mCompleteCallback.onComplete(user);
                else
                    mCompleteCallback.onComplete(null);
            }

            @Override
            public void onFailed(String message) {
                mCompleteCallback.onFailed(message);
            }
        });
    }

    public static synchronized void reset() {
        accountHolderInfo = null;
    }
}



