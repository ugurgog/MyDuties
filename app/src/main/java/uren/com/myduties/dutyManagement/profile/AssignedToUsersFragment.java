package uren.com.myduties.dutyManagement.profile;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.profile.adapters.AssignedToUsersAdapter;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.layoutManager.CustomLinearLayoutManager;

import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;

public class AssignedToUsersFragment extends BaseFragment {

    View mView;
    AssignedToUsersAdapter assignedToUsersAdapter;
    CustomLinearLayoutManager mLayoutManager;

    @BindView(R.id.rv_feed)
    RecyclerView recyclerView;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh_layout;

    @BindView(R.id.loadingView)
    AVLoadingIndicatorView loadingView;

    @BindView(R.id.mainExceptionLayout)
    RelativeLayout mainExceptionLayout;
    @BindView(R.id.noPostFoundLayout)
    LinearLayout noPostFoundLayout;
    @BindView(R.id.serverError)
    LinearLayout serverError;
    @BindView(R.id.txtNoItemFound)
    AppCompatTextView txtNoItemFound;

    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private static final int RECYCLER_VIEW_CACHE_COUNT = 10;
    private boolean pulledToRefresh = false;
    private boolean isFirstFetch = false;
    User user;

    public AssignedToUsersFragment(){

    }

    @Override
    public void onStart() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.tabMainLayout).setVisibility(View.VISIBLE);
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_assigned_to_users, container, false);
            ButterKnife.bind(this, mView);
            initVariables();
            initListeners();
            initRecyclerView();
            startGetPosts();
            loadingView.show();
        }

        return mView;
    }

    private void initVariables() {

    }

    private void initListeners() {

    }

    private void initRecyclerView() {
        isFirstFetch = true;
        mainExceptionLayout.setVisibility(View.GONE);
        setLayoutManager();
        setAdapter();
        setPullToRefresh();
    }

    private void setLayoutManager() {
        mLayoutManager = new CustomLinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void setAdapter() {
        assignedToUsersAdapter = new AssignedToUsersAdapter(getActivity(), getContext(), mFragmentNavigation);
        recyclerView.setAdapter(assignedToUsersAdapter);
    }

    private void setPullToRefresh() {
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pulledToRefresh = true;
                assignedToUsersAdapter.updatePostListItems();
                startGetPosts();
            }
        });
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
    public void customEventReceived(UserBus userBus){
        user = userBus.getUser();
    }

    private void startGetPosts() {

        UserTaskDBHelper.getIAssignedTasksToUsers(user, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                pulledToRefresh = false;
                setFetchData((Task) object);
            }

            @Override
            public void onFailed(String message) {
                pulledToRefresh = false;
                loadingView.hide();
                refresh_layout.setRefreshing(false);
            }
        });

        new Handler().postDelayed(() -> {
            if(assignedToUsersAdapter.getItemCount() == 0){
                refresh_layout.setRefreshing(false);
                CommonUtils.showExceptionLayout(true, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getContext().getResources().getString(R.string.there_is_no_task_I_assigned));
            }
        }, 3000);
    }

    private void setFetchData(Task task) {

        if (isFirstFetch) {
            isFirstFetch = false;
            loadingView.smoothToHide();
        }
        CommonUtils.showExceptionLayout(false, -1, refresh_layout, loadingView, mainExceptionLayout,
                null);
        setUpRecyclerView(task);
        refresh_layout.setRefreshing(false);
    }

    private void setUpRecyclerView(Task task) {
        boolean loading = true;
        assignedToUsersAdapter.addTask(task);
    }
}