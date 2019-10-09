package uren.com.myduties;


import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import uren.com.myduties.dbManagement.TokenDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.login.LoginActivity;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.AnimationUtil;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.TaskTypeHelper;

import static uren.com.myduties.constants.StringConstants.CHAR_E;

public class MainActivity extends AppCompatActivity {

    RelativeLayout mainActLayout;
    ImageView appIconImgv;
    SwipeRefreshLayout refresh_layout;
    Button tryAgainButton;
    TextView networkTryDesc;

    private FirebaseAuth firebaseAuth;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
        CommonUtils.hideKeyBoard(this);
        initVariables();


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else
            fillUserInfo();
    }

    private void initVariables() {
        mainActLayout = findViewById(R.id.mainActLayout);
        refresh_layout = findViewById(R.id.refresh_layout);
        appIconImgv = findViewById(R.id.appIconImgv);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        networkTryDesc = findViewById(R.id.networkTryDesc);
        AnimationUtil.blink(MainActivity.this, appIconImgv);

        tryAgainButton.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.DodgerBlue),
                getResources().getColor(R.color.White), GradientDrawable.RECTANGLE, 50, 2));

        setPullToRefresh();
        addListeners();
    }

    private void addListeners() {
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProcess();
            }
        });
    }

    private void setPullToRefresh() {
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loginProcess();
            }
        });
    }

    public void fillUserInfo() {
        loginProcess();
    }

    public void loginProcess() {
        if (!CommonUtils.isNetworkConnected(MainActivity.this)) {
            tryAgainButton.setVisibility(View.VISIBLE);
            networkTryDesc.setVisibility(View.VISIBLE);
            CommonUtils.connectionErrSnackbarShow(mainActLayout, MainActivity.this);
            refresh_layout.setRefreshing(false);
        } else {
            tryAgainButton.setVisibility(View.GONE);
            networkTryDesc.setVisibility(View.GONE);
            TaskTypeHelper taskTypeHelper = new TaskTypeHelper(MainActivity.this);
            EventBus.getDefault().postSticky(new TaskTypeBus(taskTypeHelper));
            startLoginProcess();
        }
    }

    public void startLoginProcess() {

        UserDBHelper.getUser(firebaseAuth.getCurrentUser().getUid(), new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                user = (User) object;

                if (user != null) {
                    EventBus.getDefault().postSticky(new UserBus(user));
                    refresh_layout.setRefreshing(false);
                    updateDeviceTokenForFCM();
                    startActivity(new Intent(MainActivity.this, NextActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailed(String message) {
                CommonUtils.showToastShort(MainActivity.this, message);
                refresh_layout.setRefreshing(false);
            }
        });
    }

    public void updateDeviceTokenForFCM() {
        TokenDBHelper.updateTokenSigninValue(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid(), CHAR_E,
                new OnCompleteCallback() {
                    @Override
                    public void OnCompleted() {
                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                String deviceToken = instanceIdResult.getToken();
                                TokenDBHelper.sendTokenToServer(deviceToken, Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                            }
                        });
                    }

                    @Override
                    public void OnFailed(String message) {

                    }
                });
    }
}
