package uren.com.myduties.dutyManagement.tasks;

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
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.tasks.adapters.GroupTaskAdapter;
import uren.com.myduties.dutyManagement.tasks.helper.TaskHelper;
import uren.com.myduties.dutyManagement.tasks.interfaces.TaskRefreshCallback;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.login.AccountHolderInfo;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.layoutManager.CustomLinearLayoutManager;

import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;
import static uren.com.myduties.constants.NumericConstants.VIEW_SERVER_ERROR;

public class GroupTaskFragment extends BaseFragment {

    View mView;
    GroupTaskAdapter groupTaskAdapter;
    CustomLinearLayoutManager mLayoutManager;

    @BindView(R.id.rv_feed)
    RecyclerView recyclerView;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh_layout;

    @BindView(R.id.loadingView)
    AVLoadingIndicatorView loadingView;

    @BindView(R.id.mainExceptionLayout)
    RelativeLayout mainExceptionLayout;

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
            mView = inflater.inflate(R.layout.fragment_group_task, container, false);
            ButterKnife.bind(this, mView);
            initVariables();
            initListeners();
            initRecyclerView();
            startGetGroupTasks();
            loadingView.show();
        }

        return mView;
    }

    @Override
    public void onStart() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.tabMainLayout).setVisibility(View.VISIBLE);
        super.onStart();
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
        setFeedRefreshListener();
    }

    private void setFeedRefreshListener() {
        TaskHelper.TaskRefresh.getInstance().setTaskRefreshCallback(new TaskRefreshCallback() {
            @Override
            public void onTaskRefresh() {
                refreshFeed();
            }
        });
    }

    private void setLayoutManager() {
        mLayoutManager = new CustomLinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void setAdapter() {
        groupTaskAdapter = new GroupTaskAdapter(getActivity(), getContext(), mFragmentNavigation, user);
        recyclerView.setAdapter(groupTaskAdapter);
    }

    private void setPullToRefresh() {
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                groupTaskAdapter.updatePostListItems();
                CommonUtils.showExceptionLayout(VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getContext().getResources().getString(R.string.there_is_no_group_task));
                refreshFeed();
            }
        });
    }

    private void refreshFeed() {
        startGetGroupTasks();
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

    private void startGetGroupTasks() {

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
        GroupTaskDBHelper.getGroupAllTasks(user, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                setFetchData((GroupTask)object);
            }

            @Override
            public void onFailed(String message) {
                loadingView.hide();
                refresh_layout.setRefreshing(false);
                CommonUtils.showExceptionLayout(VIEW_SERVER_ERROR, refresh_layout, loadingView, mainExceptionLayout,
                        getContext().getResources().getString(R.string.there_is_no_group_task));
            }
        });

        new Handler().postDelayed(() -> {
            if(groupTaskAdapter.getItemCount() == 0){
                refresh_layout.setRefreshing(false);
                CommonUtils.showExceptionLayout(VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getContext().getResources().getString(R.string.there_is_no_group_task));
            }
        }, 3000);
    }

    private void setFetchData(GroupTask groupTask) {

        if (isFirstFetch) {
            isFirstFetch = false;
            loadingView.smoothToHide();
        }
        setUpRecyclerView(groupTask);
        refresh_layout.setRefreshing(false);
    }

    private void setUpRecyclerView(GroupTask groupTask) {
        CommonUtils.hideExceptionLayout(mainExceptionLayout);
        groupTaskAdapter.addGroupTask(groupTask);
    }

    public void scrollRecViewInitPosition() {
        mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
    }
}
