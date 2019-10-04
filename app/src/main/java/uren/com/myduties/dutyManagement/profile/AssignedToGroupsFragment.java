package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.profile.adapters.AssignedToGroupsAdapter;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.layoutManager.CustomLinearLayoutManager;

import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;
import static uren.com.myduties.constants.NumericConstants.VIEW_RETRY;
import static uren.com.myduties.constants.NumericConstants.VIEW_SERVER_ERROR;

public class AssignedToGroupsFragment extends BaseFragment {

    View mView;
    AssignedToGroupsAdapter assignedToGroupsAdapter;
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

    private boolean loading = true;
    private boolean pulledToRefresh = false;
    private boolean isFirstFetch = false;
    User user;

    public AssignedToGroupsFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_assigned_to_groups, container, false);
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
        assignedToGroupsAdapter = new AssignedToGroupsAdapter(getActivity(), getContext(), mFragmentNavigation);
        recyclerView.setAdapter(assignedToGroupsAdapter);
    }

    private void setPullToRefresh() {
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pulledToRefresh = true;
                assignedToGroupsAdapter.updatePostListItems();
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

        GroupTaskDBHelper.getIAssignedTasksToGroups(user, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                pulledToRefresh = false;
                setFetchData((GroupTask) object);
            }

            @Override
            public void onFailed(String message) {
                pulledToRefresh = false;
                loadingView.hide();
                refresh_layout.setRefreshing(false);
            }
        });

        new Handler().postDelayed(() -> {
            if(assignedToGroupsAdapter.getItemCount() == 0){
                refresh_layout.setRefreshing(false);
                CommonUtils.showExceptionLayout(true, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getResources().getString(R.string.there_is_no_task_I_assigned_to_groups));
            }
        }, 3000);
    }

    private void setFetchData(GroupTask task) {

        if (isFirstFetch) {
            isFirstFetch = false;
            loadingView.smoothToHide();
        }
        CommonUtils.showExceptionLayout(false, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                getResources().getString(R.string.there_is_no_task_I_assigned_to_groups));
        setUpRecyclerView(task);
        refresh_layout.setRefreshing(false);
    }

    private void setUpRecyclerView(GroupTask task) {
        loading = true;
        assignedToGroupsAdapter.addTask(task);
    }
}