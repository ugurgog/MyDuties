package uren.com.myduties.dutyManagement.tasks;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import devlight.io.library.ntb.NavigationTabBar;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.tasks.adapters.FeedPagerAdapter;
import uren.com.myduties.dutyManagement.tasks.helper.TaskHelper;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;

public class MyTaskFragment extends BaseFragment implements View.OnClickListener {

    View mView;
    FeedPagerAdapter feedPagerAdapter;

   /* @BindView(R.id.imgFilter)
    ClickableImageView imgFilter;
    @BindView(R.id.llFilter)
    RelativeLayout llFilter;*/
    @BindView(R.id.llSearch)
    LinearLayout llSearch;
    @BindView(R.id.toolbarLayout)
    Toolbar toolbar;
    @BindView(R.id.htab_viewpager)
    ViewPager viewPager;
    /*@BindView(R.id.unreadMsgCntTv)
    TextView unreadMsgCntTv;
    @BindView(R.id.myMessagesImgv)
    ClickableImageView myMessagesImgv;*/

    @BindView(R.id.llSharing)
    LinearLayout llSharing;
    @BindView(R.id.smoothProgressBar)
    SmoothProgressBar smoothProgressBar;
    @BindView(R.id.ntb_horizontal)
    NavigationTabBar navigationTabBar;

    private static final int TAB_WAITING = 0;
    private static final int TAB_COMPLETED = 1;
    private static final int TAB_GROUP = 1;

    int selectedTabPosition = TAB_WAITING;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_my_task, container, false);
            TaskHelper.InitTask.setTaskFragment(this);
            ButterKnife.bind(this, mView);
            initNavigationBar();
            setUpPager();
            initListeners();
        }
        return mView;
    }


    @Override
    public void onStart() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.tabMainLayout).setVisibility(View.VISIBLE);
        super.onStart();
    }

    private void initNavigationBar(){
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_waiting_task, null),
                        Color.parseColor("#d1395c"))
                        .title(Objects.requireNonNull(getContext()).getResources().getString(R.string.waiting_task))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_completed_task, null),
                        Color.parseColor("#02c754"))
                        .title(getContext().getResources().getString(R.string.completed_task))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_group_white_24dp, null),
                        Color.parseColor("#FF861F"))
                        .title(getContext().getResources().getString(R.string.group_task))
                        .build()
        );

        navigationTabBar.setModels(models);
    }

    private void initListeners() {
        llSearch.setOnClickListener(this);
        //menuImgv.setOnClickListener(this);
    }

    private void setUpPager() {
        feedPagerAdapter = new FeedPagerAdapter(getFragmentManager(), 3);
        viewPager.setAdapter(feedPagerAdapter);
        viewPager.setPageTransformer(true, new RotateUpTransformer());
        navigationTabBar.setViewPager(viewPager, 0);
        setTabListener();
    }

    private void setTabListener() {

        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                //viewPager.setCurrentItem(position);
                selectedTabPosition = position;
            }

            @Override
            public void onPageSelected(final int position) {

            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });
    }

    public int getSelectedTabPosition() {
        return selectedTabPosition;
    }


    @Override
    public void onClick(View view) {

        if (view == llSearch) {
            mFragmentNavigation.pushFragment(new SearchFragment(), "");
        }

    }

    public void startProgressBar() {
        llSharing.setVisibility(View.VISIBLE);
        smoothProgressBar.progressiveStart();
    }

    public void stopProgressBar() {
        llSharing.setVisibility(View.GONE);
        smoothProgressBar.progressiveStop();
    }
}