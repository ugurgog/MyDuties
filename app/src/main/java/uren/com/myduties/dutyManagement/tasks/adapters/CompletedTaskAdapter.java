package uren.com.myduties.dutyManagement.tasks.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.MyDutiesUtil;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class CompletedTaskAdapter extends RecyclerView.Adapter {

    public static final int VIEW_PROG = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NULL = 2;

    private Activity mActivity;
    private Context mContext;
    private List<Task> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private ReturnCallback returnCallback;
    private User user;
    private TaskTypeHelper taskTypeHelper;

    public CompletedTaskAdapter(Activity activity, Context context,
                                BaseFragment.FragmentNavigation fragmentNavigation, User user) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
        this.user = user;
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void taskTypeReceived(TaskTypeBus taskTypeBus) {
        taskTypeHelper = taskTypeBus.getTypeMap();
    }

    public void setReturnCallback(ReturnCallback returnCallback) {
        this.returnCallback = returnCallback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.completed_task_vert_list_item, parent, false);
        viewHolder = new MyViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Task task = taskList.get(position);
        ((MyViewHolder) holder).setData(task, position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView imgProfilePic;
        ImageView moreImgv;
        ImageView taskTypeImgv;
        TextView txtProfilePic;
        AppCompatTextView txtUserName;
        AppCompatTextView txtDetail;
        CardView cardView;
        Task task;
        int position;
        LinearLayout profileMainLayout;
        TextView txtCreateAt;
        TextView txtCompletedAt;
        AppCompatTextView tvClosed;
        AppCompatTextView tvUrgency;
        ImageView completedImgv;
        PopupMenu popupMenu = null;

        public MyViewHolder(View view) {
            super(view);

            mView = view;
            cardView = view.findViewById(R.id.card_view);
            imgProfilePic = view.findViewById(R.id.imgProfilePic);
            moreImgv = view.findViewById(R.id.moreImgv);
            txtProfilePic = view.findViewById(R.id.txtProfilePic);
            txtUserName = view.findViewById(R.id.txtUserName);
            txtDetail = view.findViewById(R.id.txtDetail);
            profileMainLayout = view.findViewById(R.id.profileMainLayout);
            txtCreateAt = view.findViewById(R.id.txtCreateAt);
            txtCompletedAt = view.findViewById(R.id.txtCompletedAt);
            taskTypeImgv = view.findViewById(R.id.taskTypeImgv);
            tvClosed = view.findViewById(R.id.tvClosed);
            tvUrgency = view.findViewById(R.id.tvUrgency);
            completedImgv = view.findViewById(R.id.completedImgv);
            setListeners();
            setPopupMenu();
        }

        private void setPopupMenu() {
            popupMenu = new PopupMenu(mContext, moreImgv);
            popupMenu.inflate(R.menu.menu_completed_task_item);
        }

        private void setListeners() {
            imgProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.getAssignedFrom() != null && task.getAssignedFrom().getProfilePhotoUrl() != null &&
                            !task.getAssignedFrom().getProfilePhotoUrl().isEmpty()) {
                        fragmentNavigation.pushFragment(new ShowSelectedPhotoFragment(task.getAssignedFrom().getProfilePhotoUrl()));
                    }
                }
            });

            moreImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.callUser:
                                    if (task.getAssignedFrom().getUsername() == null || task.getAssignedFrom().getUsername().trim().isEmpty()) {

                                        UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                                            @Override
                                            public void onComplete(Object object) {

                                                if (object != null) {
                                                    task.setAssignedFrom((User) object);
                                                    MyDutiesUtil.callAssignedFromTaskUser(mContext, task, user);
                                                } else
                                                    CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.UNEXPECTED_ERROR));
                                            }

                                            @Override
                                            public void onFailed(String message) {
                                                CommonUtils.showToastShort(mContext, message);
                                            }
                                        });
                                    } else
                                        MyDutiesUtil.callAssignedFromTaskUser(mContext, task, user);

                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        public void setData(Task task, int position) {
            this.task = task;
            this.position = position;
            MyDutiesUtil.setTaskTypeImage(mContext, taskTypeImgv, task.getType(), taskTypeHelper);
            MyDutiesUtil.setUrgency(mContext, task.isUrgency(), tvUrgency, cardView);
            MyDutiesUtil.setClosedTv(task.isClosed(), tvClosed);
            MyDutiesUtil.setCompletedImgv(mContext, task.isCompleted(), completedImgv);
            MyDutiesUtil.setTaskCreatedAtValue(mContext, task, txtCreateAt);
            MyDutiesUtil.setTaskCompletedAtValue(mContext, task, txtCompletedAt);
            MyDutiesUtil.setTaskDescription(task, txtDetail);
            getAssignedFromUser();
        }

        private void getAssignedFromUser() {
            if (task.getAssignedFrom().getUsername() == null || task.getAssignedFrom().getUsername().trim().isEmpty()) {
                UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {
                        task.setAssignedFrom((User) object);
                        setAssignedFromUserViews();
                    }

                    @Override
                    public void onFailed(String message) {

                    }
                });
            }else
                setAssignedFromUserViews();
        }

        private void setAssignedFromUserViews(){
            UserDataUtil.setProfilePicture(mContext, task.getAssignedFrom().getProfilePhotoUrl(),
                    task.getAssignedFrom().getName(), task.getAssignedFrom().getUsername()
                    , txtProfilePic, imgProfilePic, true);
            txtUserName.setText(UserDataUtil.getNameOrUsername(task.getAssignedFrom().getName(), task.getAssignedFrom().getUsername()));
        }
    }

    @Override
    public int getItemCount() {
        return (taskList != null ? taskList.size() : 0);
    }

    public void addAll(List<Task> addedTaskList) {
        if (addedTaskList != null) {
            taskList.addAll(addedTaskList);
            notifyItemRangeInserted(taskList.size(), taskList.size() + taskList.size());
        }
    }

    public void updatePostListItems(List<Task> newTaskList) {
        this.taskList.clear();
        this.taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }
}