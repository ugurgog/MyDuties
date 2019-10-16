package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.MainActivity;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.helper.SettingOperation;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ProgressDialogUtil;

import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;

public class ChangePasswordFragment extends BaseFragment {

    View mView;

    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    TextView toolbarTitleTv;
    @BindView(R.id.commonToolbarTickImgv)
    ImageView commonToolbarTickImgv;
    @BindView(R.id.currPasswordEdittext)
    EditText currPasswordEdittext;
    @BindView(R.id.newPasswordEdittext)
    EditText newPasswordEdittext;
    @BindView(R.id.validatePassEdittext)
    EditText validatePassEdittext;
    @BindView(R.id.container)
    RelativeLayout container;

    FirebaseUser firebaseUser;
    String newPassword;
    User accountHolderUser;
    ProgressDialogUtil progressDialogUtil;

    public ChangePasswordFragment() {

    }

    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true)
    public void accountHolderUserReceived(UserBus userBus) {
        accountHolderUser = userBus.getUser();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_change_password, container, false);
            ButterKnife.bind(this, mView);
            init();
            addListeners();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_RIGHT_TO_LEFT;
    }

    private void init() {
        setToolbarTitle();
        progressDialogUtil = new ProgressDialogUtil(getActivity(), null, true);
    }

    public void setToolbarTitle() {
        toolbarTitleTv.setText(getResources().getString(R.string.CHANGE_PASSWORD));
    }

    public void addListeners() {
        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        commonToolbarTickImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePasswords();
            }
        });

        validatePassEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    commonToolbarTickImgv.setVisibility(View.GONE);
                } else {
                    if (currPasswordEdittext.getText().length() > 0 && newPasswordEdittext.getText().length() > 0)
                        commonToolbarTickImgv.setVisibility(View.VISIBLE);
                    else
                        commonToolbarTickImgv.setVisibility(View.GONE);
                }
            }
        });
    }

    public void validatePasswords() {

        if (newPasswordEdittext.getText().toString().trim().length() < 6) {
            Snackbar.make(container, Objects.requireNonNull(getContext()).getResources().getString(R.string.PASSWORD_ERR_LENGTH), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (!newPasswordEdittext.getText().toString().equals(validatePassEdittext.getText().toString())) {
            Snackbar.make(container, Objects.requireNonNull(getContext()).getResources().getString(R.string.CHECK_PASSWORD_VALIDATION_VALUE), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        changePassword();
    }

    private void changePassword() {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        newPassword = newPasswordEdittext.getText().toString();

        AuthCredential credential = EmailAuthProvider.
                getCredential(accountHolderUser.getEmail(),
                        currPasswordEdittext.getText().toString());

        progressDialogUtil.dialogShow();

        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        CommonUtils.showToastShort(getContext(), Objects.requireNonNull(getContext()).getResources().getString(R.string.PASSWORD_IS_CHANGED));
                                        thread.start();
                                    } else {
                                        progressDialogUtil.dialogDismiss();
                                        CommonUtils.showToastShort(getContext(), Objects.requireNonNull(getContext()).getResources().getString(R.string.error) + Objects.requireNonNull(task.getException()).getMessage());
                                    }
                                }
                            });
                        } else {
                            progressDialogUtil.dialogDismiss();
                            Snackbar.make(container, Objects.requireNonNull(getContext()).getResources().getString(R.string.CURRENT_PASSWORD_INCORRECT), Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
                progressDialogUtil.dialogDismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SettingOperation.userSignOut(getContext(), accountHolderUser, new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    progressDialogUtil.dialogDismiss();
                    Objects.requireNonNull(getActivity()).finish();
                    startActivity(new Intent(getActivity(), MainActivity.class));
                }

                @Override
                public void onFailed(String s) {
                    CommonUtils.showToastShort(getContext(), s);
                }
            });
        }
    };
}
