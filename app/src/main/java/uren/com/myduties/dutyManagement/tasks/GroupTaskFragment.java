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

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.tasks.adapters.GroupTaskAdapter;
import uren.com.myduties.dutyManagement.tasks.helper.TaskHelper;
import uren.com.myduties.dutyManagement.tasks.interfaces.TaskRefreshCallback;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.layoutManager.CustomLinearLayoutManager;

import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;

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
    @BindView(R.id.noPostFoundLayout)
    LinearLayout noPostFoundLayout;
    @BindView(R.id.serverError)
    LinearLayout serverError;
    @BindView(R.id.txtNoItemFound)
    AppCompatTextView txtNoItemFound;

    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    //private List<GroupTask> taskList = new ArrayList<>();
    private static final int RECYCLER_VIEW_CACHE_COUNT = 10;
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
        CommonUtils.showExceptionLayout(true, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                getResources().getString(R.string.there_is_no_group_task));
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
                CommonUtils.showExceptionLayout(true, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getResources().getString(R.string.there_is_no_group_task));
                refreshFeed();
            }
        });
    }

    private void refreshFeed() {
        pulledToRefresh = true;
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

        GroupTaskDBHelper.getGroupAllTasks(user, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                setFetchData((GroupTask)object);
            }

            @Override
            public void onFailed(String message) {
                loadingView.hide();
                refresh_layout.setRefreshing(false);
                CommonUtils.showExceptionLayout(true, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                        getResources().getString(R.string.there_is_no_group_task));
            }
        });
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
        CommonUtils.showExceptionLayout(false, VIEW_NO_POST_FOUND, refresh_layout, loadingView, mainExceptionLayout,
                getResources().getString(R.string.there_is_no_group_task));
        loading = true;
        groupTaskAdapter.addGroupTask(groupTask);
    }

    public void scrollRecViewInitPosition() {
        mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
    }
}
