package uren.com.myduties.dutyManagement.profile;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.FriendVerticalListAdapter;
import uren.com.myduties.evetBusModels.SelectedUsersBus;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ProgressDialogUtil;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.NumericConstants.DEFAULT_GET_FOLLOWER_PAGE_COUNT;
import static uren.com.myduties.constants.NumericConstants.DEFAULT_GET_FOLLOWER_PERPAGE_COUNT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;

public class SelectFriendFragment extends BaseFragment {

    View mView;

    @BindView(R.id.nextFab)
    FloatingActionButton nextFab;
    @BindView(R.id.imgCancelSearch)
    ImageView imgCancelSearch;
    @BindView(R.id.editTextSearch)
    EditText editTextSearch;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.searchToolbarBackImgv)
    ImageView searchToolbarBackImgv;
    @BindView(R.id.searchToolbarAddItemImgv)
    ImageView searchToolbarAddItemImgv;

    private ProgressDialogUtil progressDialogUtil;
    private FriendVerticalListAdapter adapter;
    private String groupId;
    private List<User> groupParticipantList;
    private String pendingName;
    private LinearLayoutManager linearLayoutManager;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private ReturnCallback returnCallback;
    private int perPageCnt;
    private String groupAdminUserid;
    private List<User> selectedUsers;
    private User accountholderUser;

    public SelectFriendFragment(String groupId, String groupAdminUserid, List<User> groupParticipantList, String pendingName,
                                ReturnCallback returnCallback) {
        this.groupId = groupId;
        this.groupParticipantList = groupParticipantList;
        this.pendingName = pendingName;
        this.returnCallback = returnCallback;
        this.groupAdminUserid = groupAdminUserid;
        EventBus.getDefault().postSticky(new SelectedUsersBus(new ArrayList<>()));
    }

    @Subscribe(sticky = true)
    public void selectedUsersReceived(SelectedUsersBus selectedUsersBus){
        selectedUsers = selectedUsersBus.getUsers();
    }

    @Subscribe(sticky = true)
    public void accountHolderUserReceived(UserBus userBus){
        accountholderUser = userBus.getUser();
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_select_friend, container, false);
            ButterKnife.bind(this, mView);
            addListeners();
            setShapes();
            setPaginationValues();
            setAdapter();
            setRecyclerViewScroll();
            getFriendSelectionPage();
            progressDialogUtil = new ProgressDialogUtil(getContext(), null, false);
            progressDialogUtil.dialogShow();
            searchToolbarAddItemImgv.setVisibility(View.GONE);
        }
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    private void setPaginationValues() {
        perPageCnt = DEFAULT_GET_FOLLOWER_PERPAGE_COUNT;
    }

    public void addListeners() {
        searchToolbarBackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_LEFT_TO_RIGHT;
                getActivity().onBackPressed();
            }
        });

        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextFab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                checkSelectedPerson();
            }
        });

        imgCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
                imgCancelSearch.setVisibility(View.GONE);
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    if (!s.toString().trim().isEmpty()) {
                        imgCancelSearch.setVisibility(View.VISIBLE);
                        searchToolbarBackImgv.setVisibility(View.GONE);
                    } else {
                        imgCancelSearch.setVisibility(View.GONE);
                        searchToolbarBackImgv.setVisibility(View.VISIBLE);
                    }

                    if (adapter != null)
                        adapter.updateAdapter(s.toString());
                } else
                    imgCancelSearch.setVisibility(View.GONE);
            }
        });
    }

    private void setShapes() {
        GradientDrawable shape = ShapeUtil.getShape(getResources().getColor(R.color.LightSeaGreen, null),
                0, GradientDrawable.OVAL, 50, 0);
        nextFab.setBackground(shape);
    }

    private void setRecyclerViewScroll() {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            perPageCnt = perPageCnt + DEFAULT_GET_FOLLOWER_PERPAGE_COUNT;
                            adapter.addProgressLoading();
                            getFriendSelectionPage();
                        }
                    }
                }
            }
        });
    }

    private void getFriendSelectionPage() {

        FriendsDBHelper.getFriendsByStatus(accountholderUser.getUserid(), perPageCnt, fb_child_status_friend, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                if(object != null){
                    adapter.addUser(((Friend) object).getUser());
                    progressDialogUtil.dialogDismiss();
                }
            }

            @Override
            public void onFailed(String message) {
                progressDialogUtil.dialogDismiss();
                if (adapter.isShowingProgressLoading()) {
                    adapter.removeProgressLoading();
                }
                CommonUtils.showToastShort(getContext(), message);
            }
        });
    }

    public void setAdapter() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new FriendVerticalListAdapter(getContext(), groupParticipantList);
        recyclerView.setAdapter(adapter);
    }

    public void checkSelectedPerson() {

        if (selectedUsers.size() == 0) {
            Toast.makeText(getContext(), getResources().getString(R.string.selectLeastOneFriend), Toast.LENGTH_SHORT).show();
            return;
        }

        if (pendingName != null) {
            if (pendingName.equals(ViewGroupDetailFragment.class.getName())) {
                startAddParticipantToGroup();
            }  else if (pendingName.equals(GroupManagementFragment.class.getName())) {

                if (mFragmentNavigation != null) {
                    /*mFragmentNavigation.pushFragment(new AddGroupFragment(new CompleteCallback() {
                        @Override
                        public void onComplete(Object object) {
                            Objects.requireNonNull(getActivity()).onBackPressed();
                            returnCallback.onReturn(object);
                        }

                        @Override
                        public void onFailed(Exception e) {

                        }
                    }), ANIMATE_RIGHT_TO_LEFT);*/
                }
            }
        }
    }

    private void startAddParticipantToGroup() {

        GroupDBHelper.addParticipantsToGroup(groupId, selectedUsers, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                returnCallback.OnReturn(null);
                Objects.requireNonNull(getActivity()).onBackPressed();
            }

            @Override
            public void OnFailed(String message) {
                CommonUtils.showToastShort(getContext(), message);
            }
        });
    }


}