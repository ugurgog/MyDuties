package uren.com.myduties.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.login.utils.Validation;
import uren.com.myduties.models.LoginUser;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.BitmapConversion;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.InfoDialogBoxCallback;

import static uren.com.myduties.constants.StringConstants.ALGOLIA_APP_ID;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_INDEX_NAME;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_SEARCH_API_KEY;
import static uren.com.myduties.constants.StringConstants.LOGIN_USER;


public class RegisterActivity extends AppCompatActivity
        implements View.OnClickListener {

    RelativeLayout registerLayout;
    EditText usernameET;
    EditText emailET;
    EditText passwordET;
    Button btnRegister;

    //Local
    LoginUser newLoginUser;
    String userName;
    String userEmail;
    String userPassword;
    ProgressDialog progressDialog;

    Index index;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Fabric.with(this, new Crashlytics());
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        init();
        setShapes();
        //BitmapConversion.setBlurBitmap(RegisterActivity.this, registerLayout,
        //        R.drawable.login_background, 0.2f, 20.5f, null);
    }

    public void setShapes(){
        usernameET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
        emailET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
        passwordET.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.transparent, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
        btnRegister.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.colorPrimary, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 20, 4));
    }

    private void init() {
        registerLayout = findViewById(R.id.registerLayout);
        usernameET = findViewById(R.id.input_username);
        emailET = findViewById(R.id.input_email);
        passwordET = findViewById(R.id.input_password);
        btnRegister = findViewById(R.id.btnRegister);
        registerLayout.setOnClickListener(this);
        usernameET.setOnClickListener(this);
        emailET.setOnClickListener(this);
        passwordET.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        index = client.getIndex(ALGOLIA_INDEX_NAME);
    }

    @Override
    public void onClick(View v) {

        if (v == btnRegister) {
            if (checkNetworkConnection())
                btnRegisterClicked();
        }
    }

    public boolean checkNetworkConnection() {
        if (!CommonUtils.isNetworkConnected(RegisterActivity.this)) {
            CommonUtils.connectionErrSnackbarShow(registerLayout, RegisterActivity.this);
            return false;
        } else
            return true;
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

    /*****************************CLICK EVENTS******************************/

    private void btnRegisterClicked() {

        progressDialog.setMessage(this.getString(R.string.REGISTERING_USER));
        progressDialog.show();

        userName = usernameET.getText().toString();
        userEmail = emailET.getText().toString();
        userPassword = passwordET.getText().toString();

        //validation controls
        if (!checkValidation(userName, userEmail, userPassword)) {
            return;
        }

        createUser(userName, userEmail, userPassword);

    }

    private boolean checkValidation(String name, String email, String password) {

        //username validation
        if (!Validation.getInstance().isValidUserName(this, name)) {
            progressDialog.dismiss();
            DialogBoxUtil.showInfoDialogBox(RegisterActivity.this,
                    Validation.getInstance().getErrorMessage(), null, new InfoDialogBoxCallback() {
                        @Override
                        public void okClick() {

                        }
                    });

            return false;
        }

        //email validation
        if (!Validation.getInstance().isValidEmail(this, email)) {
            progressDialog.dismiss();
            DialogBoxUtil.showInfoDialogBox(RegisterActivity.this,
                    Validation.getInstance().getErrorMessage(), null, new InfoDialogBoxCallback() {
                        @Override
                        public void okClick() {

                        }
                    });
            return false;
        }

        //password validation
        if (!Validation.getInstance().isValidPassword(this, password)) {
            //Toast.makeText(this, Validation.getInstance().getErrorMessage() , Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

            DialogBoxUtil.showInfoDialogBox(RegisterActivity.this,
                    Validation.getInstance().getErrorMessage(), null, new InfoDialogBoxCallback() {
                        @Override
                        public void okClick() {

                        }
                    });

            return false;
        }

        return true;
    }

    private void createUser(final String userName, final String userEmail, final String userPassword) {

        final Context context = this;

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            Log.i("Info", "CreateUser : Success");
                            progressDialog.dismiss();
                            setUserInfo(userName, userEmail);
                            addUserToSystem(userName, userEmail);

                            //startAppIntroPage();
                            //startMainPage();
                        } else {
                            progressDialog.dismiss();
                            Log.i("Info", "CreateUser : Fail");
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthUserCollisionException e) {
                                DialogBoxUtil.showInfoDialogBox(RegisterActivity.this,
                                        context.getString(R.string.COLLISION_EXCEPTION), null, new InfoDialogBoxCallback() {
                                            @Override
                                            public void okClick() {

                                            }
                                        });

                            } catch (Exception e) {
                                DialogBoxUtil.showInfoDialogBox(RegisterActivity.this,
                                        context.getString(R.string.UNKNOWN_ERROR) + "(" + e.toString() + ")", null, new InfoDialogBoxCallback() {
                                            @Override
                                            public void okClick() {

                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void addUserToSystem( String username, String email){

        User user = new User();
        user.setUserid(mAuth.getCurrentUser().getUid());
        user.setUsername(username);
        user.setEmail(email);
        UserDBHelper.addUser(user, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                startAppIntroPage();
            }

            @Override
            public void OnFailed(String message) {

            }
        });
    }

    private void setUserInfo(String userName, String userEmail) {

        newLoginUser = new LoginUser();
        newLoginUser.setUsername(userName);
        newLoginUser.setEmail(userEmail);
        newLoginUser.setUserId(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
    }

    public void startAppIntroPage() {
        Intent intent = new Intent(RegisterActivity.this, AppIntroductionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(LOGIN_USER, newLoginUser);
        startActivity(intent);
    }
}
