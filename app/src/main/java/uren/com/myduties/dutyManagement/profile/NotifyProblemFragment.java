package uren.com.myduties.dutyManagement.profile;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.ProblemDBHelper;
import uren.com.myduties.dbManagement.ProblemPhotoDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.fragmentControllers.FragNavController;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.PhotoSelectUtil;
import uren.com.myduties.models.Problem;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.BitmapConversion;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.IntentSelectUtil;
import uren.com.myduties.utils.PermissionModule;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.InfoDialogBoxCallback;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.PhotoChosenForReportCallback;

import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.GALLERY_TEXT;
import static uren.com.myduties.constants.StringConstants.fb_child_problems;
import static uren.com.myduties.constants.StringConstants.fb_child_value_android;
import static uren.com.myduties.constants.StringConstants.fb_child_value_bug;

public class NotifyProblemFragment extends BaseFragment {

    View mView;

    @BindView(R.id.commonToolbarTickImgv)
    ImageView commonToolbarTickImgv;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    TextView toolbarTitleTv;
    @BindView(R.id.noteTextEditText)
    EditText noteTextEditText;

    @BindView(R.id.addPhotoImgv1)
    ImageView addPhotoImgv1;

    @BindView(R.id.imgDelete1)
    ImageView imgDelete1;


    Button screenShotApproveBtn;
    Button screenShotCancelBtn;
    RelativeLayout screenShotMainLayout;
    LinearLayout profilePageMainLayout;

    PhotoSelectUtil photoSelectUtil = null;
    PermissionModule permissionModule;
    User accountHolderUser;

    private static final int CODE_GALLERY_REQUEST = 665;

    public NotifyProblemFragment() {

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
    public void accountHolderUserReceived(UserBus userBus) {
        accountHolderUser = userBus.getUser();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_notify_problem, container, false);
            ButterKnife.bind(this, mView);
            initVariables();
            addListeners();
            setShapes();
            initProblemList();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_RIGHT_TO_LEFT;
    }

    private void initVariables() {
        toolbarTitleTv.setText(getResources().getString(R.string.REPORT_PROBLEM_OR_COMMENT));
        permissionModule = new PermissionModule(getContext());
        NextActivity.notifyProblemFragment = this;
        commonToolbarTickImgv.setVisibility(View.VISIBLE);
        screenShotApproveBtn = Objects.requireNonNull(getActivity()).findViewById(R.id.screenShotApproveBtn);
        screenShotCancelBtn = getActivity().findViewById(R.id.screenShotCancelBtn);
        screenShotMainLayout = getActivity().findViewById(R.id.screenShotMainLayout);
        profilePageMainLayout = getActivity().findViewById(R.id.profilePageMainLayout);
    }

    public void setShapes() {
        GradientDrawable shape = ShapeUtil.getShape(getResources().getColor(R.color.White, null),
                getResources().getColor(R.color.Gray, null), GradientDrawable.RECTANGLE, 15, 2);
        addPhotoImgv1.setBackground(shape);
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
                if (noteTextEditText != null && noteTextEditText.getText() != null &&
                        noteTextEditText.getText().toString().isEmpty()) {
                    CommonUtils.showToastShort(getContext(), getResources().getString(R.string.CAN_YOU_SPECIFY_THE_PROBLEM));
                    return;
                }
                saveReport();
            }
        });

        addPhotoImgv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managePhotoChosen();
            }
        });

        imgDelete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePhoto();
            }
        });
    }

    public void initProblemList() {
        setViewPadding();
    }

    public void setViewPadding() {
        addPhotoImgv1.setPadding(70, 70, 70, 70);
        addPhotoImgv1.setColorFilter(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.Gray, null), PorterDuff.Mode.SRC_IN);
        addPhotoImgv1.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgDelete1.setVisibility(View.GONE);
    }

    public void clearViewPadding() {
        addPhotoImgv1.setPadding(0, 0, 0, 0);
        addPhotoImgv1.setColorFilter(null);
        addPhotoImgv1.setScaleType(ImageView.ScaleType.FIT_XY);
        imgDelete1.setVisibility(View.VISIBLE);
    }

    public void managePhotoChosen() {
        if (photoSelectUtil != null) {

            if (mFragmentNavigation != null) {
                mFragmentNavigation.pushFragment(new MarkProblemFragment(photoSelectUtil, new ReturnCallback() {
                            @Override
                            public void OnReturn(Object object) {
                                photoSelectUtil = (PhotoSelectUtil) object;
                                setPhotoSelectUtil(photoSelectUtil);
                            }
                        }),
                        ANIMATE_RIGHT_TO_LEFT);
            }
            return;
        }

        startPhotoChosen();
    }

    public void startPhotoChosen() {
        DialogBoxUtil.photoChosenForProblemReportDialogBox(getContext(), null, new PhotoChosenForReportCallback() {
            @Override
            public void onGallerySelected() {
                startGalleryProcess();
            }

            @Override
            public void onScreenShot() {
                screenShotStart();
            }
        });
    }

    private void screenShotStart() {
        Objects.requireNonNull(getActivity()).onBackPressed();

        screenShotMainLayout.setVisibility(View.VISIBLE);

        screenShotApproveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = BitmapConversion.getScreenShot(profilePageMainLayout);
                PhotoSelectUtil photoSelectUtil = new PhotoSelectUtil();
                photoSelectUtil.setBitmap(bitmap);
                setPhotoSelectUtil(photoSelectUtil);
                returnNotifyFragment();
            }
        });

        screenShotCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnNotifyFragment();
            }
        });
    }

    public void returnNotifyFragment() {
        screenShotMainLayout.setVisibility(View.GONE);
        screenShotApproveBtn.setOnClickListener(null);
        screenShotCancelBtn.setOnClickListener(null);

        if (getActivity() != null)
            ((NextActivity) getActivity()).switchAndUpdateTabSelection(FragNavController.TAB3);
        else {
            if (mFragmentNavigation != null) {
                mFragmentNavigation.pushFragment(NextActivity.notifyProblemFragment, ANIMATE_RIGHT_TO_LEFT);
            }
        }
    }

    public void removePhoto() {
        photoSelectUtil = null;
        setViewPadding();
        Glide.with(NextActivity.thisActivity)
                .load(R.drawable.ic_add_white_24dp)
                .apply(RequestOptions.centerInsideTransform())
                .into(addPhotoImgv1);
    }

    private void startGalleryProcess() {
        if (permissionModule.checkWriteExternalStoragePermission())
            startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                    getResources().getString(R.string.selectPicture)), CODE_GALLERY_REQUEST);
        else
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                        getResources().getString(R.string.selectPicture)), CODE_GALLERY_REQUEST);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODE_GALLERY_REQUEST) {
                PhotoSelectUtil photoSelectUtil = new PhotoSelectUtil(getActivity(), data, GALLERY_TEXT);
                setPhotoSelectUtil(photoSelectUtil);
            }
        }
    }

    public void setPhotoSelectUtil(PhotoSelectUtil photoSelectUtil) {
        this.photoSelectUtil = photoSelectUtil;
        clearViewPadding();

        if (photoSelectUtil.getBitmap() != null)
            Glide.with(NextActivity.thisActivity)
                    .load(photoSelectUtil.getBitmap())
                    .apply(RequestOptions.fitCenterTransform())
                    .into(addPhotoImgv1);
    }

    public void saveReport() {
        DialogBoxUtil.showInfoDialogWithLimitedTime(getContext(), null,
                getResources().getString(R.string.THANKS_FOR_FEEDBACK), 3000, new InfoDialogBoxCallback() {
                    @Override
                    public void okClick() {
                        Objects.requireNonNull(getActivity()).onBackPressed();
                        ((NextActivity) getActivity()).clearStackGivenIndex(FragNavController.TAB1);
                        ((NextActivity) getActivity()).switchAndUpdateTabSelection(FragNavController.TAB3);

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_problems);
                        String problemid = databaseReference.push().getKey();

                        Problem problem = new Problem();
                        problem.setProblemid(problemid);
                        problem.setFixed(false);
                        problem.setWhoOpened(accountHolderUser);
                        problem.setPlatform(fb_child_value_android);
                        problem.setProblemDesc(noteTextEditText.getText().toString());
                        problem.setType(fb_child_value_bug);

                        if (photoSelectUtil != null) {
                            ProblemPhotoDBHelper.uploadProblemPhoto(getContext(), problemid, photoSelectUtil, new CompleteCallback() {
                                @Override
                                public void onComplete(Object object) {
                                    String url = (String) object;
                                    problem.setProblemPhotoUrl(url);
                                    saveProblemInformation(problem);
                                }

                                @Override
                                public void onFailed(String message) {
                                    CommonUtils.showToastShort(getContext(), message);
                                }
                            });
                        }else
                            saveProblemInformation(problem);
                    }
                });
    }

    private void saveProblemInformation(Problem problem) {
        ProblemDBHelper.addProblem(problem, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {

            }

            @Override
            public void onFailed(String message) {

            }
        });
    }
}