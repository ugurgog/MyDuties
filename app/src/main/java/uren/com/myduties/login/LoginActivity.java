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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import uren.com.myduties.MainActivity;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.login.utils.Validation;
import uren.com.myduties.models.LoginUser;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.LOGIN_METHOD_GOOGLE;
import static uren.com.myduties.constants.StringConstants.LOGIN_USER;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener {

    RelativeLayout backgroundLayout;
    EditText emailET;
    EditText passwordET;
    AppCompatTextView registerText;
    AppCompatTextView forgetPasText;
    Button btnLogin;
    Button forgetPasswordBtn;
    Button createAccBtn;
    LinearLayout llGoogleSignIn;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences.Editor loginPrefsEditor;

    private static final int RC_SIGN_IN = 9001;

    //Local
    String userEmail;
    String userPassword;
    ProgressDialog progressDialog;
    public LoginUser loginUser;

    //Firebase
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_login);
        Fabric.with(this, new Crashlytics());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initVariables();
        setShapes();
    }

    public void setShapes() {
        emailET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 4));
        passwordET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 4));
        btnLogin.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 4));
        llGoogleSignIn.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.GoogleLogin),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 4));
        forgetPasswordBtn.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 4));
        createAccBtn.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 20, 4));
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
        llGoogleSignIn = findViewById(R.id.llGoogleSignIn);
    }

    private void initUIListeners() {
        backgroundLayout.setOnClickListener(this);
        emailET.setOnClickListener(this);
        passwordET.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        llGoogleSignIn.setOnClickListener(this);
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
        registerText.setLinkTextColor(getResources().getColor(R.color.White));
        forgetPasText.setLinkTextColor(getResources().getColor(R.color.White));
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
        } else if (view == llGoogleSignIn){
            googleSignIn();
        }
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                CommonUtils.showToastShort(LoginActivity.this, getResources().getString(R.string.googleSignInFailed));
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            UserDBHelper.getUser(user.getUid(), new CompleteCallback() {
                                @Override
                                public void onComplete(Object object) {
                                    if(object == null || ((User) object).getUserid() == null){
                                        String username = UserDataUtil.getUsernameFromNameWhenLoginWithGoogle(user.getDisplayName());
                                        User newUser = new User(user.getUid(), user.getDisplayName(), username,
                                                user.getEmail(), user.getPhotoUrl().toString(), null, null, false, LOGIN_METHOD_GOOGLE);

                                        UserDBHelper.addUser(newUser, new OnCompleteCallback() {
                                            @Override
                                            public void OnCompleted() {
                                                setUserInfo(username, user.getEmail());
                                                startAppIntroPage();
                                            }

                                            @Override
                                            public void OnFailed(String message) {
                                                CommonUtils.showToastShort(LoginActivity.this, message);
                                            }
                                        });
                                    }else {
                                        User user1 = (User) object;
                                        user1.setLoginMethod(LOGIN_METHOD_GOOGLE);

                                        UserDBHelper.updateUser(user1, false, new OnCompleteCallback() {
                                            @Override
                                            public void OnCompleted() {
                                                setUserInfo("", user.getEmail());
                                                startMainPage();
                                            }

                                            @Override
                                            public void OnFailed(String message) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailed(String message) {
                                    CommonUtils.showToastShort(LoginActivity.this, message);
                                }
                            });

                        } else {
                            CommonUtils.showToastShort(LoginActivity.this, task.getException().toString());
                        }
                    }
                });
    }

    public void startAppIntroPage() {
        Intent intent = new Intent(LoginActivity.this, AppIntroductionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(LOGIN_USER, loginUser);
        startActivity(intent);
    }
}
