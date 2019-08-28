package uren.com.myduties.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;


import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import uren.com.myduties.MainActivity;
import uren.com.myduties.R;
import uren.com.myduties.models.LoginUser;
import uren.com.myduties.login.utils.Validation;
import uren.com.myduties.utils.BitmapConversion;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.StringConstants.LOGIN_USER;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener {

    RelativeLayout backgroundLayout;
    EditText emailET;
    EditText passwordET;
    TextView registerText;
    TextView forgetPasText;
    Button btnLogin;
    Button forgetPasswordBtn;
    Button createAccBtn;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences.Editor loginPrefsEditor;

    private boolean fbLoginClicked = false;
    private boolean twLoginClicked = false;

    //Local
    String userEmail;
    String userPassword;
    ProgressDialog progressDialog;
    public LoginUser loginUser;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // Making notification bar transparent
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_login);
        Fabric.with(this, new Crashlytics());

        initVariables();
        setShapes();
        BitmapConversion.setBlurBitmap(LoginActivity.this, backgroundLayout,
                R.drawable.login_background, 0.3f, 15f, null);
    }

    public void setShapes() {
        emailET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
        passwordET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
        btnLogin.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.colorPrimary, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
        forgetPasswordBtn.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
        createAccBtn.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
    }

    private void initVariables() {
        initUIValues();
        setClickableTexts(this);
        initUIListeners();
        progressDialog = new ProgressDialog(this);

        loginUser = new LoginUser();
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        Boolean saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            emailET.setText(loginPreferences.getString("email", emailET.getText().toString()));
            passwordET.setText(loginPreferences.getString("password", passwordET.getText().toString()));
            rememberMeCheckBox.setChecked(true);
        }
    }

    private void initUIValues() {
        backgroundLayout = findViewById(R.id.loginLayout);
        emailET = findViewById(R.id.input_email);
        passwordET = findViewById(R.id.input_password);
        registerText = findViewById(R.id.btnRegister);
        forgetPasText = findViewById(R.id.btnForgetPassword);
        btnLogin = findViewById(R.id.btnLogin);
        rememberMeCheckBox = findViewById(R.id.rememberMeCb);
        forgetPasswordBtn = findViewById(R.id.forgetPasswordBtn);
        createAccBtn = findViewById(R.id.createAccBtn);
    }

    private void initUIListeners() {
        backgroundLayout.setOnClickListener(this);
        emailET.setOnClickListener(this);
        passwordET.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    private void setClickableTexts(Activity act) {
        String textRegister = getResources().getString(R.string.createAccount);
        String textForgetPssword = getResources().getString(R.string.forgetPassword);
        final SpannableString spanStringRegister = new SpannableString(textRegister);
        final SpannableString spanStringForgetPas = new SpannableString(textForgetPssword);
        spanStringRegister.setSpan(new UnderlineSpan(), 0, spanStringRegister.length(), 0);
        spanStringForgetPas.setSpan(new UnderlineSpan(), 0, spanStringForgetPas.length(), 0);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                if (textView.equals(registerText)) {
                    //Toast.makeText(LoginActivity.this, "RegisterActivity click!", Toast.LENGTH_SHORT).show();
                    registerTextClicked();
                } else if (textView.equals(forgetPasText)) {
                    //Toast.makeText(LoginActivity.this, "Forgetpas click!", Toast.LENGTH_SHORT).show();
                    forgetPasTextClicked();
                }
            }
        };
        spanStringRegister.setSpan(clickableSpan, 0, spanStringRegister.length(), 0);
        spanStringForgetPas.setSpan(clickableSpan, 0, spanStringForgetPas.length(), 0);

        registerText.setText(spanStringRegister);
        forgetPasText.setText(spanStringForgetPas);
        registerText.setMovementMethod(LinkMovementMethod.getInstance());
        forgetPasText.setMovementMethod(LinkMovementMethod.getInstance());
        registerText.setHighlightColor(Color.TRANSPARENT);
        forgetPasText.setHighlightColor(Color.TRANSPARENT);
        registerText.setLinkTextColor(getResources().getColor(R.color.White, null));
        forgetPasText.setLinkTextColor(getResources().getColor(R.color.White, null));
    }

    @Override
    public void onClick(View view) {

        if (view == backgroundLayout) {
            saveLoginInformation();
            CommonUtils.hideKeyBoard(LoginActivity.this);
        } else if (view == btnLogin) {
            if (checkNetworkConnection())
                loginBtnClicked();
        } else if (view == rememberMeCheckBox) {
            saveLoginInformation();
        } else {

        }
    }

    public boolean checkNetworkConnection() {
        if (!CommonUtils.isNetworkConnected(LoginActivity.this)) {
            CommonUtils.connectionErrSnackbarShow(backgroundLayout, LoginActivity.this);
            return false;
        } else
            return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveLoginInformation();

        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            saveLoginInformation();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveLoginInformation() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).hideSoftInputFromWindow(emailET.getWindowToken(), 0);

        String username = emailET.getText().toString();
        String password = passwordET.getText().toString();

        if (rememberMeCheckBox.isChecked()) {
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("email", username);
            loginPrefsEditor.putString("password", password);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }
    }

    private void registerTextClicked() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        //finish();
    }

    private void forgetPasTextClicked() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    private void loginBtnClicked() {

        saveLoginInformation();
        progressDialog.setMessage(this.getString(R.string.LOGGING_USER));
        progressDialog.show();

        userEmail = emailET.getText().toString();
        userPassword = passwordET.getText().toString();

        //validation controls
        if (!checkValidation(userEmail, userPassword)) {
            return;
        }

        loginUser(userEmail, userPassword);
    }

    private boolean checkValidation(String email, String password) {

        //email validation
        if (!Validation.getInstance().isValidEmail(this, email)) {
            //Toast.makeText(this, Validation.getInstance().getErrorMessage() , Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            openDialog(Validation.getInstance().getErrorMessage());
            return false;
        }

        //password validation
        if (!Validation.getInstance().isValidPassword(this, password)) {
            //Toast.makeText(this, Validation.getInstance().getErrorMessage() , Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            openDialog(Validation.getInstance().getErrorMessage());
            return false;
        }

        return true;
    }

    public void openDialog(String message) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("OOPS!!");
        alert.setMessage(message);
        alert.setPositiveButton("OK", null);
        alert.show();

    }

    private void loginUser(final String userEmail, String userPassword) {
        final Context context = this;

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            setUserInfo("", userEmail);
                            startMainPage();
                        } else {

                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Log.i("error register", e.toString());
                                openDialog(context.getString(R.string.INVALID_CREDENTIALS));
                            } catch (FirebaseAuthInvalidUserException e) {
                                Log.i("error register", e.toString());
                                openDialog(context.getString(R.string.INVALID_USER));
                            } catch (Exception e) {
                                Log.i("error signIn ", e.toString());
                                openDialog(context.getString(R.string.UNKNOWN_ERROR) + "(" + e.toString() + ")");

                            }
                        }
                    }
                });
    }

    private void setUserInfo(String userName, String userEmail) {

        if (!userName.isEmpty()) {
            loginUser.setUsername(userName);
        } else {
            loginUser.setUsername("undefined");
        }

        loginUser.setEmail(userEmail);
        loginUser.setUserId(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
    }

    private void startMainPage() {
        loginUser.setUserId(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(LOGIN_USER, loginUser);
        startActivity(intent);
        finish();
    }

}
