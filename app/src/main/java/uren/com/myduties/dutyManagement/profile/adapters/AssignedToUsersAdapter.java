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
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class AssignedToUsersAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private Context mContext;
    private List<Task> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private HashMap<String, Integer> taskPositionHashMap;
    private TaskTypeHelper taskTypeHelper;

    public AssignedToUsersAdapter(Activity activity, Context context,
                              BaseFragment.FragmentNavigation fragmentNavigation) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
        this.taskPositionHashMap = new HashMap<>();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void taskTypeReceived(TaskTypeBus taskTypeBus){
        taskTypeHelper = taskTypeBus.getTypeMap();
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
        User assignedFrom = null;

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
                                    if(task.isClosed())
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.taskIsClosedAlready));

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

                                    if(task.getAssignedTo() != null && task.getAssignedTo().getPhone() != null){
                                        try {
                                            String phoneNumber = task.getAssignedTo().getPhone().getDialCode() + task.getAssignedTo().getPhone().getPhoneNumber();
                                            mContext.startActivity(new Intent(Intent.ACTION_DIAL,
                                                    Uri.fromParts("tel", phoneNumber, null)));
                                        } catch (Exception e) {
                                            CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.users_phone_not_defined));
                                            e.printStackTrace();
                                        }
                                    }else
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.users_phone_not_defined));

                                    break;

                                case R.id.remind:
                                    NotificationHandler.sendUserNotification(mContext, assignedFrom, task.getAssignedTo(),
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

            //her postID bir position ile entegre halde...
            this.task = task;
            this.position = position;
            taskPositionHashMap.put(task.getTaskId(), position);
            setTaskTypeImage();
            setUrgency();
            setCompletedImage();
            setTaskDesc();
            setCreatedAtValue();
            setAssignedFromValues();
            setAssignedToValues();
            setCompletedTime();
            setClosedImgv();
        }

        private void setClosedImgv() {
            if(task.isClosed())
                tvClosed.setVisibility(View.VISIBLE);
            else
                tvClosed.setVisibility(View.GONE);
        }

        private void setAssignedToValues() {
            User assignedTo = task.getAssignedTo();
            UserDataUtil.setProfilePicture(mContext, assignedTo.getProfilePhotoUrl(), assignedTo.getName(), assignedTo.getUsername()
                    , txtAssignedToPic, imgAssignedToPic, false);
            txtAssignedToName.setText(UserDataUtil.getNameOrUsername(assignedTo.getName(), assignedTo.getUsername()));
        }

        private void setCompletedTime() {
            //Completed at
            if (task.getCompletedTime() != 0) {
                llcompleted.setVisibility(View.VISIBLE);
                txtCompletedAt.setText(CommonUtils.getMessageTime(mContext, task.getCompletedTime()));
            }else
                llcompleted.setVisibility(View.GONE);
        }

        private void setAssignedFromValues() {
            assignedFrom = task.getAssignedFrom();
            //profile picture
            UserDataUtil.setProfilePicture(mContext, assignedFrom.getProfilePhotoUrl(), assignedFrom.getName(), assignedFrom.getUsername()
                    , txtProfilePic, imgProfilePic, true);

            //username of user who assigned the task
            txtUserName.setText(UserDataUtil.getNameOrUsername(assignedFrom.getName(), assignedFrom.getUsername()));
        }

        private void setCreatedAtValue() {
            if (task.getAssignedTime() != 0)
                txtCreateAt.setText(CommonUtils.getMessageTime(mContext, task.getAssignedTime()));
        }

        private void setTaskDesc() {
            if (task.getTaskDesc() != null && !task.getTaskDesc().isEmpty()) {
                txtDetail.setText(task.getTaskDesc());
                txtDetail.setVisibility(View.VISIBLE);
            } else {
                txtDetail.setVisibility(View.GONE);
            }
        }

        private void setCompletedImage() {
            if (task.isCompleted())
                existLibImgv.setColorFilter(mContext.getResources().getColor(R.color.Green, null), PorterDuff.Mode.SRC_IN);
            else
                existLibImgv.setColorFilter(mContext.getResources().getColor(R.color.Red, null), PorterDuff.Mode.SRC_IN);
        }

        private void setUrgency() {
            CommonUtils.setUrgencyTv(task.isUrgency(), tvUrgency);
        }

        private void setTaskTypeImage() {
            CommonUtils.setTaskTypeImage(mContext, taskTypeImgv, task.getType(), taskTypeHelper);
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