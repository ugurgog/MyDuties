package uren.com.myduties.dutyManagement.profile.adapters;

import android.annotation.SuppressLint;
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
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.MyDutiesUtil;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.GroupDataUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;
import uren.com.myduties.utils.dialogBoxUtil.CustomDialogBox;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.CustomDialogListener;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.CustomDialogReturnListener;

public class AssignedToGroupsAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private Context mContext;
    private List<GroupTask> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private TaskTypeHelper taskTypeHelper;
    private User accountholderUser;

    public AssignedToGroupsAdapter(Activity activity, Context context,
                                   BaseFragment.FragmentNavigation fragmentNavigation) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
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
        ProgressBar textProgressBar;

        AppCompatTextView txtAssignedToName;
        ImageView imgAssignedToPic;
        TextView txtAssignedToPic;
        PopupMenu popupMenu = null;
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
            textProgressBar = view.findViewById(R.id.textProgressBar);
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
                                    updateGroupTask();

                                    break;

                                case R.id.remind:
                                    if (groupTask.isClosed()) {
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.closedTaskNotEdited));
                                        break;
                                    }
                                    NotificationHandler.sendNotificationToGroupParticipants(mContext, accountholderUser, groupTask.getGroup(),
                                            mContext.getResources().getString(R.string.letsRememberThisTask), groupTask.getTaskDesc());
                                    break;

                                case R.id.editText:
                                    if (groupTask.isClosed()) {
                                        CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.closedTaskNotEdited));
                                        break;
                                    }

                                    new CustomDialogBox.Builder((Activity) mContext)
                                            .setMessage(mContext.getResources().getString(R.string.sureToChangeTaskText))
                                            .setNegativeBtnVisibility(View.VISIBLE)
                                            .setNegativeBtnText(mContext.getResources().getString(R.string.cancel))
                                            .setNegativeBtnBackground(mContext.getResources().getColor(R.color.Silver, null))
                                            .setPositiveBtnVisibility(View.VISIBLE)
                                            .setPositiveBtnText(mContext.getResources().getString(R.string.ok))
                                            .setPositiveBtnBackground(mContext.getResources().getColor(R.color.bg_screen1, null))
                                            .setDurationTime(0)
                                            .isCancellable(true)
                                            .setEditTextVisibility(View.VISIBLE)
                                            .setEditTextMessage(groupTask.getTaskDesc())
                                            .OnNegativeClicked(new CustomDialogListener() {
                                                @Override
                                                public void OnClick() {
                                                    CommonUtils.hideKeyBoard(mContext);
                                                }
                                            })
                                            .OnPositiveClicked(new CustomDialogListener() {
                                                @Override
                                                public void OnClick() {
                                                    textProgressBar.setVisibility(View.VISIBLE);
                                                }
                                            })
                                            .OnReturnListenerSet(new CustomDialogReturnListener() {
                                                @Override
                                                public void OnReturn(String val) {
                                                    CommonUtils.hideKeyBoard(mContext);
                                                    groupTask.setTaskDesc(val);
                                                    updateGroupTask();
                                                }
                                            }).build();

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
                                    updateGroupTask();
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

        private void updateGroupTask() {
            GroupTaskDBHelper.updateGroupTask(groupTask, false, new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    taskList.set(position, groupTask);
                    notifyItemChanged(position);
                    textProgressBar.setVisibility(View.GONE);
                    NotificationHandler.sendNotificationToGroupParticipants(mContext, accountholderUser, groupTask.getGroup(),
                            UserDataUtil.getNameOrUsernameFromUser(accountholderUser) + " " + mContext.getResources().getString(R.string.markedThisTaskUrgent),
                            groupTask.getTaskDesc());
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(mContext, message);
                    textProgressBar.setVisibility(View.GONE);
                }
            });
        }

        public void setData(GroupTask groupTask, int position) {
            this.groupTask = groupTask;
            this.position = position;
            setAssignedFromValues();
            setWhoCompleted();
            setGroupValues();
            MyDutiesUtil.setGroupTaskCompletedTimeValueAndSetLayoutVisibility(groupTask, llcompleted);
            MyDutiesUtil.setGroupTaskDescription(groupTask, txtDetail);
            MyDutiesUtil.setGroupTaskCreatedAtValue(mContext, groupTask, txtCreateAt);
            MyDutiesUtil.setGroupTaskCompletedAtValue(mContext, groupTask, txtCompletedAt);
            MyDutiesUtil.setUrgency(mContext, groupTask.isUrgency(), tvUrgency, cardView);
            MyDutiesUtil.setClosedTv(groupTask.isClosed(), tvClosed);
            MyDutiesUtil.setCompletedImgv(mContext, groupTask.isCompleted(), existLibImgv);
            MyDutiesUtil.setTaskTypeImage(mContext, taskTypeImgv, groupTask.getType(), taskTypeHelper);
        }

        private void setGroupValues() {
            GroupDataUtil.setGroupPicture(mContext, groupTask.getGroup().getGroupPhotoUrl(),
                    groupTask.getGroup().getName(), txtAssignedToPic, imgAssignedToPic);
            GroupDataUtil.setGroupName(groupTask.getGroup(), txtAssignedToName);
        }

        @SuppressLint("SetTextI18n")
        private void setWhoCompleted() {
            if (groupTask.getWhoCompleted() != null && groupTask.getWhoCompleted().getName() != null &&
                    !groupTask.getWhoCompleted().getName().isEmpty()) {
                tvWhoCompleted.setVisibility(View.VISIBLE);
                tvWhoCompleted.setText(groupTask.getWhoCompleted().getName() + " " + mContext.getResources().getString(R.string.completed_this_task));
            } else
                tvWhoCompleted.setVisibility(View.GONE);
        }


        private void setAssignedFromValues() {
            UserDataUtil.setProfilePicture(mContext, groupTask.getAssignedFrom().getProfilePhotoUrl(),
                    groupTask.getAssignedFrom().getName(), groupTask.getAssignedFrom().getUsername(), txtProfilePic, imgProfilePic, true);
            txtUserName.setText(UserDataUtil.getNameOrUsername(groupTask.getAssignedFrom().getName(), groupTask.getAssignedFrom().getUsername()));
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