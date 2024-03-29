package uren.com.myduties.dutyManagement.assignTask;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.assignTask.adapters.TaskTypeSelectAdapter;
import uren.com.myduties.dutyManagement.assignTask.interfaces.TaskTypeCallback;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.models.TaskType;
import uren.com.myduties.utils.TaskTypeHelper;

public class TaskTypeSelectFragment extends BaseFragment {

    View mView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private TaskTypeSelectAdapter taskTypeSelectAdapter;
    private TaskTypeCallback taskTypeCallback;
    private List<TaskType> taskTypes;
    private TaskTypeHelper taskTypeHelper;

    public TaskTypeSelectFragment(TaskTypeCallback taskTypeCallback) {
        this.taskTypeCallback = taskTypeCallback;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_task_type_select, container, false);
        ButterKnife.bind(this, mView);
        initVariables();
        addListeners();
        setAdapter();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    public void taskTypeHelperReceived(TaskTypeBus taskTypeBus) {
        taskTypeHelper = taskTypeBus.getTypeMap();
    }

    private void initVariables() {
        taskTypes = new ArrayList<>();
    }

    public void addListeners() {

    }

    private void setAdapter() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        taskTypeSelectAdapter = new TaskTypeSelectAdapter(getActivity(), taskTypeHelper.getTypes(), new TaskTypeCallback() {
            @Override
            public void OnReturn(TaskType taskType) {
                taskTypeCallback.OnReturn(taskType);
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });
        recyclerView.setAdapter(taskTypeSelectAdapter);
    }
}