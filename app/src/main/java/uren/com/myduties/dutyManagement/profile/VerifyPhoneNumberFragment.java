package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.profile.helper.PhoneVerification;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.PhoneVerifyCallback;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.InfoDialogBoxCallback;

import static uren.com.myduties.constants.NumericConstants.VERIFY_PHONE_NUM_DURATION;

public class VerifyPhoneNumberFragment extends Fragment {

    View mView;

    @BindView(R.id.phoneNumberTv)
    TextView phoneNumberTv;
    @BindView(R.id.sendCodeAgainBtn)
    Button sendCodeAgainBtn;
    @BindView(R.id.changePhoneBtn)
    Button changePhoneBtn;
    @BindView(R.id.verifyCodeEt)
    EditText verifyCodeEt;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.commonToolbarTickImgv)
    ImageView commonToolbarTickImgv;
    @BindView(R.id.toolbarTitleTv)
    AppCompatTextView toolbarTitleTv;
    @BindView(R.id.warningMessageTv)
    TextView warningMessageTv;
    @BindView(R.id.remainingTimeTv)
    TextView remainingTimeTv;

    GradientDrawable buttonShape;
    PhoneVerification phoneVerification;
    CompleteCallback completeCallback;
    Phone phone;
    User user;

    public VerifyPhoneNumberFragment(Phone phone, PhoneVerification phoneVerification, CompleteCallback completeCallback) {
        this.phone = phone;
        this.phoneVerification = phoneVerification;
        this.completeCallback = completeCallback;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
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
    public void accountHolderUserReceived(UserBus userBus){
        user = userBus.getUser();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_verify_phone_num, container, false);
            ButterKnife.bind(this, mView);
            init();
            setButtonShapes();
            addListeners();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void init() {
        setPhoneNum();
        setToolbarTitle();
        setTimer();
    }

    private void setPhoneNum() {
        if (phone != null && phone.getDialCode() != null && phone.getPhoneNumber() != 0) {
            String phoneNumStr = phone.getDialCode().trim() + phone.getPhoneNumber();
            phoneNumberTv.setText(phoneNumStr);
        }
    }

    public void setToolbarTitle() {
        toolbarTitleTv.setText(getResources().getString(R.string.VERIFY));
    }

    private void setButtonShapes() {
        buttonShape = ShapeUtil.getShape(getResources().getColor(R.color.White),
                getResources().getColor(R.color.Gray), GradientDrawable.RECTANGLE, 15, 2);
        sendCodeAgainBtn.setBackground(buttonShape);
        changePhoneBtn.setBackground(buttonShape);
    }

    public void addListeners() {
        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        commonToolbarTickImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneVerification.verifyPhoneNumberWithCode(phoneVerification.getmVerificationId(),
                        verifyCodeEt.getText().toString().trim(), getContext(), new PhoneVerifyCallback() {
                            @Override
                            public void onReturn(boolean isVerified) {
                                if (isVerified)
                                    saveUserPhoneAndCountry();
                                else
                                    DialogBoxUtil.showErrorDialog(getActivity(), getResources().getString(R.string.INVALID_VERIFICATION_CODE_ENTERED), new InfoDialogBoxCallback() {
                                        @Override
                                        public void okClick() {

                                        }
                                    });
                            }
                        });
            }
        });

        verifyCodeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 6) {
                    commonToolbarTickImgv.setVisibility(View.VISIBLE);
                } else
                    commonToolbarTickImgv.setVisibility(View.GONE);
            }
        });

        sendCodeAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warningMessageTv.setVisibility(View.GONE);
                phoneVerification.resendVerificationCode(phone.getDialCode().trim() + phone.getPhoneNumber(), phoneVerification.getmResendToken());
                setTimer();
            }
        });

        changePhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    public void saveUserPhoneAndCountry() {

        user.setPhone(phone);

        UserDBHelper.updateUser(user, false, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                DialogBoxUtil.showInfoDialogWithLimitedTime(getActivity(), null, getActivity().getResources().getString(R.string.UPDATE_IS_SUCCESSFUL), 1500, new InfoDialogBoxCallback() {
                    @Override
                    public void okClick() {
                        completeCallback.onComplete(null);
                        getActivity().onBackPressed();
                    }
                });
            }

            @Override
            public void OnFailed(String message) {
                DialogBoxUtil.showErrorDialog(getActivity(), message, new InfoDialogBoxCallback() {
                    @Override
                    public void okClick() {

                    }
                });
            }
        });
    }

    public void setTimer() {
        sendCodeAgainBtn.setEnabled(false);

        new CountDownTimer(VERIFY_PHONE_NUM_DURATION * 1000, 1000) {

            int duration = VERIFY_PHONE_NUM_DURATION;

            public void onTick(long millisUntilFinished) {
                remainingTimeTv.setText(checkDigit(duration));
                duration--;
            }

            public void onFinish() {
                remainingTimeTv.setText(checkDigit(0));
                sendCodeAgainBtn.setEnabled(true);
                warningMessageTv.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

}
