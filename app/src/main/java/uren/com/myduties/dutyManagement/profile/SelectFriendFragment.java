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
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ProgressDialogUtil;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.StringConstants.ANIMATE_LEFT_TO_RIGHT;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;
import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;

public class SelectFriendFragment extends BaseFragment {

    View mView;

    @BindView(R.id.nextFab)
    FloatingActionButton nextFab;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.searchCancelImgv)
    ImageView searchCancelImgv;
    @BindView(R.id.searchToolbarBackImgv)
    ImageView searchToolbarBackImgv;
    @BindView(R.id.searchEdittext)
    EditText searchEdittext;

    private ProgressDialogUtil progressDialogUtil;
    private FriendVerticalListAdapter adapter;
    private String groupId;
    private List<User> groupParticipantList;
    private String pendingName;
    private LinearLayoutManager linearLayoutManager;
    private boolean loading = true;
    private ReturnCallback returnCallback;
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
    public void selectedUsersReceived(SelectedUsersBus selectedUsersBus) {
        selectedUsers = selectedUsersBus.getUsers();
    }

    @Subscribe(sticky = true)
    public void accountHolderUserReceived(UserBus userBus) {
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
            setAdapter();
            getFriendSelectionPage();
            progressDialogUtil = new ProgressDialogUtil(getContext(), null, false);
            progressDialogUtil.dialogShow();
        }
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

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

        searchCancelImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdittext.setText("");
                searchCancelImgv.setVisibility(View.GONE);
                CommonUtils.showKeyboard(getContext(),false, searchEdittext);
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
                if (s != null) {
                    if (!s.toString().trim().isEmpty()) {
                        searchCancelImgv.setVisibility(View.VISIBLE);
                    } else {
                        searchCancelImgv.setVisibility(View.GONE);
                    }

                    if (adapter != null)
                        adapter.updateAdapter(s.toString());
                } else
                    searchCancelImgv.setVisibility(View.GONE);
            }
        });
    }

    private void setShapes() {
        GradientDrawable shape = ShapeUtil.getShape(getResources().getColor(R.color.LightSeaGreen),
                0, GradientDrawable.OVAL, 50, 0);
        nextFab.setBackground(shape);
    }

    private void getFriendSelectionPage() {

        FriendsDBHelper.getFriendsByStatus(accountholderUser.getUserid(), fb_child_status_friend, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                if (object != null) {
                    adapter.addUser(((Friend) object).getUser());
                    progressDialogUtil.dialogDismiss();
                }
            }

            @Override
            public void onFailed(String message) {
                progressDialogUtil.dialogDismiss();
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
            } else if (pendingName.equals(GroupViewFragment.class.getName())) {

                if (mFragmentNavigation != null) {
                    mFragmentNavigation.pushFragment(new AddGroupFragment(new CompleteCallback() {
                        @Override
                        public void onComplete(Object object) {
                            Objects.requireNonNull(getActivity()).onBackPressed();
                            returnCallback.OnReturn(object);
                        }

                        @Override
                        public void onFailed(String message) {
                            CommonUtils.showToastShort(getContext(), message);
                        }
                    }), ANIMATE_RIGHT_TO_LEFT);
                }
            }
        }
    }

    private void startAddParticipantToGroup() {

        for (User user : selectedUsers) {
            GroupDBHelper.addAParticipantToGroup(groupId, user, new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    returnCallback.OnReturn(user);

                    if (user.getUserid().equals(selectedUsers.get(selectedUsers.size() - 1).getUserid()))
                        Objects.requireNonNull(getActivity()).onBackPressed();
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(getContext(), message);
                }
            });
        }
    }


}
