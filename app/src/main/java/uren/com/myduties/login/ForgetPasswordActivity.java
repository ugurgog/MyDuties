package uren.com.myduties.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import uren.com.myduties.R;
import uren.com.myduties.login.utils.Validation;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.InfoDialogBoxCallback;

import static uren.com.myduties.constants.StringConstants.APP_FB_URL;
import static uren.com.myduties.constants.StringConstants.APP_PACKAGE_NAME;

public class ForgetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout forgetPasswordLayout;
    EditText emailET;
    Button btnSendLink;

    String userEmail;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        Fabric.with(this, new Crashlytics());
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        init();
        setShapes();
    }

    private void setShapes() {
     /*   lockImgv.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparentBlack, null),
                0, GradientDrawable.OVAL, 50, 0));*/
        emailET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 4));
        btnSendLink.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 3));
    }

    private void init() {
        emailET = findViewById(R.id.input_email);
        btnSendLink = findViewById(R.id.btnSendLink);
        forgetPasswordLayout = findViewById(R.id.forgetPasswordLayout);
        //lockImgv = findViewById(R.id.lockImgv);
        forgetPasswordLayout.setOnClickListener(this);
        emailET.setOnClickListener(this);
        btnSendLink.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    Objects.requireNonNull(imm).hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onClick(View v) {

        if (v == btnSendLink) {
            if (checkNetworkConnection())
                btnSendLinkClicked();
        }
    }

    public boolean checkNetworkConnection() {
        if (!CommonUtils.isNetworkConnected(ForgetPasswordActivity.this)) {
            CommonUtils.connectionErrSnackbarShow(forgetPasswordLayout, ForgetPasswordActivity.this);
            return false;
        } else
            return true;
    }

    private void btnSendLinkClicked() {
        progressDialog.setMessage(this.getString(R.string.PLEASE_WAIT));
        progressDialog.show();

        userEmail = emailET.getText().toString();

        //validation controls
        if (!checkValidation(userEmail)) {
            return;
        }

        sendLinkToMail(userEmail);
    }

    private boolean checkValidation(String userEmail) {
        if (!Validation.getInstance().isValidEmail(this, userEmail)) {
            progressDialog.dismiss();
            DialogBoxUtil.showInfoDialogBox(ForgetPasswordActivity.this,
                    Validation.getInstance().getErrorMessage(), null, new InfoDialogBoxCallback() {
                        @Override
                        public void okClick() {

                        }
                    });
            return false;
        }
        return true;
    }

    private void sendLinkToMail(String userEmail) {

        final Context context = this;

        FirebaseAuth auth;
        ActionCodeSettings actionCodeSettings;
        auth = FirebaseAuth.getInstance();
        actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(APP_PACKAGE_NAME, true, null)
                .setHandleCodeInApp(false)
                .setIOSBundleId(null)
                .setUrl(APP_FB_URL)
                .build();

        auth.sendPasswordResetEmail(userEmail, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            DialogBoxUtil.showInfoDialogBox(ForgetPasswordActivity.this,
                                    context.getString(R.string.PASSWORD_LINK_SEND_SUCCESS), null, new InfoDialogBoxCallback() {
                                        @Override
                                        public void okClick() {

                                        }
                                    });
                        } else {
                            DialogBoxUtil.showInfoDialogBox(ForgetPasswordActivity.this,
                                    context.getString(R.string.PASSWORD_LINK_SEND_FAIL), null, new InfoDialogBoxCallback() {
                                        @Override
                                        public void okClick() {

                                        }
                                    });
                        }
                    }
                });
    }

}
