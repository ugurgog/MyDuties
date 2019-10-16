package uren.com.myduties.dutyManagement.profile.helper;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.TokenDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.login.AccountHolderInfo;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;

import static uren.com.myduties.constants.StringConstants.CHAR_H;
import static uren.com.myduties.constants.StringConstants.LOGIN_METHOD_GOOGLE;

public class SettingOperation {

    private static FirebaseAuth firebaseAuth;
    static CompleteCallback mCompleteCallback;
    private static User mUser;
    private static Context mContext;

    public static void userSignOut(Context context, User user, CompleteCallback completeCallback) {

        mContext = context;
        mUser = user;
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

        if (mUser.getLoginMethod() != null && mUser.getLoginMethod().equals(LOGIN_METHOD_GOOGLE)) {
            UserDBHelper.deleteLoginMethod(mUser.getUserid(), new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    GoogleSignInClient mGoogleSignInClient;
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(mContext.getResources().getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

                    mGoogleSignInClient.signOut().addOnCompleteListener((Activity) mContext,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    clearSingletonClasses();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            CommonUtils.showToastShort(mContext, e.getMessage());
                        }
                    });
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(mContext, message);
                }
            });
        } else
            clearSingletonClasses();
    }

    static void clearSingletonClasses() {
        AccountHolderInfo.reset();
        mCompleteCallback.onComplete(null);
    }
}
