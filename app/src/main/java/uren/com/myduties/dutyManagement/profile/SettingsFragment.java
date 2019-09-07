package uren.com.myduties.dutyManagement.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


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
import uren.com.myduties.utils.DynamicLinkUtil;
import uren.com.myduties.utils.ProgressDialogUtil;
import uren.com.myduties.utils.dialogBoxUtil.CustomDialogBox;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.CustomDialogListener;

import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;

public class SettingsFragment extends BaseFragment {

    View mView;
    ProgressDialogUtil progressDialogUtil;
    Fragment fragment;

    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    TextView toolbarTitleTv;
    @BindView(R.id.logoutLayout)
    LinearLayout logoutLayout;
    @BindView(R.id.addFromContactLayout)
    LinearLayout addFromContactLayout;
    @BindView(R.id.inviteForInstallLayout)
    LinearLayout inviteForInstallLayout;
    @BindView(R.id.changePasswordLayout)
    LinearLayout changePasswordLayout;
    @BindView(R.id.helpCenterLayout)
    LinearLayout helpCenterLayout;

    private User accountHolderUser;

    public SettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_LEFT_TO_RIGHT;
        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, mView);
        init();
        setDefaultUIValues();
        addListeners();
        return mView;
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(getActivity(), null, false);
        fragment = this;
    }

    public void setDefaultUIValues() {
        toolbarTitleTv.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.settings));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addListeners() {

        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutClicked();
            }
        });

        addFromContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startContactFriendsFragment();
            }
        });

        inviteForInstallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DynamicLinkUtil.setAppInvitationLink(Objects.requireNonNull(getContext()), fragment);
            }
        });

        changePasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangePasswordFragment();
            }
        });

        helpCenterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = Objects.requireNonNull(getContext()).getString(R.string.VERSION) + ":" + CommonUtils.getVersion(getContext()) + "\n\n" +
                        getContext().getString(R.string.ABOUT_APP) + "\n\n" +
                        getContext().getString(R.string.email) + ":" + getContext().getString(R.string.authoremail) + "\n";

                new CustomDialogBox.Builder((Activity) getContext())
                        .setMessage(message)
                        .setNegativeBtnVisibility(View.GONE)
                        .setPositiveBtnVisibility(View.VISIBLE)
                        .setPositiveBtnText(getContext().getResources().getString(R.string.ok))
                        .setPositiveBtnBackground(getContext().getResources().getColor(R.color.DodgerBlue, null))
                        .setDurationTime(0)
                        .isCancellable(true)
                        .OnPositiveClicked(new CustomDialogListener() {
                            @Override
                            public void OnClick() {

                            }
                        }).build();
            }
        });
    }


    public void startContactFriendsFragment() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new ContactsFragment(true), ANIMATE_LEFT_TO_RIGHT);
        }
    }

    public void startChangePasswordFragment() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new ChangePasswordFragment(), ANIMATE_LEFT_TO_RIGHT);
        }
    }


    private void signOutClicked() {
        SettingOperation.userSignOut(new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                Objects.requireNonNull(getActivity()).finish();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }

            @Override
            public void onFailed(String s) {
                CommonUtils.showToastShort(getContext(), s);
            }
        });
    }
}
