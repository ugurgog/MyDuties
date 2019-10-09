package uren.com.myduties.dutyManagement.tasks.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.MyDutiesUtil;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;

public class GroupAllTasksAdapter extends RecyclerView.Adapter {

    public static final int VIEW_ITEM = 1;

    private Activity mActivity;
    private Context mContext;
    private List<GroupTask> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private User user;
    private Group group;
    private TaskTypeHelper taskTypeHelper;

    public GroupAllTasksAdapter(Activity activity, Context context,
                                BaseFragment.FragmentNavigation fragmentNavigation, User user, Group group) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
        this.user = user;
        this.group = group;
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
                .inflate(R.layout.group_all_tasks_list_item, parent, false);
        viewHolder = new GroupAllTasksAdapter.MyViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GroupTask task = taskList.get(position);
        ((GroupAllTasksAdapter.MyViewHolder) holder).setData(task, position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView imgProfilePic;
        ImageView completedImgv;
        ImageView taskTypeImgv;
        ImageView moreImgv;
        ImageView closedImgv;
        TextView txtProfilePic;
        AppCompatTextView txtUserName;
        AppCompatTextView txtDetail;
        AppCompatTextView tvWhoCompleted;
        CardView cardView;
        AppCompatTextView tvClosed;
        AppCompatTextView tvUrgency;
        LinearLayout llcompleted;
        GroupTask task;
        int position;
        LinearLayout profileMainLayout;
        TextView txtCreateAt;
        TextView txtCompletedAt;
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
            llcompleted = view.findViewById(R.id.llcompleted);
            completedImgv = view.findViewById(R.id.completedImgv);
            tvWhoCompleted = view.findViewById(R.id.tvWhoCompleted);
            taskTypeImgv = view.findViewById(R.id.taskTypeImgv);
            tvClosed = view.findViewById(R.id.tvClosed);
            tvUrgency = view.findViewById(R.id.tvUrgency);
            setListeners();
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

                                    if (task.getAssignedFrom().getUsername() == null || task.getAssignedFrom().getUsername().trim().isEmpty()) {
                                        UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                                            @Override
                                            public void onComplete(Object object) {
                                                task.setAssignedFrom((User) object);
                                                MyDutiesUtil.callAssignedFromGroupTaskUser(mContext, task, user);
                                            }

                                            @Override
                                            public void onFailed(String message) {
                                                CommonUtils.showToastShort(mContext, message);
                                            }
                                        });
                                    } else
                                        MyDutiesUtil.callAssignedFromGroupTaskUser(mContext, task, user);

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
            GroupTaskDBHelper.getGroupTaskWithId(task.getTaskId(), group.getGroupid(), new CompleteCallback() {
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

        public void setData(GroupTask task, int position) {
            this.task = task;
            this.position = position;
            setPopupMenu();
            setTaskAssignedFrom();
            setWhoCompleted();
            MyDutiesUtil.setGroupTaskDescription(task, txtDetail);
            MyDutiesUtil.setGroupTaskCreatedAtValue(mContext, task, txtCreateAt);
            MyDutiesUtil.setGroupTaskCompletedAtValue(mContext, task, txtCompletedAt);
            MyDutiesUtil.setGroupTaskCompletedTimeValueAndSetLayoutVisibility(task, llcompleted);
            MyDutiesUtil.setCompletedImgv(mContext, task.isCompleted(), completedImgv);
            MyDutiesUtil.setTaskTypeImage(mContext, taskTypeImgv, task.getType(), taskTypeHelper);
            MyDutiesUtil.setClosedTv(task.isClosed(), tvClosed);
            MyDutiesUtil.setUrgency(mContext, task.isUrgency(), tvUrgency, cardView);
        }

        private void setWhoCompleted() {
            if (task.getWhoCompleted() == null || task.getWhoCompleted().getUserid() == null || task.getWhoCompleted().getUserid().isEmpty())
                tvWhoCompleted.setVisibility(View.GONE);
            else {
                tvWhoCompleted.setVisibility(View.VISIBLE);

                if (task.getWhoCompleted().getUsername() == null || task.getWhoCompleted().getUsername().trim().isEmpty()) {
                    UserDBHelper.getUser(task.getWhoCompleted().getUserid(), new CompleteCallback() {
                        @Override
                        public void onComplete(Object object) {
                            task.setWhoCompleted((User) object);
                            setWhoCompletedUserViews();
                        }

                        @Override
                        public void onFailed(String message) {
                            tvWhoCompleted.setVisibility(View.GONE);
                        }
                    });
                } else
                    setWhoCompletedUserViews();
            }
        }

        @SuppressLint("SetTextI18n")
        private void setWhoCompletedUserViews() {
            if (task.getWhoCompleted() != null) {
                if (task.getWhoCompleted().getName() != null)
                    tvWhoCompleted.setText(task.getWhoCompleted().getName() + " " + mContext.getResources().getString(R.string.completed_this_task));
                else if (task.getWhoCompleted().getUsername() != null)
                    tvWhoCompleted.setText(CHAR_AMPERSAND + task.getWhoCompleted().getUsername() + " " + mContext.getResources().getString(R.string.completed_this_task));
                else
                    tvWhoCompleted.setVisibility(View.GONE);
            }
        }

        private void setTaskAssignedFrom() {
            if (task.getAssignedFrom().getUsername() == null || task.getAssignedFrom().getUsername().trim().isEmpty()) {
                UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {
                        task.setAssignedFrom((User) object);
                        setAssignedFromUserViews();
                    }

                    @Override
                    public void onFailed(String message) {
                        UserDataUtil.setProfilePicture(mContext, null, null, null, txtProfilePic, imgProfilePic, true);
                        txtUserName.setText(UserDataUtil.getNameOrUsername(null, null));
                    }
                });
            } else
                setAssignedFromUserViews();
        }

        private void setAssignedFromUserViews() {
            UserDataUtil.setProfilePicture(mContext, task.getAssignedFrom().getProfilePhotoUrl(),
                    task.getAssignedFrom().getName(), task.getAssignedFrom().getUsername()
                    , txtProfilePic, imgProfilePic, true);
            txtUserName.setText(UserDataUtil.getNameOrUsername(task.getAssignedFrom().getName(), task.getAssignedFrom().getUsername()));
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

    public void addAll(List<GroupTask> addedTaskList) {
        if (addedTaskList != null) {
            taskList.addAll(addedTaskList);
            notifyItemRangeInserted(taskList.size(), taskList.size() + taskList.size());
        }
    }

    public void updatePostListItems(List<GroupTask> newTaskList) {
        this.taskList.clear();
        this.taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (taskList != null ? taskList.size() : 0);
    }
}