package uren.com.myduties.dutyManagement.assignTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.assignTask.controller.CheckTaskItems;
import uren.com.myduties.dutyManagement.assignTask.interfaces.TaskTypeCallback;
import uren.com.myduties.dutyManagement.profile.SelectOneFriendFragment;
import uren.com.myduties.dutyManagement.profile.SelectOneGroupFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.TaskType;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.GroupDataUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;
import uren.com.myduties.utils.dialogBoxUtil.GifDialogBox;

import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;

public class AssignTaskFragment extends BaseFragment {


    View mView;

    @BindView(R.id.profilePicImgView)
    ImageView profilePicImgView;
    @BindView(R.id.shortUserNameTv)
    TextView shortUserNameTv;
    @BindView(R.id.toolbarTitle)
    AppCompatTextView toolbarTitle;
    @BindView(R.id.toolbarSubTitle)
    AppCompatTextView toolbarSubTitle;

    @BindView(R.id.personSelectImgv)
    ImageView personSelectImgv;

    @BindView(R.id.groupSelectImgv)
    ImageView groupSelectImgv;

    @BindView(R.id.taskTypeImgv)
    ClickableImageView taskTypeImgv;
    @BindView(R.id.taskTypeName)
    AppCompatTextView taskTypeName;

    @BindView(R.id.urgentSwitch)
    Switch urgentSwitch;

    @BindView(R.id.selectedPicImgView)
    ImageView selectedPicImgView;
    @BindView(R.id.selectedShortNameTv)
    TextView selectedShortNameTv;
    @BindView(R.id.selectedNameTv)
    AppCompatTextView selectedNameTv;
    @BindView(R.id.selectedLL)
    LinearLayout selectedLL;

    @BindView(R.id.shareMsgEditText)
    EditText shareMsgEditText;

    @BindView(R.id.nextFab)
    FloatingActionButton nextFab;

    TaskTypeHelper taskTypeHelper;

    User accountHolderUser;
    User selectedUser = null;
    Group selectedGroup = null;

    TaskType selectedTaskType = null;
    boolean urgency = false;

    public AssignTaskFragment() {
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

    @Subscribe(sticky = true)
    public void taskTypeHelperReceived(TaskTypeBus taskTypeBus) {
        taskTypeHelper = taskTypeBus.getTypeMap();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mView == null) {
            mView = inflater.inflate(R.layout.fragment_assign_task, container, false);
            ButterKnife.bind(this, mView);
            initializeItems();
            initializeListeners();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = null;
    }

    private void initializeItems() {
        setToolbarInfo();
        selectedTaskType = taskTypeHelper.getTypes().get(0);
        selectedUser = null;
        selectedGroup = null;
        urgency = false;
    }

    public void showShareSuccessView() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (NextActivity.thisActivity != null) {
                new GifDialogBox.Builder(NextActivity.thisActivity)
                        .setMessage(NextActivity.thisActivity.getResources().getString(R.string.taskAssignSuccessful))
                        .setGifResource(R.drawable.gif16)
                        .setNegativeBtnVisibility(View.GONE)
                        .setPositiveBtnVisibility(View.GONE)
                        .setTitleVisibility(View.GONE)
                        .setDurationTime(3000)
                        .isCancellable(true)
                        .build();
            }
        }, 1000);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeListeners() {
        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckTaskItems checkTaskItems = new CheckTaskItems(getContext(), accountHolderUser,
                        selectedUser, selectedGroup, selectedTaskType, urgency, shareMsgEditText.getText().toString(),
                        new OnCompleteCallback() {
                            @Override
                            public void OnCompleted() {
                                Objects.requireNonNull(getActivity()).onBackPressed();
                                showShareSuccessView();
                            }

                            @Override
                            public void OnFailed(String message) {
                                CommonUtils.showToastShort(getContext(), message);
                            }
                        });
                checkTaskItems.checkTaskThenAssign();
            }
        });

        personSelectImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSelectFriendFragment();
            }
        });

        groupSelectImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSelectGroupFragment();
            }
        });

        taskTypeImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTaskTypeSelectFragment();
            }
        });

        urgentSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        urgency = !urgentSwitch.isChecked();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    public void setToolbarInfo() {
        if (accountHolderUser != null) {
            if (accountHolderUser.getName() != null && !accountHolderUser.getName().isEmpty()) {
                UserDataUtil.setName(accountHolderUser.getName(), toolbarTitle);
            }

            if (accountHolderUser.getUsername() != null && !accountHolderUser.getUsername().isEmpty()) {
                UserDataUtil.setUsername(accountHolderUser.getUsername(), toolbarSubTitle);
            }

            UserDataUtil.setProfilePicture(getContext(), accountHolderUser.getProfilePhotoUrl(), accountHolderUser.getName(),
                    accountHolderUser.getUsername(), shortUserNameTv, profilePicImgView, false);
        }
    }

    private void startTaskTypeSelectFragment() {
        CommonUtils.showKeyboard(getContext(), false, shareMsgEditText);
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new TaskTypeSelectFragment(new TaskTypeCallback() {
                @Override
                public void OnReturn(TaskType taskType) {
                    selectedTaskType = taskType;
                    setTaskTypeViews(taskType);
                }
            }));
        }
    }

    private void startSelectFriendFragment() {
        CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new SelectOneFriendFragment(new ReturnCallback() {
                @Override
                public void OnReturn(Object object) {
                    selectedUser = (User) object;
                    setSelectedViews(selectedUser);
                }
            }), ANIMATE_RIGHT_TO_LEFT);
        }
    }

    private void startSelectGroupFragment() {
        CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new SelectOneGroupFragment(new ReturnCallback() {
                @Override
                public void OnReturn(Object object) {
                    selectedGroup = (Group) object;
                    setSelectedViews(selectedGroup);
                }
            }), ANIMATE_RIGHT_TO_LEFT);
        }
    }

    public void setSelectedViews(Object object) {
        if (object != null)
            selectedLL.setVisibility(View.VISIBLE);
        else
            selectedLL.setVisibility(View.GONE);

        if (object instanceof User) {
            setSelectedUserViews((User) object);
            selectedGroup = null;
        } else if (object instanceof Group) {
            setSelectedGroupViews((Group) object);
            selectedUser = null;
        }
    }

    private void setTaskTypeViews(TaskType taskType) {
        Glide.with(getContext())
                .load(taskType.getImgId())
                .apply(RequestOptions.centerInsideTransform())
                .into(taskTypeImgv);
        taskTypeName.setText(taskType.getDesc());
    }

    public void setSelectedUserViews(User user) {
        if (user != null) {
            UserDataUtil.setNameOrUserName(user.getName(), user.getUsername(), selectedNameTv);
            UserDataUtil.setProfilePicture(getContext(), user.getProfilePhotoUrl(), user.getName(),
                    user.getUsername(), selectedShortNameTv, selectedPicImgView, false);
            fillUserSelectImgv();
        }
    }

    public void setSelectedGroupViews(Group group) {
        if (group != null) {
            if (group.getName() != null && !group.getName().isEmpty()) {
                UserDataUtil.setName(group.getName(), selectedNameTv);
            }
            GroupDataUtil.setGroupPicture(getContext(), group.getGroupPhotoUrl(), group.getName(),
                    selectedShortNameTv, selectedPicImgView);
            fillGroupSelectImgv();
        }
    }

    public void fillUserSelectImgv() {
        personSelectImgv.setBackground(ShapeUtil.getShape(0,
                Objects.requireNonNull(getContext()).getResources().getColor(R.color.RoyalBlue),
                GradientDrawable.OVAL, 50, 3));
        groupSelectImgv.setBackground(null);
    }

    public void fillGroupSelectImgv() {
        groupSelectImgv.setBackground(ShapeUtil.getShape(0,
                Objects.requireNonNull(getContext()).getResources().getColor(R.color.RoyalBlue),
                GradientDrawable.OVAL, 50, 3));
        personSelectImgv.setBackground(null);
    }

}
