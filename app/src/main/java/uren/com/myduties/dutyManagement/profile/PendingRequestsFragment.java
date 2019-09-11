package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.PendingRequestAdapter;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;

import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.fb_child_status_waiting;

public class PendingRequestsFragment extends BaseFragment {

    View mView;
    PendingRequestAdapter pendingRequestAdapter;
    LinearLayoutManager linearLayoutManager;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.warningMsgTv)
    AppCompatTextView warningMsgTv;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    TextView toolbarTitleTv;
    @BindView(R.id.following_recyclerView)
    RecyclerView following_recyclerView;

    private User accountHolderUser;

    public PendingRequestsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((NextActivity) Objects.requireNonNull(getContext())).ANIMATION_TAG = ANIMATE_RIGHT_TO_LEFT;
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_penging_requests, container, false);
            ButterKnife.bind(this, mView);
            toolbarTitleTv.setText(getContext().getResources().getString(R.string.PENDING_REQUESTS));
            warningMsgTv.setText(getContext().getResources().getString(R.string.THERE_IS_NO_PENDING_REQUEST));
            addListeners();
            setAdapter();
            getData();
        }
        return mView;
    }

    private void setAdapter() {
        pendingRequestAdapter = new PendingRequestAdapter(getContext(), new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {
                int listCount = (int) object;
                setMessageWarning(listCount);
            }
        });
        following_recyclerView.setAdapter(pendingRequestAdapter);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        following_recyclerView.setLayoutManager(linearLayoutManager);
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
    public void accountHolderUserReceived(UserBus userBus) {
        accountHolderUser = userBus.getUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    private void addListeners() {
        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void getData() {
        progressBar.setVisibility(View.VISIBLE);

        FriendsDBHelper.getAllFriendsByStatus(accountHolderUser.getUserid(), fb_child_status_waiting, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                Friend friend = (Friend) object;
                pendingRequestAdapter.addFriend(friend.getUser());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailed(String message) {
                progressBar.setVisibility(View.GONE);
            }
        });

        new Handler().postDelayed(() -> {
            if(pendingRequestAdapter.getItemCount() == 0){
                if(progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                    setMessageWarning(0);
                }
            }
        }, 3000);
    }

    private void setMessageWarning(int listCount) {
        if (listCount > 0)
            warningMsgTv.setVisibility(View.GONE);
        else
            warningMsgTv.setVisibility(View.VISIBLE);
    }
}
