package uren.com.myduties.dutyManagement;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.assignTask.AssignTaskFragment;
import uren.com.myduties.dutyManagement.fragmentControllers.FragNavController;
import uren.com.myduties.dutyManagement.fragmentControllers.FragNavTransactionOptions;
import uren.com.myduties.dutyManagement.fragmentControllers.FragmentHistory;
import uren.com.myduties.dutyManagement.profile.NotifyProblemFragment;
import uren.com.myduties.dutyManagement.profile.ProfileFragment;
import uren.com.myduties.dutyManagement.tasks.MyTaskFragment;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.StringConstants.ANIMATE_DOWN_TO_UP;
import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_UP_TO_DOWN;
import static uren.com.myduties.dutyManagement.fragmentControllers.FragNavController.TAB1;

public class NextActivity extends FragmentActivity implements
        BaseFragment.FragmentNavigation,
        FragNavController.TransactionListener,
        FragNavController.RootFragmentListener {

    public static Activity thisActivity;

    public static FrameLayout contentFrame;
    public LinearLayout profilePageMainLayout;
    public TabLayout bottomTabLayout;
    public RelativeLayout screenShotMainLayout;
    public Button screenShotCancelBtn;
    public Button screenShotApproveBtn;

    public LinearLayout tabMainLayout;

    private int selectedTabColor, unSelectedTabColor;

    public String ANIMATION_TAG;

    public FragNavTransactionOptions transactionOptions;
    public static NotifyProblemFragment notifyProblemFragment;

    private int[] mTabIconsSelected = {
            R.drawable.ic_my_tasks_white_24dp,
            R.drawable.ic_send_white_24dp,
            R.drawable.ic_person_white_24dp};

    public String[] TABS;

    private FragNavController mNavController;

    private FragmentHistory fragmentHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        Fabric.with(this, new Crashlytics());
        thisActivity = this;

        unSelectedTabColor = this.getResources().getColor(R.color.DarkGray, null);
        selectedTabColor = this.getResources().getColor(R.color.colorAccent, null);

        initValues();

        fragmentHistory = new FragmentHistory();

        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.content_frame)
                .transactionListener(this)
                .rootFragmentListener(this, TABS.length)
                .build();

        switchTab(0);
        updateTabSelection(0);

        bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSelectionControl(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mNavController.clearStack();
                tabSelectionControl(tab);
            }
        });
    }

    public void tabSelectionControl(TabLayout.Tab tab) {
        fragmentHistory.push(tab.getPosition());
        switchAndUpdateTabSelection(tab.getPosition());
    }

    private void initValues() {
        ButterKnife.bind(this);
        bottomTabLayout = findViewById(R.id.tablayout);
        profilePageMainLayout = findViewById(R.id.profilePageMainLayout);
        contentFrame = findViewById(R.id.content_frame);
        tabMainLayout = findViewById(R.id.tabMainLayout);
        screenShotMainLayout = findViewById(R.id.screenShotMainLayout);
        screenShotCancelBtn = findViewById(R.id.screenShotCancelBtn);
        screenShotApproveBtn = findViewById(R.id.screenShotApproveBtn);
        TABS = getResources().getStringArray(R.array.tab_name);
        setShapes();

        //setStatusBarTransparent();
        initTab();
    }

    public void setShapes() {
        screenShotCancelBtn.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.Red, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 15, 4));
        screenShotApproveBtn.setBackground(ShapeUtil.getShape(getResources().getColor(R.color.MediumSeaGreen, null),
                getResources().getColor(R.color.White, null), GradientDrawable.RECTANGLE, 15, 4));
    }

    private void setStatusBarTransparent() {

        // Android 5.0
        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        getWindow().getDecorView().setSystemUiVisibility(visibility);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

    }

    private void initTab() {
        if (bottomTabLayout != null) {
            for (int i = 0; i < TABS.length; i++) {
                bottomTabLayout.addTab(bottomTabLayout.newTab());
                TabLayout.Tab tab = bottomTabLayout.getTabAt(i);


                if(tab != null){
                    tab.setIcon(mTabIconsSelected[i]);
                    //tab.setText(TABS[i]);

                  /*  if(i == TAB1)
                        tab.getIcon().setColorFilter(selectedTabColor, PorterDuff.Mode.SRC_IN);*/
                }

                /*if (tab != null) {
                    tab.setCustomView(getTabView(i));
                    tab.setText(TABS[i]);
                }*/
            }
            Objects.requireNonNull(bottomTabLayout.getTabAt(0).getIcon()).setColorFilter(selectedTabColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private View getTabView(int position) {
        View view = LayoutInflater.from(NextActivity.this).inflate(R.layout.tab_item_bottom, null);
        ImageView icon = view.findViewById(R.id.tab_icon);
        icon.setImageDrawable(CommonUtils.setDrawableSelector(NextActivity.this, mTabIconsSelected[position], mTabIconsSelected[position]));
        return view;
    }

    public void clearStackGivenIndex(int index){
        mNavController.clearStackWithGivenIndex(index);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true)
    public void customEventReceived(UserBus userBus){
        User user = userBus.getUser();
    }

    public void switchTab(int position) {
        mNavController.switchTab(position);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (!mNavController.isRootFragment()) {
            setTransactionOption();
            mNavController.popFragment(transactionOptions);
        } else {

            if (fragmentHistory.isEmpty()) {
                super.onBackPressed();
            } else {

                if (fragmentHistory.getStackSize() > 1) {

                    int position = fragmentHistory.popPrevious();
                    switchAndUpdateTabSelection(position);
                } else {
                    switchAndUpdateTabSelection(0);
                    fragmentHistory.emptyStack();
                }
            }
        }
    }

    public void switchAndUpdateTabSelection(int position) {
        switchTab(position);
        updateTabSelection(position);
    }

    private void setTransactionOption() {
        if (transactionOptions == null) {
            transactionOptions = FragNavTransactionOptions.newBuilder().build();
        }

        if (ANIMATION_TAG != null) {
            switch (ANIMATION_TAG) {
                case ANIMATE_RIGHT_TO_LEFT:
                    transactionOptions.enterAnimation = R.anim.slide_from_right;
                    transactionOptions.exitAnimation = R.anim.slide_to_left;
                    transactionOptions.popEnterAnimation = R.anim.slide_from_left;
                    transactionOptions.popExitAnimation = R.anim.slide_to_right;
                    break;
                case ANIMATE_LEFT_TO_RIGHT:
                    transactionOptions.enterAnimation = R.anim.slide_from_left;
                    transactionOptions.exitAnimation = R.anim.slide_to_right;
                    transactionOptions.popEnterAnimation = R.anim.slide_from_right;
                    transactionOptions.popExitAnimation = R.anim.slide_to_left;
                    break;
                case ANIMATE_DOWN_TO_UP:
                    transactionOptions.enterAnimation = R.anim.slide_from_down;
                    transactionOptions.exitAnimation = R.anim.slide_to_up;
                    transactionOptions.popEnterAnimation = R.anim.slide_from_up;
                    transactionOptions.popExitAnimation = R.anim.slide_to_down;
                    break;
                case ANIMATE_UP_TO_DOWN:
                    transactionOptions.enterAnimation = R.anim.slide_from_up;
                    transactionOptions.exitAnimation = R.anim.slide_to_down;
                    transactionOptions.popEnterAnimation = R.anim.slide_from_down;
                    transactionOptions.popExitAnimation = R.anim.slide_to_up;
                    break;
                default:
                    transactionOptions = null;
            }
        } else
            transactionOptions = null;
    }

    public void updateTabSelection(int currentTab) {

        for (int i = 0; i < TABS.length; i++) {
            TabLayout.Tab selectedTab = bottomTabLayout.getTabAt(i);

            if (currentTab != i) {
                //selectedTab.getCustomView().setSelected(false);
                Objects.requireNonNull(selectedTab.getIcon()).setColorFilter(unSelectedTabColor, PorterDuff.Mode.SRC_IN);
            } else {
                //selectedTab.getCustomView().setSelected(true);
                Objects.requireNonNull(selectedTab.getIcon()).setColorFilter(selectedTabColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }

    @Override
    public void pushFragment(Fragment fragment, String animationTag) {

        ANIMATION_TAG = animationTag;
        setTransactionOption();

        if (mNavController != null) {
            mNavController.pushFragment(fragment, transactionOptions);
        }
    }

    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        /*if (getSupportActionBar() != null && mNavController != null) {

            //updateToolbar();
        }*/
    }

    private void updateToolbar() {
        /*getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setDisplayShowHomeEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_18dp);*/
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {

            case TAB1:
                return new MyTaskFragment();
            case FragNavController.TAB2:
                return new AssignTaskFragment();
            case FragNavController.TAB3:
                return new ProfileFragment();

        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button
        /*if (getSupportActionBar() != null && mNavController != null) {

            //updateToolbar();
        }*/
    }

    public void updateStatusBarColor(int colorCode){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(colorCode);
    }
}