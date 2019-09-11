package uren.com.myduties.dutyManagement.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.dbManagement.GroupPhotoDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.profile.adapters.FriendGridListAdapter;
import uren.com.myduties.evetBusModels.SelectedUsersBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.PhotoSelectUtil;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.IntentSelectUtil;
import uren.com.myduties.utils.PermissionModule;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.PhotoChosenCallback;

import static uren.com.myduties.constants.NumericConstants.GROUP_NAME_MAX_LENGTH;
import static uren.com.myduties.constants.StringConstants.CAMERA_TEXT;
import static uren.com.myduties.constants.StringConstants.GALLERY_TEXT;
import static uren.com.myduties.constants.StringConstants.fb_child_groups;
import static uren.com.myduties.constants.StringConstants.photo_upload_new;

@SuppressLint("ValidFragment")
public class AddGroupFragment extends BaseFragment {

    View mView;

    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    AppCompatTextView toolbarTitleTv;

    @BindView(R.id.saveGroupInfoFab)
    FloatingActionButton saveGroupInfoFab;
    @BindView(R.id.groupNameEditText)
    EditText groupNameEditText;
    @BindView(R.id.groupPictureImgv)
    ImageView groupPictureImgv;
    @BindView(R.id.addGroupDtlRelLayout)
    RelativeLayout addGroupDtlRelLayout;
    @BindView(R.id.participantSize)
    AppCompatTextView participantSize;
    @BindView(R.id.textSizeCntTv)
    AppCompatTextView textSizeCntTv;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    PhotoSelectUtil photoSelectUtil;
    int groupNameSize = 0;
    GradientDrawable imageShape;
    boolean groupPhotoExist = false;

    FriendGridListAdapter adapter;
    PermissionModule permissionModule;

    CompleteCallback completeCallback;

    private List<User> selectedUsers;
    private User accountholderUser;

    private static final int CODE_GALLERY_REQUEST = 665;
    private static final int CODE_CAMERA_REQUEST = 662;

    public AddGroupFragment(CompleteCallback completeCallback) {
        this.completeCallback = completeCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Subscribe(sticky = true)
    public void selectedUsersReceived(SelectedUsersBus selectedUsersBus) {
        selectedUsers = selectedUsersBus.getUsers();
    }

    @Subscribe(sticky = true)
    public void accountHolderUserReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_add_group, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbarTitleTv.setText(getResources().getString(R.string.addGroupName));
        addListeners();
        setGroupTextSize();
        openPersonSelectionPage();
        permissionModule = new PermissionModule(getContext());
        imageShape = ShapeUtil.getShape(getResources().getColor(R.color.LightGrey, null),
                0, GradientDrawable.OVAL, 50, 0);
        groupPictureImgv.setBackground(imageShape);
        participantSize.setText(Integer.toString(selectedUsers.size()));
    }

    public void addListeners() {

        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        groupPictureImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChooseImageProc();
            }
        });

        saveGroupInfoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
                if (groupNameEditText.getText().toString().equals("") || groupNameEditText.getText() == null) {
                    CommonUtils.showToastShort(getContext(), getResources().getString(R.string.pleaseWriteGroupName));
                    return;
                }
                saveGroup();
            }
        });

        addGroupDtlRelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
            }
        });

        groupNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                groupNameSize = GROUP_NAME_MAX_LENGTH - s.toString().length();

                if (groupNameSize >= 0)
                    textSizeCntTv.setText(Integer.toString(groupNameSize));
                else
                    textSizeCntTv.setText(Integer.toString(0));
            }
        });
    }

    private void startChooseImageProc() {
        DialogBoxUtil.photoChosenDialogBox(getContext(), getResources().
                getString(R.string.chooseProfilePhoto), groupPhotoExist, new PhotoChosenCallback() {
            @Override
            public void onGallerySelected() {
                startGalleryProcess();
            }

            @Override
            public void onCameraSelected() {
                startCameraProcess();
            }

            @Override
            public void onPhotoRemoved() {
                setGroupPhoto(null);
            }
        });
    }

    private void startGalleryProcess() {
        if (permissionModule.checkWriteExternalStoragePermission())
            startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                    getResources().getString(R.string.selectPicture)), CODE_GALLERY_REQUEST);
        else
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    public void startCameraProcess() {

        if (!CommonUtils.checkCameraHardware(Objects.requireNonNull(getContext()))) {
            CommonUtils.showToastShort(getContext(), getResources().getString(R.string.deviceHasNoCamera));
            return;
        }

        if (!permissionModule.checkCameraPermission())
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PermissionModule.PERMISSION_CAMERA);
        else
            startActivityForResult(IntentSelectUtil.getCameraIntent(), CODE_CAMERA_REQUEST);
    }

    private void setGroupTextSize() {
        groupNameSize = GROUP_NAME_MAX_LENGTH;
        textSizeCntTv.setText(Integer.toString(GROUP_NAME_MAX_LENGTH));
    }

    private void openPersonSelectionPage() {
        adapter = new FriendGridListAdapter(getContext(), new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {

                int itemCount = (int) object;

                if (itemCount == 0)
                    Objects.requireNonNull(getActivity()).onBackPressed();
                else
                    participantSize.setText(Integer.toString(itemCount));
            }
        });
        recyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void saveGroup() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_groups);
        String groupid = databaseReference.push().getKey();

        selectedUsers.add(accountholderUser);
        Group group = new Group(groupid,groupNameEditText.getText().toString(),
                null, 0, accountholderUser.getUserid(), selectedUsers);

        GroupPhotoDBHelper.uploadGroupPhoto(photo_upload_new, getContext(), groupid, photoSelectUtil, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {

                if(object != null) {
                    String url = (String) object;
                    group.setGroupPhotoUrl(url);
                }

                GroupDBHelper.addGroup(group, new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {

                        accountholderUser.getGroupIdList().add(group.getGroupid());

                        GroupDBHelper.getGroup(group.getGroupid(), new CompleteCallback() {
                            @Override
                            public void onComplete(Object object) {
                                completeCallback.onComplete(object);
                                Objects.requireNonNull(getActivity()).onBackPressed();
                            }

                            @Override
                            public void onFailed(String message) {
                                CommonUtils.showToastShort(getContext(), message);
                            }
                        });
                    }

                    @Override
                    public void onFailed(String message) {

                    }
                });
            }

            @Override
            public void onFailed(String message) {
                CommonUtils.showToastShort(getContext(), message);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == CODE_CAMERA_REQUEST) {
                photoSelectUtil = new PhotoSelectUtil(getContext(), data, CAMERA_TEXT);
                setGroupPhoto(photoSelectUtil.getMediaUri());
            } else if (requestCode == CODE_GALLERY_REQUEST) {
                photoSelectUtil = new PhotoSelectUtil(getContext(), data, GALLERY_TEXT);
                setGroupPhoto(photoSelectUtil.getMediaUri());
            }
        }
    }

    public void setGroupPhoto(Uri groupPhotoUri) {
        if (groupPhotoUri != null && !groupPhotoUri.toString().trim().isEmpty()) {
            groupPhotoExist = true;
            groupPictureImgv.setPadding(0, 0, 0, 0);
            Glide.with(Objects.requireNonNull(getContext()))
                    .load(groupPhotoUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(groupPictureImgv);
        } else {
            groupPhotoExist = false;
            photoSelectUtil = null;
            int paddingPx = getResources().getDimensionPixelSize(R.dimen.ADD_GROUP_IMGV_SIZE);
            groupPictureImgv.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            Glide.with(this)
                    .load(R.drawable.ic_camera_white_24dp)
                    .into(groupPictureImgv);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionModule.PERMISSION_WRITE_EXTERNAL_STORAGE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(Intent.createChooser(IntentSelectUtil.getGalleryIntent(),
                        getResources().getString(R.string.selectPicture)), CODE_GALLERY_REQUEST);
            }
        } else if (requestCode == PermissionModule.PERMISSION_CAMERA) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(IntentSelectUtil.getCameraIntent(), CODE_CAMERA_REQUEST);
            }
        }
    }
}
