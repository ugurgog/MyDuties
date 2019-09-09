package uren.com.myduties.dutyManagement.profile;


import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewpager.widget.ViewPager;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import devlight.io.library.ntb.NavigationTabBar;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.SpecialSelectTabAdapter;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;

import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;

public class TasksIAssignedFragment extends BaseFragment {

    View view;

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.ntb_horizontal)
    NavigationTabBar navigationTabBar;
    @BindView(R.id.toolbarTitleTv)
    AppCompatTextView toolbarTitleTv;

    SpecialSelectTabAdapter adapter;
    AssignedToUsersFragment assignedToUsersFragment;
    AssignedToGroupsFragment assignedToGroupsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_RIGHT_TO_LEFT;

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_tasks_i_assigned, container, false);
            ButterKnife.bind(this, view);
            initializeItems();
            addListeners();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initializeItems() {
        initNavigationBar();
        toolbarTitleTv.setText(getContext().getResources().getString(R.string.tasksmyassigned));
        setupViewPager();
    }

    private void initNavigationBar(){
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_person_white_24dp, null),
                        Color.parseColor("#d1395c"))
                        .title(Objects.requireNonNull(getContext()).getResources().getString(R.string.friends))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.icon_user_groups, null),
                        Color.parseColor("#FF861F"))
                        .title(getContext().getResources().getString(R.string.groupsShareText))
                        .build()
        );

        navigationTabBar.setModels(models);
    }

    private void setupViewPager() {
        assignedToUsersFragment = new AssignedToUsersFragment();
        assignedToGroupsFragment = new AssignedToGroupsFragment( );

        adapter = new SpecialSelectTabAdapter(getChildFragmentManager());
        adapter.addFragment(assignedToUsersFragment, getResources().getString(R.string.FACEBOOK));
        adapter.addFragment(assignedToGroupsFragment, getResources().getString(R.string.CONTACTS));
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new RotateUpTransformer());
        navigationTabBar.setViewPager(viewPager, 0);
    }

    private void addListeners() {
        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }
}
