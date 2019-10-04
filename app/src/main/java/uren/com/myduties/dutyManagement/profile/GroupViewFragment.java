package uren.com.myduties.dutyManagement.profile;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.GroupViewAdapter;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;

import static uren.com.myduties.constants.NumericConstants.VIEW_NO_POST_FOUND;
import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;

public class GroupViewFragment extends BaseFragment {

    View mView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.searchCancelImgv)
    ImageView searchCancelImgv;
    @BindView(R.id.searchToolbarBackImgv)
    ImageView searchToolbarBackImgv;

    @BindView(R.id.specialRecyclerView)
    RecyclerView specialRecyclerView;

    @BindView(R.id.searchEdittext)
    EditText searchEdittext;
    @BindView(R.id.nextFab)
    FloatingActionButton nextFab;

    @BindView(R.id.warningMsgLayout)
    LinearLayout warningMsgLayout;
    @BindView(R.id.warningMsgTv)
    AppCompatTextView warningMsgTv;

    private Group selectedGroupItem;
    private ReturnCallback returnCallback;
    private LinearLayoutManager linearLayoutManager;
    private GroupViewAdapter groupViewAdapter;
    private User accountHolder;

    private static final int ITEM_CHANGED = 0;
    private static final int ITEM_REMOVED = 1;
    private static final int ITEM_INSERTED = 2;

    public GroupViewFragment(ReturnCallback returnCallback) {
        this.returnCallback = returnCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((NextActivity) getActivity()).ANIMATION_TAG = ANIMATE_LEFT_TO_RIGHT;
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_group_view, container, false);
            ButterKnife.bind(this, mView);
            addListeners();
            initValues();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    public void initValues() {
        searchEdittext.setHint(getContext().getResources().getString(R.string.searchGroup));
        setGroupsListAdapter();
        getGroups();
    }

    public void addListeners() {
        searchToolbarBackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.showKeyboard(getContext(), false, searchEdittext);
                getActivity().onBackPressed();
            }
        });

        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyBoard(getContext());
                addNewGroup();
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
                if (s != null && s.toString() != null && !s.toString().isEmpty()) {
                    searchCancelImgv.setVisibility(View.VISIBLE);
                    searchItemInList(s.toString());
                } else {
                    searchCancelImgv.setVisibility(View.GONE);
                    searchItemInList("");
                }
            }
        });

        searchCancelImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdittext.setText("");
                searchCancelImgv.setVisibility(View.GONE);
                CommonUtils.showKeyboard(getContext(),false, searchEdittext);
            }
        });
    }

    public void searchItemInList(final String groupName) {
        if (groupViewAdapter != null)
            groupViewAdapter.updateAdapter(groupName, new ReturnCallback() {
                @Override
                public void OnReturn(Object object) {
                    int itemSize = (int) object;

                    if (!groupName.isEmpty()) {
                        if (itemSize == 0) {
                            warningMsgLayout.setVisibility(View.VISIBLE);
                            warningMsgTv.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.THERE_IS_NO_SEARCH_RESULT));
                        }else
                            warningMsgLayout.setVisibility(View.GONE);
                    } else
                        warningMsgLayout.setVisibility(View.GONE);
                }
            });
    }

    public void getGroups() {

        progressBar.setVisibility(View.VISIBLE);

        GroupDBHelper.getUserGroups(accountHolder, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                Group group = (Group) object;
                groupViewAdapter.addGroup(group);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailed(String message) {
                progressBar.setVisibility(View.GONE);
                CommonUtils.showToastShort(getContext(), message);
            }
        });

        new Handler().postDelayed(() -> {
            if(groupViewAdapter.getItemCount() == 0){
                progressBar.setVisibility(View.GONE);
                warningMsgLayout.setVisibility(View.VISIBLE);
                warningMsgTv.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.THERE_IS_NO_GROUP_CREATE_OR_INCLUDE));
            }else
                warningMsgLayout.setVisibility(View.GONE);
        }, 3000);
    }

    private void setGroupsListAdapter() {
        groupViewAdapter = new GroupViewAdapter(getContext(), new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {
                selectedGroupItem = (Group) object;
            }
        }, mFragmentNavigation);

        specialRecyclerView.setAdapter(groupViewAdapter);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        specialRecyclerView.setLayoutManager(linearLayoutManager);
    }

    public void addNewGroup() {
        FriendsDBHelper.getFriendCountByStatus(accountHolder.getUserid(), fb_child_status_friend, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                int count = (int) object;

                if(count == 0)
                    CommonUtils.showToastShort(getContext(), getContext().getResources().getString(R.string.addFriendFirst));
                else
                    startSelectFriendFragment();
            }

            @Override
            public void onFailed(String message) {
                CommonUtils.showToastShort(getContext(), message);
            }
        });
    }

    private void startSelectFriendFragment() {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(new SelectFriendFragment(null, null, null,
                    GroupViewFragment.class.getName(), new ReturnCallback() {
                @Override
                public void OnReturn(Object object) {
                    groupViewAdapter.addGroup((Group) object);
                }
            }), ANIMATE_RIGHT_TO_LEFT);
        }
    }
}
