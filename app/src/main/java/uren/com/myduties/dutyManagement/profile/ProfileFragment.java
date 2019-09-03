package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.tasks.SearchFragment;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;

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
    @BindView(R.id.friendsImgv)
    ImageView friendsImgv;

    @BindView(R.id.imgProfile)
    ImageView imgProfile;
    @BindView(R.id.txtProfile)
    TextView txtProfile;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtUsername)
    TextView txtUsername;
    @BindView(R.id.llUserInfo)
    LinearLayout llUserInfo;

    @BindView(R.id.imgForward1)
    ImageView imgForward1;
    @BindView(R.id.imgForward2)
    ImageView imgForward2;
    @BindView(R.id.imgForward3)
    ImageView imgForward3;

    TextView navViewNameTv;
    TextView navViewEmailTv;

    boolean mDrawerState;

    User user;

    public ProfileFragment() {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_profile, container, false);
            ButterKnife.bind(this, mView);
            initVariables();
            initListeners();
            setProfileDetails();
        }

        return mView;
    }

    private void setProfileDetails() {
        if(user != null){
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

            if (user.getEmail() != null && !user.getEmail().trim().isEmpty())
                navViewEmailTv.setText(user.getEmail());
            else
                navViewEmailTv.setVisibility(View.GONE);
        }
    }

    private void initVariables() {
        friendsImgv.setColorFilter(ContextCompat.getColor(getContext(), R.color.DodgerBlue), android.graphics.PorterDuff.Mode.SRC_IN);
        imgForward1.setColorFilter(ContextCompat.getColor(getContext(), R.color.DodgerBlue), android.graphics.PorterDuff.Mode.SRC_IN);
        imgForward2.setColorFilter(ContextCompat.getColor(getContext(), R.color.DodgerBlue), android.graphics.PorterDuff.Mode.SRC_IN);
        imgForward3.setColorFilter(ContextCompat.getColor(getContext(), R.color.DodgerBlue), android.graphics.PorterDuff.Mode.SRC_IN);

        llUserInfo.setBackground(ShapeUtil.getShape(getContext().getResources().getColor(R.color.DodgerBlue, null),
                0, GradientDrawable.RECTANGLE, 30, 0));

        setNavViewItems();
    }

    private void initListeners() {
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

        navViewLayout.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.searchItem:
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        break;

                    default:
                        break;
                }

                return false;
            }
        });
    }

    private void userEditClicked() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new UserEditFragment(), ANIMATE_LEFT_TO_RIGHT);
        }
    }

    private void setNavViewItems() {
        View v = navViewLayout.getHeaderView(0);
        navViewNameTv = v.findViewById(R.id.navViewNameTv);
        navViewEmailTv = v.findViewById(R.id.navViewEmailTv);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = null;
    }
}
