package uren.com.myduties.admin.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.admin.adapters.ProblemAdapter;
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dbManagement.ProblemDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.AssignedToGroupsAdapter;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.Problem;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.layoutManager.CustomLinearLayoutManager;

import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;
import static uren.com.myduties.constants.NumericConstants.VIEW_RETRY;
import static uren.com.myduties.constants.NumericConstants.VIEW_SERVER_ERROR;

public class InCompleteProblemsFragment extends BaseFragment {


    View mView;
    ProblemAdapter problemAdapter;
    CustomLinearLayoutManager mLayoutManager;

    @BindView(R.id.rv_feed)
    RecyclerView recyclerView;
    User user;

    public InCompleteProblemsFragment(){

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
            mView = inflater.inflate(R.layout.fragment_problems, container, false);
            ButterKnife.bind(this, mView);
            initRecyclerView();
            startGetPosts();
        }

        return mView;
    }

    private void initRecyclerView() {
        setLayoutManager();
        setAdapter();
    }

    private void setLayoutManager() {
        mLayoutManager = new CustomLinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void setAdapter() {
        problemAdapter = new ProblemAdapter(getContext(), mFragmentNavigation);
        recyclerView.setAdapter(problemAdapter);
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

        ProblemDBHelper.getProblemsByFixedValue(false, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                problemAdapter.addProblem((Problem) object);

            }

            @Override
            public void onFailed(String message) {

            }
        });
    }
}
