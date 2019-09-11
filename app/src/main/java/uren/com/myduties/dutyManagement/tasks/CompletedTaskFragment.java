package uren.com.myduties.dutyManagement.tasks;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import uren.com.myduties.dutyManagement.tasks.adapters.CompletedTaskAdapter;
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
    @BindView(R.id.retryLayout)
    LinearLayout retryLayout;
    @BindView(R.id.serverError)
    LinearLayout serverError;
    @BindView(R.id.imgRetry)
    ClickableImageView imgRetry;
    @BindView(R.id.txtNoItemFound)
    TextView txtNoItemFound;

    private int limitValue;
    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private List<Task> taskList = new ArrayList<>();
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
            mView = inflater.inflate(R.layout.fragment_completed_task, container, false);
            ButterKnife.bind(this, mView);
            initVariables();
            initListeners();
            initRecyclerView();
            startGetPosts();
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
        limitValue = REC_MAXITEM_LIMIT_COUNT;
    }

    private void initListeners() {

    }

    private void initRecyclerView() {
        isFirstFetch = true;
        mainExceptionLayout.setVisibility(View.GONE);
        setLayoutManager();
        setAdapter();
        setPullToRefresh();
        setRecyclerViewScroll();
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
        //recyclerView.setItemAnimator(new FeedItemAnimator());
    }

    private void setAdapter() {
        completedTaskAdapter = new CompletedTaskAdapter(getActivity(), getContext(), mFragmentNavigation, user);
        recyclerView.setAdapter(completedTaskAdapter);
        completedTaskAdapter.setReturnCallback(new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {
                List<Task> returnList = (ArrayList<Task>) object;
                if (returnList != null && returnList.size() == 0 )
                    showExceptionLayout(true, VIEW_NO_POST_FOUND);
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
        startGetPosts();
    }

    private void setRecyclerViewScroll() {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        //Do pagination.. i.e. fetch new data
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            limitValue = limitValue + REC_MAXITEM_LIMIT_COUNT;
                            completedTaskAdapter.addProgressLoading();
                            startGetPosts();
                        }
                    }
                }
            }
        });

    }

    /*public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
*/
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

        UserTaskDBHelper.getUserCompletedTasks(user, limitValue, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                setFetchData((List<Task>)object);
            }

            @Override
            public void onFailed(String message) {
                loadingView.hide();
                refresh_layout.setRefreshing(false);

                if (taskList.size() > 0) {
                    DialogBoxUtil.showErrorDialog(getContext(), Objects.requireNonNull(getContext()).getResources().getString(R.string.serverError), new InfoDialogBoxCallback() {
                        @Override
                        public void okClick() {

                        }
                    });
                    showExceptionLayout(false, -1);
                    if (completedTaskAdapter.isShowingProgressLoading()) {
                        completedTaskAdapter.removeProgressLoading();
                    }

                } else {
                    showExceptionLayout(true, VIEW_SERVER_ERROR);
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
            if (taskList.size() == 0 ) {
                showExceptionLayout(true, VIEW_NO_POST_FOUND);
            } else {
                showExceptionLayout(false, -1);
            }
            setUpRecyclerView(taskList);
        }

        refresh_layout.setRefreshing(false);
    }

    private void setUpRecyclerView(List<Task> taskList1) {

        loading = true;
        taskList.addAll(taskList1);

        completedTaskAdapter.removeProgressLoading();

        if (pulledToRefresh) {
            completedTaskAdapter.updatePostListItems(taskList1);
            pulledToRefresh = false;
        } else {
            completedTaskAdapter.addAll(taskList1);
        }
    }

    public void scrollRecViewInitPosition() {
        mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
        //recyclerView.smoothScrollToPosition(0);
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
                txtNoItemFound.setText(getResources().getString(R.string.there_is_no_completed_task));
            }  else if (viewType == VIEW_SERVER_ERROR) {
                serverError.setVisibility(View.VISIBLE);
            }

        } else {
            mainExceptionLayout.setVisibility(View.GONE);
        }
    }
}