package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.MainActivity;
import uren.com.myduties.R;
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dbManagement.UserTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.helper.SettingOperation;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;
import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;

public class ProfileFragment extends BaseFragment {


    View mView;

    @BindView(R.id.menuImgv)
    ClickableImageView menuImgv;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.navViewLayout)
    NavigationView navViewLayout;
    @BindView(R.id.imgUserEdit)
    ClickableImageView imgUserEdit;

    @BindView(R.id.imgProfile)
    ImageView imgProfile;
    @BindView(R.id.txtProfile)
    TextView txtProfile;
    @BindView(R.id.txtName)
    AppCompatTextView txtName;
    @BindView(R.id.txtUsername)
    AppCompatTextView txtUsername;
    @BindView(R.id.emailTv)
    AppCompatTextView emailTv;
    @BindView(R.id.phoneTv)
    AppCompatTextView phoneTv;
    @BindView(R.id.llUserInfo)
    LinearLayout llUserInfo;

    @BindView(R.id.friendsCntTv)
    TextView friendsCntTv;
    @BindView(R.id.groupsCntTv)
    TextView groupsCntTv;
    @BindView(R.id.tasksCntTv)
    TextView tasksCntTv;

    @BindView(R.id.progressBar1)
    ProgressBar progressBar1;
    @BindView(R.id.progressBar2)
    ProgressBar progressBar2;
    @BindView(R.id.progressBar3)
    ProgressBar progressBar3;

    TextView navViewNameTv;
    TextView navViewEmailTv;
    ImageView navImgProfile;
    TextView navViewShortenTextView;

    @BindView(R.id.llFollowInfo)
    RelativeLayout llFollowInfo;
    @BindView(R.id.rlOut)
    RelativeLayout rlOut;

    @BindView(R.id.friendsLayout)
    LinearLayout friendsLayout;
    @BindView(R.id.groupsLayout)
    LinearLayout groupsLayout;
    @BindView(R.id.tasksLayout)
    LinearLayout tasksLayout;

    boolean mDrawerState;
    int assignedTaskCnt = 0;

    User user;

    public ProfileFragment() {
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
        user = userBus.getUser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, mView);
        initVariables();
        initListeners();
        setProfileDetails();

        return mView;
    }

    private void setFriendsCntTv() {
        //friendsCntTv.setText(Integer.toString(0));
        FriendsDBHelper.getFriendCountByStatus(user.getUserid(), fb_child_status_friend, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                progressBar1.setVisibility(View.GONE);
                int count = (int) object;
                friendsCntTv.setText(Integer.toString(count));
            }

            @Override
            public void onFailed(String message) {
                progressBar1.setVisibility(View.GONE);
                friendsCntTv.setText(Integer.toString(0));
            }
        });
    }

    private void setGroupsCntTv() {
        if (user.getGroupIdList() != null) {
            progressBar2.setVisibility(View.GONE);
            groupsCntTv.setText(Integer.toString(user.getGroupIdList().size()));
        }else {
            GroupDBHelper.getUserGroupsCount(user.getUserid(), new ReturnCallback() {
                @Override
                public void OnReturn(Object object) {
                    progressBar2.setVisibility(View.GONE);
                    int count = (int) object;
                    groupsCntTv.setText(Integer.toString(count));
                }
            });
        }
    }

    private void setTasksCntTv() {
        assignedTaskCnt = 0;
        UserTaskDBHelper.getIAssignedTasksToUsersCount(user.getUserid(), new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {
                progressBar3.setVisibility(View.GONE);
                assignedTaskCnt = assignedTaskCnt + (int) object;
                tasksCntTv.setText(Integer.toString(assignedTaskCnt));
            }
        });


        GroupTaskDBHelper.getIAssignedTasksToGroupsCount(user.getUserid(), new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {
                progressBar3.setVisibility(View.GONE);
                assignedTaskCnt = assignedTaskCnt + (int) object;
                tasksCntTv.setText(Integer.toString(assignedTaskCnt));
            }
        });
    }

    private void setProfileDetails() {
        if (user != null) {
            UserDataUtil.setProfilePicture(getContext(), user.getProfilePhotoUrl(),
                    user.getName(), user.getUsername(), txtProfile, imgProfile, true);
            imgProfile.setPadding(7, 7, 7, 7);

            //Name
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                txtName.setText(user.getName());
                navViewNameTv.setText(user.getName());
            } else {
                txtName.setVisibility(View.GONE);
                navViewNameTv.setVisibility(View.GONE);
            }

            //Username
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty())
                txtUsername.setText(CHAR_AMPERSAND + user.getUsername());
            else
                txtUsername.setVisibility(View.GONE);

            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                navViewEmailTv.setText(user.getEmail());
                emailTv.setText(user.getEmail());
            } else
                navViewEmailTv.setVisibility(View.GONE);

            //profile picture
            UserDataUtil.setProfilePicture(getContext(), user.getProfilePhotoUrl(),
                    user.getName(), user.getUsername(), txtProfile, imgProfile, false);
            imgProfile.setPadding(3, 3, 3, 3);
            //navigation profile picture
            UserDataUtil.setProfilePicture(getContext(), user.getProfilePhotoUrl(),
                    user.getName(), user.getUsername(), navViewShortenTextView, navImgProfile, false);


            if (user.getPhone() != null && user.getPhone().getDialCode() != null && user.getPhone().getPhoneNumber() != 0)
                phoneTv.setText(user.getPhone().getDialCode() + " " + user.getPhone().getPhoneNumber());
        }
    }

    private void initVariables() {
        llFollowInfo.setBackground(ShapeUtil.getShape(getContext().getResources().getColor(R.color.White, null),
                getContext().getResources().getColor(R.color.DarkGray, null), GradientDrawable.RECTANGLE, 30, 2));
        rlOut.setBackground(ShapeUtil.getShape(getContext().getResources().getColor(R.color.White, null),
                0, GradientDrawable.RECTANGLE, 30, 0));

        setNavViewItems();
        setFriendsCntTv();
        setGroupsCntTv();
        setTasksCntTv();
    }

    private void initListeners() {
        friendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentNavigation.pushFragment(new FriendsFragment());
            }
        });

        groupsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentNavigation.pushFragment(new GroupViewFragment(new ReturnCallback() {
                    @Override
                    public void OnReturn(Object object) {

                    }
                }));
            }
        });

        tasksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentNavigation.pushFragment(new TasksIAssignedFragment());
                // TODO: 2019-09-04
            }
        });

        imgUserEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgUserEdit.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                userEditClicked();
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null && user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                    mFragmentNavigation.pushFragment(new ShowSelectedPhotoFragment(user.getProfilePhotoUrl()));
                }
            }
        });

        menuImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuImgv.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));

                if (mDrawerState) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });

        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(getActivity(),
                drawerLayout,
                null,
                0,
                0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mDrawerState = false;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerState = true;
            }
        });

        navViewLayout.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {

                case R.id.viewItem:
                    mDrawerState = false;
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startPendingRequestFragment();
                    break;

                case R.id.settingsItem:
                    mDrawerState = false;
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startSettingsFragment();
                    break;

                case R.id.reportProblemItem:
                    mDrawerState = false;
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startNotifyProblemFragment();
                    break;

                case R.id.rateUs:
                    mDrawerState = false;
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    CommonUtils.commentApp(getContext());
                    break;

                case R.id.exit:
                    mDrawerState = false;
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    signOutClicked();
                    break;

                default:
                    break;
            }

            return false;
        });
    }

    private void startPendingRequestFragment() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new PendingRequestsFragment(), ANIMATE_LEFT_TO_RIGHT);
        }
    }

    private void startSettingsFragment() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new SettingsFragment(), ANIMATE_RIGHT_TO_LEFT);
        }
    }

    private void userEditClicked() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new UserEditFragment(), ANIMATE_LEFT_TO_RIGHT);
        }
    }

    public void startNotifyProblemFragment() {
        if (mFragmentNavigation != null) {
            Objects.requireNonNull(getActivity()).findViewById(R.id.screenShotMainLayout).setVisibility(View.GONE);
            NextActivity.notifyProblemFragment = null;
            mFragmentNavigation.pushFragment(new NotifyProblemFragment(), ANIMATE_LEFT_TO_RIGHT);
        }
    }

    private void setNavViewItems() {
        View v = navViewLayout.getHeaderView(0);
        navViewNameTv = v.findViewById(R.id.navViewNameTv);
        navViewEmailTv = v.findViewById(R.id.navViewEmailTv);
        navImgProfile = v.findViewById(R.id.navImgProfile);
        navViewShortenTextView = v.findViewById(R.id.navViewShortenTextView);
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = null;
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
