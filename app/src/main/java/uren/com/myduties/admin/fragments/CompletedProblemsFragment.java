package uren.com.myduties.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.Objects;

import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;

public class CompletedProblemsFragment extends BaseFragment {

    View mView;


    public CompletedProblemsFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_completed_problems, container, false);
            ButterKnife.bind(this, mView);

        }

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = null;
    }
}
