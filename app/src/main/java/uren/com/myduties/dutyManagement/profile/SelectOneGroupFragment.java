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
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.profile.adapters.SelectOneFriendAdapter;
import uren.com.myduties.dutyManagement.profile.adapters.SelectOneGroupAdapter;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.interfaces.ReturnObjectListener;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ProgressDialogUtil;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.NumericConstants.DEFAULT_GET_FOLLOWER_PERPAGE_COUNT;
import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;

public class SelectOneGroupFragment extends BaseFragment {

    View mView;

    @BindView(R.id.nextFab)
    FloatingActionButton nextFab;
    @BindView(R.id.searchCancelImgv)
    ImageView searchCancelImgv;
    @BindView(R.id.searchEdittext)
    EditText searchEdittext;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.searchResultTv)
    AppCompatTextView searchResultTv;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    AppCompatTextView toolbarTitleTv;

    private ProgressDialogUtil progressDialogUtil;
    private SelectOneGroupAdapter selectOneGroupAdapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean loading = true;
    private ReturnCallback returnCallback;
    private Group selectedGroup;
    private User accountholderUser;

    public SelectOneGroupFragment(ReturnCallback returnCallback) {
        this.returnCallback = returnCallback;
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
            mView = inflater.inflate(R.layout.fragment_select_one_group, container, false);
            ButterKnife.bind(this, mView);
            addListeners();
            initVariables();
            progressDialogUtil = new ProgressDialogUtil(getContext(), null, false);
            progressDialogUtil.dialogShow();
        }
        return mView;
    }

    private void initVariables(){
        searchResultTv.setText(getContext().getResources().getString(R.string.GROUP_NOT_FOUND));
        toolbarTitleTv.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.chooseAGroup));
        searchEdittext.setHint(getContext().getResources().getString(R.string.searchGroup));
        setShapes();
        setAdapter();
        getFriendSelectionPage();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    public void addListeners() {
        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextFab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                checkSelectedGroup();
            }
        });

        searchCancelImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdittext.setText("");
                searchCancelImgv.setVisibility(View.GONE);
                CommonUtils.showKeyboard(getContext(), false, searchEdittext);
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

                    if (selectOneGroupAdapter != null)
                        selectOneGroupAdapter.updateAdapter(s.toString(), new ReturnCallback() {
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

    private void setShapes() {
        GradientDrawable shape = ShapeUtil.getShape(getResources().getColor(R.color.LightSeaGreen, null),
                0, GradientDrawable.OVAL, 50, 0);
        nextFab.setBackground(shape);
    }

    private void getFriendSelectionPage() {

        GroupDBHelper.getUserGroups(accountholderUser, new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                if (object != null) {
                    selectOneGroupAdapter.addGroup((Group) object);
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
        selectOneGroupAdapter = new SelectOneGroupAdapter(getContext(), new ReturnCallback() {
            @Override
            public void OnReturn(Object object) {
                selectedGroup = (Group) object;
            }
        });
        recyclerView.setAdapter(selectOneGroupAdapter);
    }

    public void checkSelectedGroup() {

        if (selectedGroup == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.selectOneGroup), Toast.LENGTH_SHORT).show();
            return;
        }

        returnCallback.OnReturn(selectedGroup);
        Objects.requireNonNull(getActivity()).onBackPressed();
    }
}