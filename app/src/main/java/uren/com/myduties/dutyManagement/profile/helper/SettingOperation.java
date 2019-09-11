package uren.com.myduties.dutyManagement.profile.helper;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

import uren.com.myduties.dbManagement.TokenDBHelper;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.login.AccountHolderInfo;

import static uren.com.myduties.constants.StringConstants.CHAR_H;

public class SettingOperation {

    private static final int CODE_REMOVE_PRIVACY = 0;
    private static final int CODE_MAKE_PRIVACY = 1;
    private static FirebaseAuth firebaseAuth;
    static CompleteCallback mCompleteCallback;

    public static void userSignOut(CompleteCallback completeCallback) {

        mCompleteCallback = completeCallback;
        firebaseAuth = FirebaseAuth.getInstance();
        updateDeviceTokenForFCM();
    }

    private static void updateDeviceTokenForFCM() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                try {
                    TokenDBHelper.updateTokenSigninValue(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid(), CHAR_H,
                            new OnCompleteCallback() {
                                @Override
                                public void OnCompleted() {
                                    String deviceToken = instanceIdResult.getToken();
                                    userLogOut();
                                }

                                @Override
                                public void OnFailed(String message) {

                                }
                            });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(e -> userLogOut());
    }

    private static void userLogOut() {
        firebaseAuth.signOut();
        clearSingletonClasses();
    }

    static void clearSingletonClasses() {
        AccountHolderInfo.reset();
        mCompleteCallback.onComplete(null);
    }
}
