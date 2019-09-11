package uren.com.myduties.dutyManagement.profile;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dbManagement.UserPhotoDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.PhotoSelectUtil;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.IntentSelectUtil;
import uren.com.myduties.utils.PermissionModule;
import uren.com.myduties.utils.ProgressDialogUtil;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.PhotoChosenCallback;

import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.CAMERA_TEXT;
import static uren.com.myduties.constants.StringConstants.GALLERY_TEXT;

public class UserEditFragment extends BaseFragment
        implements View.OnClickListener {

    View mView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.rlProfilePicture)
    RelativeLayout rlProfilePicture;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.commonToolbarTickImgv)
    ImageView commonToolbarTickImgv;
    @BindView(R.id.toolbarTitleTv)
    AppCompatTextView toolbarTitleTv;
    @BindView(R.id.imgProfile)
    ImageView imgProfile;
    @BindView(R.id.addPhotoImgv)
    ImageView addPhotoImgv;
    @BindView(R.id.shortUserNameTv)
    TextView shortUserNameTv;
    @BindView(R.id.edtName)
    EditText edtName;
    @BindView(R.id.edtUserName)
    EditText edtUserName;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edtPhone)
    EditText edtPhone;


    PermissionModule permissionModule;
    PhotoSelectUtil photoSelectUtil;

    boolean profilPicChanged = false;
    boolean photoExist = false;

    ProgressDialogUtil progressDialogUtil;

    User user;

    private static final int ACTIVITY_REQUEST_CODE_OPEN_GALLERY = 385;
    private static final int ACTIVITY_REQUEST_CODE_OPEN_CAMERA = 85;

    public UserEditFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((NextActivity) getActivity()).ANIMATION_TAG = ANIMATE_LEFT_TO_RIGHT;
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mView = inflater.inflate(R.layout.fragment_user_edit, container, false);
        ButterKnife.bind(this, mView);
        init();
        return mView;
    }

    private void init() {
        initListeners();
        setShapes();
        profilPicChanged = false;
        toolbarTitleTv.setText(getContext().getResources().getString(R.string.editProfile));
        commonToolbarTickImgv.setVisibility(View.VISIBLE);
        permissionModule = new PermissionModule(getContext());
        progressDialogUtil = new ProgressDialogUtil(getContext(), getContext().getResources().getString(R.string.UPDATING), false);
    }

    public void initListeners() {
        commonToolbarTickImgv.setOnClickListener(this);
        commonToolbarbackImgv.setOnClickListener(this);
        rlProfilePicture.setOnClickListener(this);
        edtPhone.setOnClickListener(this);
    }

    public void setShapes() {
        addPhotoImgv.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.DodgerBlue, null),
                getResources().getColor(R.color.White, null), GradientDrawable.OVAL, 50, 5));
        imgProfile.setBackground(ShapeUtil.getShape(getActivity().getResources().getColor(R.color.DodgerBlue, null),
                0, GradientDrawable.OVAL, 50, 0));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUI();
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

    private void updateUI() {

        if (user != null) {

            if (user.getName() != null && !user.getName().isEmpty()) {
                edtName.setText(user.getName());
                shortUserNameTv.setText(UserDataUtil.getShortenUserName(user.getName()));
            }
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                edtUserName.setText(user.getUsername());
            }
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                edtEmail.setText(user.getEmail());
            }
            if (user.getPhone() != null) {
                if (user.getPhone().getDialCode() != null && !user.getPhone().getDialCode().isEmpty() &&
                        user.getPhone().getPhoneNumber() != 0 )
                    edtPhone.setText(user.getPhone().getDialCode() + user.getPhone().getPhoneNumber());
            }


            if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty())
                photoExist = true;
        }

        UserDataUtil.setProfilePicture(getContext(),
                user.getProfilePhotoUrl(),
                user.getName(),
                user.getUsername(),
                shortUserNameTv, imgProfile, true);
    }

    private void setUserPhoto(Uri photoUri) {
        if (photoUri != null && !photoUri.toString().trim().isEmpty()) {
            photoExist = true;
            shortUserNameTv.setVisibility(View.GONE);
            Glide.with(getActivity())
                    .load(photoUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile);
        } else if (user.getName() != null &&
                !user.getName().trim().isEmpty()) {
            photoExist = false;
            shortUserNameTv.setVisibility(View.VISIBLE);
            imgProfile.setImageDrawable(null);
        } else {
            photoExist = false;
            shortUserNameTv.setVisibility(View.GONE);
            Glide.with(getActivity())
                    .load(R.drawable.ic_person_white_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile);
        }
    }

    @Override
    public void onClick(View v) {

        if (v == commonToolbarbackImgv) {
            editProfileCancelClicked();
        }

        if (v == commonToolbarTickImgv) {
            CommonUtils.hideKeyBoard(getActivity());
            editProfileConfirmClicked();
        }

        if (v == rlProfilePicture) {
            chooseImageProcess();
        }

        if (v == edtPhone) {
            startEditPhoneNumber();
        }
    }

    public void startEditPhoneNumber() {

        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new PhoneNumEditFragment(user.getPhone(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    if (object != null) {
                        //updateUI();
                        //String phoneNum = (String) object;
                        //edtPhone.setText(phoneNum, TextView.BufferType.EDITABLE);
                    }
                }

                @Override
                public void onFailed(String s) {

                }
            }), ANIMATE_RIGHT_TO_LEFT);
        }
    }

    private void editProfileCancelClicked() {
        getActivity().onBackPressed();
    }

    private void editProfileConfirmClicked() {

        if (!photoExist)
            user.setProfilePhotoUrl("");
        else {
            user.setProfilePhotoUrl(user.getProfilePhotoUrl());
        }

        if (edtName.getText().toString().isEmpty()) {
            user.setName("");
        } else {
            user.setName(edtName.getText().toString());
        }

        if (edtUserName.getText().toString().isEmpty()) {
            user.setUsername("");
        } else {
            user.setUsername(edtUserName.getText().toString());
        }

        if (edtEmail.getText().toString().isEmpty()) {
            user.setEmail("");
        } else {
            user.setEmail(edtEmail.getText().toString());
        }

        updateOperation();
    }

    private void updateOperation() {

        progressDialogUtil.dialogShow();
        if(profilPicChanged) {
            UserPhotoDBHelper.uploadUserPhoto(getContext(), user.getUserid(), photoSelectUtil, new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    String downloadUrl = (String) object;
                    user.setProfilePhotoUrl(downloadUrl);
                    updateUserPersonalInfo();
                }

                @Override
                public void onFailed(String message) {
                    CommonUtils.showToastShort(getContext(), message);
                    progressDialogUtil.dialogDismiss();
                }
            });
        }else {
            updateUserPersonalInfo();
        }
    }

    private void updateUserPersonalInfo(){
        UserDBHelper.updateUser(user, true, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                progressDialogUtil.dialogDismiss();
                editProfileCancelClicked();
            }

            @Override
            public void OnFailed(String message) {
                CommonUtils.showToastShort(getContext(), message);
                progressDialogUtil.dialogDismiss();
            }
        });
    }

    private void chooseImageProcess() {
        PhotoChosenCallback photoChosenCallback = new PhotoChosenCallback() {
            @Override
            public void onGallerySelected() {
                getGalleryPermission();
            }

            @Override
            public void onCameraSelected() {
                checkCameraProcess();
            }

            @Override
            public void onPhotoRemoved() {
                photoSelectUtil = null;
                photoExist = false;
                setUserPhoto(null);
            }
        };

        DialogBoxUtil.photoChosenDialogBox(getContext(), getActivity().getResources().getString(R.string.chooseProfilePhoto), photoExist, photoChosenCallback);
    }

    private void getGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionModule.checkWriteExternalStoragePermission())
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE);
            else
                startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                        getResources().getString(R.string.selectPicture)), ACTIVITY_REQUEST_CODE_OPEN_GALLERY);
        } else
            startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                    getResources().getString(R.string.selectPicture)), ACTIVITY_REQUEST_CODE_OPEN_GALLERY);
    }

    private void checkCameraProcess() {
        if (!CommonUtils.checkCameraHardware(getContext())) {
            CommonUtils.showToastShort(getContext(), getContext().getResources().getString(R.string.deviceHasNoCamera));
            return;
        }

        if (permissionModule.checkCameraPermission()) {
            startActivityForResult(IntentSelectUtil.getCameraIntent(), ACTIVITY_REQUEST_CODE_OPEN_CAMERA);
        } else
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    PermissionModule.PERMISSION_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                        getResources().getString(R.string.selectPicture)), ACTIVITY_REQUEST_CODE_OPEN_GALLERY);
            }
        } else if (requestCode == PermissionModule.PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(IntentSelectUtil.getCameraIntent(), ACTIVITY_REQUEST_CODE_OPEN_CAMERA);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ACTIVITY_REQUEST_CODE_OPEN_GALLERY) {
                photoSelectUtil = new PhotoSelectUtil(getActivity(), data, GALLERY_TEXT);
                setUserPhoto(data.getData());
                profilPicChanged = true;
            } else if (requestCode == ACTIVITY_REQUEST_CODE_OPEN_CAMERA) {
                photoSelectUtil = new PhotoSelectUtil(getActivity(), data, CAMERA_TEXT);
                setUserPhoto(data.getData());
                profilPicChanged = true;
            }
        }
    }
}