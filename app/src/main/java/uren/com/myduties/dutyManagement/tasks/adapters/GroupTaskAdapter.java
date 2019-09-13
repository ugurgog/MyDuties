package uren.com.myduties.dutyManagement.tasks.adapters;

import android.annotation.SuppressLint;
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
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.tasks.GroupAllTasksFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.GroupDataUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;

public class GroupTaskAdapter extends RecyclerView.Adapter {

    public static final int VIEW_ITEM = 1;

    private Activity mActivity;
    private Context mContext;
    private List<GroupTask> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private HashMap<String, Integer> taskPositionHashMap;
    private User user;
    private TaskTypeHelper taskTypeHelper;

    public GroupTaskAdapter(Activity activity, Context context,
                            BaseFragment.FragmentNavigation fragmentNavigation, User user) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
        this.taskPositionHashMap = new HashMap<>();
        this.user = user;
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
                .inflate(R.layout.group_task_vert_list_item, parent, false);
        viewHolder = new GroupTaskAdapter.MyViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GroupTask task = taskList.get(position);
        ((GroupTaskAdapter.MyViewHolder) holder).setData(task, position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView imgProfilePic;
        ImageView completedImgv;
        ImageView moreImgv;
        ImageView taskTypeImgv;
        TextView txtProfilePic;
        AppCompatTextView txtUserName;
        AppCompatTextView txtDetail;
        AppCompatTextView tvWhoCompleted;
        CardView cardView;
        LinearLayout llcompleted;
        GroupTask task;
        int position;
        LinearLayout profileMainLayout;
        LinearLayout mainll;
        TextView txtCreateAt;
        TextView txtCompletedAt;

        ImageView imgGroupPic;
        TextView txtGroupPic;
        AppCompatTextView txtGroupName;
        AppCompatTextView tvSeeAllTasks;
        AppCompatTextView tvClosed;
        AppCompatTextView tvUrgency;

        PopupMenu popupMenu = null;

        User assignedFrom = null;
        User whoCompleted = null;
        Group taskGroup = null;

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
            llcompleted = view.findViewById(R.id.llcompleted);
            completedImgv = view.findViewById(R.id.completedImgv);
            tvWhoCompleted = view.findViewById(R.id.tvWhoCompleted);
            taskTypeImgv = view.findViewById(R.id.taskTypeImgv);
            mainll = view.findViewById(R.id.mainll);

            imgGroupPic = view.findViewById(R.id.imgGroupPic);
            txtGroupPic = view.findViewById(R.id.txtGroupPic);
            txtGroupName = view.findViewById(R.id.txtGroupName);
            tvSeeAllTasks = view.findViewById(R.id.tvSeeAllTasks);
            tvClosed = view.findViewById(R.id.tvClosed);
            tvUrgency = view.findViewById(R.id.tvUrgency);

            setListeners();
        }

        private void setListeners() {
            imgProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (assignedFrom != null && assignedFrom.getProfilePhotoUrl() != null &&
                            !assignedFrom.getProfilePhotoUrl().isEmpty()) {
                        fragmentNavigation.pushFragment(new ShowSelectedPhotoFragment(assignedFrom.getProfilePhotoUrl()));
                    }
                }
            });

            imgGroupPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (taskGroup != null && taskGroup.getGroupPhotoUrl() != null &&
                            !taskGroup.getGroupPhotoUrl().isEmpty()) {
                        fragmentNavigation.pushFragment(new ShowSelectedPhotoFragment(taskGroup.getGroupPhotoUrl()));
                    }
                }
            });

            tvSeeAllTasks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (taskGroup != null) {
                        fragmentNavigation.pushFragment(new GroupAllTasksFragment(taskGroup));
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
                                case R.id.completeTask:

                                    task.setCompleted(true);
                                    task.setWhoCompleted(user);

                                    GroupTaskDBHelper.updateGroupTask(task, true, new OnCompleteCallback() {
                                        @Override
                                        public void OnCompleted() {
                                            taskList.set(position, task);
                                            notifyItemChanged(position);
                                            setTaskCompletedTime();
                                            NotificationHandler.sendUserNotification(mContext, user, task.getAssignedFrom(),
                                                    UserDataUtil.getNameOrUsernameFromUser(user) + " " + mContext.getResources().getString(R.string.completedThisTask),
                                                    task.getTaskDesc());
                                        }

                                        @Override
                                        public void OnFailed(String message) {
                                            CommonUtils.showToastShort(mContext, message);
                                        }
                                    });

                                    break;
                                case R.id.callUser:

                                    if (assignedFrom == null) {
                                        UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                                            @Override
                                            public void onComplete(Object object) {
                                                assignedFrom = (User) object;
                                                callAssignedFromUser();
                                            }

                                            @Override
                                            public void onFailed(String message) {
                                                CommonUtils.showToastShort(mContext, message);
                                            }
                                        });
                                    } else
                                        callAssignedFromUser();

                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        public void setTaskCompletedTime() {
            GroupTaskDBHelper.getGroupTaskWithId(task.getTaskId(), taskGroup.getGroupid(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    GroupTask groupTask = (GroupTask) object;
                    task.setCompletedTime(groupTask.getCompletedTime());
                    taskList.set(position, task);
                    notifyItemChanged(position);
                }

                @Override
                public void onFailed(String message) {

                }
            });
        }

        public void callAssignedFromUser() {
            try {
                if (assignedFrom != null && assignedFrom.getPhone() != null) {
                    String phoneNumber = assignedFrom.getPhone().getDialCode() + assignedFrom.getPhone().getPhoneNumber();
                    mContext.startActivity(new Intent(Intent.ACTION_DIAL,
                            Uri.fromParts("tel", phoneNumber, null)));
                }
            } catch (Exception e) {
                CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.users_phone_not_defined));
                e.printStackTrace();
            }
        }

        public void setData(GroupTask task, int position) {

            //her postID bir position ile entegre halde...
            this.task = task;
            this.position = position;
            taskPositionHashMap.put(task.getTaskId(), position);
            setTaskItems();
            setTaskAssignedFrom();
            setTaskGroup();
            setCompletedTimeVal();
            setWhoCompleted();
            setCompletedImage();
            setPopupMenu();
            setTaskTypeImage();
            setUrgency();
            setClosedTv();
        }

        private void setUrgency() {
            CommonUtils.setUrgencyTv(task.isUrgency(), tvUrgency);
        }

        private void setClosedTv() {
            CommonUtils.setClosedTv(task.isClosed(), tvClosed);
        }

        private void setCompletedImage() {
            CommonUtils.setCompletedImgv(mContext, task.isCompleted(), completedImgv);
        }

        private void setTaskTypeImage() {
            CommonUtils.setTaskTypeImage(mContext, taskTypeImgv, task.getType(), taskTypeHelper);
        }


        private void setWhoCompleted() {
            if (task.getWhoCompleted() == null || task.getWhoCompleted().getUserid() == null || task.getWhoCompleted().getUserid().isEmpty())
                tvWhoCompleted.setVisibility(View.GONE);
            else {
                tvWhoCompleted.setVisibility(View.VISIBLE);

                UserDBHelper.getUser(task.getWhoCompleted().getUserid(), new CompleteCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(Object object) {

                        whoCompleted = (User) object;
                        if (whoCompleted != null) {
                            if (whoCompleted.getName() != null)
                                tvWhoCompleted.setText(whoCompleted.getName() + " " + mContext.getResources().getString(R.string.completed_this_task));
                            else if (whoCompleted.getUsername() != null)
                                tvWhoCompleted.setText(CHAR_AMPERSAND + whoCompleted.getUsername() + " " + mContext.getResources().getString(R.string.completed_this_task));
                            else
                                tvWhoCompleted.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailed(String message) {
                        tvWhoCompleted.setVisibility(View.GONE);
                    }
                });
            }
        }

        private void setCompletedTimeVal() {
            if (task.getCompletedTime() != 0)
                llcompleted.setVisibility(View.VISIBLE);
            else
                llcompleted.setVisibility(View.GONE);
        }

        private void setTaskItems() {
            //Task Description
            if (task.getTaskDesc() != null && !task.getTaskDesc().isEmpty()) {
                txtDetail.setText(task.getTaskDesc());
                txtDetail.setVisibility(View.VISIBLE);
            } else {
                txtDetail.setVisibility(View.GONE);
            }

            //Create at
            if (task.getAssignedTime() != 0)
                txtCreateAt.setText(CommonUtils.getMessageTime(mContext, task.getAssignedTime()));

            //Completed at
            if (task.getCompletedTime() != 0)
                txtCompletedAt.setText(CommonUtils.getMessageTime(mContext, task.getCompletedTime()));
        }

        private void setTaskAssignedFrom() {
            UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    assignedFrom = (User) object;

                    //profile picture
                    UserDataUtil.setProfilePicture(mContext, assignedFrom.getProfilePhotoUrl(), assignedFrom.getName(), assignedFrom.getUsername()
                            , txtProfilePic, imgProfilePic, true);

                    //username of user who assigned the task
                    txtUserName.setText(UserDataUtil.getNameOrUsername(assignedFrom.getName(), assignedFrom.getUsername()));
                }

                @Override
                public void onFailed(String message) {
                    //profile picture
                    UserDataUtil.setProfilePicture(mContext, null, null, null, txtProfilePic, imgProfilePic, true);

                    //username of user who assigned the task
                    txtUserName.setText(UserDataUtil.getNameOrUsername(null, null));
                }
            });
        }

        private void setTaskGroup() {
            GroupDBHelper.getGroup(task.getGroup().getGroupid(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    taskGroup = (Group) object;

                    if (taskGroup != null) {
                        GroupDataUtil.setGroupPicture(mContext, taskGroup.getGroupPhotoUrl(),
                                taskGroup.getName(), txtGroupPic, imgGroupPic);

                        GroupDataUtil.setGroupName(taskGroup, txtGroupName);
                    }
                }

                @Override
                public void onFailed(String message) {

                }
            });
        }

        private void setPopupMenu() {
            popupMenu = new PopupMenu(mContext, moreImgv);
            popupMenu.inflate(R.menu.menu_group_task_item);

            if (task.isCompleted())
                popupMenu.getMenu().findItem(R.id.completeTask).setVisible(false);
            else
                popupMenu.getMenu().findItem(R.id.completeTask).setVisible(true);
        }
    }

    public void updatePostListItems() {
        this.taskList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (taskList != null ? taskList.size() : 0);
    }

    public void addGroupTask(GroupTask groupTask) {
        if (groupTask != null) {
            taskList.add(groupTask);
            notifyItemInserted(taskList.size() - 1);
        }
    }
}
