package uren.com.myduties.login;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.User;

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
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null)
            return firebaseAuth.getCurrentUser().getUid();
        else {
            firebaseAuth = FirebaseAuth.getInstance();
            return firebaseAuth.getCurrentUser().getUid();
        }
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

                if (user != null)
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



