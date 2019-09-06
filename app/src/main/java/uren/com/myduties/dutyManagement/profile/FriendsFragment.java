package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.FriendsAdapter;
import uren.com.myduties.dutyManagement.profile.interfaces.ListItemClickListener;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;

import static uren.com.myduties.constants.NumericConstants.DEFAULT_GET_FOLLOWER_PAGE_COUNT;
import static uren.com.myduties.constants.NumericConstants.DEFAULT_GET_FOLLOWER_PERPAGE_COUNT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;
import static uren.com.myduties.constants.StringConstants.fb_child_status_sendedrequest;

public class FriendsFragment extends BaseFragment {

    View mView;

    @BindView(R.id.following_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    TextView toolbarTitleTv;

    //Search layout variables
    @BindView(R.id.searchEdittext)
    EditText searchEdittext;
    @BindView(R.id.searchCancelImgv)
    ImageView searchCancelImgv;
    @BindView(R.id.searchResultTv)
    TextView searchResultTv;

    private LinearLayoutManager mLayoutManager;
    private FriendsAdapter friendsAdapter;
    int perPage;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private List<String> statusList = new ArrayList<>();

    private static final int CODE_FIRST_LOAD = 0;
    private static final int CODE_MORE_LOAD = 1;
    private int loadCode = CODE_FIRST_LOAD;

    User accountHolder;

    public FriendsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_friends, container, false);
            ButterKnife.bind(this, mView);
            init();
            setPaginationValues();
            setListeners();
            initRecyclerView();
            getFriendList();
        }
        return mView;
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
        accountHolder = userBus.getUser();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) getActivity()).ANIMATION_TAG = ANIMATE_LEFT_TO_RIGHT;
    }

    private void init() {
        toolbarTitleTv.setText(getContext().getResources().getString(R.string.friends));
        searchEdittext.setHint(getContext().getResources().getString(R.string.SEARCH_FRIENDS));
        searchResultTv.setText(getContext().getResources().getString(R.string.USER_NOT_FOUND));
        statusList.add(fb_child_status_friend);
        statusList.add(fb_child_status_sendedrequest);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setListeners() {
        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        searchCancelImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdittext.setText("");
                searchCancelImgv.setVisibility(View.GONE);
            }
        });

        searchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.toString() != null) {
                    if (!s.toString().trim().isEmpty()) {
                        searchCancelImgv.setVisibility(View.VISIBLE);
                    } else {
                        searchCancelImgv.setVisibility(View.GONE);
                    }

                    if (friendsAdapter != null)
                        friendsAdapter.updateAdapter(s.toString(), new ReturnCallback() {
                            @Override
                            public void OnReturn(Object object) {
                                int itemSize = (int) object;

                                if (itemSize == 0)
                                    searchResultTv.setVisibility(View.VISIBLE);
                                else
                                    searchResultTv.setVisibility(View.GONE);
                            }
                        });
                } else
                    searchCancelImgv.setVisibility(View.GONE);
            }
        });
    }

    private void setPaginationValues() {
        perPage = DEFAULT_GET_FOLLOWER_PERPAGE_COUNT;
    }

    private void initRecyclerView() {
        setLayoutManager();
        setAdapter();
        setRecyclerViewScroll();
    }

    private void setLayoutManager() {
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void setRecyclerViewScroll() {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            perPage = perPage + DEFAULT_GET_FOLLOWER_PERPAGE_COUNT;
                            friendsAdapter.addProgressLoading();
                            loadCode = CODE_MORE_LOAD;
                            getFriendList();
                        }
                    }
                }
            }
        });
    }

    private void setUpRecyclerView(Friend friend) {
        loading = true;

        if (perPage != 50)
            friendsAdapter.removeProgressLoading();

        friendsAdapter.addFriend(friend);
    }

    private void setAdapter() {
        friendsAdapter = new FriendsAdapter(getContext());
        recyclerView.setAdapter(friendsAdapter);
    }

    private void getFriendList() {

        FriendsDBHelper.getFriendsByStatusList(accountHolder.getUserid(), perPage, statusList, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                if(object != null){
                    progressBar.setVisibility(View.GONE);
                    setUpRecyclerView((Friend) object);
                }
            }

            @Override
            public void onFailed(String message) {
                progressBar.setVisibility(View.GONE);
                CommonUtils.showToastShort(getContext(), message);
            }
        });

    }


}
