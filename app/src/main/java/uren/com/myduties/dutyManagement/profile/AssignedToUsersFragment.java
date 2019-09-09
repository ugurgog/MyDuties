package uren.com.myduties.dutyManagement.profile;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.profile.adapters.AssignedToUsersAdapter;
import uren.com.myduties.dutyManagement.tasks.adapters.WaitingTaskAdapter;
import uren.com.myduties.dutyManagement.tasks.helper.TaskHelper;
import uren.com.myduties.dutyManagement.tasks.interfaces.TaskRefreshCallback;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.InfoDialogBoxCallback;
import uren.com.myduties.utils.layoutManager.CustomLinearLayoutManager;

import static uren.com.myduties.constants.NumericConstants.REC_MAXITEM_LIMIT_COUNT;
import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;
import static uren.com.myduties.constants.NumericConstants.VIEW_RETRY;
import static uren.com.myduties.constants.NumericConstants.VIEW_SERVER_ERROR;

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
    @BindView(R.id.retryLayout)
    LinearLayout retryLayout;
    @BindView(R.id.serverError)
    LinearLayout serverError;
    @BindView(R.id.imgRetry)
    ClickableImageView imgRetry;

    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private static final int RECYCLER_VIEW_CACHE_COUNT = 10;
    private boolean pulledToRefresh = false;
    private boolean isFirstFetch = false;
    User user;

    public AssignedToUsersFragment(){

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
    }

    private void setFetchData(Task task) {

        if (isFirstFetch) {
            isFirstFetch = false;
            loadingView.smoothToHide();
        }
        setUpRecyclerView(task);
        refresh_layout.setRefreshing(false);
    }

    private void setUpRecyclerView(Task task) {
        loading = true;
        assignedToUsersAdapter.addTask(task);
    }

    /**********************************************/
    private void showExceptionLayout(boolean showException, int viewType) {

        if (showException) {

            refresh_layout.setRefreshing(false);
            loadingView.hide();
            mainExceptionLayout.setVisibility(View.VISIBLE);
            retryLayout.setVisibility(View.GONE);
            noPostFoundLayout.setVisibility(View.GONE);
            serverError.setVisibility(View.GONE);

            if (viewType == VIEW_RETRY) {
                if (getContext() != null)
                    imgRetry.setColorFilter(ContextCompat.getColor(getContext(), R.color.tintColor), android.graphics.PorterDuff.Mode.SRC_IN);
                retryLayout.setVisibility(View.VISIBLE);
            } else if (viewType == VIEW_NO_POST_FOUND) {
                noPostFoundLayout.setVisibility(View.VISIBLE);
            }  else if (viewType == VIEW_SERVER_ERROR) {
                serverError.setVisibility(View.VISIBLE);
            }

        } else {
            mainExceptionLayout.setVisibility(View.GONE);
        }
    }
}