package uren.com.myduties.dutyManagement.profile.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import uren.com.myduties.dbManagement.UserTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.MyDutiesUtil;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class AssignedToUsersAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private Context mContext;
    private List<Task> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private TaskTypeHelper taskTypeHelper;
    private User accountHolderUser;

    public AssignedToUsersAdapter(Activity activity, Context context,
                                  BaseFragment.FragmentNavigation fragmentNavigation) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void taskTypeReceived(TaskTypeBus taskTypeBus) {
        taskTypeHelper = taskTypeBus.getTypeMap();
    }
    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountHolderUser = userBus.getUser();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_assigned_to_user_item, parent, false);

        viewHolder = new AssignedToUsersAdapter.MyViewHolder(itemView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Task task = taskList.get(position);
        ((AssignedToUsersAdapter.MyViewHolder) holder).setData(task, position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView imgProfilePic;
        ImageView moreImgv;
        ImageView taskTypeImgv;
        ImageView existLibImgv;
        TextView txtProfilePic;
        AppCompatTextView txtUserName;
        AppCompatTextView txtDetail;
        CardView cardView;
        Task task;
        int position;
        LinearLayout profileMainLayout;
        LinearLayout llcompleted;
        TextView txtCreateAt;
        TextView txtCompletedAt;
        AppCompatTextView tvClosed;
        AppCompatTextView tvUrgency;

        AppCompatTextView txtAssignedToName;
        ImageView imgAssignedToPic;
        TextView txtAssignedToPic;
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
            taskTypeImgv = view.findViewById(R.id.taskTypeImgv);
            existLibImgv = view.findViewById(R.id.existLibImgv);
            txtCompletedAt = view.findViewById(R.id.txtCompletedAt);
            llcompleted = view.findViewById(R.id.llcompleted);
            txtAssignedToName = view.findViewById(R.id.txtAssignedToName);
            imgAssignedToPic = view.findViewById(R.id.imgAssignedToPic);
            txtAssignedToPic = view.findViewById(R.id.txtAssignedToPic);
            tvClosed = view.findViewById(R.id.tvClosed);
            tvUrgency = view.findViewById(R.id.tvUrgency);
            setListeners();
            setPopupMenu();
        }

        private void setPopupMenu() {
            popupMenu = new PopupMenu(mContext, moreImgv);
            popupMenu.inflate(R.menu.menu_assigned_to_users_item);
        }

        private void setListeners() {

            moreImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.close:
                                    if (task.isClosed()) {
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.taskIsClosedAlready));
                                        return false;
                                    }

                                    task.setClosed(true);

                                    UserTaskDBHelper.updateUserTask(task, false, new OnCompleteCallback() {
                                        @Override
                                        public void OnCompleted() {
                                            taskList.set(position, task);
                                            notifyItemChanged(position);
                                        }

                                        @Override
                                        public void OnFailed(String message) {
                                            CommonUtils.showToastShort(mContext, message);
                                        }
                                    });

                                    break;
                                case R.id.callUser:
                                    MyDutiesUtil.callAssignedToTaskUser(mContext, task, accountHolderUser);
                                    break;

                                case R.id.remind:
                                    NotificationHandler.sendUserNotification(mContext, task.getAssignedFrom(), task.getAssignedTo(),
                                            mContext.getResources().getString(R.string.letsRememberThisTask), task.getTaskDesc());
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
            MyDutiesUtil.setUrgency(mContext, task.isUrgency(), tvUrgency, cardView);
            MyDutiesUtil.setTaskTypeImage(mContext, taskTypeImgv, task.getType(), taskTypeHelper);
            MyDutiesUtil.setClosedTv(task.isClosed(), tvClosed);
            MyDutiesUtil.setTaskCompletedAtValue(mContext, task, txtCompletedAt);
            MyDutiesUtil.setTaskCompletedTimeValueAndSetLayoutVisibility(task, llcompleted);
            MyDutiesUtil.setCompletedImgv(mContext, task.isCompleted(), existLibImgv);
            MyDutiesUtil.setTaskDescription(task, txtDetail);
            MyDutiesUtil.setTaskCreatedAtValue(mContext, task, txtCreateAt);
            setAssignedFromValues();
            setAssignedToValues();
        }

        private void setAssignedToValues() {
            User assignedTo = task.getAssignedTo();
            UserDataUtil.setProfilePicture(mContext, assignedTo.getProfilePhotoUrl(), assignedTo.getName(), assignedTo.getUsername()
                    , txtAssignedToPic, imgAssignedToPic, false);
            txtAssignedToName.setText(UserDataUtil.getNameOrUsername(assignedTo.getName(), assignedTo.getUsername()));
        }

        private void setAssignedFromValues() {
            UserDataUtil.setProfilePicture(mContext, task.getAssignedFrom().getProfilePhotoUrl(),
                    task.getAssignedFrom().getName(), task.getAssignedFrom().getUsername(), txtProfilePic, imgProfilePic, true);
            txtUserName.setText(UserDataUtil.getNameOrUsername(task.getAssignedFrom().getName(), task.getAssignedFrom().getUsername()));
        }
    }

    @Override
    public int getItemCount() {
        return (taskList != null ? taskList.size() : 0);
    }

    public void addTask(Task task) {
        if (taskList != null) {
            taskList.add(task);
            notifyItemInserted(taskList.size() - 1);
        }
    }

    public void updatePostListItems() {
        this.taskList.clear();
        notifyDataSetChanged();
    }
}