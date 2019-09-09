package uren.com.myduties.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.admin.fragments.CompletedProblemsFragment;
import uren.com.myduties.admin.fragments.InCompleteProblemsFragment;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.models.User;

public class AdminPageFragment extends BaseFragment {


    View mView;

    @BindView(R.id.incompleteProblemsll)
    LinearLayout incompleteProblemsll;
    @BindView(R.id.completedProblemsll)
    LinearLayout completedProblemsll;

    User accountHolderUser;

    public AdminPageFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_admin_page, container, false);
            ButterKnife.bind(this, mView);
            initListeners();
        }

        return mView;
    }

    private void initListeners() {

        incompleteProblemsll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentNavigation.pushFragment(new InCompleteProblemsFragment());
            }
        });

        completedProblemsll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentNavigation.pushFragment(new CompletedProblemsFragment());
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = null;
    }
}