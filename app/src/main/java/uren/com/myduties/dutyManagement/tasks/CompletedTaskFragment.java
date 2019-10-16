package uren.com.myduties.dutyManagement.tasks;


import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dbManagement.UserTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.tasks.adapters.CompletedTaskAdapter;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.login.AccountHolderInfo;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.layoutManager.CustomLinearLayoutManager;

import static uren.com.myduties.constants.NumericConstants.REC_MAXITEM_LIMIT_COUNT;
import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;
import static uren.com.myduties.constants.NumericConstants.VIEW_SERVER_ERROR;

public class CompletedTaskFragment extends BaseFragment {

    View mView;
    CompletedTaskAdapter completedTaskAdapter;
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
    private List<Task> taskList = new ArrayList<>();
    private boolean pulledToRefresh = false;
    private boolean isFirstFetch = false;
    User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_completed_task, container, false);
            ButterKnife.bind(this, mView);
            initRecyclerView();
            startGetTasks();
            loadingView.show();
        }

        return mView;
    }

    @Override
    public void onStart() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.tabMainLayout).setVisibility(View.VISIBLE);
        super.onStart();
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
        completedTaskAdapter = new CompletedTaskAdapter(getContext(), mFragmentNavigation, user);
        recyclerView.setAdapter(completedTaskAdapter);
        completedTaskAdapter.setReturnCallback(new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {
                List<Task> returnList = (ArrayList<Task>) object;
                if (returnList != null && returnList.size() == 0)
                    CommonUtils.showExceptionLayout(true, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                            getContext().getResources().getString(R.string.there_is_no_completed_task));
            }
        });
    }

    private void setPullToRefresh() {
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });
    }

    private void refreshFeed() {
        pulledToRefresh = true;
        startGetTasks();
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
    public void customEventReceived(UserBus userBus) {
        user = userBus.getUser();
    }

    private void startGetTasks() {

        if (user == null || user.getUserid() == null) {
            UserDBHelper.getUser(AccountHolderInfo.getUserIdFromFirebase(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    user = (User) object;
                    getTasks();
                }

                @Override
                public void onFailed(String message) {
                    loadingView.hide();
                    refresh_layout.setRefreshing(false);
                    CommonUtils.showToastShort(getContext(), message);
                }
            });
        }else
            getTasks();
    }

    private void getTasks(){
        UserTaskDBHelper.getUserCompletedTasks(user, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                setFetchData((List<Task>) object);
            }

            @Override
            public void onFailed(String message) {
                loadingView.hide();
                refresh_layout.setRefreshing(false);

                if (taskList.size() > 0) {
                    CommonUtils.showToastShort(getContext(),
                            Objects.requireNonNull(getContext()).getResources().getString(R.string.serverError));
                    CommonUtils.showExceptionLayout(false, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                            getContext().getResources().getString(R.string.there_is_no_completed_task));
                } else {
                    CommonUtils.showExceptionLayout(true, VIEW_SERVER_ERROR, refresh_layout, loadingView, mainExceptionLayout,
                            getContext().getResources().getString(R.string.there_is_no_completed_task));
                }
            }
        });
    }

    private void setFetchData(List<Task> taskList) {

        if (isFirstFetch) {
            isFirstFetch = false;
            loadingView.smoothToHide();
        }

        if (taskList != null) {
            if (taskList.size() == 0) {
                CommonUtils.showExceptionLayout(true, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getContext().getResources().getString(R.string.there_is_no_completed_task));
            } else {
                CommonUtils.showExceptionLayout(false, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getContext().getResources().getString(R.string.there_is_no_completed_task));
            }
            setUpRecyclerView(taskList);
        }

        refresh_layout.setRefreshing(false);
    }

    private void setUpRecyclerView(List<Task> taskList1) {

        loading = true;
        taskList.addAll(taskList1);

        if (pulledToRefresh) {
            completedTaskAdapter.updatePostListItems(taskList1);
            pulledToRefresh = false;
        } else {
            completedTaskAdapter.addAll(taskList1);
        }
    }

    public void scrollRecViewInitPosition() {
        mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
    }
}