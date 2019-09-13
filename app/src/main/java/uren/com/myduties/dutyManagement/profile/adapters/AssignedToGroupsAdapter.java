package uren.com.myduties.dutyManagement.profile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
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
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.GroupDataUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class AssignedToGroupsAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private Context mContext;
    private List<GroupTask> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private HashMap<String, Integer> taskPositionHashMap;
    private TaskTypeHelper taskTypeHelper;
    private User accountholderUser;

    public AssignedToGroupsAdapter(Activity activity, Context context,
                                   BaseFragment.FragmentNavigation fragmentNavigation) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
        this.taskPositionHashMap = new HashMap<>();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
    }

    @Subscribe(sticky = true)
    public void taskTypeReceived(TaskTypeBus taskTypeBus) {
        taskTypeHelper = taskTypeBus.getTypeMap();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_assigned_to_group_item, parent, false);

        viewHolder = new AssignedToGroupsAdapter.MyViewHolder(itemView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GroupTask groupTask = taskList.get(position);
        ((AssignedToGroupsAdapter.MyViewHolder) holder).setData(groupTask, position);
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
        int position;
        LinearLayout profileMainLayout;
        LinearLayout llcompleted;
        TextView txtCreateAt;
        TextView txtCompletedAt;
        AppCompatTextView tvClosed;
        AppCompatTextView tvUrgency;
        AppCompatTextView tvWhoCompleted;

        AppCompatTextView txtAssignedToName;
        ImageView imgAssignedToPic;
        TextView txtAssignedToPic;
        PopupMenu popupMenu = null;
        User assignedFrom = null;
        GroupTask groupTask;

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
            tvWhoCompleted = view.findViewById(R.id.tvWhoCompleted);
            setListeners();
            setPopupMenu();
        }

        private void setPopupMenu() {
            popupMenu = new PopupMenu(mContext, moreImgv);
            popupMenu.inflate(R.menu.menu_assigned_to_groups_item);
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
                                    if (groupTask.isClosed()) {
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.taskIsClosedAlready));
                                        break;
                                    }
                                    groupTask.setClosed(true);
                                    updateGroup();

                                    break;

                                case R.id.remind:
                                    NotificationHandler.sendNotificationToGroupParticipants(mContext, accountholderUser, groupTask.getGroup(),
                                            mContext.getResources().getString(R.string.letsRememberThisTask), groupTask.getTaskDesc());
                                    break;

                                case R.id.makeUrgent:
                                    if (groupTask.isUrgency()) {
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.taskIsUrgentAlready));
                                        break;
                                    }

                                    if (groupTask.isClosed()) {
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.closedTaskNotMarkedUrgent));
                                        break;
                                    }

                                    groupTask.setUrgency(true);
                                    updateGroup();
                                    break;

                                case R.id.delete:
                                    if (!groupTask.isClosed()) {
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.openTasksCouldNotBeDeleted));
                                        break;
                                    }

                                    GroupTaskDBHelper.deleteGroupTask(accountholderUser.getUserid(), groupTask.getGroup().getGroupid(),
                                            groupTask.getTaskId(), new OnCompleteCallback() {
                                                @Override
                                                public void OnCompleted() {
                                                    taskList.remove(position);
                                                    notifyItemRemoved(position);
                                                    notifyItemRangeChanged(position, getItemCount());
                                                }

                                                @Override
                                                public void OnFailed(String message) {

                                                }
                                            });

                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        private void updateGroup() {
            GroupTaskDBHelper.updateGroupTask(groupTask, false, new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    taskList.set(position, groupTask);
                    notifyItemChanged(position);
                    NotificationHandler.sendNotificationToGroupParticipants(mContext, accountholderUser, groupTask.getGroup(),
                            UserDataUtil.getNameOrUsernameFromUser(accountholderUser) + " " + mContext.getResources().getString(R.string.markedThisTaskUrgent),
                            groupTask.getTaskDesc());
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(mContext, message);
                }
            });
        }

        public void setData(GroupTask groupTask, int position) {
            this.groupTask = groupTask;
            this.position = position;
            taskPositionHashMap.put(groupTask.getTaskId(), position);
            setTaskTypeImage();
            setUrgency();
            setCompletedImage();
            setTaskDesc();
            setCreatedAtValue();
            setAssignedFromValues();
            setCompletedTime();
            setClosedImgv();
            setWhoCompleted();
            setGroupValues();
        }

        private void setGroupValues() {
            GroupDataUtil.setGroupPicture(mContext, groupTask.getGroup().getGroupPhotoUrl(),
                    groupTask.getGroup().getName(), txtAssignedToPic, imgAssignedToPic);
            GroupDataUtil.setGroupName(groupTask.getGroup(), txtAssignedToName);
        }

        private void setWhoCompleted() {
            if (groupTask.getWhoCompleted() != null && groupTask.getWhoCompleted().getName() != null &&
                    !groupTask.getWhoCompleted().getName().isEmpty()) {
                tvWhoCompleted.setVisibility(View.VISIBLE);
                tvWhoCompleted.setText(groupTask.getWhoCompleted().getName() + " " + mContext.getResources().getString(R.string.completed_this_task));
            } else
                tvWhoCompleted.setVisibility(View.GONE);
        }

        private void setClosedImgv() {
            if (groupTask.isClosed())
                tvClosed.setVisibility(View.VISIBLE);
            else
                tvClosed.setVisibility(View.GONE);
        }

        private void setCompletedTime() {
            //Completed at
            if (groupTask.getCompletedTime() != 0) {
                llcompleted.setVisibility(View.VISIBLE);
                txtCompletedAt.setText(CommonUtils.getMessageTime(mContext, groupTask.getCompletedTime()));
            } else
                llcompleted.setVisibility(View.GONE);
        }

        private void setAssignedFromValues() {
            assignedFrom = groupTask.getAssignedFrom();
            //profile picture
            UserDataUtil.setProfilePicture(mContext, assignedFrom.getProfilePhotoUrl(), assignedFrom.getName(), assignedFrom.getUsername()
                    , txtProfilePic, imgProfilePic, true);

            //username of user who assigned the task
            txtUserName.setText(UserDataUtil.getNameOrUsername(assignedFrom.getName(), assignedFrom.getUsername()));
        }

        private void setCreatedAtValue() {
            if (groupTask.getAssignedTime() != 0)
                txtCreateAt.setText(CommonUtils.getMessageTime(mContext, groupTask.getAssignedTime()));
        }

        private void setTaskDesc() {
            if (groupTask.getTaskDesc() != null && !groupTask.getTaskDesc().isEmpty()) {
                txtDetail.setText(groupTask.getTaskDesc());
                txtDetail.setVisibility(View.VISIBLE);
            } else {
                txtDetail.setVisibility(View.GONE);
            }
        }

        private void setCompletedImage() {
            if (groupTask.isCompleted())
                existLibImgv.setColorFilter(mContext.getResources().getColor(R.color.Green, null), PorterDuff.Mode.SRC_IN);
            else
                existLibImgv.setColorFilter(mContext.getResources().getColor(R.color.Red, null), PorterDuff.Mode.SRC_IN);
        }

        private void setUrgency() {
            CommonUtils.setUrgencyTv( groupTask.isUrgency(), tvUrgency);
        }

        private void setTaskTypeImage() {
            CommonUtils.setTaskTypeImage(mContext, taskTypeImgv, groupTask.getType(), taskTypeHelper);
        }
    }

    @Override
    public int getItemCount() {
        return (taskList != null ? taskList.size() : 0);
    }

    public void addTask(GroupTask groupTask) {
        if (taskList != null) {
            taskList.add(groupTask);
            notifyItemInserted(taskList.size() - 1);
        }
    }

    public void updatePostListItems() {
        this.taskList.clear();
        notifyDataSetChanged();
    }
}