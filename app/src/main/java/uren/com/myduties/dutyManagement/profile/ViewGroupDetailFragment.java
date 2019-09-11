package uren.com.myduties.dutyManagement.profile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.dbManagement.GroupPhotoDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.GroupDetailListAdapter;
import uren.com.myduties.dutyManagement.tasks.GroupAllTasksFragment;
import uren.com.myduties.evetBusModels.SelectedUsersBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ItemClickListener;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.RecyclerViewAdapterCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.PhotoSelectUtil;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.IntentSelectUtil;
import uren.com.myduties.utils.PermissionModule;
import uren.com.myduties.utils.ProgressDialogUtil;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dialogBoxUtil.CustomDialogBox;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.PhotoChosenCallback;

import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.CAMERA_TEXT;
import static uren.com.myduties.constants.StringConstants.GALLERY_TEXT;
import static uren.com.myduties.constants.StringConstants.photo_upload_change;
import static uren.com.myduties.dutyManagement.profile.adapters.GroupDetailListAdapter.CODE_CHANGE_AS_ADMIN;
import static uren.com.myduties.dutyManagement.profile.adapters.GroupDetailListAdapter.CODE_REMOVE_FROM_GROUP;

public class ViewGroupDetailFragment extends BaseFragment {

    View mView;

    @BindView(R.id.groupPictureImgv)
    ImageView groupPictureImgV;
    @BindView(R.id.editImageView)
    ImageView editImageView;
    @BindView(R.id.groupCoordinatorLayout)
    CoordinatorLayout groupCoordinatorLayout;
    @BindView(R.id.personCntTv)
    AppCompatTextView personCntTv;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.addFriendCardView)
    CardView addFriendCardView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.addFriendImgv)
    ImageView addFriendImgv;
    @BindView(R.id.changePicImgv)
    ImageView changePicImgv;
    @BindView(R.id.backImgv)
    ImageView backImgv;
    @BindView(R.id.seeGroupTasksCardview)
    CardView seeGroupTasksCardview;

    boolean photoExistOnImgv = false;

    GroupDetailListAdapter adapter;
    Group groupRequestResultResultArrayItem;

    PermissionModule permissionModule;
    ProgressDialog mProgressDialog;

    PhotoSelectUtil photoSelectUtil;
    ProgressDialogUtil progressDialogUtil;

    RecyclerViewAdapterCallback recyclerViewAdapterCallback;
    String selectedType = "";
    User accountHolder;
    private List<User> selectedUsers;

    private static final int REQUEST_CODE_PHOTO_GALLERY_SELECT = 592;
    private static final int REQUEST_CODE_PHOTO_CAMERA_SELECT = 676;

    public ViewGroupDetailFragment(Group groupRequestResultResultArrayItem, RecyclerViewAdapterCallback recyclerViewAdapterCallback) {
        this.groupRequestResultResultArrayItem = groupRequestResultResultArrayItem;
        this.recyclerViewAdapterCallback = recyclerViewAdapterCallback;
    }

    @Override
    public void onStart() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.tabMainLayout).setVisibility(View.VISIBLE);
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
        accountHolder = userBus.getUser();
    }

    @Subscribe(sticky = true)
    public void selectedUsersReceived(SelectedUsersBus selectedUsersBus){
        selectedUsers = selectedUsersBus.getUsers();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mView == null) {
            mView = inflater.inflate(R.layout.fragment_view_group_detail, container, false);
            ButterKnife.bind(this, mView);
            setGUIVariables();
            setupViewRecyclerView();
            getGroupInformation();
            addListeners();
            setShapes();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_RIGHT_TO_LEFT;
    }

    public void setGUIVariables() {
        permissionModule = new PermissionModule(getActivity());
        mProgressDialog = new ProgressDialog(getActivity());
        progressDialogUtil = new ProgressDialogUtil(getActivity(), null, true);

    }

    public void setShapes() {
        addFriendImgv.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.LimeGreen, null),
                0, GradientDrawable.OVAL, 50, 0));
    }

    private void getGroupInformation() {
        if (groupRequestResultResultArrayItem != null) {
            setCardViewVisibility();
            setGroupTitle();
            setGroupImage(groupRequestResultResultArrayItem.getGroupPhotoUrl());
            startGetGroupParticipants();
        }
    }

    private void startGetGroupParticipants() {

        progressDialogUtil.dialogShow();

        for (User user : groupRequestResultResultArrayItem.getMemberList()) {
            if (user != null && user.getUserid() != null) {
                UserDBHelper.getUser(user.getUserid(), new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {
                        adapter.addFriend((User) object);
                        progressDialogUtil.dialogDismiss();
                    }

                    @Override
                    public void onFailed(String message) {
                        progressDialogUtil.dialogDismiss();
                    }
                });
            }
        }
    }

    public void setCardViewVisibility() {
        if (accountHolder.getUserid().equals(groupRequestResultResultArrayItem.getGroupAdmin()))
            addFriendCardView.setVisibility(View.VISIBLE);
    }

    public void setGroupTitle() {
        collapsingToolbarLayout.setTitle(groupRequestResultResultArrayItem.getName());
    }

    public void setGroupImage(String photoUrl) {
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            photoExistOnImgv = true;
            Glide.with(this)
                    .load(photoUrl)
                    .apply(RequestOptions.centerInsideTransform())
                    .into(groupPictureImgV);
        } else {
            photoExistOnImgv = false;
            Glide.with(this)
                    .load(R.drawable.peoplegroup_bg)
                    .apply(RequestOptions.centerInsideTransform())
                    .into(groupPictureImgV);
        }
    }

    public void addListeners() {
        seeGroupTasksCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentNavigation.pushFragment(new GroupAllTasksFragment(groupRequestResultResultArrayItem));
            }
        });

        backImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        addFriendCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFragmentNavigation != null) {
                    mFragmentNavigation.pushFragment(new SelectFriendFragment(groupRequestResultResultArrayItem.getGroupid(),
                            groupRequestResultResultArrayItem.getGroupAdmin(),
                            adapter.getGroupParticipantList(), ViewGroupDetailFragment.class.getName(),
                            new ReturnCallback() {
                                @Override
                                public void OnReturn(Object object) {
                                    adapter.addFriend((User) object);
                                }
                            }), ANIMATE_RIGHT_TO_LEFT);
                }
            }
        });

        editImageView.setOnClickListener((View v) -> {
            editImageView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
            if (mFragmentNavigation != null) {
                mFragmentNavigation.pushFragment(new EditGroupNameFragment(groupRequestResultResultArrayItem,
                        new CompleteCallback() {
                            @Override
                            public void onComplete(Object object) {
                                if (object != null) {
                                    String edittedGroupName = (String) object;
                                    collapsingToolbarLayout.setTitle(edittedGroupName);
                                    recyclerViewAdapterCallback.OnChanged(groupRequestResultResultArrayItem);
                                }
                            }

                            @Override
                            public void onFailed(String s) {
                                CommonUtils.showToastShort(getContext(), s);
                            }
                        }), ANIMATE_LEFT_TO_RIGHT);
            }
        });

        groupPictureImgV.setOnClickListener((View v) -> {
            startShowSelectedPhotoFragment();
        });

        changePicImgv.setOnClickListener((View v) -> {
            startChooseImageProc();
        });
    }

    private void startShowSelectedPhotoFragment() {
        if (groupRequestResultResultArrayItem != null && groupRequestResultResultArrayItem.getGroupPhotoUrl() != null &&
                !groupRequestResultResultArrayItem.getGroupPhotoUrl().trim().isEmpty())
            mFragmentNavigation.pushFragment(new ShowSelectedPhotoFragment(groupRequestResultResultArrayItem.getGroupPhotoUrl()));
    }

    private void setupViewRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new GroupDetailListAdapter(getActivity(), groupRequestResultResultArrayItem, new ItemClickListener() {
            @Override
            public void onClick(Object object, int clickedItem) {
                if (clickedItem == CODE_CHANGE_AS_ADMIN) {
                    recyclerViewAdapterCallback.OnChanged(object);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void startChooseImageProc() {
        DialogBoxUtil.photoChosenDialogBox(getActivity(), getResources().
                getString(R.string.CHOOSE_GROUP_PHOTO), photoExistOnImgv, new PhotoChosenCallback() {
            @Override
            public void onGallerySelected() {
                selectedType = GALLERY_TEXT;
                startGalleryProcess();
            }

            @Override
            public void onCameraSelected() {
                selectedType = CAMERA_TEXT;
                startCameraProcess();
            }

            @Override
            public void onPhotoRemoved() {
                groupRequestResultResultArrayItem.setGroupPhotoUrl("");
                photoSelectUtil = null;
                updateGroup();
            }
        });
    }

    public void startCameraProcess() {

        if (!CommonUtils.checkCameraHardware(Objects.requireNonNull(getActivity()))) {
            CommonUtils.showToastShort(getActivity(), getResources().getString(R.string.deviceHasNoCamera));
            return;
        }

        if (!permissionModule.checkWriteExternalStoragePermission())
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE);
        else
            checkCameraPermission();
    }

    public void checkCameraPermission() {
        if (!permissionModule.checkCameraPermission())
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PermissionModule.PERMISSION_CAMERA);
        else {
            startActivityForResult(IntentSelectUtil.getCameraIntent(), REQUEST_CODE_PHOTO_CAMERA_SELECT);
        }
    }

    private void startGalleryProcess() {
        if (!permissionModule.checkWriteExternalStoragePermission()) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else
            startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                    getResources().getString(R.string.selectPicture)), REQUEST_CODE_PHOTO_GALLERY_SELECT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PHOTO_CAMERA_SELECT) {
                photoSelectUtil = new PhotoSelectUtil(getActivity(), data, CAMERA_TEXT);
                updateGroup();
            } else if (requestCode == REQUEST_CODE_PHOTO_GALLERY_SELECT) {
                photoSelectUtil = new PhotoSelectUtil(getActivity(), data, GALLERY_TEXT);
                updateGroup();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (selectedType.equals(CAMERA_TEXT))
                    checkCameraPermission();
                else if (selectedType.equals(GALLERY_TEXT))
                    startGalleryProcess();
            }
        } else if (requestCode == PermissionModule.PERMISSION_CAMERA) {
            checkCameraPermission();
        }
    }

    public void updateGroup() {

        GroupPhotoDBHelper.uploadGroupPhoto(photo_upload_change, getContext(), groupRequestResultResultArrayItem.getGroupid(), photoSelectUtil,
                new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {
                        if (object != null)
                            groupRequestResultResultArrayItem.setGroupPhotoUrl((String) object);
                        else
                            groupRequestResultResultArrayItem.setGroupPhotoUrl(null);

                        GroupDBHelper.updateGroupPhoto(groupRequestResultResultArrayItem.getGroupid(),
                                groupRequestResultResultArrayItem.getGroupPhotoUrl(), new OnCompleteCallback() {
                                    @Override
                                    public void OnCompleted() {
                                        setGroupImage(groupRequestResultResultArrayItem.getGroupPhotoUrl());
                                        recyclerViewAdapterCallback.OnChanged(groupRequestResultResultArrayItem);
                                    }

                                    @Override
                                    public void OnFailed(String message) {

                                    }
                                });
                    }

                    @Override
                    public void onFailed(String message) {
                        CommonUtils.showToastShort(getContext(), message);
                    }
                });
    }
}
