package uren.com.myduties.dutyManagement.profile;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.helper.PhoneVerification;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ItemClickListener;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Country;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;

import static uren.com.myduties.constants.StringConstants.ANIMATE_DOWN_TO_UP;
import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;

public class PhoneNumEditFragment extends BaseFragment {

    View mView;

    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.commonToolbarNextImgv)
    ImageView commonToolbarNextImgv;
    @BindView(R.id.commonToolbarTickImgv)
    ImageView commonToolbarTickImgv;
    @BindView(R.id.toolbarTitleTv)
    TextView toolbarTitleTv;
    @BindView(R.id.countryCodeTv)
    TextView countryCodeTv;
    @BindView(R.id.countryDialCodeTv)
    TextView countryDialCodeTv;
    @BindView(R.id.phoneNumEt)
    EditText phoneNumEt;
    @BindView(R.id.editPhoneMainLayout)
    RelativeLayout editPhoneMainLayout;

    Phone phone;
    PhoneVerification phoneVerification;
    CompleteCallback completeCallback;
    ProgressDialog mProgressDialog;
    String completePhoneNum;
    Phone selectedPhone;
    User user;
    List<Country> countryList;

    public PhoneNumEditFragment(Phone phone, CompleteCallback completeCallback) {
        this.phone = phone;
        this.completeCallback = completeCallback;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((NextActivity) getActivity()).ANIMATION_TAG = ANIMATE_LEFT_TO_RIGHT;
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_phone_num_edit, container, false);
            ButterKnife.bind(this, mView);
            init();
            addListeners();
            checkPhoneNumExistance();
        }

        return mView;
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
    public void accountHolderUserReceived(UserBus userBus) {
        user = userBus.getUser();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void init() {
        toolbarTitleTv.setText(getResources().getString(R.string.PHONE_NUM));
        mProgressDialog = new ProgressDialog(getActivity());
        countryList = new ArrayList<>();
        selectedPhone = new Phone();
    }

    public void addListeners() {
        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        commonToolbarNextImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

        commonToolbarTickImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyBoard(getContext());
                clearUserPhoneNum();
            }
        });

        countryCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountryFragment();
            }
        });

        countryDialCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountryFragment();
            }
        });

        phoneNumEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && !s.toString().isEmpty()) {
                    commonToolbarTickImgv.setVisibility(View.GONE);

                    if (phone != null && phone.getPhoneNumber() != 0) {
                        if (s.toString().trim().equals(Long.toString(phone.getPhoneNumber())))
                            commonToolbarNextImgv.setVisibility(View.GONE);
                        else
                            commonToolbarNextImgv.setVisibility(View.VISIBLE);
                    } else
                        commonToolbarNextImgv.setVisibility(View.VISIBLE);
                } else if (s != null && s.toString().isEmpty()) {
                    commonToolbarTickImgv.setVisibility(View.VISIBLE);
                    commonToolbarNextImgv.setVisibility(View.GONE);
                }
            }
        });
    }

    public void checkPhoneNumExistance() {

        if (phone != null && phone.getPhoneNumber() != 0) {
            phoneNumEt.setText(Long.toString(phone.getPhoneNumber()));
        }

        if (user != null && user.getPhone() != null) {

            if (user.getPhone().getDialCode() != null && !user.getPhone().getDialCode().trim().isEmpty() &&
                    user.getPhone().getCountryCode() != null && !user.getPhone().getCountryCode().trim().isEmpty() &&
                    user.getPhone().getPhoneNumber() != 0 ) {
                countryDialCodeTv.setText(user.getPhone().getDialCode().trim());
                countryCodeTv.setText(user.getPhone().getCountryCode().trim());
                phoneNumEt.setText(Long.toString(user.getPhone().getPhoneNumber()));
                selectedPhone.setDialCode(user.getPhone().getDialCode().trim());
                selectedPhone.setCountryCode(user.getPhone().getCountryCode().trim());
                selectedPhone.setPhoneNumber(user.getPhone().getPhoneNumber());
            } else
                getCountryList();
        } else
            getCountryList();
    }

    public void startCountryFragment() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new SelectCountryFragment(new ItemClickListener() {
                @Override
                public void onClick(Object object, int clickedItem) {
                    Country country = (Country) object;
                    setSelectedPhone(country);
                }
            }), ANIMATE_DOWN_TO_UP);
        }
    }

    public void getCountryList() {
        dialogShow();

        String countryCodesStr = CommonUtils.readCountryCodes(getContext());
        Gson gson = new Gson();
        Country[] countries = gson.fromJson(countryCodesStr, Country[].class);

        String locale = getActivity().getResources().getConfiguration().locale.getCountry();

        for (Country country : countries) {
            if (country != null && country.getCode() != null && !country.getCode().trim().isEmpty()) {
                if (country.getCode().trim().equals(locale)) {
                    setSelectedPhone(country);
                    break;
                }
            }
        }
        dialogDismiss();
    }

    public void setSelectedPhone(Country country) {
        countryDialCodeTv.setText(country.getDialCode());
        countryCodeTv.setText(country.getCode());
        selectedPhone.setCountryCode(country.getCode());
        selectedPhone.setDialCode(country.getDialCode());
    }

    public void clearUserPhoneNum() {
        user.setPhone(new Phone());

        UserDBHelper.addOrUpdateUser(user, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                completeCallback.onComplete(" ");
                getActivity().onBackPressed();
            }

            @Override
            public void OnFailed(String message) {
                CommonUtils.showToastShort(getActivity(), message);
            }
        });
    }

    public void sendVerificationCode() {
        dialogShow();
        completePhoneNum = countryDialCodeTv.getText().toString().trim() + phoneNumEt.getText().toString().trim();

        phoneVerification = new PhoneVerification(getActivity(), completePhoneNum, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                dialogDismiss();
                if (object != null) {
                    selectedPhone.setPhoneNumber(new Long(phoneNumEt.getText().toString().trim()));
                    startVerifyPhoneNumFragment();
                }
            }

            @Override
            public void onFailed(String s) {
                dialogDismiss();
                CommonUtils.showToastShort(getContext(), getActivity().getResources().getString(R.string.error) + s);
            }
        });
        phoneVerification.startPhoneNumberVerification();
    }

    public void startVerifyPhoneNumFragment() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new VerifyPhoneNumberFragment(selectedPhone, phoneVerification, new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    completeCallback.onComplete(completePhoneNum);
                    getActivity().onBackPressed();
                }

                @Override
                public void onFailed(String s) {

                }
            }), ANIMATE_DOWN_TO_UP);
        }
    }

    public void dialogShow() {
        if (!mProgressDialog.isShowing()) mProgressDialog.show();
    }

    public void dialogDismiss() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }
}
