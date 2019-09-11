package uren.com.myduties.dutyManagement.assignTask.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.assignTask.interfaces.TaskTypeCallback;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.models.TaskType;
import uren.com.myduties.utils.TaskTypeHelper;

public class TaskTypeSelectAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<TaskType> taskTypes;
    private TaskTypeHelper taskTypeHelper;
    private Context context;
    private TaskTypeCallback taskTypeCallback;

    public TaskTypeSelectAdapter(Context context, List<TaskType> taskTypes, TaskTypeCallback taskTypeCallback) {
        this.context = context;
        this.taskTypes = taskTypes;
        this.taskTypeCallback = taskTypeCallback;
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void taskTypeReceived(TaskTypeBus taskTypeBus) {
        taskTypeHelper = taskTypeBus.getTypeMap();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_type_item, parent, false);

        viewHolder = new TaskTypeSelectAdapter.MyViewHolder(itemView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TaskType taskType = taskTypes.get(position);
        ((TaskTypeSelectAdapter.MyViewHolder) holder).setData(taskType, position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;

        int position;
        ImageView typeImgv;
        TextView typeTv;
        TaskType taskType;

        public MyViewHolder(View view) {
            super(view);

            mView = view;
            typeImgv = view.findViewById(R.id.typeImgv);
            typeTv = view.findViewById(R.id.typeTv);
            setListeners();
        }

        private void setListeners() {
            typeImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    taskTypeCallback.OnReturn(taskType);
                }
            });
        }

        public void setData(TaskType taskType, int position) {
            this.taskType = taskType;
            this.position = position;
            setTaskTypeData();
            setTaskTypeDesc();
        }

        private void setTaskTypeDesc() {
            typeTv.setText(taskType.getDesc());
        }

        private void setTaskTypeData() {
            Glide.with(context)
                    .load(taskType.getImgId())
                    .apply(RequestOptions.centerInsideTransform())
                    .into(typeImgv);
        }
    }

    @Override
    public int getItemCount() {
        return (taskTypes != null ? taskTypes.size() : 0);
    }


}